/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;
import TFM.Atmosphere.InternationalStandardAtmosphere;
import TFM.Performance.VerticalProfile;
import TFM.Simulation;
import classes.base.Coordinate;
import classes.base.Pilot;
import classes.base.Route;
import classes.base.TrafficSimulated;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class CreateCommand implements Command {
    @Override
    public void execute(Simulation simulation, SimulationEvent event) {
        String command = event.getCommand();
        Map<String, String> variables = event.getVariables();
        TrafficSimulated.TrafficSimulatedBuilder builder = new TrafficSimulated.TrafficSimulatedBuilder();
        
        builder.setSimulation(simulation);

        if (variables.containsKey("ICAO")) {
            String icaoCode = variables.get("ICAO");
            if (simulation.getTrafficSimulationMap().get(icaoCode)==null){
                builder.setHexCode(icaoCode);
            }else{
                throw new IllegalArgumentException("ICAO code " + icaoCode + " already exists in the simulation.");
            }                   
        }
        if (variables.containsKey("lat") & variables.containsKey("lon")) {
            double lat = Double.parseDouble(variables.get("lat"));
            double lon = Double.parseDouble(variables.get("lon"));
            builder.setPosition(new Coordinate("Origin",lat,lon));
        }
        if (variables.containsKey("lat") & variables.containsKey("lon") & variables.containsKey("alt")) {
            double lat = Double.parseDouble(variables.get("lat"));
            double lon = Double.parseDouble(variables.get("lon"));
            double altitude = Double.parseDouble(variables.get("alt"));
            builder.setPosition(new Coordinate("Origin",lat,lon,altitude));
        }
        if (variables.containsKey("bea")) {
            double course = Double.parseDouble(variables.get("bea"));
            builder.setCourse(course);
        }else {builder.setCourse(0);}
        if (variables.containsKey("spd")) {
            double traffic_speed = Double.parseDouble(variables.get("spd"));
            builder.setSpeed(traffic_speed);
        } else {builder.setSpeed(100);}
        if (variables.containsKey("tlat") & variables.containsKey("tlon")) {
            double tlat = Double.parseDouble(variables.get("tlat"));
            double tlon = Double.parseDouble(variables.get("tlon"));
            builder.setTarget(new Coordinate("Target",tlat,tlon));
        } else {
            builder.setTarget(new Coordinate("DefaultTarget",0,0));
        }

        TrafficSimulated traffic = builder.build();

        Route route;
        if (variables.containsKey("route")){

        try {
            String currentDirectory = System.getProperty("user.dir");
            String path = currentDirectory+File.separator+"src"+File.separator+"routes"+File.separator+variables.get("route")+".txt";
            route = new Route(path);
        } catch (IOException ex) {
            System.out.println("Error reading file: " + ex.getMessage());
            route = new Route();
        }

        }else{
            route = new Route();
        }
        simulation.getTrafficSimulationMap().putTraffic( traffic);

        VerticalProfile vp = new VerticalProfile(new InternationalStandardAtmosphere(),route,TrafficSimulated.FLY_ORTHODROMIC,traffic);
        
        //Create Pilot for the newly created aircraft.
        Pilot pilot = new Pilot(route,traffic,TrafficSimulated.FLY_ORTHODROMIC,simulation, vp);
        // Set the FlightPhase to the first one.
        pilot.changeToNextPhase();
        //pilot.initPhaseFlight();
        pilot.start();
        simulation.getPilotMap().put(traffic.getHexCode(), pilot);
        
        
        System.out.println("CREATE COMMAND EXECUTED !!");
    }
}
