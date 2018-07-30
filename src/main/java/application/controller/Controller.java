package application.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.Main;
import application.common.DateFormatter;
import application.model.Model;
import application.model.Project;
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

      Main.projectRepo.save(project);
   }

   @Override
   public Object getDetails(final LocalDate date) {
      // XXX Auto-generated method stub
      return null;
   }

   @Override
   public void setColors(final Object colors) {
      // XXX Auto-generated method stub

   }

   @Override
   public void shutdown() {
      // XXX Auto-generated method stub

   }

   @Override
   public void renameProject(final Project p, final String newName) {
      Log.info("Renaming project '{}' to '{}'", p, newName);
      p.setName(newName);
      Main.projectRepo.save(p);
   }

   @Override
   public void deleteProject(final Project p) {
      Log.info("Disabeling project '{}'.", p);
      p.setEnabled(false);
      model.availableProjects.remove(p);
      Main.projectRepo.save(p);
   }

   @Override
   public void changeProjectColor(final Project p, final Color newColor) {
      Log.info("Changing project '{}' color to '{}'.", p, newColor);
      p.setColor(newColor);
      Main.projectRepo.save(p);
   }

}
