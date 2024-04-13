/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import TFM.utils.Utils.ComparisonOperator;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class FlightPhase {
    private Type type;
    private double speed; //
    private String speedType; // IAS, EAS, TAS or mach.
    private double climbRate; // Climb rate in ft/min
    private ConditionType conditionType; // Condition type necessary to be met to switch to next phase (e.g altitude (ft) reached)
    private double contidionValue; // Condition necessary to be met to switch to next phase (e.g. 10000 ft)
    private ComparisonOperator operator; // (e.g "GREATER")
    
    public enum Type {
        TAXI,
        TAKEOFF,
        CLIMB,
        CRUISE,
        DESCENT,
        LANDING
    }
    
    public enum  ConditionType{
        SPEED,
        ALTITUDE,
        TOC,
        TOD
    }
    
    
    public FlightPhase(Type type,double speed, String speedType, double climbRate, ConditionType conditionType, double contidionValue, ComparisonOperator operator) {
        this.type = type;
        this.speed = speed;
        this.speedType = speedType;
        this.climbRate = climbRate;
        this.conditionType = conditionType;
        this.contidionValue = contidionValue;
        this.operator = operator;
    }

    public Type getType() {
        return type;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public double getClimbRate() {
        return climbRate;
    }

    public String getSpeedType() {
        return speedType;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public double getContidionValue() {
        return contidionValue;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}
