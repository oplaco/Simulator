/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import TFM.Routes.Route;
import TCAS.TCASTransponder;
import TFM.AircraftControl.AircraftControlCommand;
import TFM.AircraftControl.ControllableAircraft;
import TFM.AircraftControl.ICommandSource;
import TFM.Atmosphere.AtmosphericModel;
import TFM.Atmosphere.InternationalStandardAtmosphere;
import TFM.Models.BearingStrategy;
import TFM.Models.SimpleBearingStrategy;
import TFM.Performance.AircraftSpecifications;
import TFM.Performance.FlightPhase;
import TFM.Performance.VerticalProfile;
import TFM.Simulation;
import TFM.utils.UnitConversion;
import static classes.base.TrafficSimulated.FLY_LOXODROMIC;
import classes.googleearth.GoogleEarthTraffic;
import java.awt.Color;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms & Gabriel Alfonsín Espín
 */
public class Pilot extends Thread implements ICommandSource {
     
    private Route route;
    private TrafficSimulated plane;
    private String flightPhase;
    private int routeMode; // ortho or loxodromic 
    private int waitTime; // in milliseg
    public double distanceThreshold; //Meters traveled each iteration
    private volatile boolean running = true; //The volatile keyword ensures that changes to the variable are immediately visible to all threads.
    private boolean verbose;
    private GoogleEarthTraffic ge;
    private boolean PaintInGoogleEarth;
    private PilotListener listener = null;
    
    //Bearing/Course
    private BearingStrategy sbs;
    private double targetBearing;
    
    //Aircraft Control
    private static final int PILOT_PRIORITY = 3;
    private Coordinate to; // Current plane destination.
    
    // TCAS
    private TCASTransponder myTCASTransponder;
    
    //Simulation
    private Simulation simulation;
    private long lastSimulationTime;
    private long simulationTime;

    //Atmosphere
    private AtmosphericModel atmosphericModel;
    
    //Performance
    private AircraftSpecifications aircraftSpecifications;
    
    //From flight plan
    private VerticalProfile vp;
    
    //Distance estimator
    private int nextWp;
    private double distanceInWpLeft; //distance left between next waypoint and destination     
    
    public Pilot(Route route, TrafficSimulated plane, int routeMode) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.waitTime = plane.getWaitTime() / 10;
        updateDistanceThreshold();
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        
        this.atmosphericModel = new InternationalStandardAtmosphere();
        this.sbs = new SimpleBearingStrategy();
        
        this.aircraftSpecifications = new AircraftSpecifications(atmosphericModel);      
    }

    public Pilot(Route route, TrafficSimulated plane, int routeMode , Simulation simulation, VerticalProfile vp) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.simulation = simulation;
        this.waitTime = plane.getWaitTime() / 10;
        updateDistanceThreshold();
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        
        this.atmosphericModel = new InternationalStandardAtmosphere();
        this.sbs = new SimpleBearingStrategy();
        
        this.aircraftSpecifications = new AircraftSpecifications(atmosphericModel);
        this.vp = vp;
    }
    
    public void changeToNextPhase() {
        Map<String, FlightPhase> flightPhases = vp.getFlightPhases();

        // If flightPhase is null or not found in the map, set it to the first phase
        if (flightPhase == null || !flightPhases.containsKey(flightPhase)) {
            flightPhase = flightPhases.keySet().iterator().next();
            this.plane.setSpeed(0);
            updateParameters();
            System.out.println("Setting flightPhase to the first phase: " + flightPhase);
            //Initially calculate TOC and TOD
            vp.getVps().updateTOCTOD(route.getCruiseSpeed());
            return;
        }
   
        boolean foundCurrent = false;
        
        for (String phaseName : flightPhases.keySet()) {
            if (foundCurrent) {
                // Found current phase, so set next phase as current
                flightPhase = phaseName;
                updateParameters();
                System.out.println("Change to next phase from: " + foundCurrent + " to " + phaseName);
                return;
            }
            if (phaseName.equals(flightPhase)) {
                // Found current phase, set foundCurrent flag to true
                foundCurrent = true;
            }
        }  
        // If current phase is the last phase, wrap around to the first phase
        flightPhase = flightPhases.keySet().iterator().next();
    }
    
    public Pilot(Route route, TrafficSimulated plane, int routeMode, PilotListener listener, Simulation simulation) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.flightPhase = "Cruise";
        this.simulation = simulation;
        this.waitTime = plane.getWaitTime() / 10;
        updateDistanceThreshold();
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        this.listener = listener;
        
        this.atmosphericModel = new InternationalStandardAtmosphere();    
    }
    
//    public void initPhaseFlight(){
//        this.flightPhase = TAKEOFFRUN; 
//        this.plane.setSpeed(0);
//    }
    
    //Meters traveled each iteration
    private void updateDistanceThreshold(){
        distanceThreshold = 3*(plane.getSpeed() * UnitConversion.knotToMs) * simulation.getElapsedSimulationTime() /(1000);
    }
    
    /**
     * Update the speed acording to the FlighPhase and transform it to TAS (True Air Speed).
     */
    private void updateSpeed() {
        FlightPhase currentFlightPhase = vp.getFlightPhases().get(flightPhase);
        Double flightPhaseSpeed = currentFlightPhase.getSpeed();
        String speedType = currentFlightPhase.getSpeedType();
        Double geometricAltitude = plane.getPosition().getAltitude();

        Double flightPhaseTAS = atmosphericModel.calculateTAS(flightPhaseSpeed, speedType, geometricAltitude);
        Double planeTAS = plane.getSpeed();
        Double TAS;

        if (currentFlightPhase.getType() == FlightPhase.Type.TAKEOFF || currentFlightPhase.getType() == FlightPhase.Type.LANDING) {
            double acceleration = (currentFlightPhase.getType() == FlightPhase.Type.TAKEOFF) ? aircraftSpecifications.getTakeoffAcc() : aircraftSpecifications.getLandingAcc();
            TAS = planeTAS + acceleration * (simulationTime - lastSimulationTime) / 1000;
        } else {
            TAS = flightPhaseTAS;
        }
        sendCommand(plane, new AircraftControlCommand(AircraftControlCommand.CommandType.SPEED, TAS, PILOT_PRIORITY));
    }
    
    private void updateVerticalRate() {
        FlightPhase currentFlightPhase = vp.getFlightPhases().get(flightPhase);
        Double climbingRate = currentFlightPhase.getClimbRate();
        sendCommand(plane, new AircraftControlCommand(AircraftControlCommand.CommandType.VERTICAL_RATE, climbingRate, PILOT_PRIORITY));
    }
    
     /**
     * Update plane VerticalRate depending on the flight phase.
     */
    private void updateParameters()
    {     
        updateVerticalRate();
        updateSpeed();
    }
    
     /**
     * Calculates the total distance (following the route) remaining to the destination which is equal 
     * to the remaining distance to the next waypoint + the distance from that waypoint to the destination throught
     * the rest of waypoints.
     * @return 
     */
    public double getDistanceLeft()
    {
        double distToNext=0;
        if(nextWp >= 0)
        {
            distToNext = plane.getPosition().getGreatCircleDistance(route.getWp()[nextWp]);          
        }
        else
        {
            distToNext = plane.getPosition().getGreatCircleDistance(route.getDestination());
        }
        return distToNext + distanceInWpLeft;

    }
    
    /**
     * Calculates the distance (following the route) from the next Waypoint to next Waypoint until the destionation.
     *   ...  plane ---W4---W5---destination
     * @return 
     */
    private double getDistanceInWp()
    {
        double distancewp = 0;
        if(nextWp >= 0)
        {
            for(int i = nextWp+1; i<route.getWp().length;i++)
            {
                distancewp += route.getWp()[i-1].getGreatCircleDistance(route.getWp()[i]);
            }
            distancewp += route.getWp()[route.getWp().length-1].getGreatCircleDistance(route.getDestination());
        }
        return distancewp;
    }
     
    public void fly(Coordinate to) {

        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: Starting from " + plane.getPosition().getLocationName() + " to " + to.getLocationName());
            System.out.println("[PILOT OF " + plane + "]: waiting...");
        }
        distanceInWpLeft = getDistanceInWp();

        plane.flyit(to, routeMode);
        
        double distance = to.getRhumbLineDistance(plane.getPosition());
        double distanceToTOD;
        double distanceToRunwayEnd;
        //System.out.println(distanceThreshold);
     
        while (distance > distanceThreshold) {
            // Pause thread if running is false
            synchronized (this) {
                while (!running) {
                    try {
                        wait(); // Wait until notified
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Reset the interrupt status
                        return; // Optional: Stop the execution if the thread is interrupted
                    }
                }
            }
                                        
            // Code inside the loop
            simulationTime = this.simulation.getSimulationTime(); //First line
   
            adjustBearingTo(to);

            myTCASTransponder.iteration();
                    
            updateDistanceThreshold();
            distanceToTOD = route.getTodPos().getGreatCircleDistance(plane.getPosition());
            distanceToRunwayEnd = route.getDestination().getGreatCircleDistance(plane.getPosition()); 
            //System.out.println("Pilot of: "+plane.getHexCode()+"D to next wp: " + distance + "dThreshold: " + distanceThreshold+ " p alt: "+plane.getPosition().getAltitude());
            //System.out.println("[PILOT] Phase "+flightPhase); 
            //System.out.println("distanceToTOD (km): "+distanceToTOD/1000);
            double geometricAltitude = plane.getPosition().getAltitude();
//            System.out.println("[PILOT] Geometric h (ft): " + geometricAltitude/UnitConversion.ftToMeter +
//                               " Temperature (K): " + atmosphericModel.calculateTemperature(geometricAltitude) +
//                               " Pressure (Pa): " + atmosphericModel.calculatePressure(geometricAltitude) +
//                               " Speed of sound: " + atmosphericModel.calculateSpeedOfSound(atmosphericModel.calculateTemperature(geometricAltitude)) );   
//            System.out.println("[PILOT] distance "+distance + "distanceThreshold" + distanceThreshold); 
            flightPhase = vp.checkFlightPhase(flightPhase, plane.getSpeed(), geometricAltitude, distanceToTOD, distanceThreshold);
            
            
            //Leave while if plane is stopped
            if(!plane.isMoving())
            {
                return;
            }
            distance = to.getRhumbLineDistance(plane.getPosition());
            //this.updateVerticalRate();
            this.updateParameters();
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(Pilot.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            lastSimulationTime = simulationTime; //Last line
        }
        if (verbose) {
            System.out.println("[PILOT]: ****[" + plane + "] in " + to.getLocationName() + " (estimated leg error: " + Math.round(distance) + " meters)");
        }
        if (listener != null) {
            listener.targetReached(to);
        }
        plane.getPosition().setLocationName(to.getLocationName()); // update position name in plane
    }
    
    private void adjustBearingTo(Coordinate to) {
        if (routeMode == FLY_LOXODROMIC) {
            targetBearing = plane.getPosition().getRhumbLineBearing(to);
        } else {
            targetBearing = plane.getPosition().getGreatCircleInitialBearing(to);
        }
        double currentBearing = plane.getCourse();
        double nextBearing = sbs.calculateBearing(currentBearing, targetBearing, simulationTime - lastSimulationTime);

        // Sending command through the ICommandSource interface
        sendCommand(plane, new AircraftControlCommand(AircraftControlCommand.CommandType.COURSE, nextBearing, PILOT_PRIORITY));
    }
    
    @Override
    public void run() {
        int i;
        this.myTCASTransponder = new TCASTransponder(plane.getHexCode(),simulation.getTrafficDisplayer().getWwd(),simulation.getPilotMap());
        if (listener != null) {
            listener.starting(route.getDeparture());
        }
        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: Flying from " + route.getDeparture().getLocationName() + " to " + route.getDestination().getLocationName());
        }
        if (route.getWp() != null) { // ruta con waypoints
            
            for (i = 0; i < route.getWp().length; i++) {
                nextWp=i;
                System.out.println("Waypoint number: "+ nextWp);
                to = route.getWp()[i];
                fly(this.to);
                if(!plane.isMoving())
                {
                    return;
                }
            }
            nextWp=-1;//no wp
            to = route.getDestination();
            fly(to);
            plane.stoppit();
        } else { // ruta directa entre los aeropuertos
            nextWp=-1; // no wp
            to = route.getDestination();
            fly(to);
            plane.stoppit();
        }
        try {
            plane.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Pilot.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        if (PaintInGoogleEarth) {
            ge.closeAndLaunch();
        }

        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: End");
        }

    }
    
    // Generic method to send any type of command
    @Override
    public void sendCommand(ControllableAircraft controllableAircraft, AircraftControlCommand command) {
       controllableAircraft.processCommand(command);
    }
    
    // Method to continue route following
    public void continueRoute() {
        if (nextWp < route.getWp().length) {
            Coordinate nextWaypoint = route.getWp()[nextWp];
            plane.flyit(to, routeMode);
            System.out.println("[PILOT]: Continuing route to " + nextWaypoint.getLocationName());
        }
    }
       
    public void paint(String fileName, String altMode, Color col, int tick) {
        PaintInGoogleEarth = true;

        ge = new GoogleEarthTraffic(fileName, plane, altMode, col, tick);
        plane.setPaintInGoogleEarth(ge);
    }

    public void pauseThread() {
        System.out.println("Pilot of: "+plane.getHexCode()+" was paused.");
        running = false;
        plane.stoppit();
    }

    public synchronized void resumeThread() {
        System.out.println("Pilot of: "+plane.getHexCode()+" was resumed.");
        running = true;
        notifyAll(); // Notify the thread to continue
        plane.startit();
    } 
    
    public int getRouteMode()
    {
        return routeMode;
    }

    public TrafficSimulated getPlane() {
        return plane;
    }
    
    public void verboseON() {
        verbose = true;
    }

    public void verboseOFF() {
        verbose = false;
    }
  
}
