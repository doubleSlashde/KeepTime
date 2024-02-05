// Copyright 2024 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.REST_API.DTO.ProjectColorDTO;
import de.doubleslash.keeptime.REST_API.DTO.WorkDTO;
import de.doubleslash.keeptime.REST_API.mapper.ProjectMapper;
import de.doubleslash.keeptime.REST_API.mapper.WorkMapper;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
   public ResponseEntity<List<ProjectColorDTO>> getProjectColorDTOsByName(
         @RequestParam(name = "name", required = false) final String name) {
      List<Project> projects;

      if (name != null) {
         projects = projectRepository.findByName(name);
      } else {
         projects = projectRepository.findAll();
      }
      List<ProjectColorDTO> projectColorDTOs = projects.stream()
                                                       .map(ProjectMapper.INSTANCE::projectToProjectDTO)
                                                       .collect(Collectors.toList());
      return ResponseEntity.ok(projectColorDTOs);
   }

   @GetMapping("/{id}")
   public @Valid ProjectColorDTO getProjectById(@PathVariable final long id) {
      final Optional<Project> project = projectRepository.findById(id);

      if (project.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id '" + id + "' not found");
      }
      return ProjectMapper.INSTANCE.projectToProjectDTO(project.get());
   }

   @GetMapping("/{id}/works")
   public List<WorkDTO> getWorksFromProject(@PathVariable final long id) {
      return workRepository.findAll().stream().filter(work -> {
         Project project = work.getProject();
         return project != null && project.getId() == id;
      }).map(work -> WorkMapper.INSTANCE.workToWorkDTO(work)).collect(Collectors.toList());
   }

   @GetMapping("/{projectId}/works/{workId}")
   public WorkDTO getWorkByIdFromProject(@PathVariable final long projectId, @PathVariable final long workId) {
      return workRepository.findAll()
                           .stream()
                           .filter(work -> {
                              Project project = work.getProject();
                              return project != null && project.getId() == projectId && work.getId() == workId;
                           })
                           .findFirst()
                           .map(WorkMapper.INSTANCE::workToWorkDTO)
                           .orElseThrow(() -> new ResourceNotFoundException(
                                 "Work with id '" + workId + "' related to project with id '" + projectId
                                       + "' not found"));
   }

   @PostMapping("")
   public ResponseEntity<ProjectColorDTO> createProject(@Valid @RequestBody final ProjectColorDTO newProjectDTO) {
      try {
         Project newProject = ProjectMapper.INSTANCE.projectDTOToProject(newProjectDTO);

         controller.addNewProject(newProject);

         model.getProjectRepository().save(newProject);

         ProjectColorDTO projectDTO = ProjectMapper.INSTANCE.projectToProjectDTO(newProject);

         return ResponseEntity.status(HttpStatus.CREATED).body(projectDTO);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
   }

   @PutMapping("/{id}")
   public ResponseEntity<ProjectColorDTO> updateProjectColorDTO(@PathVariable final long id,
         @Valid @RequestBody final ProjectColorDTO newValuedProjectDTO) {
      try {
         Optional<Project> optionalProject = projectRepository.findById(id);

         if (optionalProject.isEmpty()) {
            return ResponseEntity.notFound().build();
         }

         Project existingProject = optionalProject.get();
         Project newValuedProject = ProjectMapper.INSTANCE.projectDTOToProject(newValuedProjectDTO);

         existingProject.setName(newValuedProject.getName());
         existingProject.setDescription(newValuedProject.getDescription());
         existingProject.setIndex(newValuedProject.getIndex());
         existingProject.setWork(newValuedProject.isWork());
         existingProject.setColor(newValuedProject.getColor());
         existingProject.setDefault(newValuedProject.isDefault());
         existingProject.setEnabled(newValuedProject.isEnabled());

         projectRepository.save(existingProject);

         ProjectColorDTO updatedProjectDTO = ProjectMapper.INSTANCE.projectToProjectDTO(existingProject);

         return ResponseEntity.ok(updatedProjectDTO);
      } catch (DataAccessException e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
   }

   @PostMapping("/{id}/works")
   public ResponseEntity<WorkDTO> createWorkInProject(@PathVariable final long id,
         @Valid @RequestBody final Work work) {
      Optional<Project> projectOptional = projectRepository.findById(id);

      if (projectOptional.isPresent()) {
         Project project = projectOptional.get();
         work.setProject(project);
         workRepository.save(work);

         WorkDTO workDTO = WorkMapper.INSTANCE.workToWorkDTO(work);

         return ResponseEntity.status(HttpStatus.CREATED).body(workDTO);
      } else {
         return ResponseEntity.notFound().build();
      }
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteProject(@PathVariable final long id) {
      Optional<Project> projectOptional = projectRepository.findById(id);

      if (!projectOptional.isPresent()) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with the ID '" + id + "' not found");
      }

      Project project = projectOptional.get();

      if (project.isDefault()) {
         return new ResponseEntity<>("Project cannot be deleted as it is the default", HttpStatus.BAD_REQUEST);
      } else {
         controller.deleteProject(project);
         projectRepository.delete(project);
         return new ResponseEntity<>("Project successfully deleted", HttpStatus.OK);
      }
   }

   @GetMapping("/current")
   public ProjectColorDTO getWorkProjects() {
      Project project = model.activeWorkItem.get().getProject();
      return ProjectMapper.INSTANCE.projectToProjectDTO(project);
   }

   @PutMapping("/current")
   public ResponseEntity<ProjectColorDTO> changeProject(@Valid @RequestBody Project newProject) {
      try {
         controller.changeProject(newProject);
         ProjectColorDTO projectDTO = ProjectMapper.INSTANCE.projectToProjectDTO(newProject);
         return ResponseEntity.ok(projectDTO);
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