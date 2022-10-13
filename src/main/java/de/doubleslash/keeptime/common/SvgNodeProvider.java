package de.doubleslash.keeptime.common;

import javafx.geometry.Bounds;
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
 * class loads SvgPath from a SvgFile because Java can not load the original svg file as it is. <p>
 * To load the svg the class extracts the path as string and creates a new SvgPath and returns it.
 */

public class SvgNodeProvider {

    private static String getSvgPathWithXMl(Resources.RESOURCE resource) {
        String svgPath;
        Document document;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e + "Tried to build new document");
        }

        try (InputStream inputStream = Resources.getResource(resource).openStream()) {
            document = db.parse(inputStream);
            NodeList nodeList = document.getElementsByTagName("path");
            svgPath = nodeList.item(0).getAttributes().getNamedItem("d").getNodeValue();
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e + " ");
        }
        return svgPath;
    }

    public static Node getSvgNode(Resources.RESOURCE resource){
        SVGPath IconSvg = new SVGPath();
        IconSvg.setContent(getSvgPathWithXMl(resource));
        Bounds bounds = IconSvg.getBoundsInParent();
        double scale = Math.min(15 / bounds.getWidth(), 15 / bounds.getHeight());
        IconSvg.setScaleX(scale);
        IconSvg.setScaleY(scale);
        return IconSvg;
    }
}
