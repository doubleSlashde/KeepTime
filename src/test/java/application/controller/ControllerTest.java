package application.controller;

import org.junit.Before;

import application.model.Model;

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
