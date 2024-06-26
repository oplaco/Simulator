/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Models;

/**
 *
 * @author Gabriel
 */
public class NoBearingStrategy implements BearingStrategy {

    @Override
    public double calculateBearing(double currentBearing, double targetBearing, long stepTime) {
        return normalizeBearing(normalize360(targetBearing));
    }
    
        private double normalizeBearing(double bearing) {
        if (bearing > 180) {
            return bearing - 360;
        } else if (bearing < -180) {
            return bearing + 360;
        }
        return bearing;
    }

    private double normalize360(double bearing) {
        return (bearing + 360) % 360;
    }
    
}
