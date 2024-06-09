/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import TFM.Core.Simulation;
import TFM.utils.UnitConversion;
import TFM.Routes.Route;
import TFM.Core.TrafficSimulated;
import TFM.Coordinates.WayPoint;

/**
 *
 * @author Alfredo Torres Pons
 * This VP Solver assumes:
 * - There are only 3 flight phases: climb, cruise and descent.
 */
public class VPSolver implements VerticalProfileSolver{
    
    //Vertical profile
    private double cruiseAlt; // feet
    private double climbRate; // fpm
    private double descentRate; // fpm
    private Route route;
    private int routeMode;
    private TrafficSimulated plane;

    
    private final boolean verbose = false;
    private double topOfClimb; // m
    private double topOfDescent; // m

    public VPSolver(double cruiseAlt, double climbRate, double descentRate, Route route, int routeMode, TrafficSimulated plane) {
        this.cruiseAlt = cruiseAlt;
        this.climbRate = climbRate;
        this.descentRate = descentRate;
        this.route = route;
        this.routeMode = routeMode;
        this.plane = plane;
    }
    
    public VPSolver(Route route, int routeMode, TrafficSimulated plane) {
        this.climbRate = 2000;
        this.descentRate = 1400;
        this.route = route;
        this.routeMode = routeMode;
        this.plane = plane;
    }
    
    
    @Override
    /**
     * Calculate and update TOCTOD based on vertical profile input
     * Also computes between which wp is located 
     */
    public void updateTOCTOD(double cruiseSpeed){
        
        //Parametros de calculo de TOC y TOD
        double V = cruiseSpeed*UnitConversion.knotToMs; // Assumption that speed during the entire flight is cruise speed
        double vy1=climbRate*UnitConversion.ftToMeter/60;
        double vy2=Math.abs(descentRate)*UnitConversion.ftToMeter/60;
        double k1 = vy1/V;
        double k2= vy2/V;
        double D = route.getRouteLength(routeMode);
        double H = cruiseAlt*UnitConversion.ftToMeter;
        
        
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
            this.cruiseAlt = h_intersect/UnitConversion.ftToMeter;
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
        route.setTOCTOD(cruiseAlt*UnitConversion.ftToMeter, tocWP, tocWP+1, distTocToWP, true);
        route.setTOCTOD(cruiseAlt*UnitConversion.ftToMeter, todWP, todWP+1, distTodToWP, false);

        
        
        //Configura la altitud de los waypoints intermedios
        if(route.getNumWaypoints()>0){
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
                    wp.setAltitude(cruiseAlt*UnitConversion.ftToMeter);
                }  
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
    
    public int getFlightPhase()
    {
        // return-> 0: crucero, -1: descenso, 1 ascenso
        //If cruise Alt is 0, vertical profile not used, Always in cruise
        if(cruiseAlt==0)
        {
            return 0;
        }
  
        
        double dist_to_start = plane.getTraveled();
        
        
        double descAltError = Math.abs(TrafficSimulated.SAMPLE_TIME*descentRate)*UnitConversion.ftToMeter;
        
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
    

    
}
