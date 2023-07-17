package com.github.dabidew.spotless.actions;

import com.intellij.CodeStyleBundle;
import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.LayoutCodeInfoCollector;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorScrollingPositionKeeper;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SlowOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * A code processor used to execute the spotlessApply gradle task on the current file to reformat it
 */
public class ReformatCodeProcessor
        extends AbstractLayoutCodeProcessor {
    private static final Logger LOG = Logger.getInstance(ReformatCodeProcessor.class);
    private final ReformatScope reformatScope;
    enum ReformatScope {
        FILE,
        MODULE
    }

    public ReformatCodeProcessor(
            @NotNull PsiFile file,
            ReformatScope reformatScope
    ) {
        super(
                file.getProject(),
                file,
                getProgressText(),
                getCommandName(),
                true
        );
        this.reformatScope = reformatScope;
    }

    @Override
    protected @NotNull FutureTask<Boolean> prepareTask(
            @NotNull PsiFile file,
            boolean processChangedTextOnly
    ) throws IncorrectOperationException {
        return new FutureTask<>(() -> {
            try {
                PsiFile fileToProcess = ensureValid(file);
                if (fileToProcess == null) {
                    return false;
                }
                if (!isPartOfMavenProject(file)) {
                    return false;
                }

                Document document = PsiDocumentManager
                        .getInstance(myProject)
                        .getDocument(fileToProcess);

                final LayoutCodeInfoCollector infoCollector = getInfoCollector();
                LOG.assertTrue(infoCollector == null || document != null);
                reformatFilePreservingScrollPosition(
                        fileToProcess,
                        document
                );

                return true;
            } catch (IncorrectOperationException e) {
                LOG.error(e);
                return false;
            }
        });
    }

    private boolean isPartOfMavenProject(final @NotNull PsiFile file) {
        return getPomPath(file) != null;
    }

    /**
     * Executes the spotlessApply task for the current file, using the {@link
     * EditorScrollingPositionKeeper} to maintain scroll position. The gradle task is executed
     * asynchronously in the background of the IDE.'
     *
     * @param fileToProcess the file to format using spotlessApply
     * @param document      the {@link Document} of the file
     */
    private void reformatFilePreservingScrollPosition(
            PsiFile fileToProcess,
            Document document
    ) {
        EditorScrollingPositionKeeper.perform(
                document,
                true,
                () -> SlowOperations.allowSlowOperations(() -> {
                    assertFileIsValid(fileToProcess);

                    var spotlessApply = constructRunnerParametersWithSpotlessApplyGoal(fileToProcess);
                    var settings = constructSettingsWithFileToProcessArgument(fileToProcess);
                    scheduleMavenRun(
                            spotlessApply,
                            settings
                    );
                })
        );
    }

    private void scheduleMavenRun(
            final MavenRunnerParameters params,
            final MavenRunnerSettings settings
    ) {
        MavenRunConfigurationType.runConfiguration(myProject, params, null, settings, null, true);
    }

    private MavenRunnerSettings constructSettingsWithFileToProcessArgument(final PsiFile fileToProcess) {
        final MavenRunnerSettings mavenRunnerSettings = new MavenRunnerSettings();
        if (reformatScope == ReformatScope.FILE) {
            mavenRunnerSettings.setMavenProperties(Map.of(
                    "spotlessFiles",
                    fileToProcess
                            .getVirtualFile()
                            .getPath()
            ));
        }
        mavenRunnerSettings.setRunMavenInBackground(true);
        return mavenRunnerSettings;
    }

    private MavenRunnerParameters constructRunnerParametersWithSpotlessApplyGoal(final PsiFile fileToProcess) {
        final String pomPath = getPomPath(fileToProcess);
        if (pomPath == null) {
            LOG.error("pom path is null");
        } else {
            LOG.info("This is the pom path: " + pomPath);
        }
        return new MavenRunnerParameters(
                true,
                fileToProcess
                        .getVirtualFile()
                        .getParent()
                        .getPath(),
                pomPath,
                List.of("spotless:apply"),
                (java.util.Collection<String>) null
        );
    }

    private String getPomPath(final PsiFile file) {
        final Module moduleForFile = ModuleUtil.findModuleForFile(file);
        if (moduleForFile == null) {
            return null;
        }
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(myProject);
        if (!mavenProjectsManager.isMavenizedModule(moduleForFile)) {
            return null;
        }
        MavenProject mavenProject = mavenProjectsManager.findProject(moduleForFile);
        if (mavenProject == null) {
            return null;
        }

        return mavenProject.getPath();
    }

    @Nullable
    private static PsiFile ensureValid(@NotNull PsiFile file) {
        if (file.isValid()) {
            return file;
        }

        VirtualFile virtualFile = file.getVirtualFile();
        if (!virtualFile.isValid()) {
            return null;
        }

        FileViewProvider provider = file
                .getManager()
                .findViewProvider(virtualFile);
        if (provider == null) {
            return null;
        }

        Language language = file.getLanguage();
        return provider.hasLanguage(language) ?
                provider.getPsi(language) :
                provider.getPsi(provider.getBaseLanguage());
    }

    private static void assertFileIsValid(@NotNull PsiFile file) {
        if (!file.isValid()) {
            LOG.error("Invalid Psi file, name: " + file.getName() + " , class: " + file
                    .getClass()
                    .getSimpleName() + " , " + PsiInvalidElementAccessException.findOutInvalidationReason(file));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static @NlsContexts.ProgressText String getProgressText() {
        return CodeStyleBundle.message("reformat.progress.common.text");
    }

    @SuppressWarnings("UnstableApiUsage")
    public static @NlsContexts.Command String getCommandName() {
        return CodeStyleBundle.message("process.reformat.code");
    }
}