# KeepTime

Application to track your time spent on different projects each day. Aim was to create an easy and fast way to track the doings over the day. In the end you get a summary for the day.

Create projects and choose if they are counted as 'work time'. Select the project you work on. Before you switch the project, write a comment on what u did. Change the project. Repeat.

## Usage

### Main view:
![Main Ui with description](readme/images/contextMenuDescription.png?raw=true "Main")  

+ You can move the window by dragging it around
+ Open the context menu (with a right-click) for a project to edit/delete or change the project and transfer n minutes of the current running one
+ In the taskbar you will also see the current time + the color of the active project
+ The current Project will be saved eyery minute to mitigate loss on system crash or shutdown without closing window manually first. 
+ After a day you can open the Reports, which will summarize the work done for the different projects during the day

**You need to close the application manualy before you shutdown your PC. Otherwise the last running project is not saved completely to database. (will be last state saved by auto-save)**

### Settings:
![Settings Screen](readme/images/settings.png?raw=true "Settings")

+ Colors: you can define various Colors to use in the UI to customize the application. The Reset resets the color to the default color.
+ Display projects on the right:
+ Hide projects on mouse leave: If you don't hover over the application the project list collapses
+ Use Hotkey(Strg+Win): You can change the project by clicking in the project list, or by using the Hotkey feature (if activated in settings) Strg+Win button. A popup will appear at the mouse cursor. With a mouseclick you have to first focus the app before you can make use of the search functionality. Up/Down will scroll through the projects and Enter will select the project.
+ Save Position on Screen: remember the Position of the Main UI and place it there on start.
+ Ask for notes when switching project (if empty): pops up a dialouge to add notes if no notes are given and you try to switch projects

### Reports:
![Report Screen](readme/images/reportDescription.png?raw=true "Report")

+ the report screen gives you a summary for every day

## Install

* Download keeptime.bat and keeptime-1.1.0-SNAPSHOT-bin.zip (see [releases](https://github.com/doubleSlashde/KeepTime/releases))
* Extract the downloaded .zip
* Copy the downloaded keeptime.bat file next to the *.jar*. Adapt the path inside the *keeptime.bat* to the name of the *.jar* file (if needed). Try starting the application by executing the *keeptime.bat* file. The start may take up to one minute.

It is recommended to run the application at computer start so you do not forget to track your time. For autostart also execute the following steps (for windows)
* Open the autostart folder: Press *Windows+R*, execute *shell:startup*
* Create a shortcut to the *.bat* in the autostart folder

You should put the .jar in an extra folder as a *logs* and a *db* folder will be created next to it.\

**migrate from old version (before v1.0-beta.2) **

If you have a folder *db* next to the old .jar, the folder has to be next to the new .jar as well. The folder contains your tracked times.\
If you used this application before with a *config.xml* you can import your old projects in the settings dialog. Place your config.xml next to the jar and press "parse config.xml". Otherwise no steps are needed.

## Requirements

* Windows 7, 10
* Linux (tested on Ubuntu 18.04)
* Java 8 + JavaFX
