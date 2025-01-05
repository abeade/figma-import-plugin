<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Figma Import Plugin Changelog

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

## 1.4.0 - 2025-01-05

### Added

- Allows to target mipmap folders

### Changed

- Migrate to [IntelliJ Platform Gradle Plugin 2.0](https://blog.jetbrains.com/platform/2024/07/intellij-platform-gradle-plugin-2-0/).
- Using [Android plugin](https://plugins.jetbrains.com/plugin/22989-android) version [233.15619.7](https://plugins.jetbrains.com/plugin/22989-android/versions/stable/598665)
- Update platformVersion to 2023.3.8
- Change since build to 233 (2023.3)
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.9.25`
- Dependencies - upgrade `org.jetbrains.kotlinx.kover` to `0.8.3`
- Dependencies - upgrade `org.jetbrains.intellij.platform` to `2.1.0`
- Dependencies - upgrade `org.jetbrains.qodana` to `2024.2.3`
- Dependencies - upgrade `org.jetbrains.changelog` to `2.2.1`

## 1.3.2 - 2024-12-23

### Fixed

- Fixed wrong dialog height

## 1.3.1 - 2024-05-06

### Changed

- Updated compatibility range

## 1.3.0 - 2023-10-23

### Added

- Allows to launch the WebP conversion when import finishes

## 1.2.1 - 2023-10-12

### Changed

- Updated compatibility range

### Fixed

- Compatibility warnings by avoiding deprecated usages

## 1.2.0 - 2022-05-01

### Added

- Preview dialog showing all images in the selected ZIP file
- Preview popup showing image in a matched density

## 1.1.1 - 2022-02-27

### Added

- Duplicated suffixes validation

### Fixed

- Fixed file system refresh after files import

## 1.1.0 - 2022-02-02

### Added

- Support for drawable folder modifiers
- Allows skip resources when density folder don't exist

### Changed

- Target latest Idea IC JDK
- Move project to gradle

## 1.0.8 - 2020-04-03

### Changed

- Bump version

## 1.0.7 - 2020-03-14

### Fixed

- Fix runtime issues by avoid using UI DSL

## 1.0.6 - 2019-03-14

### Fixed

- Fix issue when accessing plugin settings

## 1.0.5 - 2019-03-04

### Fixed

- Fix plugin settings to global instead of by project

## 1.0.4 - 2019-03-02

### Changed

- Allow mixed file types in same zip

## 1.0.3 - 2019-03-01

### Fixed

- Update readme dialog image to include new override setting

## 1.0.2 - 2019-02-28

### Changed

- Update readme image

## 1.0.1 - 2019-02-28

### Fixed

- Fix wrong xxxhdpi resource folder

## 1.0.0 - 2019-02-28

### Added

- Initial version
