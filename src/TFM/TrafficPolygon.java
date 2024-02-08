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
//Different class Position from wwd and adsb.
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import java.util.ArrayList;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;


public class TrafficPolygon{
    
    private Polygon polygon;
    private String icaoCode;
    
    public TrafficPolygon(TrafficSimulated ts, Position pos){
            // Create a polygon, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            
            double length = 1; // Length of the aircraft in degrees, adjust as needed
            double width = 0.5; // Width of the aircraft in degrees, adjust as needed

            double lat = pos.getLatitude().getDegrees(); // Aircraft's current latitude
            double lon = pos.getLongitude().getDegrees(); // Aircraft's current longitude
            double course = ts.getCourse(); // Aircraft's current bearing in degrees

            
            // Calculate offsets in degrees - this is a simplified approach
            double deltaLat = Math.cos(Math.toRadians(course)) * length;
            double deltaLon = Math.sin(Math.toRadians(course)) * length;

            // Calculate front point (in the direction of bearing)
            double frontLat = lat + deltaLat;
            double frontLon = lon + deltaLon;

            // Calculate rear points (perpendicular to bearing, to the left and right of the current position)
            double rearLeftLat = lat - deltaLat + deltaLon * width;
            double rearLeftLon = lon - deltaLon - deltaLat * width;

            double rearRightLat = lat - deltaLat - deltaLon * width;
            double rearRightLon = lon - deltaLon + deltaLat * width;

            // Add these positions to your pathPositions
            pathPositions.add(Position.fromDegrees(frontLat, frontLon, 30000)); // Front
            pathPositions.add(Position.fromDegrees(rearLeftLat, rearLeftLon, 30000)); // Rear Left
            pathPositions.add(Position.fromDegrees(rearRightLat, rearRightLon, 30000)); // Rear Right
            // Ensure the polygon is closed by re-adding the first position
            pathPositions.add(Position.fromDegrees(frontLat, frontLon, 30000));

            
            
            
            this.polygon = new Polygon(pathPositions);
            
            // Create and set an attribute bundle.
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorMaterial(Material.YELLOW);
            normalAttributes.setOutlineOpacity(0.5);
            normalAttributes.setInteriorOpacity(0.8);
            normalAttributes.setOutlineMaterial(Material.GREEN);
            normalAttributes.setOutlineWidth(2);
            normalAttributes.setDrawOutline(true);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setEnableLighting(true);
            
            polygon.setAttributes(normalAttributes);
            
            this.icaoCode = ts.getHexCode();
            
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public String getIcaoCode() {
        return icaoCode;
    }
    
    
    
    
    
}
