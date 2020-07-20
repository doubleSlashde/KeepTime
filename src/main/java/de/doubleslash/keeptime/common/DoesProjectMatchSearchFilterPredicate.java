package de.doubleslash.keeptime.common;

import java.util.function.Predicate;

import de.doubleslash.keeptime.model.Project;

public class DoesProjectMatchSearchFilterPredicate implements Predicate<Project> {

   private final String searchText;

   public DoesProjectMatchSearchFilterPredicate(final String searchText) {
      this.searchText = searchText;
   }

   @Override
   public boolean test(final Project project) {
      // If filter text is empty, display all data.
      if (searchText == null || searchText.isEmpty()) {
         return true;
      }

      final String lowerCaseFilter = searchText.toLowerCase();

      if (project.getName().toLowerCase().contains(lowerCaseFilter)
            || project.getDescription().toLowerCase().contains(lowerCaseFilter)) {
         return true;
      }

      return false;
   }
}
