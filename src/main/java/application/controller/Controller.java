package application.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import application.Main;
import application.common.DateFormatter;
import application.model.Model;
import application.model.Project;
import application.model.Settings;
import application.model.Work;
import javafx.scene.paint.Color;

@Service
public class Controller {

   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   private final Model model;

   public Controller(final Model model) {
      this.model = model;

   }

   public void changeProject(final Project newProject) {
      changeProject(newProject, 0);
   }

   public void changeProject(final Project newProject, final long minusSeconds) {
      // TODO consider day change (clean workRepo, ...)

      final Work currentWork = model.activeWorkItem.get();

      final LocalDateTime now = LocalDateTime.now().minusSeconds(minusSeconds);
      if (currentWork != null) {
         currentWork.setEndTime(now);
         // currentWork.setNotes(notes);
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

   public void addNewProject(final String projectName, final boolean isWork, final Color projectColor) {
      final Project project = new Project(projectName, projectColor, isWork, false);
      model.allProjects.add(project);
      model.availableProjects.add(project);

      Main.projectRepository.save(project);
   }

   public void updateSettings(final Color hoverBackgroundColor, final Color hoverFontColor,
         final Color defaultBackgroundColor, final Color defaultFontColor, final Color taskBarColor,
         final boolean useHotkey) {
      // TODO create holder for all the properties (or reuse Settings.class?)
      final Settings settings = Main.settingsRepository.findAll().get(0);
      settings.setTaskBarColor(taskBarColor);

      settings.setDefaultBackgroundColor(defaultBackgroundColor);
      settings.setDefaultFontColor(defaultFontColor);

      settings.setHoverBackgroundColor(hoverBackgroundColor);
      settings.setHoverFontColor(hoverFontColor);
      settings.setUseHotkey(useHotkey);

      Main.settingsRepository.save(settings);

      model.defaultBackgroundColor.set(settings.getDefaultBackgroundColor());
      model.defaultFontColor.set(settings.getDefaultFontColor());
      model.hoverBackgroundColor.set(settings.getHoverBackgroundColor());
      model.hoverFontColor.set(settings.getHoverFontColor());
      model.taskBarColor.set(settings.getTaskBarColor());
      model.useHotkey.set(settings.isUseHotkey());
   }

   @PreDestroy
   public void shutdown() {
      // XXX Auto-generated method stub
      Log.info("Controller shutdown");
      changeProject(model.idleProject, 0); // TODO get notes from view
   }

   public void deleteProject(final Project p) {
      Log.info("Disabeling project '{}'.", p);
      p.setEnabled(false); // TODO or can we remove the project? but work references??
      model.availableProjects.remove(p);
      Main.projectRepository.save(p);
   }

   public void editProject(final Project p, final String newName, final Color newColor, final boolean isWork) {
      Log.info("Changing project '{}' to '{}' '{}' '{}'", p, newName, newColor, isWork);
      p.setName(newName);
      p.setColor(newColor);
      p.setWork(isWork);
      Main.projectRepository.save(p);
   }

   public void setComment(final String notes) {
      final Work work = model.activeWorkItem.get();
      work.setNotes(notes);
      // TODO when to save to repo??
      // Main.workRepository.save(work);
   }

   /**
    * Calculate todays seconds counted as work
    */
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
   public long calcTodaysSeconds() {
      return model.pastWorkItems.stream().mapToLong((work) -> {
         return Duration.between(work.getStartTime(), work.getEndTime()).getSeconds();
      }).sum();
   }

}
