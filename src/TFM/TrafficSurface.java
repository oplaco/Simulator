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


public class TrafficSurface{
    
    private String icaoCode;
    private SurfaceImage surfaceImage;
    private Path boundary;
    private SurfaceSquare surfaceSquare;
    private Globe globe;
    // Constants for Earth
    final double EARTH_RADIUS = 6378137; // Radius in meters
    
    public TrafficSurface(TrafficSimulated ts, Position pos,double viewAltitude,Globe globe){
            // Create a polygon, set some of its properties and set its attributes.
            this.globe = globe;

            double baseValue = 19070; // This is the default viewAltitude when launching a worldWind application.
            double adjustmentFactor = 0.5; // To adjust the non-linearity of the scale factor.
            double scaleFactor = Math.pow(viewAltitude / baseValue, adjustmentFactor); // Use to resize the aircraft depending on the viewAltitude

            double lat = pos.getLatitude().getDegrees(); // Aircraft's current latitude
            double lon = pos.getLongitude().getDegrees(); // Aircraft's current longitude
            //double alt = pos.getAltitude();
            double course = ts.getCourse(); // Aircraft's current bearing in degrees

            
            SurfaceSquare surfaceSquare = new SurfaceSquare(LatLon.fromDegrees(lat , lon ),400000*scaleFactor);
            surfaceSquare.setHeading(Angle.fromDegrees(course));
            SurfaceImage surfaceImage = new SurfaceImage("src/plane.png", surfaceSquare.getLocations(this.globe));
            
            //Might be interesting for future bounding boxes.
            Path boundary = new Path(surfaceImage.getCorners(), 0);
            boundary.setSurfacePath(true);
            boundary.setPathType(AVKey.RHUMB_LINE);
            var attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(new Color(0, 255, 0)));
            boundary.setAttributes(attrs);
            boundary.makeClosed();
            
            this.surfaceSquare = surfaceSquare;
            this.boundary = boundary;
            this.surfaceImage = surfaceImage;
            this.icaoCode = ts.getHexCode();           
    }

    public String getIcaoCode() {
        return icaoCode;
    }

    public SurfaceImage getSurfaceImage() {
        return surfaceImage;
    }

    public Path getBoundary() {
        return boundary;
    }

    public SurfaceSquare getSurfaceSquare() {
        return surfaceSquare;
    }

}
