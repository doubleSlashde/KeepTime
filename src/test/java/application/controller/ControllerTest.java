package application.controller;

import org.junit.Before;

import de.ds.keeptime.controller.Controller;
import de.ds.keeptime.model.Model;

public class ControllerTest {

   static Controller testee;

   @Before
   public static void beforeTest() {
      final Model model = new Model();
      testee = new Controller(model);
   }

   public void startsWithIdleProject() {

   }
}
