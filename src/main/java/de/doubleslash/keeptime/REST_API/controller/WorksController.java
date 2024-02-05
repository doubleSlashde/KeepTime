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

import de.doubleslash.keeptime.REST_API.DTO.WorkDTO;
import de.doubleslash.keeptime.REST_API.mapper.WorkMapper;
import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
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

   public WorksController(final WorkRepository workRepository, final Controller controller, Model model) {
      this.workRepository = workRepository;
      this.model = model;
   }

   @GetMapping("")
   public List<WorkDTO> getWorks(@RequestParam(name = "name", required = false) final String projectName) {
      List<Work> works = workRepository.findAll();

      Stream<Work> workStream = works.stream();

      if (projectName != null) {
         workStream = workStream.filter(work -> work.getProject().getName().equals(projectName));
      }
      return workStream.map(WorkMapper.INSTANCE::workToWorkDTO).collect(Collectors.toList());
   }

   @PutMapping("/{id}")
   public ResponseEntity<WorkDTO> editWork(@PathVariable("id") Long workId, @RequestBody WorkDTO newValuedWorkDTO) {
      Work newValuedWork = WorkMapper.INSTANCE.workDTOToWork(newValuedWorkDTO);
      Optional<Work> optionalWork = workRepository.findById(workId);

      if (optionalWork.isPresent()) {
         Work workToBeEdited = optionalWork.get();

         workToBeEdited.setStartTime(newValuedWork.getStartTime());
         workToBeEdited.setEndTime(newValuedWork.getEndTime());
         workToBeEdited.setNotes(newValuedWork.getNotes());
         workToBeEdited.setProject(newValuedWork.getProject());

         Work editedWork = workRepository.save(workToBeEdited);

         return ResponseEntity.ok(WorkMapper.INSTANCE.workToWorkDTO(editedWork));
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
   public ResponseEntity<WorkDTO> getCurrentWork() {
      Work workProjects = model.activeWorkItem.get();

      if (workProjects != null) {
         return ResponseEntity.ok(WorkMapper.INSTANCE.workToWorkDTO(workProjects));
      } else {
         return ResponseEntity.notFound().build();
      }
   }
}
