package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.Game;

import java.awt.*;

public class NukeFloater extends Floater {

	public static final int SPAWN_NUKE_FLOATER = Game.FRAMES_PER_SECOND * 50;
	public NukeFloater() {
		setColor(Color.YELLOW);
		setExpiry(120);
	}


}
