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

package de.doubleslash.keeptime.rest.controller;

import de.doubleslash.keeptime.rest.DTO.ProjectDTO;
import de.doubleslash.keeptime.rest.DTO.ProjectIdentificationDTO;
import de.doubleslash.keeptime.rest.DTO.WorkDTO;
import de.doubleslash.keeptime.rest.mapper.ProjectMapper;
import de.doubleslash.keeptime.rest.mapper.WorkMapper;
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

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
   private final ProjectRepository projectRepository;
   private final WorkRepository workRepository;
   private final Controller controller;
   private final Model model;
   private final WorkMapper workMapper;
   private final ProjectMapper projectMapper;

   public ProjectController(final ProjectRepository projectRepository, final WorkRepository workRepository,
         final Controller controller, Model model, WorkMapper workMapper, ProjectMapper projectMapper) {
      this.projectRepository = projectRepository;
      this.workRepository = workRepository;
      this.controller = controller;
      this.model = model;
      this.workMapper = workMapper;
      this.projectMapper = projectMapper;
   }

   @GetMapping
   public ResponseEntity<List<ProjectDTO>> getProjectColorDTOsByName(
         @RequestParam(name = "name", required = false) final String name) {
      List<Project> projects;

      if (name != null) {
         projects = projectRepository.findByName(name);
      } else {
         projects = projectRepository.findAll();
      }
      List<ProjectDTO> projectDTOS = projects.stream()
                                             .map(projectMapper::projectToProjectDTO)
                                             .toList();
      return ResponseEntity.ok(projectDTOS);
   }

   @GetMapping("/{id}")
   public @Valid ProjectDTO getProjectById(@PathVariable final long id) {
      final Optional<Project> project = projectRepository.findById(id);

      if (project.isEmpty()) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id '" + id + "' not found");
      }
      return projectMapper.projectToProjectDTO(project.get());
   }

   @GetMapping("/{id}/works")
   public List<WorkDTO> getWorksFromProject(@PathVariable final long id) {
      return workRepository.findByProjectId(id).stream().map(workMapper::workToWorkDTO).toList();
   }

   @PostMapping
   public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody final ProjectDTO newProjectDTO) {
      try {
         Project newProject = projectMapper.projectDTOToProject(newProjectDTO);

         FXUtils.runInFxThreadAndWait(()-> controller.addNewProject(newProject));

         ProjectDTO projectDTO = projectMapper.projectToProjectDTO(newProject);
         return ResponseEntity.status(HttpStatus.CREATED).body(projectDTO);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
   }

   @PutMapping("/{id}")
   public ResponseEntity<ProjectDTO> updateProject(@PathVariable final long id,
         @Valid @RequestBody final ProjectDTO newValuedProjectDTO) {

      if(id != newValuedProjectDTO.getId()){
         return ResponseEntity.badRequest().build();
      }
      Optional<Project> optionalProject = projectRepository.findById(id);

      if (optionalProject.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      Project existingProject = optionalProject.get();

      try {
         Project newValuedProject = projectMapper.projectDTOToProject(newValuedProjectDTO);

         FXUtils.runInFxThreadAndWait(()->
               controller.editProject(existingProject, newValuedProject));

         ProjectDTO updatedProjectDTO = projectMapper.projectToProjectDTO(existingProject);

         return ResponseEntity.ok(updatedProjectDTO);
      } catch (DataAccessException e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
   }

   @PostMapping("/{id}/works")
   public ResponseEntity<WorkDTO> createWorkInProject(@PathVariable final long id,
         @Valid @RequestBody final WorkDTO workDTO) {

      if(id != workDTO.getProject().getId()){
         return ResponseEntity.badRequest().build();
      }

      Optional<Project> projectOptional = projectRepository.findById(id);

      if (projectOptional.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      final Work newWork = workMapper.workDTOToWork(workDTO);
      Project project = projectOptional.get();
      newWork.setProject(project);

      workRepository.save(newWork);

      WorkDTO createdWorkDTO = workMapper.workToWorkDTO(newWork);

      return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkDTO);
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteProject(@PathVariable final long id) {
      Optional<Project> projectOptional = projectRepository.findById(id);

      if (projectOptional.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      Project project = projectOptional.get();

      if (project.isDefault()) {
         return new ResponseEntity<>("Project cannot be deleted as it is the default", HttpStatus.BAD_REQUEST);
      }
      FXUtils.runInFxThreadAndWait(()->
            controller.deleteProject(project));

      return new ResponseEntity<>("Project successfully deleted", HttpStatus.OK);
   }

   @GetMapping("/current")
   public ProjectDTO getWorkProjects() {
      Project project = model.activeWorkItem.get().getProject();
      return projectMapper.projectToProjectDTO(project);
   }

   @PutMapping("/current")
   public ResponseEntity<ProjectIdentificationDTO> changeProject(@Valid @RequestBody ProjectIdentificationDTO newProject) {
      Optional<Project> projectOptional = projectRepository.findById(newProject.getId());

      if (projectOptional.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      try {
         FXUtils.runInFxThreadAndWait(()->
               controller.changeProject(projectOptional.get()));

         return ResponseEntity.ok(newProject);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
      }
   }

   @ResponseStatus(value = HttpStatus.NOT_FOUND)
   public static class ResourceNotFoundException extends RuntimeException {
      public ResourceNotFoundException(String message) {
         super(message);
      }
   }
}