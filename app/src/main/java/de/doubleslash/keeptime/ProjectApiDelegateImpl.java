package de.doubleslash.keeptime;

import de.doubleslash.keeptime.api.ProjectsApiDelegate;
import de.doubleslash.keeptime.model.Project;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

 import java.util.List;

@Component
public class ProjectApiDelegateImpl implements ProjectsApiDelegate {
    @Override
    public ResponseEntity<List<Project>> getProjects() {
        Project project= new Project();
        project.setName("Test");
        project.setWork(true);

        return ResponseEntity.ok(List.of(project));
    }
}
