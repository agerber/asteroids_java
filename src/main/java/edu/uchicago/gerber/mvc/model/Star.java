package edu.uchicago.gerber.mvc.model;

import edu.uchicago.gerber.mvc.controller.Game;
import lombok.Data;

import java.awt.*;
import java.util.LinkedList;

//Sprite has a lot of bloat that we don't need to simply render a star field.
//This class demonstrates how we can use the Movable interface without extending Sprite.
@Data
public class Star implements Movable{

    private Point center;
    private Color color;

    public Star() {
        //center is some random point in the game space
        center = new Point(Game.R.nextInt(Game.DIM.width), Game.R.nextInt(Game.DIM.height));
        int bright = Game.R.nextInt(226); //Stars are muted at max brightness of 225 out of 255
        color = new Color(bright, bright, bright); //some grey value
    }

    //The following methods are contract methods from Movable. We need all of them to satisfy the contract.
    @Override
    public void draw(Graphics g) {

        g.setColor(color);
        g.drawOval(center.x, center.y, getRadius(), getRadius());

    }

    @Override
    public Point getCenter() {
        return center;
    }

    @Override
    public int getRadius() {
        return 1;
    }

    @Override
    public Team getTeam() {
        return Team.DEBRIS;
    }




    @Override
    public void move() {
        //do nothing
    }


    @Override
    public void add(LinkedList<Movable> list) {
        list.add(this);
    }

    @Override
    public void remove(LinkedList<Movable> list) {
       list.remove(this);
    }



}
