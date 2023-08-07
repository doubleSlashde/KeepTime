package de.doubleslash.keeptime.REST_API.controller;


import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("")
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/project")
    public Project getProjectById(@RequestParam(name = "id") final long id) {
        final Optional<Project> project = projectRepository.findById(id);

        if (project.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id '" + id + "' not found");

        return project.get();
    }

    @GetMapping("/byName")
    public List<Project> getProjectsByName(@RequestParam(name = "name") final String name) {
        final List<Project> projects = projectRepository.findAll().stream().filter(project -> project.getName().equals(name)).collect(Collectors.toList());

        return projects;
    }
}
