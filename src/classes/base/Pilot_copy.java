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
public class Pilot_copy extends Thread {

    private Route route;
    private TrafficSimulated plane;
    private int routeMode; // ortho or loxodromic 
    private int waitTime; // in milliseg
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

    public Pilot_copy(Route route, TrafficSimulated plane, int routeMode) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.waitTime = plane.getWaitTime() / 10;
        this.distanceThreshold = (plane.getSpeed() * 1852 / 3600) * plane.SAMPLE_TIME * 60 / 2; // meters traveled in each iteration / 2
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

    public Pilot_copy(Route route, TrafficSimulated plane, int routeMode, PilotListener listener) {
        this.route = route;
        this.plane = plane;
        this.routeMode = routeMode;
        this.waitTime = plane.getWaitTime() / 10;
        this.distanceThreshold = (plane.getSpeed() * 1852 / 3600) * plane.SAMPLE_TIME * 60 / 2; // meters traveled in each iteration / 2
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
            throw new RuntimeException("Pilot of "+plane.getHexCode()+": Crise altitude should be positive");
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
        double V = plane.getSpeed()*knotToMs;
        double vy1=climbRate*ftToMeter/60;
        double vy2=Math.abs(descentRate)*ftToMeter/60;
        double k1 = vy1/V;
        double k2= vy2/V;
        double D = route.getRouteLength(routeMode);
        double H = cruiseAlt*ftToMeter;
        
        
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
            this.cruiseAlt = h_intersect/ftToMeter;
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
        route.setTOCTOD(cruiseAlt*ftToMeter, tocWP, tocWP+1, distTocToWP, true);
        route.setTOCTOD(cruiseAlt*ftToMeter, todWP, todWP+1, distTodToWP, false);

        
        
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
                wp.setAltitude(cruiseAlt*ftToMeter);
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
        while (distance > distanceThreshold) {
            
            //Leave while if plane is stopped
            if(!plane.isMoving())
            {
                return;
            }
            distance = to.getRhumbLineDistance(plane.getPosition());
            //System.out.println("Distance: " + distance);
            this.updateVerticalRate();
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(Pilot_copy.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
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
            Logger.getLogger(Pilot_copy.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        if (PaintInGoogleEarth) {
            ge.closeAndLaunch();
        }

        if (verbose) {
            System.out.println("[PILOT OF " + plane + "]: End");
        }

    }

    public void paint(String fileName, String altMode, Color col, int tick) {
        PaintInGoogleEarth = true;

        ge = new GoogleEarthTraffic(fileName, plane, altMode, col, tick);
        plane.setPaintInGoogleEarth(ge);
    }

    public void verboseON() {
        verbose = true;
    }

    public void verboseOFF() {
        verbose = false;
    }
    private void updateVerticalRate()
    {
        // calcular la fase de vuelo: ascenso, crucero y descenso
            // a partir de la posición del avión y los parámetros del perfil vertical.
            // Con esto, se notificará al avión la tasa de ascenso/descenso mediante: plane.setVerticalRate(tasa);
          
        int flightPhase = this.getFlightPhase();
        switch (flightPhase) {
            case -1:
                plane.setVerticalRate(descentRate);
                break;
            case 0:
                plane.setVerticalRate(0);
                break;
            case 1:
                plane.setVerticalRate(climbRate);
                break;
            default:
                break;
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
        
        
        double descAltError = Math.abs(TrafficSimulated.SAMPLE_TIME*descentRate)*ftToMeter;
        
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
     * Devuelve la distancia estimada que queda para terminar la ruta, como la distancia que queda hasta el proximo wp
     * mas la distancia en wp que queda
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
     * Devuelve la distancia que queda a partir del proximo waypoint
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
