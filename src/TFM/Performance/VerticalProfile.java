/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import TFM.Atmosphere.AtmosphericModel;
import TFM.utils.Constants;
import TFM.utils.UnitConversion;
import TFM.utils.Utils;
import classes.base.Coordinate;
import TFM.Routes.Route;
import classes.base.TrafficSimulated;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class VerticalProfile {
    private Map<String, FlightPhase> flightPhases;
    private AtmosphericModel atmosphericModel;
    private VerticalProfileSolver vps;
    private double simpleAverageClimbRate;
    private double simpleAverageDescentRate;
    private double cruiseAlt; //fts
    
    //Constructor without flightPhases uses the A320 vertical profile as default
    public VerticalProfile(AtmosphericModel atmosphericModel, VerticalProfileSolver vps){
        this.atmosphericModel = atmosphericModel;
        this.flightPhases = new LinkedHashMap<>();
        this.vps = vps;
        this.getA320VerticalProfile();
    }
    
    //Constructor without flightPhases uses the A320 vertical profile as default
    public VerticalProfile(AtmosphericModel atmosphericModel, Route route, int routeMode, TrafficSimulated plane){
        this.atmosphericModel = atmosphericModel;
        this.flightPhases = new LinkedHashMap<>();
        this.getA320VerticalProfile();
        //Calculate average climb rates and speed for simple approach to TOC and TOD
        this.calculateAverageClimbAndDescentRates();
        cruiseAlt = getCruiseAltitude();
        double cruiseTAS = atmosphericModel.calculateTAS(getFlightPhase("Cruise").getSpeed(), getFlightPhase("Cruise").getSpeedType(), cruiseAlt);
        route.setCruiseSpeed(cruiseTAS);
        
        this.vps = new VPSolver(cruiseAlt, simpleAverageClimbRate, simpleAverageDescentRate, route, routeMode, plane);
    }
    
    public VerticalProfile(Map<String, FlightPhase> phases) {
        flightPhases = phases;
    }
        
    public void addFlightPhase(String name, FlightPhase phase) {
        flightPhases.put(name, phase);
    }
    
    public FlightPhase getFlightPhase(String name) {
        return flightPhases.get(name);
    }
    
    private void getA320VerticalProfile() {
        this.addFlightPhase("Takeoff", new FlightPhase(FlightPhase.Type.TAKEOFF,145,"IAS", 0,FlightPhase.ConditionType.SPEED,145,Utils.ComparisonOperator.GREATER_THAN)); // 
        this.addFlightPhase("Initial Climb (to 5000ft)", new FlightPhase(FlightPhase.Type.CLIMB,175, "IAS",2500,FlightPhase.ConditionType.ALTITUDE,5000,Utils.ComparisonOperator.GREATER_THAN)); //
        this.addFlightPhase("Climb (to FL150)", new FlightPhase(FlightPhase.Type.CLIMB,290, "IAS",2200,FlightPhase.ConditionType.ALTITUDE,15000,Utils.ComparisonOperator.GREATER_THAN)); //
        this.addFlightPhase("Climb (to FL240)", new FlightPhase(FlightPhase.Type.CLIMB,290, "IAS",1400,FlightPhase.ConditionType.ALTITUDE,24000,Utils.ComparisonOperator.GREATER_THAN)); //
        this.addFlightPhase("Mach Climb", new FlightPhase(FlightPhase.Type.CLIMB,0.78, "MACH",1000, FlightPhase.ConditionType.ALTITUDE,30000,Utils.ComparisonOperator.GREATER_THAN)); //
        this.addFlightPhase("Cruise", new FlightPhase(FlightPhase.Type.CRUISE,0.79, "MACH",0,FlightPhase.ConditionType.TOD,0,Utils.ComparisonOperator.LESS_THAN)); //
        this.addFlightPhase("Initial Descent (to FL240)", new FlightPhase(FlightPhase.Type.DESCENT,0.78, "MACH",-1000,FlightPhase.ConditionType.ALTITUDE,24000,Utils.ComparisonOperator.LESS_THAN)); //
        this.addFlightPhase("Descent (to FL100)", new FlightPhase(FlightPhase.Type.DESCENT,290, "IAS",-3500,FlightPhase.ConditionType.ALTITUDE,10000,Utils.ComparisonOperator.LESS_THAN)); //
        this.addFlightPhase("Approach", new FlightPhase(FlightPhase.Type.DESCENT,250, "IAS",-1500,FlightPhase.ConditionType.ALTITUDE,100,Utils.ComparisonOperator.LESS_THAN)); //
        this.addFlightPhase("Landing", new FlightPhase(FlightPhase.Type.LANDING,137, "IAS",-0,FlightPhase.ConditionType.SPEED,0,Utils.ComparisonOperator.LESS_THAN)); //
    }
    
    /*
        Get the cruise altitude from the Flightphases dictionary as the higher 
        condition value of all the FlightPhase.Type.CLIMB
    */
    public double getCruiseAltitude() {
        double maxAltitude = 0;
        for (FlightPhase phase : flightPhases.values()) {
            if (phase.getType() == FlightPhase.Type.CLIMB && phase.getConditionType() == FlightPhase.ConditionType.ALTITUDE) {
                if (phase.getContidionValue() > maxAltitude) {
                    maxAltitude = phase.getContidionValue();
                }
            }
        }
        return maxAltitude;
    }
    
    public String checkFlightPhase(String cfp , double planeTAS, double geometricAltitude, double distanceToTOD, double distanceThreshold) {
        FlightPhase currentPhase = flightPhases.get(cfp);
        if (currentPhase != null) {
            // Check condition to transition to the next phase
            // For example, if current climb rate is 0, transition to next phase
            boolean isContidionMet = this.conditionMet(cfp, planeTAS, geometricAltitude, distanceToTOD, distanceThreshold);
            
            if (isContidionMet) {
                Iterator<Map.Entry<String, FlightPhase>> iterator = flightPhases.entrySet().iterator();
                boolean found = false;
                while (iterator.hasNext() && !found) {
                    Map.Entry<String, FlightPhase> entry = iterator.next();
                    if (entry.getKey().equals(cfp)) {
                        found = true;
                        if (iterator.hasNext()) {
                            cfp = iterator.next().getKey();                 
                            System.out.println("Transitioned to next phase: " + cfp);
                            return cfp;
                        } else {
                            System.out.println("No more phases available.");
                        }
                    }
                }
            } else {
                
            }
        } else {
            System.out.println("Invalid flight phase.");
        }
        return cfp;
    }
    
    /*
     This function checks if the condition switch to the vertical profile next flight phase is met.
    */
    private boolean conditionMet(String fp, double planeTAS, double geometricAltitude, double distanceToTOD, double distanceThreshold){
        FlightPhase flightPhase = flightPhases.get(fp);
        FlightPhase.ConditionType ContidionType = flightPhase.getConditionType();
        double conditionValue = flightPhase.getContidionValue();
        Utils.ComparisonOperator operator = flightPhase.getOperator();
        
        if(ContidionType ==  FlightPhase.ConditionType.ALTITUDE){
            return Utils.compare(geometricAltitude, conditionValue*UnitConversion.ftToMeter, operator);
            
        } else if(ContidionType ==  FlightPhase.ConditionType.SPEED){
            double conditionTAS;
            double fligthPhaseSpeed = flightPhase.getSpeed();
            String speedType = flightPhase.getSpeedType();
            conditionTAS = atmosphericModel.calculateTAS(fligthPhaseSpeed, speedType, geometricAltitude);
            
            return Utils.compare(planeTAS, conditionTAS, operator);
            
        }else if (ContidionType ==  FlightPhase.ConditionType.TOD){
            return Utils.compare(distanceToTOD, distanceThreshold, operator);
        } else {
            throw new RuntimeException("Unkown ContidionType "+ContidionType);
        }
    }
    
    /*
        Method to calculate the average climb and descent rate
    */
    public void calculateAverageClimbAndDescentRates() {
        double totalClimbRate = 0;
        int climbCount = 0;
        double totalDescentRate = 0;
        int descentCount = 0;

        for (FlightPhase phase : flightPhases.values()) {
            if (phase.getType() == FlightPhase.Type.CLIMB && phase.getClimbRate() != 0) {
                totalClimbRate += phase.getClimbRate();
                climbCount++;
            } else if (phase.getType() == FlightPhase.Type.DESCENT && phase.getClimbRate() != 0) {
                totalDescentRate += phase.getClimbRate();
                descentCount++;
            }
        }

        double averageClimbRate = (climbCount > 0) ? totalClimbRate / climbCount : 0;
        double averageDescentRate = (descentCount > 0) ? totalDescentRate / descentCount : 0;

        simpleAverageClimbRate = averageClimbRate;
        simpleAverageDescentRate = averageDescentRate;
        System.out.println("Average Climb Rate: " + averageClimbRate + " ft/min");
        System.out.println("Average Descent Rate: " + averageDescentRate + " ft/min");
    }
    
    public Map<String, FlightPhase> getFlightPhases() {
        return flightPhases;
    }

    public VerticalProfileSolver getVps() {
        return vps;
    }
     
}
