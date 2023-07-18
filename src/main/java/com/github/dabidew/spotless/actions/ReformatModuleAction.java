package com.github.dabidew.spotless.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/** The action which is called on "Reformat All Files With Spotless". */
public class ReformatModuleAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    DataContext dataContext = event.getDataContext();
    event.getData(CommonDataKeys.PSI_FILE);

    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return;
    }

    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (editor != null) {
      final PsiFile psiFile =
          PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (psiFile == null) {
        return;
      }

      new ReformatCodeProcessor(psiFile, ReformatCodeProcessor.ReformatScope.MODULE).run();
    }
  }
}
