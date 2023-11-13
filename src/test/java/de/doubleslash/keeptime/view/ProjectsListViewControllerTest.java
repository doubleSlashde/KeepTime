// Copyright 2019 doubleSlash Net Business GmbH
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

package de.doubleslash.keeptime.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import de.doubleslash.keeptime.model.Project;
import org.junit.jupiter.api.Test;

class ProjectsListViewControllerTest {

   @Test
   void shouldMatchWhenSearchTermIsInDescription() {
      final Project project = new Project();

      project.setName("NotSearchTerm");
      project.setDescription("Searchterm_test");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("test", project));
   }

   @Test
   void shouldMatchWhenSearchTermIsInName() {
      final Project project = new Project();

      project.setName("SearchTerm_CZ");
      project.setDescription("NotSearchTerm");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("CZ", project));
   }

   @Test
   void shouldMatchWhenSearchTermIsNameAndDescription() {
      final Project project = new Project();

      project.setName("SearchTerm_Peter");
      project.setDescription("Peter_IsSearchTerm");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("Peter", project));
   }

   @Test
   void shouldMatchWhenCaseDiffersInNameAsSearchTerm() {
      final Project project = new Project();

      project.setName("PeTerPAn");
      project.setDescription("");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("pEtErpAn", project));
   }

   @Test
   void shouldMatchWhenCaseDiffersInDescriptionAsSearchTerm() {
      final Project project = new Project();

      project.setName("");
      project.setDescription("MylItTLeaNT");

      assertTrue(ProjectsListViewController.doesProjectMatchSearchFilter("mYliTTlEAnT", project));
   }

   @Test
   void shouldNotMatchWhenDescriptionAndNameAreNotMatching() {
      final Project project = new Project();

      project.setName("MyNameIsJohn");
      project.setDescription("I am a project.");

      assertFalse(ProjectsListViewController.doesProjectMatchSearchFilter("Hellow world", project));
   }

}
