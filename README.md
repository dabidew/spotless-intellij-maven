# Spotless Intellij Maven

<!-- TODO, actually make a plugin and set up build workflow
![Build](https://github.com/ragurney/spotless/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/18321.svg)](https://plugins.jetbrains.com/plugin/18321)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18321.svg)](https://plugins.jetbrains.com/plugin/18321)
-->

<!-- Plugin description -->
An IntelliJ plugin to allow running the [spotless](https://github.com/diffplug/spotless) maven task
from within the IDE on the current file selected in the editor. 

You may find the spotless action via <kbd>Code</kbd> > <kbd>Reformat All Files With Spotless For Maven</kbd> or <kbd>Code</kbd> > <kbd>Reformat Current File With Spotless For Maven</kbd>.

![spotlessdemo](https://user-images.githubusercontent.com/15261525/147841908-d5cc3bda-56c8-4cbd-ba29-13ebe29f6a1d.gif)

Report bugs or contribute enhancements on GitHub [(dabidew/spotless-intellij-maven)](https://github.com/dabidew/spotless-intellij-maven)
<!-- Plugin description end -->

## Features
* `spotlessApply` can be run on the current file via <kbd>Code</kbd> > <kbd>Reformat Code with Spotless</kbd>.
  You may also assign a keyboard shortcut to this action for convenience.

## Installation
>**NOTE:** Before using this extension, ensure you've [configured Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven)
correctly in your pom file.

### Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Spotless Maven"</kbd> >
  <kbd>Install Plugin</kbd>

### Manually:

  Download the [latest release](https://github.com/dabidew/spotless-intellij-maven/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## How it Works
This plugin runs the `spotless:apply` Maven taks on the current file or the Maven module of the current file.
## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md)

## Release Notes
See [CHANGELOG.md](CHANGELOG.md)

## License
See [License](LICENSE)