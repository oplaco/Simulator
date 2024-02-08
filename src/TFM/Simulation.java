package TFM;

import TFM.simulationEvents.SimulationEvent;
import classes.base.Coordinate;
import classes.base.Pilot;
import classes.base.Route;
import classes.base.TrafficSimulated;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Gabriel
 */

// Singleton 

public class Simulation extends Thread {
    private static Simulation instance;
    private long simulationTime = 0; // Start at 0 milliseconds
    private double speed = 1.0;   // Speed factor (1x, 2x, etc.)
    private boolean running = false; // Control flag for the simulation
    private long lastUpdateTime = 0; // To track the last update time
    private volatile boolean stopSimulation = false; // Flag to indicate if the simulation should stop

    private TimeUpdateListener timeUpdateListener; // Listener to notify of time updates
    private ExecuteCommandListener executeCommandListener;  // Listener to notify when a command is executed
    
    private TrafficSimulationMap trafficSimulationMap;
    private ConcurrentHashMap<String, Pilot> pilotMap;
    
    //private TrafficMap
    private List<SimulationEvent> events;

  
    public Simulation() {
        this.start();
    }

    public Simulation(List<SimulationEvent> events) {
        this.events = events;
        this.pilotMap = new ConcurrentHashMap<String, Pilot>();
        this.start();
    }
    
    public Simulation(List<SimulationEvent> events, TrafficSimulationMap trafficSimulationMap) {
        this.events = events;
        this.trafficSimulationMap = trafficSimulationMap;
        this.pilotMap = new ConcurrentHashMap<String, Pilot>();
        this.start();
    }
    // ... other fields and methods ...

    public void start() {
        super.start(); // Start the thread
    }
    
    public static synchronized Simulation getInstance() {
        if (instance == null) {
            instance = new Simulation();
        }
        return instance;
    }
        
    // Time control methods
    public synchronized void play() {
        running = true;
        this.lastUpdateTime = System.currentTimeMillis(); // Reset the last update time
        
    }

    public synchronized void pause() {
        running = false;
    }

    public synchronized void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public synchronized void doubleSpeed() {
        this.speed = 2*this.speed;
    }
    
    public synchronized void stopit() {
        System.out.println("Stopping Simulation");
        stopSimulation = true; // Set the stopSimulation flag to true
        try {
            // Wait for the thread to finish
            join();
        } catch (InterruptedException e) {
            // Handle the InterruptedException if needed
        }
    }

    public void run() {
        this.lastUpdateTime = System.currentTimeMillis(); // Initialize the last update time
        while (!stopSimulation) {
            if (running) {
                long currentTime = System.currentTimeMillis();
                long elapsedRealTime = currentTime - lastUpdateTime;

                // Calculate the amount of "simulation time" to advance
                long elapsedSimulationTime = (long) (elapsedRealTime * speed);

                // Update the simulation time
                simulationTime += elapsedSimulationTime;

                // Perform simulation updates (e.g., move aircraft, update conditions)
                updateSimulation(elapsedSimulationTime);
                //System.out.println("Simulation Time: " + simulationTime);
                
                //Loop trough the simulation events (if any) and execute the commands. Remove the used ones to avoid iterating again trough them.
                Iterator<SimulationEvent> iterator = events.iterator();
                while (iterator.hasNext()) {
                    SimulationEvent event = iterator.next();
                    if (event.getTime() <= simulationTime) {
                        try{
                            executeCommand(event);
                            this.executeCommandListener.onCommandExecuted(event.toString(), "normal");
                        }catch (IllegalArgumentException e) {
                            this.executeCommandListener.onCommandExecuted(e.toString(), "error");
                        }
                        iterator.remove(); // Remove the executed event from the list
                    }
                }

                // Update lastUpdateTime to the current time
                lastUpdateTime = currentTime;
            }

            // Sleep for a short duration
            try {
                Thread.sleep(100); // Adjust as needed
            } catch (InterruptedException e) {
                // Handle interrupted exception
            }
        }
    }

    
    // Update the state of the simulation based on the current time
    private void updateSimulation(long elapsedSimulationTime) {
        // Loop trhough each pilot to update each aircraft.
        Iterator<Entry<String, Pilot>> new_Iterator
            = this.pilotMap.entrySet().iterator();
        // Iterating every set of entry in the HashMap
        while (new_Iterator.hasNext()) {
            Map.Entry<String, Pilot> new_Map
                = (Map.Entry<String, Pilot>)
                      new_Iterator.next();
            Pilot pilot = new_Map.getValue();

            pilot.update(this.simulationTime-elapsedSimulationTime);
            
            //Update the traffic in the trafficDisplayer (i.e. the renderableLayer)
            this.trafficSimulationMap.updateTraffic(pilot.getPlane());
        }
        
        // Notify the listener of the time update
        if (timeUpdateListener != null) {
            timeUpdateListener.onTimeUpdate(simulationTime);
        }
    }
    
    private void executeCommand(SimulationEvent event) {
        String command = event.getCommand();
        switch (command) {
            case "CREATE":
                // Handle Command logic
                System.out.println("Create event at: " + event.getTime() + ".Variables: " + event.getVariables());
                Map<String, String> variables = event.getVariables();
                
                TrafficSimulated.TrafficSimulatedBuilder builder = new TrafficSimulated.TrafficSimulatedBuilder();
                // Assuming event.variables is a Map of some sort
                if (variables.containsKey("ICAO")) {
                    String icaoCode = variables.get("ICAO");
                    if (this.trafficSimulationMap.get(icaoCode)==null){
                        builder.setHexCode(icaoCode);
                    }else{
                        throw new IllegalArgumentException("ICAO code " + icaoCode + " already exists in the simulation.");
                    }
                    
                }
                if (variables.containsKey("lat") & variables.containsKey("lat")) {
                    double lat = Double.parseDouble(variables.get("lat"));
                    double lon = Double.parseDouble(variables.get("lon"));
                    builder.setPosition(new Coordinate("Origin",lat,lon));
                }
                if (variables.containsKey("lat") & variables.containsKey("lat") & variables.containsKey("alt")) {
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
                
                this.trafficSimulationMap.putTraffic( traffic);
                
                //Create Pilot for the newly created aircraft.
                Pilot pilot = new Pilot(new Route(),traffic,TrafficSimulated.FLY_LOXODROMIC);
                this.pilotMap.put(traffic.getHexCode(), pilot);
                
                break;

            case "UPDATE":
                // Handle Command2 logic
                System.out.println("Update event at: " + event.getTime() + " " + ".Variables: " + event.getVariables());
                break;
                
            default:
                System.out.println("Default case command");
                break;
        }
    }
    // ... additional methods ...
    
    public long getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(long simulationTime) {
        this.simulationTime = simulationTime;
    }

    public List<SimulationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<SimulationEvent> events) {
        this.events = events;
    }
    
    public void setTimeUpdateListener(TimeUpdateListener listener) {
        this.timeUpdateListener = listener;
    }

    public void setExecuteCommandListener(ExecuteCommandListener executeCommandListener) {
        this.executeCommandListener = executeCommandListener;
    }
}
