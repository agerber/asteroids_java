package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Game;
import edu.uchicago.gerber.mvc.controller.SoundLoader;

import java.awt.*;
import java.util.LinkedList;

public class NukeFloater extends Floater {

	//spawn every 12 seconds
	public static final int SPAWN_NUKE_FLOATER = Game.FRAMES_PER_SECOND * 12;
	public NukeFloater() {
		setColor(Color.YELLOW);
		setExpiry(120);
	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		super.removeFromGame(list);
		//if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
		if (getExpiry() > 0) {
			SoundLoader.playSound("nuke-up.wav");
			CommandCenter.getInstance().getFalcon().setNukeMeter(Falcon.MAX_NUKE);
		}

	}


}
