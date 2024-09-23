package edu.uchicago.gerber.mvc.controller;

import edu.uchicago.gerber.mvc.model.prime.PolarPoint;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {


    ////////////////////////////////////////////////////////////////////
    //Utility method for transforming cartesian2Polar
    ////////////////////////////////////////////////////////////////////
    public static List<PolarPoint> cartesiansToPolars(Point[]  pntCartesians) {

        //Function used in stream below, as well as in BiFunction below
        Function<Point, Double> hypotenuseOfPoint = (pnt) ->
                Math.sqrt(Math.pow(pnt.x, 2) + Math.pow(pnt.y, 2));

        //determine the largest hypotenuse
        //we must make hypotenuse final to pass into a stream below.
        final double LARGEST_HYP = Arrays.stream(pntCartesians)
                .map(hypotenuseOfPoint)
                .max(Double::compare)
                //since .max returns an Optional<Double>, we use .orElse to return 0.0 in the event it is null.
                .orElse(0.0);


        //BiFunction used in stream below. We can use Functions within functions e.g. hypotenuseOfPoint.apply()
        BiFunction<Point, Double, PolarPoint> cartToPolarTransform = (pnt, dbl) -> new PolarPoint(
                //this is r from PolarPoint(r,theta).
                hypotenuseOfPoint.apply(pnt) / dbl, //r is relative to the LARGEST_HYP (dbl)
                //this is theta from PolarPoint(r,theta)
                Math.toDegrees(Math.atan2(pnt.y, pnt.x)) * Math.PI / 180
        );

        return Arrays.stream(pntCartesians)
                .map(pnt -> cartToPolarTransform.apply(pnt, LARGEST_HYP))
                .collect(Collectors.toList());

    }


}
