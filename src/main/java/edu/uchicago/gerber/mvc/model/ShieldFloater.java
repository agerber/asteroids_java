package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.Game;

import java.awt.*;

public class ShieldFloater extends Floater {
	//spawn every 25 seconds
	public static final int SPAWN_SHIELD_FLOATER = Game.FRAMES_PER_SECOND * 25;
	public ShieldFloater() {
		setColor(Color.CYAN);
		setExpiry(260);
	}


}
