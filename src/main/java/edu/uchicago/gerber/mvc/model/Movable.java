package edu.uchicago.gerber.mvc.model;

import java.awt.*;
import java.util.LinkedList;


/* TODO This interface is an example of the Facade design pattern which provides a simplified
interface to a complex subsystem or set of classes. It hides the complexity by offering a more straightforward and unified API.
The goal is to make subsystems easier to use by providing a higher-level interface that clients can interact with.
 */
public interface Movable {

	enum Team {FRIEND, FOE, FLOATER, DEBRIS}

	//for the game to move and draw movable objects. See the GamePanel class.
	void move();
	void draw(Graphics g);

	//for collision detection
	Point getCenter();
	int getRadius();
	Team getTeam();



	//lifecycle callbacks which occur before or after this object is added or removed from the game-space.
	//this is your opportunity to add sounds or perform other side effects.
	//The 'list' parameter will be one of the following: movFriends, movFoes, movDebris, movFloaters.
	//See processGameOpsQueue() of Game class
	void addToGame(LinkedList<Movable> list);

	void removeFromGame(LinkedList<Movable> list);


} //end Movable
