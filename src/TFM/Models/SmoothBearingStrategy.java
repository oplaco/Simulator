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

    @Override
    public double calculateBearing(double currentBearing, double targetBearing, long stepTime) {
        double turningThisStep = calculateTurningRate(currentBearing, targetBearing, stepTime);
        double bearingDifference = targetBearing - currentBearing;
        bearingDifference = normalizeBearing(bearingDifference);
        double finalBearing;

        if (Math.abs(bearingDifference) < turningThisStep) {
            finalBearing = targetBearing;
        } else {
            finalBearing = normalize360(currentBearing + Math.signum(bearingDifference) * turningThisStep);
        }

        // Validar el resultado para asegurarse de que no es NaN
        if (Double.isNaN(finalBearing)) {
            throw new IllegalArgumentException("El cálculo del bearing resultó en NaN.");
        }

        return finalBearing;
    }

    private double calculateTurningRate(double currentBearing, double targetBearing, long stepTime) {
        // Calcula la tasa de giro máxima posible para este paso de tiempo
        double maxTurningThisStep = MAX_TURNING_RATE * stepTime / 1000.0;

        // Evita valores negativos o NaN
        if (maxTurningThisStep < 0 || Double.isNaN(maxTurningThisStep)) {
            throw new IllegalArgumentException("La tasa de giro calculada es inválida.");
        }

        // Considerar la aceleración
        double accelerationThisStep = ACCELERATION_RATE * stepTime / 1000.0;
        
        // Evita valores negativos o NaN
        if (accelerationThisStep < 0 || Double.isNaN(accelerationThisStep)) {
            throw new IllegalArgumentException("La aceleración calculada es inválida.");
        }

        // Si estamos cerca del destino, empezamos a desacelerar
        double bearingDifference = normalizeBearing(targetBearing - currentBearing);
        double decelerationDistance = maxTurningThisStep * maxTurningThisStep / (2 * ACCELERATION_RATE);

        if (Math.abs(bearingDifference) <= decelerationDistance) {
            // Desacelerar para detenerse suavemente
            double requiredDecelerationRate = Math.sqrt(2 * ACCELERATION_RATE * Math.abs(bearingDifference));
            return Math.min(maxTurningThisStep, requiredDecelerationRate);
        } else {
            // Acelerar hasta la tasa de giro máxima
            return Math.min(maxTurningThisStep, accelerationThisStep);
        }
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


