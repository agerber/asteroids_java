package edu.uchicago.gerber._08final.mvc.model;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.ImageLoader;
import edu.uchicago.gerber._08final.mvc.controller.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.awt.Point;

public class Brick extends Sprite {

	private final int BRICK_IMAGE = 0;

	//The size of this brick is always square!
	//we use upperLeftCorner because that is the origin when drawing graphics in Java
	public Brick(Point upperLeftCorner, int size) {

		//you can shoot to destroy the wall which yields big points
		setTeam(Team.FOE);

		setCenter(new Point(upperLeftCorner.x + size/2, upperLeftCorner.y + size/2));

		setRadius(size/2);

		//As this sprite does not animate or change state, we could just store a BufferedImage as a member, but
		//since we already have a rasterMap in the Sprite class, we might as well be consistent for all raster sprites
		// and use it.
    	Map<Integer, BufferedImage> rasterMap = new HashMap<>();
		//brick from Mario Bros
		rasterMap.put(BRICK_IMAGE, ImageLoader.masterImageMap.get("Brick_Block100.png"));

		setRasterMap(rasterMap);


	}

	@Override
	public void draw(Graphics g) {
		renderRaster((Graphics2D) g, getRasterMap().get(BRICK_IMAGE));
		//if you uncomment these, you can see how collision works. Feel free to remove these two lines.
		//g.setColor(Color.LIGHT_GRAY);
		//g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
	}

	//the reason we override the move method is to skip the logic contained in super-class Sprite move() method
	//which is laborious, thereby gaining slight performance
	@Override
	public void move(){
		//do NOT call super.move() and do nothing; a brick does not move.
	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		super.removeFromGame(list);
		CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 1000);
		Sound.playSound("rock.wav");

	}
} //end class
