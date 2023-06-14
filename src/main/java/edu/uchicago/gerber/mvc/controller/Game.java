package edu.uchicago.gerber.mvc.controller;

import edu.uchicago.gerber.mvc.model.*;
import edu.uchicago.gerber.mvc.view.GamePanel;


import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Random;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension DIM = new Dimension(1100, 900); //the dimension of the game.
    private final GamePanel gamePanel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 40; // milliseconds between frames

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;


    //key-codes
    private static final int
            PAUSE = 80, // p key
            QUIT = 81, // q key
            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow
            UP = 38, // thrust; up arrow
            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m-key mute

            NUKE = 78; // n-key mute

    // for possible future use
    // HYPER = 68, 					// D key
    //ALIEN = 65;                // A key
    // SPECIAL = 70; 					// fire special weapon;  F key

    private final Clip soundThrust;
    private final Clip soundBackground;



    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this); //Game object implements KeyListener
        soundThrust = Sound.clipForLoopFactory("whitenoise.wav");
        soundBackground = Sound.clipForLoopFactory("music-background.wav");

        //fire up the animation thread
        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        animationThread.start();


    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {
        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }

    // Game implements runnable, and must have run method
    @Override
    public void run() {

        // lower animation thread's priority, thereby yielding to the "main" aka 'Event Dispatch'
        // thread which listens to keystrokes
        animationThread.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long startTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == animationThread) {


            //this call will cause all movables to move() and draw() themselves every ~40ms
            // see GamePanel class for details
            gamePanel.update(gamePanel.getGraphics());

            checkCollisions();
            checkNewLevel();
            checkFloaters();

            //keep track of the frame for development purposes
            CommandCenter.getInstance().incrementFrame();

            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation
            try {
                // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
                // between frames takes longer than ANIMATION_DELAY, then the difference between startTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                startTime += ANIMATION_DELAY;

                Thread.sleep(Math.max(0,
                        startTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        } // end while
    } // end run

    private void checkFloaters() {
        spawnNewWallFloater();
        spawnShieldFloater();
        spawnNukeFloater();
    }


    private void checkCollisions() {

        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;

        //This has order-of-growth of O(n^2), there is no way around this.
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {

                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {
                    //remove the friend (so long as he is not protected)
                    if (!movFriend.isProtected()) {
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                    }

                    //remove the foe
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);

                    if (movFoe instanceof Brick) {
                        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 1000);
                        Sound.playSound("rock.wav");
                    } else {
                        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 10);
                        Sound.playSound("kapow.wav");
                    }
                }

            }//end inner for
        }//end outer for

        //check for collisions between falcon and floaters. Order of growth of O(n) where n is number of floaters
        Point pntFalCenter = CommandCenter.getInstance().getFalcon().getCenter();
        int radFalcon = CommandCenter.getInstance().getFalcon().getRadius();

        Point pntFloaterCenter;
        int radFloater;
        for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
            pntFloaterCenter = movFloater.getCenter();
            radFloater = movFloater.getRadius();

            //detect collision
            if (pntFalCenter.distance(pntFloaterCenter) < (radFalcon + radFloater)) {

                Class<? extends Movable> clazz = movFloater.getClass();
                switch (clazz.getSimpleName()) {
                    case "ShieldFloater":
                        Sound.playSound("shieldup.wav");
                        CommandCenter.getInstance().getFalcon().setShield(Falcon.MAX_SHIELD);
                        break;
                    case "NewWallFloater":
                        Sound.playSound("insect.wav");
                        buildWall();
                        break;
                    case "NukeFloater":
                        Sound.playSound("nuke-up.wav");
                        CommandCenter.getInstance().getFalcon().setNukeMeter(Falcon.MAX_NUKE);
                        break;
                }
                CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);


            }//end if
        }//end for

        processGameOpsQueue();

    }//end meth


    //this method adds and removes movables to/from their respective linked-lists
    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {
            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();
            Movable mov = gameOp.getMovable();
            GameOp.Action action = gameOp.getAction();

            switch (mov.getTeam()) {
                case FOE:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovFoes().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFoes().remove(mov);
                        if (mov instanceof Asteroid) spawnSmallerAsteroidsOrDebris((Asteroid) mov);
                    }

                    break;
                case FRIEND:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovFriends().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        if (mov instanceof Falcon) {
                            CommandCenter.getInstance().initFalconAndDecrementFalconNum();
                        } else {
                            CommandCenter.getInstance().getMovFriends().remove(mov);
                        }
                    }
                    break;

                case FLOATER:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovFloaters().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovFloaters().remove(mov);
                    }
                    break;

                case DEBRIS:
                    if (action == GameOp.Action.ADD) {
                        CommandCenter.getInstance().getMovDebris().add(mov);
                    } else { //GameOp.Operation.REMOVE
                        CommandCenter.getInstance().getMovDebris().remove(mov);
                    }
                    break;


            }

        }
    }

    //shows how to add walls or rectangular elements one brick at a time
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


    private void spawnNewWallFloater() {

        if (CommandCenter.getInstance().getFrame() % NewWallFloater.SPAWN_NEW_WALL_FLOATER == 0 && isBrickFree()) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new NewWallFloater(), GameOp.Action.ADD);
        }
    }

    private void spawnShieldFloater() {

        if (CommandCenter.getInstance().getFrame() % ShieldFloater.SPAWN_SHIELD_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new ShieldFloater(), GameOp.Action.ADD);
        }
    }

    private void spawnNukeFloater() {

        if (CommandCenter.getInstance().getFrame() % NukeFloater.SPAWN_NUKE_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new NukeFloater(), GameOp.Action.ADD);
        }
    }


    //this method spawns new Large (0) Asteroids
    private void spawnBigAsteroids(int num) {
        while (num-- > 0) {
            //Asteroids with size of zero are big
            CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(0), GameOp.Action.ADD);

        }
    }

    private void spawnSmallerAsteroidsOrDebris(Asteroid originalAsteroid) {

        int size = originalAsteroid.getSize();
        //small asteroids
        if (size > 1) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(originalAsteroid), GameOp.Action.ADD);
        }
        //med and large
        else {
            //for large (0) and medium (1) sized Asteroids only, spawn 2 or 3 smaller asteroids respectively
            //We can use the existing variable (size) to do this
            size += 2;
            while (size-- > 0) {
                CommandCenter.getInstance().getOpsQueue().enqueue(new Asteroid(originalAsteroid), GameOp.Action.ADD);
            }
        }

    }

    private boolean isBrickFree() {
        //if there are no more Bricks on the screen
        boolean brickFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Brick) {
                brickFree = false;
                break;
            }
        }
        return brickFree;
    }

    private boolean isLevelClear() {
        //if there are no more Asteroids on the screen
        boolean asteroidFree = true;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            if (movFoe instanceof Asteroid) {
                asteroidFree = false;
                break;
            }
        }
        return asteroidFree;
    }

    private void checkNewLevel() {

        if (isLevelClear()) {
            //currentLevel will be zero at beginning of game
            int level = CommandCenter.getInstance().getLevel();
            //award some points for having cleared the previous level
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + (10_000L * level));
            //bump the level up
            level = level + 1;
            CommandCenter.getInstance().setLevel(level);
            //spawn some big new asteroids
            spawnBigAsteroids(level);
            //make falcon invincible momentarily in case new asteroids spawn on top of him, and give player
            //time to adjust to new asteroids in game space.
            CommandCenter.getInstance().getFalcon().setShield(Falcon.INITIAL_SPAWN_TIME);
            //show "Level X" in middle of screen
            CommandCenter.getInstance().getFalcon().setShowLevel(Falcon.INITIAL_SPAWN_TIME);

        }
    }


    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        Arrays.stream(clpClips).forEach(clip -> clip.stop());
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        Falcon falcon = CommandCenter.getInstance().getFalcon();
        int keyCode = e.getKeyCode();

        if (keyCode == START && CommandCenter.getInstance().isGameOver()) {
            CommandCenter.getInstance().initGame();
            return;
        }


        switch (keyCode) {
            case PAUSE:
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                if (CommandCenter.getInstance().isPaused()) stopLoopingSounds(soundBackground, soundThrust);
                break;
            case QUIT:
                System.exit(0);
                break;
            case UP:
                falcon.setThrusting(true);
                soundThrust.loop(Clip.LOOP_CONTINUOUSLY);
                break;
            case LEFT:
                falcon.setTurnState(Falcon.TurnState.LEFT);
                break;
            case RIGHT:
                falcon.setTurnState(Falcon.TurnState.RIGHT);
                break;


            // possible future use
            // case KILL:
            // case SHIELD:
            // case NUM_ENTER:

            default:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        Falcon falcon = CommandCenter.getInstance().getFalcon();
        int keyCode = e.getKeyCode();
        //show the key-code in the console
        System.out.println(keyCode);


        switch (keyCode) {
            case FIRE:
                CommandCenter.getInstance().getOpsQueue().enqueue(new Bullet(falcon), GameOp.Action.ADD);
                Sound.playSound("thump.wav");
                break;
            //releasing either the LEFT or RIGHT arrow key will set the TurnState to IDLE
            case LEFT:
            case RIGHT:
                falcon.setTurnState(Falcon.TurnState.IDLE);
                break;
            case UP:
                falcon.setThrusting(false);
                soundThrust.stop();
                break;
            case NUKE:
                if (CommandCenter.getInstance().getFalcon().getNukeMeter() > 0){
                    CommandCenter.getInstance().getOpsQueue().enqueue(new Nuke(falcon), GameOp.Action.ADD);
                    Sound.playSound("nuke.wav");
                    CommandCenter.getInstance().getFalcon().setNukeMeter(0);
                }

                break;
            case MUTE:
                CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

                if (!CommandCenter.getInstance().isMuted()) {
                    stopLoopingSounds(soundBackground);
                } else {
                    soundBackground.loop(Clip.LOOP_CONTINUOUSLY);
                }
                break;

            default:
                break;
        }

    }

    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }

}


