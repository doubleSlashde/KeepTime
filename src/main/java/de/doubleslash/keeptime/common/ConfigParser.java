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

package de.doubleslash.keeptime.common;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.doubleslash.keeptime.controller.Controller;
import de.doubleslash.keeptime.model.Project;
import javafx.scene.paint.Color;

public class ConfigParser {
   private static final Logger LOG = LoggerFactory.getLogger(ConfigParser.class);

   private final Controller controller;

   public ConfigParser(final Controller controller) {
      this.controller = controller;
   }

   public static boolean hasConfigFile(final String fileName) {
      final File f = new File(fileName);
      return f.exists();
   }

   public void parseConfig(final File inputFile) {
      LOG.info("Starting import of projects in file '{}'.", inputFile);
      try {
         final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         final Document doc = dBuilder.parse(inputFile);
         doc.getDocumentElement().normalize();
         LOG.debug("Root element '{}'.", doc.getDocumentElement().getNodeName());
         final NodeList nList = doc.getElementsByTagName("project");

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

               LOG.debug("Testing if project with name '{}' already exists.", name);
               boolean exists = false;

               for (final Project p : controller.getAvailableProjects()) {
                  if (name.equals(p.getName())) {
                     exists = true;
                     break;
                  }
               }
               if (!exists) {
                  LOG.info("Adding project '{}'.", name);
                  final Color colorTemp = Color.valueOf(color);
                  controller.addNewProject(name, Boolean.parseBoolean(isWork), colorTemp, index);
                  index++;
               } else {
                  LOG.debug("Project '{}' already exists", name);
               }

            }
         }
         LOG.info("Import of '{}' finished.", inputFile);
      } catch (final Exception e) {
         LOG.error("There was an error while importing projects from config.xml", e);
      }
   }
}
