package edu.uchicago.gerber.mvc.model;


import lombok.Data;

import java.awt.*;

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

    //a nuke is invincible until it collides 10 times
    @Override
    public boolean isProtected() {
        return true;
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
                setRadius(getRadius() + 16);
                break;
            //imploding
            case 4:
            case 5:
            default:
                setRadius(getRadius() - 22);
                break;


        }

    }


}
