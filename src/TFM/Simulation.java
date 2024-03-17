package TFM;

import TFM.simulationEvents.Command;
import TFM.simulationEvents.CommandFactory;
import TFM.simulationEvents.SimulationEvent;
import classes.base.Coordinate;
import classes.base.Pilot;
import classes.base.Route;
import classes.base.TrafficSimulated;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Gabriel Alfonsín Espín 
 */

// Singleton 

public class Simulation extends Thread {
    private static Simulation instance;
    private long simulationTime = 0; // Start at 0 milliseconds
    private double speed = 1.0;   // Speed factor (1x, 2x, etc.)
    private boolean running = false; // Control flag for the simulation
    private long lastUpdateTime = 0; // To track the last update time
    private long elapsedSimulationTime;
    private TrafficDisplayer trafficDisplayer;
    
    private volatile boolean stopSimulation = false; // Flag to indicate if the simulation should stop

    private TimeUpdateListener timeUpdateListener; // Listener to notify of time updates
    private ExecuteCommandListener executeCommandListener;  // Listener to notify when a command is executed
    
    private TrafficSimulationMap trafficSimulationMap;
    private ConcurrentHashMap<String, Pilot> pilotMap;
    
    //private TrafficMap
    private List<SimulationEvent> events;

    //conversion
    static public double ftToMeter = 0.3048;
    static public double knotToMs = 0.514444;
    static public double meterToNM = 1/1852;
    static public double ftMinToMs = 1/196.85039370078738;
    
    public Simulation() {
        this.start();
    }

    public Simulation(List<SimulationEvent> events) {
        this.events = events;
        this.pilotMap = new ConcurrentHashMap<String, Pilot>();
        this.start();
    }
    
    public Simulation(List<SimulationEvent> events, TrafficDisplayer trafficDisplayer) {
        this.events = events;
        this.trafficSimulationMap = trafficDisplayer.getTrafficSimulationmap();
        this.trafficDisplayer = trafficDisplayer;
        this.pilotMap = new ConcurrentHashMap<String, Pilot>();
        this.start();
    }

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
        
        // Resume each pilot
        forEachPilot(Pilot::resumeThread);
    }

    public synchronized void pause() {
        running = false;
        
        // Pause each pilot
        forEachPilot(Pilot::pauseThread);
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
                elapsedSimulationTime = (long) (elapsedRealTime * speed);

                // Update the simulation time
                simulationTime += elapsedSimulationTime;

                // Perform simulation updates (e.g., move aircraft, update conditions)
                updateSimulation();
                
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
    private void updateSimulation() {
        // Loop trhough each pilot to update each aircraft.
        Iterator<Entry<String, Pilot>> new_Iterator
            = this.pilotMap.entrySet().iterator();
        // Iterating every set of entry in the HashMap
        while (new_Iterator.hasNext()) {
            Map.Entry<String, Pilot> new_Map
                = (Map.Entry<String, Pilot>)
                      new_Iterator.next();
            Pilot pilot = new_Map.getValue();
            
            //Update the traffic in the trafficDisplayer (i.e. the renderableLayer)
            this.trafficSimulationMap.updateTraffic(pilot.getPlane());
        }
        
        // Notify the listener of the time update
        if (timeUpdateListener != null) {
            timeUpdateListener.onTimeUpdate(simulationTime);
        }
    }
    
    private void executeCommand(SimulationEvent event) {
    String commandStr = event.getCommand();
        try {
            Command command = CommandFactory.getCommand(commandStr);
            command.execute(this,event);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
      
    private void forEachPilot(Consumer<Pilot> action) {
        for (Map.Entry<String, Pilot> entry : pilotMap.entrySet()) {
            Pilot pilot = entry.getValue();
            action.accept(pilot);
        }
    }
   
    public long getSimulationTime() {
        return simulationTime;
    }

    public double getSpeed() {
        return speed;
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

    public TrafficSimulationMap getTrafficSimulationMap() {
        return trafficSimulationMap;
    }
    
    public ConcurrentHashMap<String, Pilot> getPilotMap() {
        return pilotMap;
    }

    public long getElapsedSimulationTime() {
        return elapsedSimulationTime;
    }

    public TrafficDisplayer getTrafficDisplayer() {
        return trafficDisplayer;
    }
          
}
