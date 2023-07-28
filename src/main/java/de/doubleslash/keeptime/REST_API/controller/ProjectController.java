package de.doubleslash.keeptime.REST_API.controller;


import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private ProjectRepository projectRepository;
    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

@GetMapping("")
    public List<Project>index(){
return projectRepository.findAll();
    }
}
