# KeepTime
Project is Beta

# Use
Application to track your time spent on different projects each day. Aim was to create an easy and fast way to track the doings over the day. In the end you get a summary for the day.

Create projects and choose if they are counted as 'work time'. Select the project you work on. Before you switch the project, write a comment on what u did. Change the project. Repeat.

Main view (when you hover over the app):\
![Alt text](/readme/images/main.png?raw=true "Main")  

+ You can move the window by dragging it around
+ If you don't hover over the application the project list collapses
+ Open the context menu for a project to edit/delete or change the project and transfer n minutes of the current running one
+ You can change the project by clicking in the project list, or by using the Hotkey feature (if activated in settings) Strg+Win button. A popup will appear at the mouse cursor. With a mouseclick you have to first focus the app before you can make use of the search functionality. Up/Down will scroll through the projects and Enter will select the project.
+ In the taskbar you will also see the current time + the color of the active project
+ You can choose the used colors for the main window, if the project list should be left or right and if you want to use the Hotkey
+ After a day you can open the Reports, which will summarize the work done for the different projects during the day

**You need to close the application manualy before you shutdown your PC. Otherwise the last running project is not saved to database.**

# Install
Download and execute .jar file (see releases). You should put the .jar in an extra folder as a *logs* and a *db* folder will be created next to it.\
It is recommended to run the application at windows start. 
* Copy the keeptime.bat file from this repo next to the *.jar*. Adapt the path inside the *keeptime.bat* to the name of the *.jar* file (if needed). Try starting the application by executing the *keeptime.bat* file. Close the app.
* Open the autostart folder: Press *Windows+R*, execute *shell:startup*.
* Create a shortcut to the *.bat* in the autostart folder

**migrate from old version**
If you used this application before with a *config.xml* you can import your old projects in the settings dialog. Place your config.xml next to the jar and press "parse config.xml". Otherwise no steps are needed.
## Requirements
Only works correct for windows currently. Tested on 7 and 10.
