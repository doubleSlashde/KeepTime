package de.doubleslash.keeptime.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class ConfigParserTest {
   private final Logger Log = LoggerFactory.getLogger(this.getClass());

   ClassLoader classLoader = getClass().getClassLoader();
   private final String CONFIG_FILENAME = classLoader.getResource("configTest.xml").getFile();

   private final Project idle = new Project("Idle", Color.AQUAMARINE, true, 0);
   private final Project heinz = new Project("Heinz", Color.BLUEVIOLET, true, 1);
   private final Project peter = new Project("Peter", Color.CRIMSON, true, 2);
   private final Project hinzUndKunz = new Project("Hinz und Kunz", Color.DARKRED, true, 3);
   private final ObservableList<Project> projects = FXCollections.observableArrayList();
   private final Controller controller = mock(Controller.class);

   @Before
   public void setUp() {
      projects.add(idle);
      projects.add(heinz);
      projects.add(peter);
      projects.add(hinzUndKunz);

   }

   @Test
   public void testImportFromFileOnce() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);

      verify(controller).addNewProject(eq("Peter Lustig"), anyBoolean(), any(Color.class), anyInt());
      verify(controller).addNewProject(eq("Peter Pan"), anyBoolean(), any(Color.class), anyInt());

      assertEquals(6, projects.size());
   }

   private void loadConfigFile(final String configFile) {
      final ConfigParser testSubject = new ConfigParser(controller);
      final File inputfile = new File(configFile);
      testSubject.parseConfig(inputfile);
   }

   @Test
   public void testDoNotImportIdle() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);

      verify(controller, never()).addNewProject(eq("Idle"), anyBoolean(), any(Color.class), anyInt());
   }

   @Test
   public void testImportNonexistingProjects() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);

      verify(controller, atLeastOnce()).addNewProject(eq("Peter Pan"), anyBoolean(), any(Color.class), anyInt());
   }

   @Test
   public void testIndexIsRight() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);

      verify(controller).addNewProject(eq("Peter Pan"), anyBoolean(), any(Color.class), eq(5));
   }

   @Test
   public void testProject1IsParsedCorrectly() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);
      verify(controller, atLeastOnce()).addNewProject("Peter Lustig", true, Color.GREEN, 4);
   }

   @Test
   public void testProject2IsParsedCorrectly() {
      when(controller.getAvailableProjects()).thenReturn(projects);
      doAnswer(new Answer() {
         @Override
         public Object answer(final InvocationOnMock invocation) {
            final Object[] args = invocation.getArguments();
            final Object mock = invocation.getMock();
            Log.debug("called with arguments: " + Arrays.toString(args));
            projects.add(new Project((String) args[0], (Color) args[2], (Boolean) args[1], (Integer) args[3]));
            return "";

         }
      }).when(controller).addNewProject(anyString(), anyBoolean(), any(Color.class), anyInt());

      loadConfigFile(CONFIG_FILENAME);

      verify(controller, atLeastOnce()).addNewProject("Peter Pan", false, Color.WHITE, 5);
   }
}
