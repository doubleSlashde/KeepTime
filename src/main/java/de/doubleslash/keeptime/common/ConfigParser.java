package de.doubleslash.keeptime.common;

import java.io.File;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;

public class ConfigParser {
   Logger log = Logger.getLogger(ConfigParser.class.getName());

   Controller controller;

   public ConfigParser(final Controller controller) {
      this.controller = controller;
   }

   public static boolean hasConfigFile(final String fileName) {
      final File f = new File(fileName);
      return f.exists();
   }

   public void parserConfig(final File inputFile) {
      try {
         final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         final Document doc = dBuilder.parse(inputFile);
         doc.getDocumentElement().normalize();
         log.info("Root element :" + doc.getDocumentElement().getNodeName());
         final NodeList nList = doc.getElementsByTagName("project");
         log.info("----------------------------");

         // index makes sure to add new projects at the end
         int index = controller.getAvailableProjects().size();
         for (int temp = 0; temp < nList.getLength(); temp++) {
            final Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               final Element eElement = (Element) nNode;
               // get element
               final String name = eElement.getElementsByTagName("name").item(0).getTextContent();
               final String isWork = eElement.getElementsByTagName("isWork").item(0).getTextContent();
               final String color = eElement.getElementsByTagName("color").item(0).getTextContent();

               log.info("checking for: " + name);
               boolean exists = false;

               for (final Project p : controller.getAvailableProjects()) {
                  if (name.equals(p.getName())) {
                     log.info(name + "    " + Boolean.toString(exists));
                     exists = true;
                  }
               }
               if (!exists) {
                  final Color colorTemp = Color.valueOf(color);
                  controller.addNewProject(name, Boolean.parseBoolean(isWork), colorTemp, index);
                  index++;
               }

            }
         }

      } catch (final Exception e) {
         e.printStackTrace();
      }
   }
}
