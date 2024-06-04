/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.Models;

import TFM.AircraftControl.TrafficSimulated;

/**
 *
 * @author Gabriel
 */
public interface BearingStrategy {
    public double calculateBearing(double currentBearing , double targetBearing, long stepTime);
}
