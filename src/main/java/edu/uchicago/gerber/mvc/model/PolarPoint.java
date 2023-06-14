package edu.uchicago.gerber.mvc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

//this class used in conjunction with Point[] for rendering vector graphics
@Data
@AllArgsConstructor
public class PolarPoint {
    //We use the wrapper-class Double as members to get the Comparable interface
    //because Asteroid needs to sort by theta when generating random-shapes.
    private Double r; // corresponds to the hypotenuse in cartesean, number between 0 and 1
    private Double theta; //degrees in radians, number between 0 and 6.283
}
