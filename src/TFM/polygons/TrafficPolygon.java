/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM;

/**
 *
 * @author Gabriel
 * 
 * Class to build a Nasa WorldWind Polygon to represent a traffic.
 */

import classes.base.TrafficSimulated;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
//Different class Position from wwd and adsb.
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfaceSquare;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class TrafficPolygon{
    
    private String icaoCode;
    private Polygon polygon;
    // Constants for Earth
    final double EARTH_RADIUS = 6378137; // Radius in meters
    
    public TrafficPolygon(TrafficSimulated ts, Position pos,double distanceEyeToViewCenter){
            // Create a polygon, set some of its properties and set its attributes.
            double baseValue = 19070000; // This is the default value of viewAltitude when launching a worldWind application (in m).
            double adjustmentFactor = 0.9; // To adjust the non-linearity of the scale factor.
            double scaleFactor = Math.pow(distanceEyeToViewCenter / baseValue, adjustmentFactor); // Use to resize the aircraft depending on the viewAltitude

            double course = ts.getCourse(); // Aircraft's current bearing in degrees

            
            double diagonalDistance = scaleFactor*(200000 * Math.sqrt(2)) / 2; // Half the diagonal
            double adjustedBearing = course + 45; // Adjust bearing to point to corner
            // Calculate corner positions
            // Calculate corner position for each of the four corners
            Position center = pos;
            Position cornerNE = calculatePosition(center, adjustedBearing, diagonalDistance);
            Position cornerSE = calculatePosition(center, adjustedBearing + 90, diagonalDistance);
            Position cornerSW = calculatePosition(center, adjustedBearing + 180, diagonalDistance);
            Position cornerNW = calculatePosition(center, adjustedBearing + 270, diagonalDistance);

            // Define texture coordinates
            float[] texCoords = {   
                1.0f, 0.0f, // Bottom right
                0.0f, 0.0f,  // Bottom left
                0.0f, 1.0f, // Top left
                1.0f, 1.0f, // Top right
            };

            ArrayList<Position> corners = new ArrayList<>();
            corners.add(cornerSW);
            corners.add(cornerNW);
            corners.add(cornerNE);
            corners.add(cornerSE);
            
            // Create and texture the square
            polygon = new Polygon(corners);
            
            polygon.setTextureImageSource("src/plane.png", texCoords, 4);
                this.icaoCode = ts.getHexCode();           
    }
    
    public static Position calculatePosition(Position start, double bearing, double distance) {
        double radiusEarth = 6371000.0; // Earth's radius in meters
        double distRatio = distance / radiusEarth;
        double bearingRad = Math.toRadians(bearing);

        double startLatRad = start.getLatitude().radians;
        double startLonRad = start.getLongitude().radians;

        double sinStartLat = Math.sin(startLatRad);
        double cosStartLat = Math.cos(startLatRad);
        double cosDistRatio = Math.cos(distRatio);
        double sinDistRatio = Math.sin(distRatio);

        double endLatRad = Math.asin(sinStartLat * cosDistRatio + cosStartLat * sinDistRatio * Math.cos(bearingRad));
        double endLonRad = startLonRad + Math.atan2(Math.sin(bearingRad) * sinDistRatio * cosStartLat,
                                                    cosDistRatio - sinStartLat * Math.sin(endLatRad));

        // Normalize the longitude to be within -180 to 180 degrees
        endLonRad = (endLonRad + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return Position.fromRadians(endLatRad, endLonRad, start.getElevation());
    }

    public String getIcaoCode() {
        return icaoCode;
    }

    public Polygon getPolygon() {
        return polygon;
    }


}
