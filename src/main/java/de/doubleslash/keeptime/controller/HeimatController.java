// Copyright 2025 doubleSlash Net Business GmbH
//
// This file is part of KeepTime.
// KeepTime is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package de.doubleslash.keeptime.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.doubleslash.keeptime.model.*;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
import de.doubleslash.keeptime.rest.integration.heimat.model.ExistingAndInvalidMappings;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTask;
import de.doubleslash.keeptime.rest.integration.heimat.model.HeimatTime;
import de.doubleslash.keeptime.view.ProjectReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HeimatController {
   private static final Logger LOG = LoggerFactory.getLogger(HeimatController.class);

   private final Controller controller;
   private final HeimatSettings heimatSettings;
   private final ExternalProjectsMappingsRepository externalProjectsMappingsRepository;

   private final ObjectMapper objectMapper = new ObjectMapper();

   private HeimatAPI heimatAPI;
   private final Model model;

   @Autowired
   public HeimatController(HeimatSettings heimatSettings,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository, final Controller controller,
         Model model) {
      this.heimatSettings = heimatSettings;
      this.controller = controller;
      this.model = model;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
      this.heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
   }

   // for testing only
   HeimatController(HeimatSettings heimatSettings, HeimatAPI heimatAPI,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository, final Controller controller,
         Model model) {
      this.heimatSettings = heimatSettings;
      this.controller = controller;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
      this.heimatAPI = heimatAPI;
      this.model = model;
   }

   /**
    * can be called when heimat settings have changed
    */
   public void refreshConnection() {
      heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
   }

   /**
    * throws SecurityException when login or url is not valid
    */
   public void tryLogin() {
      try {
         heimatAPI.isLoginValid();
      } catch (Exception e) {
         throw new SecurityException("Could not connect to Heimat API. Maybe wrong configuration?", e);
      }
   }

   public List<Mapping> getTableRows(final LocalDate currentReportDate, final List<Work> currentWorkItems) {
      final List<HeimatTask> heimatTasks = heimatAPI.getMyTasks(currentReportDate);
      final List<HeimatTime> heimatTimes = heimatAPI.getMyTimes(currentReportDate);
      final List<ExternalProjectMapping> mappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);

      final List<Mapping> list = new ArrayList<>();

      final SortedSet<Project> workedProjectsSet = currentWorkItems.stream()
                                                                   .map(Work::getProject)
                                                                   .filter(Project::isWork)
                                                                   .collect(Collectors.toCollection(() -> new TreeSet<>(
                                                                         Comparator.comparing(Project::getIndex))));
      final Map<Long, List<HeimatTime>> taskIdToHeimatTimesMap = heimatTimes.stream()
                                                                            .collect(Collectors.groupingBy(
                                                                                  HeimatTime::taskId));

      for (final Project project : workedProjectsSet) {
         String heimatNotes = "";
         long heimatTimeSeconds = 0;
         boolean isMappedInHeimat = false;
         String bookingHint = "";
         final Optional<ExternalProjectMapping> optHeimatMapping = mappedProjects.stream()
                                                                                 .filter(mp -> mp.getProject().getId()
                                                                                       == project.getId())
                                                                                 .findAny();
         List<HeimatTime> optionalAlreadyBookedTimes = new ArrayList<>();
         Optional<Mapping> optionalExistingMapping = Optional.empty();
         if (optHeimatMapping.isPresent()) {
            isMappedInHeimat = true;
            bookingHint = heimatTasks.stream()
                                     .filter(ht -> ht.id() == optHeimatMapping.get().getExternalTaskId())
                                     .map(HeimatTask::bookingHint)
                                     .findAny()
                                     .orElseGet(String::new);
            optionalExistingMapping = list.stream()
                                          .filter(mapping -> mapping.heimatTaskId == optHeimatMapping.get()
                                                                                                     .getExternalTaskId())
                                          .findAny();

            final List<HeimatTime> heimatTimesForTaskId = taskIdToHeimatTimesMap.get(
                  optHeimatMapping.get().getExternalTaskId());
            if (heimatTimesForTaskId != null) {
               optionalAlreadyBookedTimes = heimatTimesForTaskId;
            }
            if (!optionalAlreadyBookedTimes.isEmpty()) {
               heimatNotes = addHeimatNotes(optionalAlreadyBookedTimes);
               heimatTimeSeconds = addHeimatTimes(optionalAlreadyBookedTimes);
            }
         }
         final List<Work> onlyCurrentProjectWork = currentWorkItems.stream()
                                                                   .filter(w -> w.getProject() == project)
                                                                   .toList();

         final long projectWorkSeconds = controller.calcSeconds(onlyCurrentProjectWork);

         final ProjectReport pr = new ProjectReport();
         for (final Work work : onlyCurrentProjectWork) {
            final String currentWorkNote = work.getNotes();
            pr.appendToWorkNotes(currentWorkNote);
         }
         final String keeptimeNotes = pr.getNotes();
         StyledMessage canBeSyncedMessage;

         if (!isMappedInHeimat) {
            canBeSyncedMessage = StyledMessage.of(
                  new StyledMessage.TextSegment("Not mapped to Heimat task.\nMap in settings dialog."));
         } else if (heimatTasks.stream().noneMatch(ht -> ht.id() == optHeimatMapping.get().getExternalTaskId())) {
            canBeSyncedMessage = StyledMessage.of(new StyledMessage.TextSegment(
                  "Heimat Task is not available (anymore).\nPlease check mappings in settings dialog."));
            isMappedInHeimat = false;
         } else {
            final ExternalProjectMapping externalProjectMapping = optHeimatMapping.get();
            canBeSyncedMessage = StyledMessage.of(new StyledMessage.TextSegment("Sync to "),
                  new StyledMessage.TextSegment(externalProjectMapping.getExternalTaskName(), true),
                  new StyledMessage.TextSegment("\n(" + externalProjectMapping.getExternalProjectName() + ")"));
         }

         if (optionalExistingMapping.isPresent()) {
            final Mapping existingMapping = optionalExistingMapping.get();
            final ArrayList<Project> projects = new ArrayList<>(existingMapping.projects());
            projects.add(project);
            final long keepTimeSeconds = existingMapping.keeptimeSeconds() + projectWorkSeconds;
            final long heimatSeconds = existingMapping.heimatSeconds();
            final boolean shouldBeSynced =
                  isMappedInHeimat && differenceGreaterOrEqual15Minutes(heimatSeconds, keepTimeSeconds);
            final Mapping mapping = new Mapping(isMappedInHeimat ? optHeimatMapping.get().getExternalTaskId() : -1,
                  isMappedInHeimat, shouldBeSynced, canBeSyncedMessage, bookingHint, existingMapping.existingTimes(),
                  projects, existingMapping.heimatNotes(), existingMapping.keeptimeNotes() + ". " + keeptimeNotes,
                  heimatSeconds, keepTimeSeconds);
            list.remove(existingMapping);
            list.add(mapping);
         } else {
            final boolean shouldBeSynced =
                  isMappedInHeimat && differenceGreaterOrEqual15Minutes(heimatTimeSeconds, projectWorkSeconds);
            final List<Project> projects = Collections.singletonList(project);
            final Mapping mapping = new Mapping(isMappedInHeimat ? optHeimatMapping.get().getExternalTaskId() : -1,
                  isMappedInHeimat, shouldBeSynced, canBeSyncedMessage, bookingHint, optionalAlreadyBookedTimes,
                  projects, heimatNotes, keeptimeNotes, heimatTimeSeconds, projectWorkSeconds);
            list.add(mapping);
         }
      }

      final List<Long> mappedIds = mappedProjects.stream().map(ExternalProjectMapping::getExternalTaskId).toList();
      final Map<Long, List<HeimatTime>> notMappedExistingTimes = heimatTimes.stream()
                                                                            .filter(ht -> !mappedIds.contains(
                                                                                  ht.taskId()))
                                                                            .collect(Collectors.groupingBy(
                                                                                  HeimatTime::taskId));
      notMappedExistingTimes.forEach((id, times) -> {
         String heimatNotes = times.stream().map(HeimatTime::note).collect(Collectors.joining(". "));
         long heimatTimeSeconds = times.stream()
                                       .reduce(0L, (subtotal, element) -> subtotal + element.durationInMinutes() * 60L,
                                             Long::sum);

         final Optional<HeimatTask> optionalHeimatTask = heimatTasks.stream().filter(t -> t.id() == id).findAny();
         String taskName = "Cannot resolve Heimat Task Id: " + id + " to name\nPlease check in Heimat";
         if (optionalHeimatTask.isPresent()) {
            final HeimatTask heimatTask = optionalHeimatTask.get();
            taskName = heimatTask.name() + "\n" + heimatTask.taskHolderName();
         }

         final Mapping mapping = new Mapping(id, true, false,
               StyledMessage.of(new StyledMessage.TextSegment("Not mapped in KeepTime\n\n" + taskName)), "", times,

               new ArrayList<>(0), heimatNotes, "", heimatTimeSeconds, 0);
         list.add(mapping);
      });

      taskIdToHeimatTimesMap.forEach((id, times) -> {
         final Optional<ExternalProjectMapping> mapping = mappedProjects.stream()
                                                                        .filter(mp -> mp.getExternalTaskId() == id)
                                                                        .findAny();
         if (mapping.isEmpty())
            return;
         final ExternalProjectMapping externalProjectMapping = mapping.get();
         final Optional<Project> optionalProject = workedProjectsSet.stream()
                                                                    .filter(wp -> wp.getId()
                                                                          == externalProjectMapping.getProject()
                                                                                                   .getId())
                                                                    .findAny();
         if (optionalProject.isPresent()) {
            return;
         }
         String heimatNotes = addHeimatNotes(times);
         long heimatTimeSeconds = addHeimatTimes(times);

         StyledMessage syncMessage = StyledMessage.of(
               new StyledMessage.TextSegment("Present in Heimat but not KeepTime\n\nSync to "),
               new StyledMessage.TextSegment(externalProjectMapping.getExternalTaskName(), true),
               new StyledMessage.TextSegment("\n(" + externalProjectMapping.getExternalProjectName() + ")"));

         final Mapping mapping2 = new Mapping(id, true, false, syncMessage, "", times, mappedProjects.stream()
                                                                                                     .filter(
                                                                                                           mp -> mp.getExternalTaskId()
                                                                                                                 == id)
                                                                                                     .map(ExternalProjectMapping::getProject)
                                                                                                     .toList(),
               heimatNotes, "", heimatTimeSeconds, 0);
         list.add(mapping2);
      });

      return list;
   }

   private static boolean differenceGreaterOrEqual15Minutes(final long heimatTimeSeconds,
         final long projectWorkSeconds) {
      return heimatTimeSeconds == 0L || Math.abs(heimatTimeSeconds - projectWorkSeconds) >= 15 * 60L;
   }

   private static long addHeimatTimes(final List<HeimatTime> optionalAlreadyBookedTimes) {
      long heimatTimeSeconds;
      heimatTimeSeconds = optionalAlreadyBookedTimes.stream()
                                                    .reduce(0L, (subtotal, element) -> subtotal
                                                          + element.durationInMinutes() * 60L, Long::sum);
      return heimatTimeSeconds;
   }

   private static String addHeimatNotes(final List<HeimatTime> optionalAlreadyBookedTimes) {
      String heimatNotes;
      heimatNotes = optionalAlreadyBookedTimes.stream().map(HeimatTime::note).collect(Collectors.joining(". "));
      return heimatNotes;
   }

   public List<HeimatErrors> saveDay(final List<UserMapping> items, LocalDate date) {
      List<HeimatErrors> errors = new ArrayList<>();

      items.stream().filter(tr -> tr.shouldSync).forEach(item -> {
         final int durationInMinutes = item.userMinutes;
         final HeimatTime heimatTime = new HeimatTime(item.mapping.heimatTaskId, date, null, null, durationInMinutes,
               item.userNotes, 0L);

         try {
            item.mapping.existingTimes().forEach(existingTime -> {
               LOG.info("Removing existing booked time '{}'", existingTime);
               heimatAPI.deleteMyTime(existingTime.id());
            });
            LOG.info("Adding new time time '{}'", heimatTime);
            heimatAPI.addMyTime(heimatTime);
         } catch (Exception e) {
            LOG.error("Error while persisting time '{}'", heimatTime, e);
            errors.add(new HeimatErrors(item, "Error while persisting." + e.getMessage()));
         }
      });

      return errors;
   }

   public String getUrlForDay(final LocalDate currentReportDate) {
      return heimatSettings.getHeimatUrl() + "/core/heimat/time/main/day/" + currentReportDate.format(
            DateTimeFormatter.ofPattern("yyyy/M/d"));
   }

   public List<HeimatTask> getTasks(final LocalDate forDate) {
      final List<HeimatTask> myTasks = heimatAPI.getMyTasks(forDate);
      // TODO remove this when api returns tasks only once
      Map<Long, HeimatTask> uniqueMap = new LinkedHashMap<>();
      for (HeimatTask obj : myTasks) {
         uniqueMap.putIfAbsent(obj.id(), obj);
      }
      return uniqueMap.values()
                      .stream()
                      .filter(p -> !p.isStartAndEndTimeRequired()) // not supported
                      .sorted(Comparator.comparing(HeimatTask::taskHolderName).thenComparing(HeimatTask::name))
                      .toList();
   }

   public void updateMappings(final List<ProjectMapping> newMappings) {
      LOG.debug("New mappings to be saved '{}'.", newMappings);
      final List<ExternalProjectMapping> alreadyMappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);

      final List<ExternalProjectMapping> mappingsToCreateOrUpdate = newMappings.stream()
                                                                               .filter(pm -> pm.getHeimatTask() != null)
                                                                               .map(projectMapping -> {
                                                                                  final Optional<ExternalProjectMapping> any = alreadyMappedProjects.stream()
                                                                                                                                                    .filter(
                                                                                                                                                          pm -> pm.getProject()
                                                                                                                                                                  .getId()
                                                                                                                                                                == projectMapping.getProject()
                                                                                                                                                                                 .getId())
                                                                                                                                                    .findAny();
                                                                                  final HeimatTask heimatTask = projectMapping.getHeimatTask();
                                                                                  if (any.isPresent()) {
                                                                                     final ExternalProjectMapping projectMapping1 = any.get();
                                                                                     if (projectMapping1.getExternalTaskId()
                                                                                           == heimatTask.id()) {
                                                                                        // mapping did not change
                                                                                        return null;
                                                                                     }
                                                                                     projectMapping1.setExternalProjectName(
                                                                                           heimatTask.taskHolderName());
                                                                                     projectMapping1.setExternalTaskId(
                                                                                           heimatTask.id());
                                                                                     projectMapping1.setExternalTaskName(
                                                                                           heimatTask.name());
                                                                                     projectMapping1.setExternalTaskMetadata(
                                                                                           getAsJson(heimatTask));

                                                                                     return projectMapping1;
                                                                                  }
                                                                                  return new ExternalProjectMapping(
                                                                                        ExternalSystem.Heimat,
                                                                                        heimatTask.taskHolderName(),
                                                                                        heimatTask.id(),
                                                                                        heimatTask.name(),
                                                                                        getAsJson(heimatTask),
                                                                                        projectMapping.getProject());
                                                                               })
                                                                               .filter(Objects::nonNull)
                                                                               .toList();
      LOG.info("Save/Updating mappings '{}'", mappingsToCreateOrUpdate);
      externalProjectsMappingsRepository.saveAll(mappingsToCreateOrUpdate);

      // remove mappings which were removed also from database
      final ArrayList<ExternalProjectMapping> mappingsToRemove = alreadyMappedProjects.stream()
                                                                                      .filter(em -> newMappings.stream()
                                                                                                               .anyMatch(
                                                                                                                     wantedMapping ->
                                                                                                                           wantedMapping.getProject()
                                                                                                                                        .getId()
                                                                                                                                 == em.getProject()
                                                                                                                                      .getId()
                                                                                                                                 &&
                                                                                                                                 wantedMapping.getHeimatTask()
                                                                                                                                       == null))
                                                                                      .collect(Collectors.toCollection(
                                                                                            ArrayList::new));
      // remove mappings of projects which were 'deleted'
      alreadyMappedProjects.stream().filter(em -> !em.getProject().isEnabled()).forEach(mappingsToRemove::add);
      LOG.info("Removing mappings '{}'", mappingsToRemove);
      externalProjectsMappingsRepository.deleteAll(mappingsToRemove);
   }

   private String getAsJson(final HeimatTask heimatTask) {
      try {
         return objectMapper.writeValueAsString(heimatTask);
      } catch (JsonProcessingException e) {
         throw new RuntimeException(e);
      }
   }

   public ExistingAndInvalidMappings getExistingProjectMappings(List<HeimatTask> externalProjects) {
      final List<ExternalProjectMapping> alreadyMappedProjects = externalProjectsMappingsRepository.findByExternalSystemId(
            ExternalSystem.Heimat);
      final List<ExternalProjectMapping> invalidExternalMappings = new ArrayList<>();

      final List<ProjectMapping> validProjectMappings = model.getSortedAvailableProjects().stream().map(p -> {
         final Optional<ExternalProjectMapping> mapping = alreadyMappedProjects.stream()
                                                                               .filter(mp -> mp.getProject().getId()
                                                                                     == p.getId())
                                                                               .findAny();
         if (mapping.isEmpty()) {
            return new ProjectMapping(p, null);
         }
         final Optional<HeimatTask> any = externalProjects.stream()
                                                          .filter(ep -> ep.id() == mapping.get().getExternalTaskId())
                                                          .findAny();
         if (any.isEmpty()) {
            LOG.warn("A mapping exists but task does not exist anymore in Heimat! '{}'->'{}'.",
                  mapping.get().getProject(), mapping.get().getExternalTaskId());
            invalidExternalMappings.add(mapping.get());
            return new ProjectMapping(p, null);
         }
         return new ProjectMapping(p, any.get());
      }).toList();

      final List<String> invalidMappingsAsString = invalidExternalMappings.stream()
                                                                          .map(em -> "Task no longer exists: "
                                                                                + em.getExternalProjectName() + " - "
                                                                                + em.getExternalTaskName()
                                                                                + "'. Was mapped to '" + em.getProject()
                                                                                                           .getName()
                                                                                + "'.")
                                                                          .collect(Collectors.toCollection(
                                                                                ArrayList::new));
      /*
      // I do not have all external projects here :( only already filtered ones
      allExternalProjects.stream()
                         .filter(HeimatTask::isStartAndEndTimeRequired)
                         .map(p -> "Task " + p.taskHolderName() + " - " + p.name()
                               + " requires start+end time which is not supported.")
                         .forEach(invalidMappingsAsString::add);
      */

      return new ExistingAndInvalidMappings(validProjectMappings, invalidMappingsAsString);
   }

   public record UserMapping(Mapping mapping, boolean shouldSync, String userNotes, int userMinutes) {}

   public record Mapping(long heimatTaskId, boolean canBeSynced, boolean shouldBeSynced, StyledMessage syncMessage,
                         String bookingHint, List<HeimatTime> existingTimes, List<Project> projects, String heimatNotes,
                         String keeptimeNotes, long heimatSeconds, long keeptimeSeconds) {}

   public record HeimatErrors(UserMapping mapping, String errorMessage) {}

   public static class ProjectMapping {
      private Project project;
      private HeimatTask heimatTask;

      public ProjectMapping(final Project project, final HeimatTask heimatTask) {
         this.project = project;
         this.heimatTask = heimatTask;
      }

      public Project getProject() {
         return project;
      }

      public void setProject(final Project project) {
         this.project = project;
      }

      public HeimatTask getHeimatTask() {
         return heimatTask;
      }

      public void setHeimatTask(final HeimatTask heimatTask) {
         this.heimatTask = heimatTask;
      }
   }
}
