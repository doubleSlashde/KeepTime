package de.doubleslash.keeptime.REST_API.controller;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/works")

public class WorksController {
      private WorkRepository workRepository;

      public WorksController(final WorkRepository workRepository,
            final Controller controller) {
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
}
