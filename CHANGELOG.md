# Change Log

All notable changes to this project will be documented in this file.

## v1.3.TODO - 2023-01-11

### Added 
- option to show note reminder only when switching from a work project without notes
- option to show a 'are you sure' confirmation before closing KeepTime
- import functionality of database (to be used with an export of the export functionality)
- app icon shown in dialogs
- Collapse / Expand button for the project report 

### Changed
- updated the Settings design
- replaced deprecated fontawesome dependencies
- updated dependencies 
- isWork is now the default when creating a new project
- autofocus the note field instead of the project name when editing a project
- active work item is not editable anymore
- simplify note format

### Fixed 
- "hide projects" on linux 
- notes disappear after an auto safe 
- creating a project without a name

## v1.2.0 - yyyy-mm-dd

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
