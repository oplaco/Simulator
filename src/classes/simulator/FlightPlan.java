package classes.simulator;
import TFM.Atmosphere.InternationalStandardAtmosphere;
import TFM.Performance.VerticalProfile;
import classes.base.TrafficSimulated;
import classes.base.Pilot;
import TFM.Routes.Route;
import classes.base.TrafficSimulatedListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms
 */
public class FlightPlan extends Thread {

    private String name;
    private String routeFileName;
    private double speed;
    private Route route;
    private TrafficSimulated plane;
    private Pilot pilot;
    private TrafficSimulatedListener tsl;
    private VerticalProfile verticalProfile;
    
//    public FlightPlan(String name, String routeFileName) {
//        this.name = name;
//        this.speed = 100; // default
//        try {
//            route = new Route(routeFileName, this.speed);
//        } catch (IOException ex) {
//            Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        this.tsl = null;
//    }
//
//    public FlightPlan(String name, String routeFileName, double speed) {
//        this.name = name;
//        try {
//            route = new Route(routeFileName, speed);
//        } catch (IOException ex) {
//            Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        this.speed = speed;
//        this.tsl = null;
//    }
//
    public FlightPlan(String name, String routeFileName, TrafficSimulatedListener tsl) {
        this.name = name;
        this.speed = 100; // default
        try {
            route = new Route(routeFileName, this.speed);
        } catch (IOException ex) {
            Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tsl = tsl;
    }
//
//    public FlightPlan(String name, String routeFileName, double speed, TrafficSimulatedListener tsl) {
//        this.name = name;
//        try {
//            route = new Route(routeFileName, speed);
//        } catch (IOException ex) {
//            Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        this.speed = speed;
//        this.tsl= tsl;
//    }

    public FlightPlan(String name, Route route, TrafficSimulated plane, Pilot pilot) {
        this.name = name;
        this.route = route;
        this.plane = plane;
        this.pilot = pilot;
        this.verticalProfile = new VerticalProfile(new InternationalStandardAtmosphere(),route, pilot.getRouteMode(),plane);
    }
    
    @Override
    public void run() {
        // El nombre del avión será el nombre del plan + : + PLANE
        plane = new TrafficSimulated(name + ": PLANE", route.getDeparture(), speed, 0, tsl);
       
        //Crea un piloto para este avion
        pilot = new Pilot(route, plane, TrafficSimulated.FLY_ORTHODROMIC);
        
        //Si se ha configurado perfil vertical, añadelo al piloto
//        if(cruiseAlt!=0)
//        {
//            pilot.setVerticalProfile(cruiseAlt, climbRate, descentRate);
//        }
        if (tsl != null){
            // desactivamos los mensajes del piloto si hay listener
            pilot.verboseOFF();
        }
        pilot.start();
        try {
            pilot.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("FLIGHT PLAN " + name + " FINISHED");
    }
    
    public void stopit()
    {
        try
        {   
            //Para primero el avión y luego al piloto
            plane.stoppit();
            pilot.join();     
        }catch(Exception ex)
        {
             Logger.getLogger(FlightPlan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Route getRoute()
    {
        return route;
    }
    public TrafficSimulated getPlane()
    {
        return plane;
    }
    
    public String getPlanName()
    {
        return this.name;
    }
    public Pilot getPilot()
    {
        return pilot;
    }
}
