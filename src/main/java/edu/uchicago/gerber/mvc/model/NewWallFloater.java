package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Game;
import edu.uchicago.gerber.mvc.controller.GameOp;
import edu.uchicago.gerber.mvc.controller.Sound;

import java.awt.*;
import java.util.LinkedList;

public class NewWallFloater extends Floater {

	private static final Color MAROON = new Color(186, 0, 22);
	//spawn every 40 seconds
	public static final int SPAWN_NEW_WALL_FLOATER = Game.FRAMES_PER_SECOND * 40;
	public NewWallFloater() {
		setColor(MAROON);
		setExpiry(230);
	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		super.removeFromGame(list);
		//if getExpiry() > 0, then this remove was the result of a collision, rather than natural mortality
		if (getExpiry() > 0) {
			Sound.playSound("insect.wav");
			buildWall();
		}
	}

	private void buildWall() {
		final int BRICK_SIZE = Game.DIM.width / 30, ROWS = 2, COLS = 20, X_OFFSET = BRICK_SIZE * 5, Y_OFFSET = 50;

		for (int nCol = 0; nCol < COLS; nCol++) {
			for (int nRow = 0; nRow < ROWS; nRow++) {
				CommandCenter.getInstance().getOpsQueue().enqueue(
						new Brick(
								new Point(nCol * BRICK_SIZE + X_OFFSET, nRow * BRICK_SIZE + Y_OFFSET),
								BRICK_SIZE),
						GameOp.Action.ADD);

			}
		}
	}

}
