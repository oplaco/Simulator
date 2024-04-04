/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class FlightPhase {
    private double speed; //
    private String speedType; // IAS, EAS, TAS or mach.
    private double climbRate; // Climb rate in ft/min
    
    public FlightPhase(double speed, String speedType, double climbRate) {
        this.speed = speed;
        this.speedType = speedType;
        this.climbRate = climbRate;
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

}
