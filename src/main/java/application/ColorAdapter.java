package application;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import javafx.scene.paint.Color;

public class ColorAdapter extends XmlAdapter<String, Color> {

	public Color unmarshal(final String xml) throws Exception {
		try {
			return Color.valueOf(xml);
		} catch (Exception e) {
			return Color.BLACK;
		}
	}

	public String marshal(final Color object) throws Exception {
		return object.toString();
	}

}
