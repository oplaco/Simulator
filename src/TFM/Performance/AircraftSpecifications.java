/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import TFM.Atmosphere.AtmosphericModel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Gabriel Alfonsín Espín
 * Class for potentially store diferent Aircraft Specifivations.
 */
public class AircraftSpecifications {
    private double takeoffAcc;
    private double landingAcc;
    private double wingSpan; // m
    private double length; // m
    private double height; // m
    
    public AircraftSpecifications() {
        this.takeoffAcc = 2.5;
        this.landingAcc = -5;
    }
    
    public double getWingSpan() {
        return wingSpan;
    }

    public double getLength() {
        return length;
    }

    public double getHeight() {
        return height;
    } 

    public double getTakeoffAcc() {
        return takeoffAcc;
    }

    public double getLandingAcc() {
        return landingAcc;
    } 
}