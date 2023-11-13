package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.controller.Controller;
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

@RestController
@RequestMapping("/works")

public class WorksController {
   private WorkRepository workRepository;

   public WorksController(final WorkRepository workRepository, final Controller controller) {
      this.workRepository = workRepository;
   }

   @GetMapping("")
   public List<Work> getWorks(@RequestParam(name = "name", required = false) final String project) {
      List<Work> works = workRepository.findAll();

      if (project != null) {
         return works.stream().filter(work -> work.getProject().equals(project)).collect(Collectors.toList());
      }
      return works;
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
}
