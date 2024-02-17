/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.polygons;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polygon;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;

/**
 * 
 * @author Gabriel Alfonsín Espín
 * 
 * Take Off surface simplified version according to spanish regulation.
 * https://www.mitma.gob.es/aviacion-civil/politicas-aeroportuarias/integracion-territorial-aeroportuaria/servidumbres-y-ruidos/descripcion-de-las-servidumbres-de-aerodromos
 * Assumptions:
 * - All the runways are the main runway.
 * - Each runway takes the maximum possible slope.
 * 
 */
public class TakeOffSurface {
    
    private Polygon polygon;
    private double divergence;
    private Position start;
    private double bearing;
    private int widthInnerEdge;
    private int widthOuterEdge;
    private int length;
    private double slope;
    
    public TakeOffSurface(String RunwayKey, Position start, double bearing){
        this.start = start;
        this.bearing = bearing;
        initializeRunway(RunwayKey);
        createSlopedSurfacePolygon();
    }
    
    public TakeOffSurface(double length, Position start, double bearing){
        this.start = start;
        this.bearing = bearing;
        String RunwayKey = determineRunwayKey(length);
        initializeRunway(RunwayKey);
        createSlopedSurfacePolygon();
    }
    
    private String determineRunwayKey(double length) {
        if (length < 750) {
            return "E";
        } else if (length < 900) {
            return "D";
        } else if (length < 1500) {
            return "C";
        } else if (length < 2100) {
            return "B";
        } else {
            return "A";
        }
    }
    
    private void initializeRunway(String RunwayKey){
        switch(RunwayKey) {
            case "A":
                this.divergence = 12.5;
                this.widthInnerEdge = 180;
                this.widthOuterEdge = 1200;
                this.length = 15000;
                this.slope = 0.02;
                break;
            case "B":
                this.divergence = 12.5;
                this.widthInnerEdge = 180;
                this.widthOuterEdge = 1200;
                this.length = 15000;
                this.slope = 0.02;
                break;
            case "C":
                this.divergence = 12.5;
                this.widthInnerEdge = 180;
                this.widthOuterEdge = 1200;
                this.length = 15000;
                this.slope = 0.02;
                break;
            case "D":
                this.divergence = 10;
                this.widthInnerEdge = 80;
                this.widthOuterEdge = 580;
                this.length = 2500;
                this.slope = 0.04;
                break;
            case "E":
                this.divergence = 10;
                this.widthInnerEdge = 60;
                this.widthOuterEdge = 380;
                this.length = 2500;
                this.slope = 0.05;
                break;
        }     
    }
    
    private void createSlopedSurfacePolygon() {
        double bearing = this.bearing;
        Position centerEndOfRunway = this.start;
        double halfInnerWidth = this.widthInnerEdge / 2;
        double halfOuterWidth = this.widthOuterEdge / 2;
        double wideningLenght = (this.widthOuterEdge - this.widthInnerEdge) / (this.divergence / 100); // 2% slope for widening
        double totalLength = this.length;

        // Calculate the positions at the inner edge
        Position innerLeft = calculatePoint(centerEndOfRunway, bearing - 90, halfInnerWidth,0);
        Position innerRight = calculatePoint(centerEndOfRunway, bearing + 90, halfInnerWidth,0);

        // Calculate the positions where the slope reaches its full width
        Position slopeLeftEnd1 = calculatePoint(innerLeft, bearing-90, halfOuterWidth,wideningLenght);
        Position slopeRightEnd1 = calculatePoint(innerRight, bearing+90, halfOuterWidth,wideningLenght);
        
        Position slopeLeftEnd2 = calculatePoint(slopeLeftEnd1, bearing, wideningLenght,wideningLenght);
        Position slopeRightEnd2 = calculatePoint(slopeRightEnd1, bearing, wideningLenght,wideningLenght);

        // Calculate the positions at the outer edge
        Position outerLeftEnd = calculatePoint(slopeLeftEnd2, bearing, totalLength - wideningLenght,totalLength - wideningLenght);
        Position outerRightEnd = calculatePoint(slopeRightEnd2, bearing, totalLength - wideningLenght,totalLength - wideningLenght);

        // Create the polygon
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        pathPositions.add(innerLeft);
        pathPositions.add(innerRight);
        pathPositions.add(slopeRightEnd2);
        pathPositions.add(outerRightEnd);
        pathPositions.add(outerLeftEnd);
        pathPositions.add(slopeLeftEnd2);
        this.polygon = new Polygon(pathPositions);
    }

    /**
     * 
     * @param start starting position to calculate the next one.
     * @param bearing is the direction in where the new position will be created.
     * @param delta_distance is the distance from the start position to the new position.
     * @param distance is the distance (surface projection) from the start position. 
     * @return Position
     */
    private Position calculatePoint(Position start, double bearing, double delta_distance, double distance) {
        // Convert the bearing to radians
        bearing = Math.toRadians(bearing);

        // Calculate the new latitude and longitude using the Haversine formula
        double dR = delta_distance / 6371000; // Earth's radius in meters
        double lat1 = start.getLatitude().radians;
        double lon1 = start.getLongitude().radians;

        double newLat = asin(sin(lat1) * cos(dR) + cos(lat1) * sin(dR) * cos(bearing));
        double newLon = lon1 + atan2(sin(bearing) * sin(dR) * cos(lat1), cos(dR) - sin(lat1) * sin(newLat));

        // Calculate the elevation change based on the slope
        double elevationChange = distance * this.slope; // Slope percentage converted to a decimal
        double newElevation = start.getElevation() + elevationChange;

        // Return the new position with the adjusted elevation
        return Position.fromRadians(newLat, newLon, newElevation);
    }

    public Polygon getPolygon() {
        return polygon;
    }
    
}
