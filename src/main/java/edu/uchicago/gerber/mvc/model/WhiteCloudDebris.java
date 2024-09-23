package edu.uchicago.gerber.mvc.model;


import edu.uchicago.gerber.mvc.controller.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class WhiteCloudDebris extends Sprite{

   private int index = 0;
   //the higher the number, the slower the animation
   private final static int SLOW_MO = 3;

    public WhiteCloudDebris(Sprite explodingSprite) {

        //DEBRIS means that this sprite is inert, and does not interact with other teams.
        setTeam(Team.DEBRIS);

        Map<Integer, BufferedImage> rasterMap = new HashMap<>();
        //see readme.txt file in the resources/imgs/exp directory for how I created these assets
        rasterMap.put(0, ImageLoader.getImage("/imgs/exp/row-1-column-1.png"));
        rasterMap.put(1, ImageLoader.getImage("/imgs/exp/row-1-column-2.png"));
        rasterMap.put(2, ImageLoader.getImage("/imgs/exp/row-1-column-3.png"));
        rasterMap.put(3, ImageLoader.getImage("/imgs/exp/row-2-column-1.png"));
        rasterMap.put(4, ImageLoader.getImage("/imgs/exp/row-2-column-2.png"));
        rasterMap.put(5, ImageLoader.getImage("/imgs/exp/row-2-column-3.png"));
        rasterMap.put(6, ImageLoader.getImage("/imgs/exp/row-3-column-1.png"));
        rasterMap.put(7, ImageLoader.getImage("/imgs/exp/row-3-column-2.png"));
        rasterMap.put(8, ImageLoader.getImage("/imgs/exp/row-3-column-3.png"));

        setRasterMap(rasterMap);

        //expire it out after it has done its animation. Multiply by SLOW_MO to slow down the animation
        setExpiry(rasterMap.size() * SLOW_MO);

        //everything is relative to the exploding sprite
        setSpin(explodingSprite.getSpin());
        setCenter((Point)explodingSprite.getCenter().clone());
        setDeltaX(explodingSprite.getDeltaX());
        setDeltaY(explodingSprite.getDeltaY());
        setRadius((int) (explodingSprite.getRadius() * 1.3));

    }

    //In this example, we are simply in-order traversing the rasterMap once.
    //However, we could also create a looping animation; think bird flapping over and over.
    //We can also create a hybrid of looping and image-state; think Mario
    //walking (looping), standing (suspended loop), jumping (one state), crouching (another state).
    //See Falcon class for example of image-state.
    @Override
    public void draw(Graphics g) {


        renderRaster((Graphics2D) g, getRasterMap().get(index));
        //hold the image for SLOW_MO frames to slow down the dust cloud animation
        //we already have a simple decrement-to-zero counter with expiry; see move() method of Sprite.
        if (getExpiry() % SLOW_MO == 0) index++;


    }
}
