package application;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javafx.scene.paint.Color;

public class Project {
	public String name;
	@XmlJavaTypeAdapter(ColorAdapter.class)
	public Color color;
	public boolean isWork;

	@XmlAttribute
	public boolean isDefault;

	public Project() {

	}

	public Project(String name, Color color, boolean isWork) {
		super();
		this.name = name;
		this.color = color;
		this.isWork = isWork;
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", color=" + color + ", isWork=" + isWork + ", isDefault=" + isDefault + "]";
	}

}
