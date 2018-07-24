package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import application.model.repos.ProjectRepository;

@Component
public class Test {
   @Autowired
   public ProjectRepository projectRepository;
}
