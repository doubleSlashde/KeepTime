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

import de.doubleslash.keeptime.rest.DTO.WorkDTO;
import de.doubleslash.keeptime.rest.mapper.WorkMapper;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/works")
public class WorksController {

   private final WorkRepository workRepository;
   private final ProjectRepository projectRepository;
   private final Model model;
   private final WorkMapper workMapper;

   public WorksController(final WorkRepository workRepository,final ProjectRepository projectRepository, Model model, WorkMapper workMapper) {
      this.workRepository = workRepository;
      this.projectRepository=projectRepository;
      this.model = model;
      this.workMapper = workMapper;
   }

   @GetMapping
   public List<WorkDTO> getWorks(@RequestParam(name = "name", required = false) final String projectName) {
      List<Work> works = workRepository.findAll();

      Stream<Work> workStream = works.stream();

      if (projectName != null) {
         workStream = workStream.filter(work -> work.getProject().getName().equals(projectName));
      }
      return workStream.map(workMapper::workToWorkDTO).toList();
   }

   @PutMapping("/{id}")
   public ResponseEntity<WorkDTO> editWork(@PathVariable("id") Long workId, @RequestBody WorkDTO newValuedWorkDTO) {

      if(workId != newValuedWorkDTO.getId() ){
         return ResponseEntity.badRequest().build();
      }

      Work newValuedWork = workMapper.workDTOToWork(newValuedWorkDTO);
      Optional<Work> optionalWork = workRepository.findById(workId);
      Optional<Project> optionalProject = projectRepository.findById(newValuedWorkDTO.getProject().getId());

      if (optionalWork.isEmpty() || optionalProject.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      Work workToBeEdited = optionalWork.get();

      workToBeEdited.setStartTime(newValuedWork.getStartTime());
      workToBeEdited.setEndTime(newValuedWork.getEndTime());
      workToBeEdited.setNotes(newValuedWork.getNotes());
      workToBeEdited.setProject(optionalProject.get());

      Work editedWork = workRepository.save(workToBeEdited);

      return ResponseEntity.ok(workMapper.workToWorkDTO(editedWork));
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<String> deleteWork(@PathVariable final long id) {
      Optional<Work> optionalWork = workRepository.findById(id);

      if (optionalWork.isEmpty()) {
         return ResponseEntity.notFound().build();
      }

      Work workToBeDeleted = optionalWork.get();
      workRepository.delete(workToBeDeleted);
      return new ResponseEntity<>("Work successfully deleted", HttpStatus.OK);
   }

   @GetMapping("/current")
   public ResponseEntity<WorkDTO> getCurrentWork() {
      Work workProjects = model.activeWorkItem.get();

      if (workProjects != null) {
         return ResponseEntity.ok(workMapper.workToWorkDTO(workProjects));
      } else {
         return ResponseEntity.notFound().build();
      }
   }
}
