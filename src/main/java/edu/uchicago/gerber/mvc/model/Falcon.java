package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Game;
import edu.uchicago.gerber.mvc.controller.ImageLoader;
import edu.uchicago.gerber.mvc.controller.SoundLoader;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================

	//static fields

	//number of degrees the falcon will turn at each animation cycle if the turnState is LEFT or RIGHT
	public final static int TURN_STEP = 11;
	//number of frames that the falcon will be protected after a spawn
	public static final int INITIAL_SPAWN_TIME = 48;
	//number of frames falcon will be protected after consuming a NewShieldFloater
	public static final int MAX_SHIELD = 200;
	public static final int MAX_NUKE = 600;

	public static final int MIN_RADIUS = 28;


	//images states
	public enum ImageState {
		FALCON_INVISIBLE, //for pre-spawning
		FALCON, //normal ship
		FALCON_THR, //normal ship thrusting
		FALCON_SHIELD, //shielded ship (cyan)
		FALCON_SHIELD_THR, //shielded ship (cyan) thrusting

	}


	//instance fields (getters/setters provided by Lombok @Data above)
	private int shield;

	private int nukeMeter;
	private int invisible;
	private boolean maxSpeedAttained;

	//showLevel is not germane to the Falcon. Rather, it controls whether the level is shown in the middle of the
	// screen. However, given that the Falcon reference is never null, and that a Falcon is a Movable whose move/draw
	// methods are being called every ~40ms, this is a very convenient place to store this variable.
	private int showLevel;

	/* TODO The enum TurnState as well as the boolean thrusting are examples of the State design pattern. This pattern
	allows an object to change its behavior when its internal state changes. In this case, the boolean thrusting, and
	 the TurnState (with values IDLE, LEFT, and RIGHT) affects how the Falcon moves and draws itself. */
	public enum TurnState {IDLE, LEFT, RIGHT}
	private TurnState turnState = TurnState.IDLE;

	private boolean thrusting;

	// ==============================================================
	// CONSTRUCTOR
	// ==============================================================
	
	public Falcon() {

		setTeam(Team.FRIEND);

		setRadius(MIN_RADIUS);


		//We use HashMap which has a seek-time of O(1)
		//See the resources directory in the root of this project for pngs.
		//Using enums as keys is safer b/c we know the value exists when we reference the consts later in code.
    	Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
		rasterMap.put(ImageState.FALCON_INVISIBLE, null );
		rasterMap.put(ImageState.FALCON, ImageLoader.getImage("/imgs/fal/falcon125.png")); //normal ship
		rasterMap.put(ImageState.FALCON_THR, ImageLoader.getImage("/imgs/fal/falcon125_thr.png")); //normal ship thrusting
		rasterMap.put(ImageState.FALCON_SHIELD, ImageLoader.getImage("/imgs/fal/falcon125_SHIELD.png")); //SHIELD
		rasterMap.put(ImageState.FALCON_SHIELD_THR, ImageLoader.getImage("/imgs/fal/falcon125_SHIELD_thr.png")); //S+THR

		setRasterMap(rasterMap);


	}


	// ==============================================================
	// METHODS 
	// ==============================================================
	@Override
	public void move() {

		//only call super.move() if falcon is not fixed
		if (!CommandCenter.getInstance().isFalconPositionFixed()) super.move();

		if (invisible > 0) invisible--;
		if (shield > 0) shield--;
		if (nukeMeter > 0) nukeMeter--;
		//The falcon is a convenient place to decrement the showLevel variable as the falcon
		//move() method is being called every frame (~40ms); and the falcon reference is never null.
		if (showLevel > 0) showLevel--;

		final double THRUST = 0.85;
		final int MAX_VELOCITY = 39;


		//apply some thrust vectors using trig.
		if (thrusting) {
			double vectorX = Math.cos(Math.toRadians(getOrientation()))
					* THRUST;
			double vectorY = Math.sin(Math.toRadians(getOrientation()))
					* THRUST;

			//Absolute velocity is the hypotenuse of deltaX and deltaY
			int absVelocity =
					(int) Math.sqrt(Math.pow(getDeltaX()+ vectorX, 2) + Math.pow(getDeltaY() + vectorY, 2));

			//only accelerate (or adjust radius) if we are below the maximum absVelocity.
			if (absVelocity < MAX_VELOCITY){
				//accelerate
				setDeltaX(getDeltaX() + vectorX);
				setDeltaY(getDeltaY() + vectorY);
				//Make the ship radius bigger when the absolute velocity increases, thereby increasing difficulty when not
				// protected, and allowing player to use the shield offensively when protected.
				setRadius(MIN_RADIUS + absVelocity / 3);
				maxSpeedAttained = false;
			} else {
				//at max speed, you will lose steerage if you attempt to accelerate in the same general direction
				//show WARNING message to player using this flag (see drawFalconStatus() of GamePanel class)
				maxSpeedAttained = true;
			}

		}

		//adjust the orientation given turnState
		int adjustOr = getOrientation();
		switch (turnState){
			case LEFT:
				adjustOr = getOrientation() <= 0 ? 360 - TURN_STEP : getOrientation() - TURN_STEP;
				break;
			case RIGHT:
				adjustOr = getOrientation() >= 360 ? TURN_STEP : getOrientation() + TURN_STEP;
				break;
			case IDLE:
			default:
				//do nothing
		}
		setOrientation(adjustOr);

	}

	//Since the superclass Spite does not provide an
	// implementation for draw() (contract method from Movable) ,we inherit that contract debt, and therefore must
	// provide an implementation. This is a raster and vector (see drawShield below) implementation of draw().
	@Override
	public void draw(Graphics g) {

		if (nukeMeter > 0) drawNukeHalo(g);

		//set local image-state
		ImageState imageState;
		if (invisible > 0){
			imageState = ImageState.FALCON_INVISIBLE;
		}
		else if (shield > 0){
			imageState = thrusting ? ImageState.FALCON_SHIELD_THR : ImageState.FALCON_SHIELD;
		    drawShieldHalo(g);
		}
		else { //not protected
			imageState = thrusting ? ImageState.FALCON_THR : ImageState.FALCON;
		}

		//down-cast (widen the aperture of) the graphics object to gain access to methods of Graphics2D
		//and render the raster image according to the image-state
		renderRaster((Graphics2D) g, getRasterMap().get(imageState));

	}

	private void drawShieldHalo(Graphics g){
		g.setColor(Color.CYAN);
		g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() *2, getRadius() *2);
	}

	private void drawNukeHalo(Graphics g){
		if (invisible > 0) return;
		g.setColor(Color.YELLOW);
		g.drawOval(getCenter().x - getRadius()+10, getCenter().y - getRadius()+10, getRadius() *2 -20,
				getRadius() *2-20);
	}

	@Override
	public void removeFromGame(LinkedList<Movable> list) {
		//The falcon is never actually removed from the game-space; instead we decrement numFalcons
		//only execute the decrementFalconNumAndSpawn() method if shield is down.
		if ( shield == 0)  decrementFalconNumAndSpawn();
	}


	public void decrementFalconNumAndSpawn(){

		CommandCenter.getInstance().setNumFalcons(CommandCenter.getInstance().getNumFalcons() -1);
		if (CommandCenter.getInstance().isGameOver()) return;
		SoundLoader.playSound("shipspawn.wav");
		setShield(Falcon.INITIAL_SPAWN_TIME);
		setInvisible(Falcon.INITIAL_SPAWN_TIME/5);
		//random number between 0-360 in steps of TURN_STEP
		setOrientation(Game.R.nextInt(360 / Falcon.TURN_STEP) * Falcon.TURN_STEP);
		setDeltaX(0);
		setDeltaY(0);
		setRadius(Falcon.MIN_RADIUS);
		setMaxSpeedAttained(false);
		setNukeMeter(0);

	}

} //end class
