/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class AircraftSpecifications {
    private Map<String, FlightPhase> flightPhases;
    private double wingSpan; // m
    private double length; // m
    private double height; // m
    
    //Empty constructor method uses the A320 specs as default
    public AircraftSpecifications() {
        flightPhases = new HashMap<>();
        this.getA320Specifications();
    }
    
    public AircraftSpecifications(Map<String, FlightPhase> phases) {
        flightPhases = phases;
    }
    
    public void addFlightPhase(String name, FlightPhase phase) {
        flightPhases.put(name, phase);
    }
    
    public FlightPhase getFlightPhase(String name) {
        return flightPhases.get(name);
    }
    
    // Other methods...
    
    public  void getA320Specifications() {
        this.addFlightPhase("Takeoff", new FlightPhase(145,"IAS", 0)); // 
        this.addFlightPhase("Initial Climb", new FlightPhase(175, "IAS",2500)); //
        this.addFlightPhase("Climb (to FL150)", new FlightPhase(290, "IAS",2200)); //
        this.addFlightPhase("Climb (to FL240)", new FlightPhase(290, "IAS",1400)); //
        this.addFlightPhase("Mach Climb", new FlightPhase(0.78, "MACH",1000)); //
        this.addFlightPhase("Cruise", new FlightPhase(0.79, "MACH",0)); //
        this.addFlightPhase("Initial Descent (to FL240)", new FlightPhase(0.78, "MACH",-1000)); //
        this.addFlightPhase("Descent (to FL100)", new FlightPhase(290, "IAS",-3500)); //
        this.addFlightPhase("Approach", new FlightPhase(250, "IAS",-1500)); //
        this.addFlightPhase("Landing", new FlightPhase(137, "IAS",-0)); //
    
    }
}