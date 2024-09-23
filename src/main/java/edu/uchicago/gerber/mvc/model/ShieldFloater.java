package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Game;
import edu.uchicago.gerber.mvc.controller.SoundLoader;

import java.awt.*;
import java.util.LinkedList;

public class ShieldFloater extends Floater {
	//spawn every 25 seconds
	public static final int SPAWN_SHIELD_FLOATER = Game.FRAMES_PER_SECOND * 25;
	public ShieldFloater() {
		setColor(Color.CYAN);
		setExpiry(260);
	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		super.removeFromGame(list);
		//if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
		if (getExpiry() > 0) {
			SoundLoader.playSound("shieldup.wav");
		    CommandCenter.getInstance().getFalcon().setShield(Falcon.MAX_SHIELD);
	   }

	}


}
