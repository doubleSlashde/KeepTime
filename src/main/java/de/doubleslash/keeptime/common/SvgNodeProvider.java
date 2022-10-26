package de.doubleslash.keeptime.common;

import javafx.scene.Node;
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
         dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      }

      /* Fixes Sonar Vulnerability Issue "XML parsers should not be vulnerable to XXE attacks"
      * XML standard allows the use of entities, declared in the DOCTYPE of the document, which can be internal or external.
      Problem:
      When parsing the XML file, the content of the external entities is retrieved from an external storage such as the file system or network,
      which may lead, if no restrictions are put in place, to arbitrary file disclosures or server-side request forgery (SSRF) vulnerabilities.

       Solution:
      *It’s recommended to limit resolution of external entities by using one of these solutions:
            If DOCTYPE is not necessary, completely disable all DOCTYPE declarations.
      * */

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
