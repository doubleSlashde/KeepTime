package application;

import javafx.scene.paint.Color;

public enum ProjectOld {

	idle("Idle", Color.BROWN, false), kardex("Kardex", Color.GREEN, true), zeppelin("Zeppelin", Color.BLUE, true), zf(
			"ZF", Color.PINK, true), kicker("Kicker", Color.YELLOW,
					false), mittag("Mittagessen", Color.RED, false), other("Other", Color.ORANGE, true);

	public String name;
	public Color color;
	public boolean isWork;

	private ProjectOld(String name, Color color, boolean isWork) {
		this.name = name;
		this.color = color;
		this.isWork = isWork;
	}

}
