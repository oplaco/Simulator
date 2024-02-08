/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import classes.googleearth.GoogleEarthTraffic;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms & Alfredo Torres Pons
 */
public class Pilot {

    private Route route;
    private TrafficSimulated plane;
    private int routeMode; // ortho or loxodromic 
    public double distanceThreshold; // meters
    private boolean running;
    private boolean verbose;
    private GoogleEarthTraffic ge;
    private boolean PaintInGoogleEarth;
    private PilotListener listener = null;
    
    //Vertical profile
    private double cruiseAlt; // feet
    private double climbRate; // fpm
    private double descentRate; // fpm
    private double topOfClimb; // m
    private double topOfDescent; // m
        
    
    //Distance estimator
    private int nextWp;
    private double distanceInWpLeft; //distance left between next waypoint and destination
    
    //conversion
    static public double ftToMeter = 0.3048;
    static public double knotToMs = 0.514444;
    static public double meterToNM = 1/1852;

    public Pilot(Route route, TrafficSimulated plane, int routeMode) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.distanceThreshold = (plane.getSpeed() * 1852 / 3600) * TrafficSimulated.SAMPLE_TIME * 60 / 2; // meters traveled in each iteration / 2
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        
        //If vert profile not specified, assume its no used
        cruiseAlt=0;
        climbRate=0;
        descentRate=0;
        topOfClimb=0;
        topOfDescent=0;
        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: Starting from " + plane.getPosition().getLocationName() + " to " + route.getDestination().toString());
        }
    }

    public Pilot(Route route, TrafficSimulated plane, int routeMode, PilotListener listener) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.distanceThreshold = (plane.getSpeed() * 1852 / 3600) * TrafficSimulated.SAMPLE_TIME * 60 / 2; // meters traveled in each iteration / 2
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        this.listener = listener;
        
        //If vert profile not specified, assume its no used
        cruiseAlt=0;
        climbRate=0;
        descentRate=0;
        topOfClimb=0;
        topOfDescent=0;
    }
    

    public void update(long simulationStepTime){
        //this.plane.move(simulationStepTime);
        int i;

        if (listener != null) {
            listener.starting(route.getDeparture());
        }
        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: Flying from " + route.getDeparture().getLocationName() + " to " + route.getDestination().getLocationName());
        }
        if (route.getWp() != null) { // ruta con waypoints
            
            for (i = 0; i < route.getWp().length; i++) {
                nextWp=i;
                fly(route.getWp()[i], simulationStepTime);
                if(!plane.isMoving())
                {
                    return;
                }
            }
            nextWp=-1;//no wp
            fly(route.getDestination(),simulationStepTime);
            plane.stoppit();
        } else { // ruta directa entre los aeropuertos
            nextWp=-1; // no wp
            fly(route.getDestination(), simulationStepTime);
            plane.stoppit();
        }


        if (PaintInGoogleEarth) {
            ge.closeAndLaunch();
        }

        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: End");
        }
    }
  
    public void fly(Coordinate to, long simulationStepTime) {

        //distanceInWpLeft = getDistanceInWp();
        plane.flyit(to, routeMode, simulationStepTime);
        double distance = to.getRhumbLineDistance(plane.getPosition());
        if (distance < distanceThreshold) {
            if (verbose) {
                System.out.println("[PILOT]: ****[" + plane + "] arrived in " + to.getLocationName() + " (estimated leg error: " + Math.round(distance) + " meters)");
            }
        }
        //if (listener != null) {
        //    listener.targetReached(to);
        //}
        //plane.getPosition().setLocationName(to.getLocationName()); // update position name in plane
    }
    

    public TrafficSimulated getPlane() {
        return plane;
    }
    
    public double getTopOfClimb() {
        return topOfClimb;
    }

    public double getTopOfDescent() {
        return topOfDescent;
    }
    public int getRouteMode()
    {
        return routeMode;
    }

}
