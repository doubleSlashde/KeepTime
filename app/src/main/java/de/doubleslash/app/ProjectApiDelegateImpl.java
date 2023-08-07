package de.doubleslash.app;

import de.doubleslash.model.Project;
import de.doubleslash.api.ProjectsApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

 import java.util.List;

@Component
public class ProjectApiDelegateImpl implements ProjectsApiDelegate {
    @Override
    public ResponseEntity<List<Project>> getProjects() {
        System.out.println("--------------------------------------------------------");
        Project project= new Project();
        project.setName("Test");
        project.setIsWork(true);

        return ResponseEntity.ok(List.of(project));
    }
}
