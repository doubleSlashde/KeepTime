package de.doubleslash.keeptime.controller;

import de.doubleslash.keeptime.model.ExternalProjectMapping;
import de.doubleslash.keeptime.model.ExternalSystem;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ExternalProjectsMappingsRepository;
import de.doubleslash.keeptime.model.settings.HeimatSettings;
import de.doubleslash.keeptime.rest.integration.heimat.HeimatAPI;
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

   private HeimatAPI heimatAPI;

   @Autowired
   public HeimatController(HeimatSettings heimatSettings,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository, final Controller controller) {
      this.heimatSettings = heimatSettings;
      this.controller = controller;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
      this.heimatAPI = new HeimatAPI(heimatSettings.getHeimatUrl(), heimatSettings.getHeimatPat());
   }

   // for testing only
   HeimatController(HeimatSettings heimatSettings, HeimatAPI heimatAPI,
         ExternalProjectsMappingsRepository externalProjectsMappingsRepository, final Controller controller) {
      this.heimatSettings = heimatSettings;
      this.controller = controller;
      this.externalProjectsMappingsRepository = externalProjectsMappingsRepository;
      this.heimatAPI = heimatAPI;
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
         throw new SecurityException("Could not connect to HEIMAT API. Maybe wrong configuration?", e);
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
         final Optional<ExternalProjectMapping> optHeimatMapping = mappedProjects.stream()
                                                                                 .filter(mp -> mp.getProject().getId()
                                                                                       == project.getId())
                                                                                 .findAny();
         List<HeimatTime> optionalAlreadyBookedTimes = new ArrayList<>();
         Optional<Mapping> optionalExistingMapping = Optional.empty();
         if (optHeimatMapping.isPresent()) {
            isMappedInHeimat = true;
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
         String canBeSynced;
         if (!isMappedInHeimat) {
            canBeSynced = "Not mapped to Heimat task.\nMap in settings dialog.";
         } else if (heimatTasks.stream().noneMatch(ht -> ht.id() == optHeimatMapping.get().getExternalTaskId())) {
            canBeSynced = "Heimat Task is not available (anymore).\nPlease check mappings in settings dialog.";
            isMappedInHeimat = false;
         } else {
            final ExternalProjectMapping externalProjectMapping = optHeimatMapping.get();
            canBeSynced = "Sync to " + externalProjectMapping.getExternalTaskName() + "\n("
                  + externalProjectMapping.getExternalProjectName() + ")";
         }

         if (optionalExistingMapping.isPresent()) {
            final Mapping existingMapping = optionalExistingMapping.get();
            final ArrayList<Project> projects = new ArrayList<>(existingMapping.projects());
            projects.add(project);
            final Mapping mapping = new Mapping(isMappedInHeimat ? optHeimatMapping.get().getExternalTaskId() : -1,
                  isMappedInHeimat, canBeSynced, existingMapping.existingTimes(), projects,
                  existingMapping.heimatNotes(), existingMapping.keeptimeNotes() + ". " + keeptimeNotes,
                  existingMapping.heimatSeconds(), existingMapping.keeptimeSeconds() + projectWorkSeconds);
            list.remove(existingMapping);
            list.add(mapping);
         } else {
            final List<Project> projects = Collections.singletonList(project);
            final Mapping mapping = new Mapping(isMappedInHeimat ? optHeimatMapping.get().getExternalTaskId() : -1,
                  isMappedInHeimat, canBeSynced, optionalAlreadyBookedTimes, projects, heimatNotes, keeptimeNotes,
                  heimatTimeSeconds, projectWorkSeconds);
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
         final HeimatTask heimatTask = heimatTasks.stream()
                                                  .filter(t -> t.id() == times.get(0).taskId())
                                                  .findAny()
                                                  .orElseThrow();
         final Mapping mapping = new Mapping(id, false,
               "Not mapped in KeepTime\n\n" + heimatTask.name() + "\n" + heimatTask.taskHolderName(), times,
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

         final Mapping mapping2 = new Mapping(id, true,
               "Present in HEIMAT but not KeepTime\n\n" + externalProjectMapping.getExternalTaskName() + "\n"
                     + externalProjectMapping.getExternalProjectName(), times, mappedProjects.stream()
                                                                                             .filter(
                                                                                                   mp -> mp.getExternalTaskId()
                                                                                                         == id)
                                                                                             .map(ExternalProjectMapping::getProject)
                                                                                             .toList(), heimatNotes, "",
               heimatTimeSeconds, 0);
         list.add(mapping2);
      });

      return list;
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
         if (item.userNotes.isEmpty()) {
            errors.add(new HeimatErrors(item, "No notes were given"));
            return;
         }
         final int durationInMinutes = item.userMinutes;
         if (durationInMinutes <= 0 || durationInMinutes % 15 != 0) {
            errors.add(new HeimatErrors(item, "Duration '" + durationInMinutes + "' is not valid for project"));
            return;
         }

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
                      .sorted(Comparator.comparing(HeimatTask::taskHolderName).thenComparing(HeimatTask::name))
                      .toList();
   }

   public record UserMapping(Mapping mapping, boolean shouldSync, String userNotes, int userMinutes) {}

   public record Mapping(long heimatTaskId, boolean canBeSynced, String syncMessage, List<HeimatTime> existingTimes,
                         List<Project> projects, String heimatNotes, String keeptimeNotes, long heimatSeconds,
                         long keeptimeSeconds) {}

   public record HeimatErrors(UserMapping mapping, String errorMessage) {}
}
