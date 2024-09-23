package edu.uchicago.gerber.mvc.model;



import edu.uchicago.gerber.mvc.controller.CommandCenter;
import edu.uchicago.gerber.mvc.controller.Game;
import edu.uchicago.gerber.mvc.model.Falcon;
import edu.uchicago.gerber.mvc.model.Nuke;
import edu.uchicago.gerber.mvc.model.NukeFloater;
import edu.uchicago.gerber.mvc.model.Sprite;
import edu.uchicago.gerber.mvc.model.prime.AspectRatio;

import java.awt.*;


/**
 * Inspired by Michael Vasiliou's Sinistar, winner of Java game contest 2016.
 */
public class MiniMap extends Sprite {


    //size of mini-map as percentage of screen (game dimension)
    private final double MINI_MAP_PERCENT = 0.31;

    //used to adjust non-square universes. Set in draw()
    private AspectRatio aspectRatio;

    private final Color PUMPKIN = new Color(200, 100, 50);
    private final Color LIGHT_GRAY = new Color(200, 200, 200);

    public MiniMap() {
        setTeam(Team.DEBRIS);
        setCenter(new Point(0,0));
    }

    @Override
    public void move() {}


    @Override
    public void draw(Graphics g) {

        //controlled by the A-key
        if (!CommandCenter.getInstance().isRadar()) return;

        //get the aspect-ratio which is used to adjust for non-square universes
        aspectRatio = aspectAdjustedRatio(CommandCenter.getInstance().getUniDim());

        //scale to some percent of game-dim
        int miniWidth = (int) Math.round( MINI_MAP_PERCENT * Game.DIM.width * aspectRatio.getWidth());
        int miniHeight = (int) Math.round(MINI_MAP_PERCENT * Game.DIM.height * aspectRatio.getHeight());

        //gray bounding box (entire universe)
        g.setColor(Color.DARK_GRAY);
        g.drawRect(
                0,
                0,
                miniWidth,
                miniHeight
        );


        //draw the view-portal box
        g.setColor(Color.DARK_GRAY);
        int miniViewPortWidth = miniWidth / CommandCenter.getInstance().getUniDim().width;
        int miniViewPortHeight = miniHeight / CommandCenter.getInstance().getUniDim().height;
        g.drawRect(
                0 ,
                0,
                miniViewPortWidth,
                miniViewPortHeight

        );


        //draw debris radar-blips.
        CommandCenter.getInstance().getMovDebris().forEach( mov -> {
                    g.setColor(Color.DARK_GRAY);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillOval(translatedPoint.x - 1, translatedPoint.y - 1, 2, 2);
                }
        );


        //draw foe (asteroids) radar-blips
        CommandCenter.getInstance().getMovFoes().forEach( mov -> {
                    if (!(mov instanceof  Asteroid)) return;
                    Asteroid asteroid = (Asteroid) mov;
                    g.setColor(LIGHT_GRAY);
                    Point translatedPoint = translatePoint(asteroid.getCenter());
                    switch (asteroid.getSize()){
                        //large
                        case 0:
                            g.fillOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6);
                            break;
                        //med
                        case 1:
                            g.drawOval(translatedPoint.x - 3, translatedPoint.y - 3, 6, 6);
                            break;
                        //small
                        case 2:
                        default:
                            g.drawOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                    }
                }
        );


        //draw floater radar-blips
        CommandCenter.getInstance().getMovFloaters().forEach( mov -> {
                    g.setColor(mov instanceof NukeFloater ? Color.YELLOW : Color.CYAN);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillRect(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                }
        );



        //draw friend radar-blips
        CommandCenter.getInstance().getMovFriends().forEach( mov -> {
                    Color color;
                    if (mov instanceof Falcon && CommandCenter.getInstance().getFalcon().getShield() > 0)
                        color = Color.CYAN;
                    else if (mov instanceof Nuke)
                        color = Color.YELLOW;
                    else
                        color = PUMPKIN;
                    g.setColor(color);
                    Point translatedPoint = translatePoint(mov.getCenter());
                    g.fillOval(translatedPoint.x - 2, translatedPoint.y - 2, 4, 4);
                }
        );


    }

    //this function takes a center-point of a movable and scales it to display the blip on the mini-map.
    //Since Java's draw origin (0,0) is at the top-left, points will translate up and left.
    private Point translatePoint(Point point){
        return new Point(
                (int) Math.round( MINI_MAP_PERCENT  * point.x / CommandCenter.getInstance().getUniDim().width * aspectRatio.getWidth()),
                (int) Math.round( MINI_MAP_PERCENT  * point.y / CommandCenter.getInstance().getUniDim().height * aspectRatio.getHeight())
        );
    }


    //the purpose of this method is to adjust the aspect of non-square universes
    private AspectRatio aspectAdjustedRatio(Dimension universeDim){
        if (universeDim.width == universeDim.height){
            return new AspectRatio(1.0, 1.0);
        }
        else if (universeDim.width > universeDim.height){
            double wMultiple = (double) universeDim.width / universeDim.height;
            return new AspectRatio(wMultiple, 1.0).scale(0.5);
        }
        //universeDim.width < universeDim.height
        else {
            double hMultiple = (double) universeDim.height / universeDim.width;
            return new AspectRatio(1.0, hMultiple).scale(0.5);
        }

    }



}
