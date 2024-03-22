package de.doubleslash.keeptime.controller.report;

import de.doubleslash.keeptime.common.DateFormatter;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Report {
   private final LocalDate date;
   private final Model model;
   private final Controller controller;
   private List<Work> workItems;
   private long workItemsSeconds;
   private SortedSet<Project> workedProjectsSet;
   private long presentTime;
   private long workTime;
   private Map<Long, Long> projectWorkSecondsMap;

   public Report(LocalDate date, Model model, Controller controller) {
      this.date = date;
      this.model = model;
      this.controller = controller;

      fetchWorkItems();

      fetchProjects();

      calculateSeconds();

   }

   public LocalDate getDate() {
      return date;
   }

   public String getDateString() {
      return DateFormatter.toDayDateString(this.date);
   }

   public List<Work> getWorkItems() {
      return workItems;
   }

   public long getWorkItemsSeconds() {
      return workItemsSeconds;
   }

   public SortedSet<Project> getWorkedProjectsSet() {
      return workedProjectsSet;
   }

   public String getPresentTimeString() {
      return DateFormatter.secondsToHHMMSS(this.presentTime);
   }

   public String getWorkTimeString() {
      return DateFormatter.secondsToHHMMSS(this.workTime);
   }

   public Map<Long, Long> getProjectWorkSecondsMap() {
      return this.projectWorkSecondsMap;
   }

   private void fetchWorkItems() {
      this.workItems = model.getWorkRepository().findByStartDateOrderByStartTimeAsc(date);

      if (date.equals(LocalDate.now())) {
         Work activeWorkItem = model.activeWorkItem.get();
         if (activeWorkItem != null && !this.workItems.contains(activeWorkItem)) {
            this.workItems.add(activeWorkItem);
         }
      }
   }

   private void fetchProjects() {
      this.workedProjectsSet = workItems.stream()
                                        .map(Work::getProject)
                                        .collect(Collectors.toCollection(
                                              () -> new TreeSet<>(Comparator.comparing(Project::getIndex))));
   }

   private void calculateSeconds() {
      this.workItemsSeconds = this.controller.calcSeconds(this.workItems);

      this.projectWorkSecondsMap = new HashMap<>();

      for (final Project project : workedProjectsSet) {
         final List<Work> onlyCurrentProjectWork = workItems.stream()
                                                            .filter(w -> w.getProject() == project)
                                                            .collect(Collectors.toList());

         final long projectWorkSeconds = this.controller.calcSeconds(onlyCurrentProjectWork);

         projectWorkSecondsMap.put(project.getId(), projectWorkSeconds);

         presentTime += projectWorkSeconds;
         if (project.isWork()) {
            workTime += projectWorkSeconds;
         }
      }
   }

}
