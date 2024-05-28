package edu.uchicago.gerber.mvc.model;


import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;

@Data
public class Nuke extends Sprite{

    private static final int EXPIRE = 60;
    private int nukeState = 0;

    public Nuke(Falcon falcon) {
        setCenter(falcon.getCenter());
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
                setRadius(2);
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

    @Override
    public void addToGame(LinkedList<Movable> list) {
        if (CommandCenter.getInstance().getFalcon().getNukeMeter() > 0){
            list.add(this);
            Sound.playSound("nuke.wav");
            CommandCenter.getInstance().getFalcon().setNukeMeter(0);
        }
    }

    @Override
    public void removeFromGame(LinkedList<Movable> list) {
        //only remove upon natural mortality (see expire() of Sprite), otherwise a Nuke is invincible
        if (getExpiry() == 0) list.remove(this);
    }
}
