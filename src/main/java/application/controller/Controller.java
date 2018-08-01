package application.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.Main;
import application.common.DateFormatter;
import application.model.Model;
import application.model.Project;
import application.model.Settings;
import application.model.Work;
import javafx.scene.paint.Color;

public class Controller implements IController {

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   private final Model model;

   public Controller(final Model model) {
      this.model = model;

   }

   @Override
   public void changeProject(final Project newProject, final String notes) {
      changeProject(newProject, notes, 0);
   }

   @Override
   public void changeProject(final Project newProject, final String notes, final long minusSeconds) {
      final Work currentWork = model.activeWorkItem.get();

      final LocalDateTime now = LocalDateTime.now().minusSeconds(minusSeconds);
      if (currentWork != null) {
         currentWork.setEndTime(now);
         currentWork.setNotes(notes);
         if (currentWork.getNotes().isEmpty()) {
            currentWork.setNotes("- No notes -");
         }

         final String time = DateFormatter
               .secondsToHHMMSS(Duration.between(currentWork.getStartTime(), currentWork.getEndTime()).getSeconds());
         Log.info("You worked from '{}' to '{}' ({}) on project '{}' with notes '{}'", currentWork.getStartTime(),
               currentWork.getEndTime(), time, currentWork.getProject().getName(), currentWork.getNotes());

         // Save in db
         Main.workRepository.save(currentWork);
      }

      // Start new work
      final Work work = new Work(now, now.plusSeconds(minusSeconds), newProject, "");
      model.pastWorkItems.add(work);

      model.activeWorkItem.set(work);
   }

   @Override
   public void addNewProject(final String projectName, final boolean isWork, final Color projectColor) {
      final Project project = new Project(projectName, projectColor, isWork, false);
      model.allProjects.add(project);
      model.availableProjects.add(project);

      Main.projectRepository.save(project);
   }

   @Override
   public void setColors(final Color hoverBackgroundColor, final Color hoverFontColor,
         final Color defaultBackgroundColor, final Color defaultFontColor, final Color taskBarColor) {
      // TODO what is this mess :)
      final Settings settings = Main.settingsRepository.findAll().get(0);
      settings.setTaskBarColor(taskBarColor);

      settings.setDefaultBackgroundColor(defaultBackgroundColor);
      settings.setDefaultFontColor(defaultFontColor);

      settings.setHoverBackgroundColor(hoverBackgroundColor);
      settings.setHoverFontColor(hoverFontColor);

      Main.settingsRepository.save(settings);

      model.defaultBackgroundColor.set(settings.getDefaultBackgroundColor());
      model.defaultFontColor.set(settings.getDefaultFontColor());
      model.hoverBackgroundColor.set(settings.getHoverBackgroundColor());
      model.hoverFontColor.set(settings.getHoverFontColor());
      model.taskBarColor.set(settings.getTaskBarColor());
   }

   @Override
   public void shutdown() {
      // XXX Auto-generated method stub

   }

   @Override
   public void deleteProject(final Project p) {
      Log.info("Disabeling project '{}'.", p);
      p.setEnabled(false); // TODO or can we remove the project? but work references??
      model.availableProjects.remove(p);
      Main.projectRepository.save(p);
   }

   @Override
   public void editProject(final Project p, final String newName, final Color newColor, final boolean isWork) {
      Log.info("Changing project '{}' to '{}' '{}' '{}'", p, newName, newColor, isWork);
      p.setName(newName);
      p.setColor(newColor);
      p.setWork(isWork);
      Main.projectRepository.save(p);
   }

   /**
    * Calculate todays seconds counted as work
    */
   @Override
   public long calcTodaysWorkSeconds() {

      return model.pastWorkItems.stream().filter((work) -> {
         final Project project = work.getProject();
         // find up to date reference to project
         final Optional<Project> optionalProject = model.allProjects.stream().filter(p -> {
            return p.getId() == project.getId();
         }).findAny();
         if (optionalProject.isPresent()) {
            return optionalProject.get().isWork();
         }
         // TODO should not happen
         return false;
      }).mapToLong((work) -> {
         return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
      }).sum();
   }

   /**
    * Calculate todays present seconds (work+nonWork)
    */
   @Override
   public long calcTodaysSeconds() {
      return model.pastWorkItems.stream().mapToLong((work) -> {
         return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
      }).sum();
   }

}
