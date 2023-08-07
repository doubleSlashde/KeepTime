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

package de.doubleslash.app.common;

import javafx.scene.shape.SVGPath;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class loads SvgPath from a SvgFile because Java can not load the original svg file as it is.
 * <p>
 * To load the svg the class extracts the path as string and creates a new SvgPath and returns it.
 */

public class SvgNodeProvider {

   public static String getSvgPathWithXMl(Resources.RESOURCE resource){
      String svgPath;
      Document document;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      try {
         // fixes sonar issue RSPEC-2755
         dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      }

      DocumentBuilder db;

      try {
         db = dbf.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException("Tried to build new document", e);
      }

      try (InputStream inputStream = Resources.getResource(resource).openStream()) {
         document = db.parse(inputStream);
         NodeList nodeList = document.getElementsByTagName("path");
         svgPath = nodeList.item(0).getAttributes().getNamedItem("d").getNodeValue();
      } catch (IOException | SAXException e) {
         throw new RuntimeException("Could not extract SvgPath from resource " + resource, e);
      }
      return svgPath;
   }

   public static SVGPath getSvgNodeWithScale(Resources.RESOURCE resource, Double scaleX, Double scaleY) {
      SVGPath iconSvg = new SVGPath();
      iconSvg.setContent(getSvgPathWithXMl(resource));
      iconSvg.setScaleX(scaleX);
      iconSvg.setScaleY(scaleY);
      return iconSvg;
   }
}
