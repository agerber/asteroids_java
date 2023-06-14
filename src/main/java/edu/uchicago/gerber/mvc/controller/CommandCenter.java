package edu.uchicago.gerber.mvc.controller;



import edu.uchicago.gerber.mvc.model.*;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numFalcons;
	private  int level;
	private  long score;
	private  boolean paused;
	private  boolean muted;

	//this value is used to count the number of frames (full animation cycles) in the game
	private long frame;

	//the falcon is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the falcon ref always points to the single falcon object on heap.
	//Lombok will not provide setter methods on final members
	private final Falcon falcon  = new Falcon();

	//lists containing our movables subdivided by team
	private final List<Movable> movDebris = new LinkedList<>();
	private final List<Movable> movFriends = new LinkedList<>();
	private final List<Movable> movFoes = new LinkedList<>();
	private final List<Movable> movFloaters = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//for sound playing. Limit the number of threads to 5 at a time.
	private final ThreadPoolExecutor soundExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {}

    //this class maintains game state - make this a singleton.
	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}


	public void initGame(){
		clearAll();
		generateStarField();
		setLevel(0);
		setScore(0);
		setPaused(false);
		//set to one greater than number of falcons lives in your game as initFalconAndDecrementNum() also decrements
		setNumFalcons(4);
		initFalconAndDecrementFalconNum();
		//add the falcon to the movFriends list
		opsQueue.enqueue(falcon, GameOp.Action.ADD);



	}

	private void generateStarField(){

		int count = 100;
		while (count-- > 0){
			opsQueue.enqueue(new Star(), GameOp.Action.ADD);
		}

	}




	public void initFalconAndDecrementFalconNum(){
		numFalcons--;
		if (isGameOver()) return;
		Sound.playSound("shipspawn.wav");
		falcon.setShield(Falcon.INITIAL_SPAWN_TIME);
		falcon.setInvisible(Falcon.INITIAL_SPAWN_TIME/4);
		//put falcon in the middle of the game-space
		falcon.setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		//random number between 0-360 in steps of TURN_STEP
		falcon.setOrientation(Game.R.nextInt(360 / Falcon.TURN_STEP) * Falcon.TURN_STEP);
		falcon.setDeltaX(0);
		falcon.setDeltaY(0);
		falcon.setRadius(Falcon.MIN_RADIUS);
		falcon.setMaxSpeedAttained(false);
		falcon.setNukeMeter(0);
	}

	public void incrementFrame(){
		//use of ternary expression to simplify the logic to one line
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;
	}

	private void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}

	public boolean isGameOver() {		//if the number of falcons is zero, then game over
		return numFalcons < 1;
	}






}
