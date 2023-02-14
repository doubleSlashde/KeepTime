# KeepTime

Application to track your time spent on different projects each day. Aim was to create an easy and fast way to track the doings over the day. In the end you get a summary for the day.

Create projects and choose if they are counted as 'work time'. Select the project you work on. Before you switch the project, write a comment on what u did. Change the project. Repeat.

## Usage

### Main view:
![Main Ui with description](readme/images/contextMenuDescription.png?raw=true "Main")  

+ You can move the window by dragging it around
+ Open the context menu (with a right-click) for a project to edit/delete or change the project and transfer n minutes of the current running one
+ In the taskbar you will also see the current time + the color of the active project
+ The current Project will be saved every minute to mitigate loss on system crash or shutdown without closing window manually first. 
+ After a day you can open the Reports, which will summarize the work done for the different projects during the day

**You need to close the application manually before you shut down your PC. Otherwise, the last running project is not saved completely to database. (will be last state saved by auto-save)**

### Settings:
![Settings Screen](readme/images/settingsColor.PNG?raw=true "Settings")

+ Colors: you can define various colors to use in the UI to customize the application. The Reset resets the color to the default color.
+ Display projects on the right: Will show the list of projects on the right side, instead of the left
+ Hide projects on mouse leave: If you don't hover over the application the project list collapses
+ Use Hotkey (`Strg`+`Win`): Change the project by using the Hotkey feature. A popup will appear at the mouse cursor.
+ Save Position on Screen: Remembers the last position of the Main UI on application start.
+ Ask for notes when switching project (if empty): Pops up a dialog to add notes if no notes are given and you try to switch projects
+ Export: export database for backup and later import (import currently not yet implemented)

### Reports:
![Report Screen](readme/images/reportDescription.png?raw=true "Report")

+ the report screen gives you a summary for every day

## Install

* Download keeptime.bat and keeptime-<version>-bin.zip (see [releases](https://github.com/doubleSlashde/KeepTime/releases))
* Extract the downloaded .zip
* Try starting the application by executing the *keeptime.bat* file. The start may take up to one minute.

It is recommended to run the application at computer start, so you do not forget to track your time. For autostart also execute the following steps (for windows)
* Open the autostart folder: Press *Windows+R*, execute *shell:startup*
* Create a shortcut to the *.bat* in the autostart folder

You should put the .jar in an extra folder as a *logs* and a *db* folder will be created next to it.\

### Migrate from older version than v1.2.0

1. Download new version and replace the .jar file.
2. Start new version of KeepTime. Notice that your old data is not available.
3. Stop KeepTime
4. Copy the files (not directories) of directory `db` (next to the .jar file) into `db/1.4.197/` (path now includes the database version).
5. Start KeepTime again. Notice that your data is available again.

### Migrate from KeepTime v1.2.0

1. Start your current version of KeepTime (v1.2.0)
2. Go to the settings and export your KeepTime data 
3. Download new version and replace the .jar file. 
4. Start new version of KeepTime. Notice that your old data is not available. 
5. Open the new version and import the exported sql script 
6. After the import KeepTime closes automatically
7. To see the changes just start the new KeepTime again

## Requirements

* Windows 7, 10
* Linux (tested on Ubuntu 18.04)
* Java 11