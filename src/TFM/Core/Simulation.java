package TFM.Core;

import TFM.Others.TrafficSimulationMap;
import TFM.GUI.TrafficDisplayer;
import TFM.GUI.ExecuteCommandListener;
import TFM.GUI.TimeUpdateListener;
import TFM.Atmosphere.AtmosphericModel;
import TFM.Atmosphere.InternationalStandardAtmosphere;
import TFM.simulationEvents.Command;
import TFM.simulationEvents.CommandFactory;
import TFM.simulationEvents.SimulationEvent;
import TFM.Models.BearingStrategy;
import TFM.Models.NoBearingStrategy;
import TFM.Models.SimpleBearingStrategy;
import TFM.Models.SmoothBearingStrategy;
import TFM.Routes.DijkstraAlgorithm;
import TFM.Routes.PathfindingAlgorithm;
import TFM.TCAS.RA_Solver;
import TFM.TCAS.ResolutionAdvisorySolver;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Gabriel Alfonsín Espín 
 */

public class Simulation extends Thread {
    private long simulationTime = 0; // Start at 0 milliseconds
    private int sleepTime = 100;
    private double speed = 1.0;   // Speed factor (1x, 2x, etc.)
    private boolean running = false; // Control flag for the simulation
    private long lastUpdateTime = 0; // To track the last update time
    private long elapsedSimulationTime;
    private volatile boolean stopSimulation = false; // Flag to indicate if the simulation should stop
        
    private TrafficDisplayer trafficDisplayer;

    private TimeUpdateListener timeUpdateListener; // Listener to notify of time updates
    private ExecuteCommandListener executeCommandListener;  // Listener to notify when a command is executed
    
    //Traffic and Pilot maps
    private TrafficSimulationMap trafficSimulationMap;
    private ConcurrentHashMap<String, Pilot> pilotMap;
    
    //Commands 
    private List<SimulationEvent> events;
    
    //Models
    private AtmosphericModel atm;
    private PathfindingAlgorithm pathfindingAlgorithm;
    private BearingStrategy bearingStrategy;
    private ResolutionAdvisorySolver RASolver;
            
    public Simulation(List<SimulationEvent> events, TrafficDisplayer trafficDisplayer) {
        this.events = events;
        this.trafficSimulationMap = trafficDisplayer.getTrafficSimulationmap();
        this.trafficDisplayer = trafficDisplayer;
        this.pilotMap = new ConcurrentHashMap<String, Pilot>();
        this.setModels();
        this.start();
    }
    
    /**
     * Sets the models used in the simulation.
     * <p>
     * This method initializes various models used within the simulator. Each model is assigned
     * a specific implementation, but these can be easily swapped out for different implementations to change
     * the behavior of the simulation.
     * </p>
     * <p>
     * The current models set in this method include:
     * </p>
     * <ul>
     *     <li><b>Atmospheric Model:</b> {@link InternationalStandardAtmosphere} - This model simulates the 
     *     international standard atmosphere conditions.</li>
     *     <li><b>Pathfinding Algorithm:</b> {@link DijkstraAlgorithm} - This algorithm is used for finding 
     *     the shortest path in a network, typically used in route planning.</li>
     *     <li><b>Bearing Strategy:</b> {@link SmoothBearingStrategy} - This strategy determines how the aircraft 
     *     turns to adjust its bearing gradually towards a target bearing, simulating a smooth turn.</li>
     *     <li><b>Resolution Advisory Solver:</b> {@link RA_Solver} - This solver provides a mechanism to 
     *     resolve conflicts and advisories in flight paths, ensuring safe navigation.</li>
     * </ul>
     * <p>
     * To change the behavior of the simulator, different implementations of these interfaces can be provided.
     * For example, one could implement a different bearing strategy or pathfinding algorithm and simply 
     * replace the current implementation in this method.
     * </p>
     */
    private void setModels() {
        this.atm = new InternationalStandardAtmosphere(); // Atmospheric model
        this.pathfindingAlgorithm = new DijkstraAlgorithm(); // Pathfinding algorithm
        this.bearingStrategy = new SimpleBearingStrategy(); // Bearing strategy
        this.RASolver = new RA_Solver(); // Resolution advisory solver
    }


    @Override
    public void start() {
        super.start(); // Start the thread
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

    @Override
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
                Thread.sleep(sleepTime); // Adjust as needed
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

    public AtmosphericModel getAtm() {
        return atm;
    }

    public PathfindingAlgorithm getPathfindingAlgorithm() {
        return pathfindingAlgorithm;
    }

    public BearingStrategy getBearingStrategy() {
        return bearingStrategy;
    }

    public ResolutionAdvisorySolver getRASolver() {
        return RASolver;
    }  
}
