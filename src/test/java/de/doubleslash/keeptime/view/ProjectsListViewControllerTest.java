package de.doubleslash.keeptime.view;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.doubleslash.keeptime.model.Project;

public class ProjectsListViewControllerTest {

   @Test
   public void shouldMatchWhenSearchTermIsInDescription() {
      final Project project = new Project();

      project.setName("NotSearchTerm");
      project.setDescription("Searchterm_test");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("test", project));
   }

   @Test
   public void shouldMatchWhenSearchTermIsInName() {
      final Project project = new Project();

      project.setName("SearchTerm_CZ");
      project.setDescription("NotSearchTerm");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("CZ", project));
   }

   @Test
   public void shouldMatchWhenSearchTermIsNameAndDescription() {
      final Project project = new Project();

      project.setName("SearchTerm_Peter");
      project.setDescription("Peter_IsSearchTerm");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("Peter", project));
   }

   @Test
   public void shouldMatchWhenCaseDiffersInNameAsSearchTerm() {
      final Project project = new Project();

      project.setName("PeTerPAn");
      project.setDescription("");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("pEtErpAn", project));
   }

   @Test
   public void shouldMatchWhenCaseDiffersInDescriptionAsSearchTerm() {
      final Project project = new Project();

      project.setName("");
      project.setDescription("MylItTLeaNT");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("mYliTTlEAnT", project));
   }

}
