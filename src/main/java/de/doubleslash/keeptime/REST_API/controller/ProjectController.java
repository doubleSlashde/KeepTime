package de.doubleslash.keeptime.REST_API.controller;


import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Model;
import de.doubleslash.keeptime.model.Project;
import de.doubleslash.keeptime.model.Work;
import de.doubleslash.keeptime.model.repos.ProjectRepository;
import de.doubleslash.keeptime.model.repos.WorkRepository;
import org.springframework.http.HttpStatus;
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

    public ProjectController(final ProjectRepository projectRepository, final WorkRepository workRepository, final Controller controller) {
        this.projectRepository = projectRepository;
        this.workRepository = workRepository;
        this.controller = controller;
    }

    @GetMapping("")
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/project")
    public Project getProjectById(@RequestParam(name = "id") final long id) {
        final Optional<Project> project = projectRepository.findById(id);

        if (project.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id '" + id + "' not found");
        }

        return project.get();
    }

    @GetMapping("/project-index")
    public Project getProjectByIndex(@RequestParam(name = "index") final long index) {
        final Optional<Project> project = projectRepository.findById(index);

        if (project.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with index '" + index + "' not found");
        }

        return project.get();
    }

    @GetMapping("/by-name")
    public List<Project> getProjectsByName(@RequestParam(name = "name") final String name) {
        final List<Project> projects = projectRepository.findAll().stream().filter(project -> project.getName().equals(name)).collect(Collectors.toList());

        return projects;
    }

    @GetMapping("/{id}/works")
    public List<Work> getWorksFromProject(@PathVariable final long id) {
        return workRepository.findAll().stream().filter(work -> work.getProject().getId() == id).collect(Collectors.toList());
    }

    @GetMapping("/{projectId}/works/{workId}")
    public Work getWorkByIdFromProject(@PathVariable final long projectId, @PathVariable final long workId) {
        final Optional<Work> workOpt = workRepository.findAll().stream().filter(work -> work.getProject().getId() == projectId && work.getId() == workId).reduce((a, b) -> {
            throw new IllegalStateException("Multiple elements: " + a + ", " + b);
        });

        if (workOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work with id '" + workId + "' related to project with id '" + projectId + "' not found");
        }

        return workOpt.get();
    }

    @PostMapping("")
    public Project createProject(@Valid @RequestBody final Project project) {
        controller.addNewProject(project);
        return project;
    }
}
