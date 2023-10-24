![Build](https://github.com/abeade/figma-import-plugin/workflows/Build/badge.svg)
![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/12037-import-figma-resources.svg?color=green&style=plastic)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/12037-import-figma.svg)](https://plugins.jetbrains.com/plugin/12037-import-figma)

# Figma Import Plugin
<img src="src/main/resources/META-INF/pluginIcon.svg" width="100" />

<!-- Plugin description -->
Imports figma exported resources ZIP file containing PNG or JPG files by matching its suffixes with density folders
<!-- Plugin description end -->

# Installation
You can install the plugin from the [Plugin Repository](https://plugins.jetbrains.com/plugin/12037-import-figma-resources).

Or you can download the latest binary from [releases](https://github.com/abeade/figma-import-plugin/releases) on this repo

# Usage
The plugin installs one menu entry under the context menu when click over a `res` folder in a project:

<img src="images/popup.png" width="400" />

When using this option an import dialog will be shown:

<img src="images/dialog.png" width="700" />

In it you need to select the ZIP file, once selected the purpose resource name will be filled (you can change it if you need it) and matched and not matched suffixes will show an icon on right side.

Optionally, you can add qualifiers to the resource folders.

The "Remember suffixes" check allows to store the given suffixes for next plugin executions.

By selecting "Launch WebP conversion after import" check, the WebP conversion will be executed over the imported resources.

# Setup
You can change the proposed resource prefix and the folder creation policy in the plugin settings:

<img src="images/settings.png" width="700" />
