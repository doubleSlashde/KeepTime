# Change Log

All notable changes to this project will be documented in this file.

## v1.3.x - 2023-02-14

### Added 
- option to show note reminder only when switching from a work project without notes
- option to show a 'are you sure' confirmation before closing KeepTime
- import functionality of database (to be used with an export of the export functionality)
- app icon shown in dialogs
- in the report a collapse / expand button was added

### Changed
- updated the settings dialog design
- replaced deprecated fontawesome dependencies
- updated dependencies - we now also require Java 11 (instead of 8). **note** migration of data of previous version. see section 'Migrate from old version' in readme before updating
- when creating a new project 'isWork' is selected by default
- when editing a note the note field will be in focus instead of the project
- in the report dialog the currently active work item is not editable anymore (marked as 'active work' instead)
- in report dialog simplified the note format when copying them (removed the note count)
- when creating/editing a project the name cannot be empty anymore
- moved CI to github actions

### Fixed 
- 'hide projects' setting was not working correctly on linux. we disabled the setting for linux

## v1.2.0 - 2021-04-14

### Added

- export functionality of database
- search functionality
- functionality to edit past work items
- possibility to save position on screen
- minutely auto-save to not lose work on crash
- optional reminder for notes if switching projects without notes
- shortcut for minimizing window (Win+Down)

### Changed

- hotkey popup has same functionality now as main project selection
- removed old xml import feature
- adapted report layout
- the main ui is always on top
- Icons changed

### Fixed

## v1.1.0 - 2019-07-15

### Added

Linux support (tested on Ubuntu 18.04), hot keys are disabled for Linux

- about screen with licenses of third party software
- colored time line to report screen
- copy to clipboard button to report screen
- convenient keyboard controls

### Changed

- new Font (OpenSans)

### Fixed

## v1.0.0 - 2018-10-28

### Added

- setting to prevent hiding the project list.

### Changed

- Decreased height of projects in list, so more projects fit in the same space
- "Styled" settings window

### Fixed

- (workaround) 'stuck' key when using hotkeys and locking the pc

## v1.0-beta.2 - 2018-09-25

### Added

- Import functionality for old config.xml
- Database Migrations

### Changed

- Cleaning works from UI if the work is changed on another day
- Moved db folder from user home/keeptime/db to ./db (relative to .jar like logs folder)

### Fixed
