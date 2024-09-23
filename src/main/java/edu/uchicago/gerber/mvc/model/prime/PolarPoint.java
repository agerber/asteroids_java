package edu.uchicago.gerber.mvc.model.prime;

import lombok.AllArgsConstructor;
import lombok.Data;

//this class used in conjunction with Point[] for rendering vector graphics
@Data
@AllArgsConstructor
public class PolarPoint  {

    private double r; // corresponds to the hypotenuse in cartesean, number between 0 and 1
    private double theta; //degrees in radians, number between 0 and 6.283

    //because sprites such as Asteroid needs to sort by theta when generating random-shapes.
    public int compareTheta(PolarPoint other) {
        return Double.compare(this.theta, other.theta);
    }

}
