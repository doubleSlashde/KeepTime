package application;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javafx.scene.paint.Color;

@XmlRootElement
public class KeepTimeConfigXML {

	@XmlAttribute
	@XmlJavaTypeAdapter(ColorAdapter.class)
	public Color taskBarColor;

	@XmlElementWrapper(name = "projects")
	@XmlElement(name = "project")
	public List<Project> projects = new ArrayList<>();

	public Project idleProject;

	public KeepTimeConfigXML() {
		projects.add(idleProject);
	}
}
