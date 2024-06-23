/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Models;

import TFM.Core.TrafficSimulated;

/**
 *
 * @author Gabriel Alfonsín Espín
 * 
 * Simple bearing strategy that assumes the standard rate turn, also known as rate 
 * one turn (ROT). A santard rate turn is defined as 3º per second turn, which 
 * completes a 360º turn in 2 minutes.
 */
public class SimpleBearingStrategy implements BearingStrategy {
    @Override
    public double calculateBearing(double currentBearing , double targetBearing, long stepTime) {
        double turningRate = 3.0; // Default turning rate
        double turningThisStep = turningRate * stepTime/1000; 
        double bearingDifference = targetBearing - currentBearing;
        bearingDifference = normalizeBearing(bearingDifference);
        double finalBearing;
        
        if (Math.abs(bearingDifference) < turningThisStep) {
            finalBearing = targetBearing;
        } else {
            finalBearing = normalize360(currentBearing + Math.signum(bearingDifference) * turningThisStep);
        }
        return finalBearing;
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

