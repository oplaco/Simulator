/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Models;

/**
 * Smooth bearing strategy that simulates a more gradual turn towards the target bearing.
 * The turn starts with a low turning rate, accelerates to a maximum rate, and then 
 * decelerates as it approaches the target bearing.
 */
public class SmoothBearingStrategy implements BearingStrategy {
    private static final double MAX_TURNING_RATE = 3.0; // Maximum turning rate in degrees per second
    private static final double ACCELERATION_RATE = 0.5; // Acceleration rate in degrees per second^2
    private double currentTurningRate = 0.0; // Current turning rate in degrees per second

    @Override
    public double calculateBearing(double currentBearing, double targetBearing, long stepTime) {
        currentBearing = normalizeBearing(currentBearing);
        targetBearing = normalizeBearing(targetBearing);
        double bearingDifference = targetBearing - currentBearing;
        bearingDifference = normalizeBearing(bearingDifference);
        double turningThisStep = calculateTurningRate(bearingDifference, stepTime);
        double finalBearing;

        if (Math.abs(bearingDifference) < turningThisStep) {
            finalBearing = targetBearing;
        } else {
            finalBearing = normalizeBearing(currentBearing + Math.signum(bearingDifference) * turningThisStep);
        }

        // Validar el resultado para asegurarse de que no es NaN
        if (Double.isNaN(finalBearing)) {
            throw new IllegalArgumentException("El cálculo del bearing resultó en NaN.");
        }

        return finalBearing;
    }

    private double calculateTurningRate(double bearingDifference, long stepTime) {
        // Paso de tiempo en segundos
        double stepTimeSeconds = stepTime / 1000.0;

        // Calcular la distancia de desaceleración
        double decelerationDistance = (MAX_TURNING_RATE * MAX_TURNING_RATE) / (2 * ACCELERATION_RATE);

        if (Math.abs(bearingDifference) <= decelerationDistance) {
            // Desacelerar para detenerse suavemente
            currentTurningRate = Math.max(0, currentTurningRate - ACCELERATION_RATE * stepTimeSeconds);
        } else if (currentTurningRate < MAX_TURNING_RATE) {
            // Acelerar hasta la tasa de giro máxima
            currentTurningRate = Math.min(MAX_TURNING_RATE, currentTurningRate + ACCELERATION_RATE * stepTimeSeconds);
        } else {
            // Mantener la tasa de giro constante
            currentTurningRate = MAX_TURNING_RATE;
        }

        return currentTurningRate * stepTimeSeconds;
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

