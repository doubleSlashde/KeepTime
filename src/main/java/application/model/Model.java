package application.model;

import java.util.ArrayList;
import java.util.List;

// TODO observable stuff for ui
public class Model {

   public List<Project> availableProjects = new ArrayList<>();

   public List<Work> pastWorkItems = new ArrayList<>();

   public Work activeWorkItem;

}
