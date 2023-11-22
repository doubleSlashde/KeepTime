package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/works")

public class WorksController {

   private WorkRepository workRepository;
   private Model model;

   public WorksController(final WorkRepository workRepository, final Controller controller,Model model) {
      this.workRepository = workRepository;
      this.model= model;
   }

//   @GetMapping("")
//   public List<Work> getWorks(@RequestParam(name = "name", required = false) final String project) {
//      List<Work> works = workRepository.findAll();
//
//      if (project != null) {
//         return works.stream().filter(work -> work.getProject().equals(project)).collect(Collectors.toList());
//      }
//      return  works;
//   }
@GetMapping("")
public List<WorkDTO> getWorks(@RequestParam(name = "name", required = false) final String projectName) {
   List<Work> works = workRepository.findAll();

   Stream<Work> workStream = works.stream();

   if (projectName != null) {
      workStream = workStream.filter(work -> work.getProject().getName().equals(projectName));
   }

   return workStream.map(WorkMapper.INSTANCE::workToWorkDTO)
                    .collect(Collectors.toList());
}



   @PutMapping("/{id}")
   public ResponseEntity<Work> editWork(@PathVariable("id") Long workId, @RequestBody Work newValuedWork) {
      Optional<Work> optionalWork = workRepository.findById(workId);

      if (optionalWork.isPresent()) {
         Work workToBeEdited = optionalWork.get();

         workToBeEdited.setStartTime(newValuedWork.getStartTime());
         workToBeEdited.setEndTime(newValuedWork.getEndTime());
         workToBeEdited.setNotes(newValuedWork.getNotes());
         workToBeEdited.setProject(newValuedWork.getProject());

         Work editedWork = workRepository.save(workToBeEdited);
         return ResponseEntity.ok(editedWork);
      } else {
         return ResponseEntity.notFound().build();
      }
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteWork(@PathVariable final long id) {
      Optional<Work> optionalWork = workRepository.findById(id);

      if (optionalWork.isPresent()) {
         Work workToBeDeleted = optionalWork.get();
         workRepository.delete(workToBeDeleted);
         return new ResponseEntity<>("Work successfully deleted", HttpStatus.OK);
      } else {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work with the ID " + id + " not found");
      }
   }

   @GetMapping("/current")
   public Work getWorkProjects() {
      Work workProjects = model.activeWorkItem.get();
      return workProjects;
   }
}
