/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import TFM.Simulation;
import classes.googleearth.GoogleEarthTraffic;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms & Alfredo Torres Pons
 */
public class Pilot extends Thread {
     
    private Route route;
    private TrafficSimulated plane;
    private int flightPhase;
    private int routeMode; // ortho or loxodromic 
    private int waitTime; // in milliseg
    public double distanceThreshold; //Meters traveled each iteration
    private boolean running;
    private boolean verbose;
    private GoogleEarthTraffic ge;
    private boolean PaintInGoogleEarth;
    private PilotListener listener = null;
    
    //Simulation
    private Simulation simulation;
    private long lastSimulationTime;
    private long simulationTime;
    
    
    //Flight Phases
    public static final int TAXI = 1; //Taxi phase includes both taxi-out and taxi-in.
    public static final int TAKEOFFRUN = 2; // Takeoff run phase begins when the crew increases thrust for the purpose of lift-off.
    public static final int CLIMB = 3;
    public static final int CRUISE = 4; // Cruise phase begins when the aircraft reaches the initial cruise altitude. It ends when the crew initiates a descent for the purpose of landing.
    public static final int DESCENT = 5; // Initial descent phase starts when the crew leaves the cruise altitude in order to land
    public static final int APPROACH = 6; // Approach phase starts when the crew initiates changes in the aircraft’s configuration and/or speed in view of the landing. 
    public static final int LANDING = 7; // Landing phase begins when the aircraft is in the landing configuration and the crew is dedicated to land on a particular runway
    
    
    //Speeds
    private double cruiseSpeed = 560; // Knots
    private double takeoffSpeed = 160; // Knots
    private double approachSpeed = 160; // Knots
    
    //Vertical profile
    private double cruiseAlt = 15000; // feet
    private double climbRate; // fpm
    private double descentRate; // fpm
    private double topOfClimb; // m
    private double topOfDescent; // m
        
    
    //Distance estimator
    private int nextWp;
    private double distanceInWpLeft; //distance left between next waypoint and destination
    
    public Pilot(Route route, TrafficSimulated plane, int routeMode ) {
        this.route = route;
        this.plane = plane;
        this.flightPhase = Pilot.CRUISE;
        this.routeMode = routeMode;
        this.waitTime = plane.getWaitTime() / 2;
        System.out.println("Builder: "+plane.getSpeed() + " " + simulation.getElapsedSimulationTime());
        updateDistanceThreshold();
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        
        //If vert profile not specified, assume its no used
        cruiseAlt=0;
        climbRate=0;
        descentRate=0;
        topOfClimb=0;
        topOfDescent=0;
    }
        

    public Pilot(Route route, TrafficSimulated plane, int routeMode , Simulation simulation) {
        this.route = route;
        this.plane = plane;
        this.flightPhase = Pilot.CRUISE;
        this.routeMode = routeMode;
        this.simulation = simulation;
        this.waitTime = plane.getWaitTime() / 10;
        System.out.println("Builder: "+plane.getSpeed() + " " + simulation.getElapsedSimulationTime());
        updateDistanceThreshold();
        this.running = true;
        this.PaintInGoogleEarth = false;
        this.verbose = true; // informa por pantalla
        
        //If vert profile not specified, assume its no used
        cruiseAlt=0;
        climbRate=0;
        descentRate=0;
        topOfClimb=0;
        topOfDescent=0;
    }

    public Pilot(Route route, TrafficSimulated plane, int routeMode, PilotListener listener, Simulation simulation) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.flightPhase = Pilot.CRUISE;
        this.simulation = simulation;
        this.waitTime = plane.getWaitTime() / 10;
        updateDistanceThreshold();
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
    
    public void initPhaseFlight(){
        this.flightPhase = TAKEOFFRUN; 
        this.plane.setSpeed(0);
    }
    //Meters traveled each iteration
    private void updateDistanceThreshold(){
        distanceThreshold = 2*(plane.getSpeed() * Simulation.knotToMs) * simulation.getElapsedSimulationTime() /(1000);
    }
    /**
     * Uniformly accelerated rectilinear motion.
     */
    private void updateSpeed(){
 
        double newSpeed;
        
        switch (flightPhase) {
            case TAXI:
                plane.setSpeed(10);
                break;
                
            case TAKEOFFRUN:
                
                newSpeed = plane.getSpeed() + plane.getTakeoffAcceleration()* (simulationTime-lastSimulationTime)/1000;
                System.out.println("Simulation time: "+simulationTime + " lastSimulationTime " +lastSimulationTime);
                if (newSpeed>takeoffSpeed){
                    newSpeed = takeoffSpeed;
                    this.flightPhase = CLIMB;
                }
                plane.setSpeed(newSpeed);

            case CLIMB:
                newSpeed = plane.getSpeed() + plane.getClimbAcceleration()* (simulationTime-lastSimulationTime)/1000;
                if (newSpeed<cruiseSpeed){
                    plane.setSpeed(newSpeed);
                }
                break;
                
            case CRUISE:
                plane.setSpeed(cruiseSpeed);
                break;
                
            case DESCENT:
                
                newSpeed = plane.getSpeed() - plane.getClimbAcceleration()* (simulationTime-lastSimulationTime)/1000;
                if (newSpeed>approachSpeed){
                    plane.setSpeed(newSpeed);
                }
                break;
            case APPROACH:
                plane.setSpeed(approachSpeed);
                break;
            case LANDING:
                newSpeed = plane.getSpeed() -plane.getTakeoffAcceleration()* (simulationTime-lastSimulationTime)/1000;
                if (newSpeed>0){
                    plane.setSpeed(newSpeed);
                }else{
                    plane.setSpeed(0);
                }
                break;
        }
        
        System.out.println("Update speed: " + plane.getSpeed());
    }
    
    private void updateVerticalRate(){
 
        switch (flightPhase) {
            case Pilot.TAXI:
                plane.setVerticalRate(0);
                break;
            case Pilot.TAKEOFFRUN:
                plane.setVerticalRate(0);
                break;
            case Pilot.CLIMB:
                plane.setVerticalRate(climbRate);
                if (plane.getPosition().getAltitude()>cruiseAlt){
                    this.flightPhase = CRUISE;
                }
                break;
            case Pilot.CRUISE:
                plane.setVerticalRate(0);
                break;
            case Pilot.DESCENT:
                plane.setVerticalRate(descentRate);
                if (plane.getPosition().getAltitude()<300){
                    this.flightPhase = this.flightPhase=Pilot.APPROACH;
                }
                break;
            case Pilot.APPROACH:
                plane.setVerticalRate(0);
                break;
            case Pilot.LANDING:
                plane.setVerticalRate(0);
                break;
        }
        System.out.println("Vertical Rate (ft/m): " + plane.getVerticalRate()+" Plane altitude (ft): " + plane.getPosition().getAltitude()+" Cruise alt(ft): "+cruiseAlt);
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
     * Config vertical profile of the plane
     * @param cruiseAltFt
     * @param climbRateFpm
     * @param descentRateFpm 
     */
    public void setVerticalProfile(double cruiseAltFt,double climbRateFpm,double descentRateFpm)
    {
        //Check input is valid
        if(cruiseAltFt<= 0)
        {
            throw new RuntimeException("Pilot of "+plane.getHexCode()+": Cruise altitude should be positive");
        }
        if(climbRateFpm<= 0 )
        {
            throw new RuntimeException("Pilot of "+plane.getHexCode()+": Climb rate should be positive");
        }
        if(descentRateFpm >=0)
        {
            throw new RuntimeException("Pilot of "+plane.getHexCode()+": Descent rate should be negative");
        }
        this.cruiseAlt=cruiseAltFt;
        this.climbRate=climbRateFpm;
        this.descentRate=descentRateFpm;    
        
        this.updateTOCTOD();
    }
    
   
    /**
     * Calculate and update TOCTOD based on vertical profile input
     * Also computes between which wp is located 
     */
    private void updateTOCTOD()
    {
        
        //Parametros de calculo de TOC y TOD
        double V = cruiseSpeed*Simulation.knotToMs;
        double vy1=climbRate*Simulation.ftToMeter/60;
        double vy2=Math.abs(descentRate)*Simulation.ftToMeter/60;
        double k1 = vy1/V;
        double k2= vy2/V;
        double D = route.getRouteLength(routeMode);
        double H = cruiseAlt*Simulation.ftToMeter;
        
        
        //Se calcula el punto del TOC y TOD como la intersección entre tres rectas
        //La recta de ascenso, la de descenso y la de crucero.
        //Si se intersecta antes el TOD con el TOC que con la de crucero,
        //no se llega a altitud de crucero
        double x_intersect = (k2*D)/(k1+k2);
        double h_intersect = k1*x_intersect;
        double x_TOC = H/k1;
        double x_TOD = (k2*D-H)/k2;
        
        //Muestra la posición del TOC y TOD
        System.out.println("x intersect = " + Double.toString(x_intersect));
        System.out.println("x TOC = " + Double.toString(x_TOC));
        System.out.println("x TOD = " + Double.toString(x_TOD));
        
        //Comprueba si se alcanza la altitud de crucero
        if(x_TOC>x_TOD)
        {
            this.topOfClimb=x_intersect;
            this.topOfDescent=x_intersect;
            this.cruiseAlt = h_intersect/Simulation.ftToMeter;
        }
        else
        {
            this.topOfClimb=x_TOC;
            this.topOfDescent=x_TOD;
        }
        
        
       
        //Obten el WP del TOC y el TOD
        int tocWP = route.getLastWpIndexByDistance(this.topOfClimb, this.routeMode);
        
        int todWP = route.getLastWpIndexByDistance(topOfDescent, this.routeMode);
        
        //Distancias hasta TOC y TOD
        double distwptoc = route.getDistanceToWp(tocWP, routeMode);
        double distwptod = route.getDistanceToWp(todWP, routeMode);
        
        double distTocToWP = topOfClimb-distwptoc;
        double distTodToWP = topOfDescent-distwptod;
        
       
        //Set el TOC y TOD de la ruta (3D)
        route.setTOCTOD(cruiseAlt*Simulation.ftToMeter, tocWP, tocWP+1, distTocToWP, true);
        route.setTOCTOD(cruiseAlt*Simulation.ftToMeter, todWP, todWP+1, distTodToWP, false);

        
        
        //Configura la altitud de los waypoints intermedios
        for(int i = 0;i<route.getWp().length;i++)
        {
            WayPoint wp = route.getWp()[i];
            double distWp = route.getDistanceToWp(i, routeMode);
            
            //Si se ha llegado el TOC
            if(distWp<topOfClimb)
            {
                double altitud = k1*distWp;
                wp.setAltitude(altitud);
            }
            //Si se ha llegado al TOD
            else if(distWp>topOfDescent)
            {
                double altitud = k2*(D-distWp);
                wp.setAltitude(altitud);
            }
            //Si se esta en crucero
            else
            {
                wp.setAltitude(cruiseAlt*Simulation.ftToMeter);
            }  
        }
        
        if(verbose)
        {
            System.out.print("[PILOT OF " + plane + "]: Set top of Climb: "
            +String.format("%.2f",topOfClimb/1852)+ " NM");
            
            if(tocWP>=0)
            {
                System.out.println(" -> "+ String.format("%.2f",distTocToWP/1852.0)
                        + "NM after WayPoint: "+ route.getWp()[tocWP].getLocationName());
            }
            else if(tocWP == -1)
            {
                System.out.println("-> after departure ");
            }
            else
            {
                System.out.println("-> outside of route");
            }
            
         
            System.out.print("[PILOT OF " + plane + "]: Set top of Descent: "
            +String.format("%.2f",topOfDescent/1852)+ " NM");
            
            if(todWP>=0)
            {
                System.out.println(" -> "+ String.format("%.2f",distTodToWP/1852.0)
                        + "NM after WayPoint: "+ route.getWp()[todWP].getLocationName());
            }
            else if(todWP == -1)
            {
                System.out.println("-> after departure ");
            }
            else
            {
                System.out.println("-> outside of route");
            }
        }
        
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
            simulationTime = this.simulation.getSimulationTime();
   

            updateDistanceThreshold();
            distanceToTOD = route.getTodPos().getGreatCircleDistance(plane.getPosition());
            distanceToRunwayEnd = route.getDestination().getGreatCircleDistance(plane.getPosition()); 
            System.out.println("Distance to next wp: " + distance + "distanceThreshold: " + distanceThreshold);
            //System.out.println("distance to TOD: "+distanceToTOD); 
            //System.out.println("Flight phase: "+flightPhase); 
   
            if (distanceToTOD<distanceThreshold){
                this.flightPhase = DESCENT;
            }
            if (distanceToRunwayEnd<3000){
                this.flightPhase = LANDING;
            }
            
            
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
            lastSimulationTime = simulationTime;
        }
        if (verbose) {
            System.out.println("[PILOT]: ****[" + plane + "] in " + to.getLocationName() + " (estimated leg error: " + Math.round(distance) + " meters)");
        }
        if (listener != null) {
            listener.targetReached(to);
        }
        plane.getPosition().setLocationName(to.getLocationName()); // update position name in plane
    }

    @Override
    public void run() {
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
                System.out.println("Waypoint number: "+ nextWp);
                fly(route.getWp()[i]);
                if(!plane.isMoving())
                {
                    return;
                }
            }
            nextWp=-1;//no wp
            fly(route.getDestination());
            plane.stoppit();
        } else { // ruta directa entre los aeropuertos
            nextWp=-1; // no wp
            fly(route.getDestination());
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
    
    public int getFlightPhase()
    {
        // return-> 0: crucero, -1: descenso, 1 ascenso
        //If cruise Alt is 0, vertical profile not used, Always in cruise
        if(cruiseAlt==0)
        {
            return 0;
        }
  
        
        double dist_to_start = plane.getTraveled();
        
        
        double descAltError = Math.abs(TrafficSimulated.SAMPLE_TIME*descentRate)*Simulation.ftToMeter;
        
        //Take Wp into acount and calculates All distances in each iteration
        //Its expensive, and should only update the distance left to next waypoint
        
        if(dist_to_start < topOfClimb)
        {
            return 1;
        }
        if(dist_to_start >= topOfDescent && plane.getPosition().getAltitude() >= descAltError)
        {
            return -1;
        }


        return 0;
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
    
    
    public void paint(String fileName, String altMode, Color col, int tick) {
        PaintInGoogleEarth = true;

        ge = new GoogleEarthTraffic(fileName, plane, altMode, col, tick);
        plane.setPaintInGoogleEarth(ge);
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
