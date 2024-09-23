package edu.uchicago.gerber.mvc.model;


import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.SoundLoader;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;

@Data
public class Nuke extends Sprite{

    private static final int EXPIRE = 60;
    private int nukeState = 0;

    public Nuke(Falcon falcon) {
        setCenter((Point)falcon.getCenter().clone());
        setColor(Color.YELLOW);
        setExpiry(EXPIRE);
        setRadius(0);
        setTeam(Team.FRIEND);

        final double FIRE_POWER = 11.0;
        double vectorX =
                Math.cos(Math.toRadians(falcon.getOrientation())) * FIRE_POWER;
        double vectorY =
                Math.sin(Math.toRadians(falcon.getOrientation())) * FIRE_POWER;

        //fire force: falcon inertia + fire-vector
        setDeltaX(falcon.getDeltaX() + vectorX);
        setDeltaY(falcon.getDeltaY() + vectorY);

    }


    @Override
    public void draw(Graphics g) {

        g.setColor(getColor());
        g.drawOval(getCenter().x -getRadius(), getCenter().y - getRadius(), getRadius() * 2, getRadius()* 2);

    }



    @Override
    public void move() {
        super.move();
        if (getExpiry() % (EXPIRE/6) == 0) nukeState++;
        switch (nukeState) {
            //travelling
            case 0:
                setRadius(17);
                break;
            //exploding
            case 1:
            case 2:
            case 3:
                setRadius(getRadius() + 8);
                break;
            //imploding
            case 4:
            case 5:
            default:
                setRadius(getRadius() - 11);
                break;


        }

    }

    /* TODO The following overrides are examples of Observer design pattern (Lifecycle Callbacks). Lifecycle
     Callbacks allow an object to perform specific actions at
    well-defined stages of its lifecycle. Lifecycle Callbacks encapsulate logic that would otherwise be scattered throughout
    other classes, thereby making the code more organized and easier to manage.

    Subject (Game): The Game class acts as the subject that triggers changes in the state of Movable objects in the
    processGameOpsQueue() method.

    Observer (Movable): Each Movable object implements the lifecycle methods (addToGame, removeFromGame). These methods are
    called by the Game's processGameOpsQueue() method to notify the Movable objects about their state changes (e.g.,
    being added to or removed from the game).

      */
    @Override
    public void addToGame(LinkedList<Movable> list) {
        //only deploy the nuke if the nukeMeter > 0
        if (CommandCenter.getInstance().getFalcon().getNukeMeter() > 0){
            list.add(this);
            SoundLoader.playSound("nuke.wav");
            CommandCenter.getInstance().getFalcon().setNukeMeter(0);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        //only remove upon natural mortality (see expire() of Sprite), otherwise a Nuke is invincible
        if (getExpiry() == 0) list.remove(this);
    }
}
