package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.persistenceconverter.ColorConverter;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Convert;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {
   private ProjectRepository projectRepository;
   private WorkRepository workRepository;
   private Controller controller;

   private Model model;

   public ProjectController(final ProjectRepository projectRepository, final WorkRepository workRepository,
         final Controller controller, Model model) {
      this.projectRepository = projectRepository;
      this.workRepository = workRepository;
      this.controller = controller;
      this.model = model;
   }

   @GetMapping("")
   public List<Project> getProjects(@RequestParam(name = "name", required = false) final String name) {
      List<Project> projects = projectRepository.findAll();

      if (name != null) {
         return projects.stream().filter(project -> project.getName().equals(name)).collect(Collectors.toList());
      }
      return projects;
   }

   @GetMapping("/{id}")
   public @Valid Project getProjectById(@PathVariable final long id) {
      final Optional<Project> project = projectRepository.findById(id);

      if (project.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id '" + id + "' not found");
      }

      return project.get();
   }

   @GetMapping("/{id}/works")
   public List<Work> getWorksFromProject(@PathVariable final long id) {
      return workRepository.findAll()
                           .stream()
                           .filter(work -> work.getProject().getId() == id)
                           .collect(Collectors.toList());
   }

   @GetMapping("/{projectId}/works/{workId}")
   public Work getWorkByIdFromProject(@PathVariable final long projectId, @PathVariable final long workId) {
      return workRepository.findAll()
                           .stream()
                           .filter(work -> work.getProject().getId() == projectId && work.getId() == workId)
                           .reduce((a, b) -> {
                              throw new IllegalStateException("Multiple elements: " + a + ", " + b);
                           })
                           .orElseThrow(() -> new ResourceNotFoundException(
                                 "Work with id '" + workId + "' related to project with id '" + projectId
                                       + "' not found"));
   }

   @PostMapping("")
   public @Valid Project createProject(@Valid @RequestBody final Project project) {
      controller.addNewProject(project);
      return project;
   }

   //   @Convert(converter = ColorConverter.class, disableConversion = false)
   //   private Color color;
   //
   //   @PutMapping("{id}")
   //   public ResponseEntity<Project> updateProject(@PathVariable final long id,
   //         @Valid @RequestBody final Project newValuedProject) {
   //      final Project updateProject = getProjectById(id);
   //
   //      updateProject.setName(newValuedProject.getName());
   //      updateProject.setDescription(newValuedProject.getDescription());
   //      updateProject.setIndex(newValuedProject.getIndex());
   //      updateProject.setWork(newValuedProject.isWork());
   //      color = newValuedProject.getColor();
   //      updateProject.setColor(color);
   //      //      project.setColor(Color.RED);
   //      System.err.println(newValuedProject.getColor().getRed() + " R " + updateProject.getColor().getRed());
   //      System.err.println(newValuedProject.getColor().getGreen() + " G " + updateProject.getColor().getGreen());
   //      System.err.println(newValuedProject.getColor().getBlue() + " B " + updateProject.getColor().getBlue());
   //
   //
   //      updateProject.setDefault(newValuedProject.isDefault());
   //      updateProject.setEnabled(newValuedProject.isEnabled());
   //
   //      projectRepository.save(updateProject);
   //
   //      return ResponseEntity.ok(updateProject);
   //   }
   //

   ColorConverter colorConverter;

   @PutMapping("{id}")
   public ResponseEntity<Project> updateProject(@PathVariable final long id,
         @Valid @RequestBody final Project newValuedProject) {
      final Project updateProject = getProjectById(id);

      updateProject.setName(newValuedProject.getName());
      updateProject.setDescription(newValuedProject.getDescription());
      updateProject.setIndex(newValuedProject.getIndex());
      updateProject.setWork(newValuedProject.isWork());

      updateProject.setColor(newValuedProject.getColor());
      updateProject.setDefault(newValuedProject.isDefault());
      updateProject.setEnabled(newValuedProject.isEnabled());

      projectRepository.save(updateProject);

      return ResponseEntity.ok(updateProject);
   }

   @PostMapping("/{id}/works")
   public @Valid Work createWorkInProject(@PathVariable final long id, @Valid @RequestBody final Work work) {
      work.setProject(getProjectById(id));
      workRepository.save(work);

      return work;
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteProject(@PathVariable final long id) {
      Project project = getProjectById(id);
      if (project == null) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with the ID '" + id + "' not found");
      } else if (project.isDefault()) {
         return new ResponseEntity<>("Project cannot be deleted as it is the default", HttpStatus.BAD_REQUEST);
      } else {
         controller.deleteProject(project);
         projectRepository.delete(project);
         return new ResponseEntity<>("Project successfully deleted", HttpStatus.OK);
      }
   }

   @GetMapping("/current")
   public Project getWorkProjects() {
      Project workProjects = model.activeWorkItem.get().getProject();
      return workProjects;
   }

   @PutMapping("/current")
   public ResponseEntity<Project> changeProject(@Valid @RequestBody Project newProject) {
      try {
         controller.changeProject(newProject);
         return ResponseEntity.ok(newProject);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
      }
   }

   @ResponseStatus(value = HttpStatus.NOT_FOUND)
   public class ResourceNotFoundException extends RuntimeException {
      public ResourceNotFoundException(String message) {
         super(message);
      }
   }

}