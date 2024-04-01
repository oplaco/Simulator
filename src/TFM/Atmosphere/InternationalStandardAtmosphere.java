/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Atmosphere;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public class InternationalStandardAtmosphere implements AtmosphericModel{
    // Define base constants
    private static final double SEA_LEVEL_TEMPERATURE = 288.15; // Kelvin
    private static final double SEA_LEVEL_PRESSURE = 101325; // Pascals
    private static final double EARTH_GRAVITY = 9.80665; // m/s^2
    private static final double AIR_GAS_CONSTANT = 287.05; // J/(kg·K)
    private static final double EARTH_RADIUS =  6356766;// nominal spherical earth radius (Meter)
    
    // Base geopotential altitude AMSL
    private static final double TROPOPAUSE_ALTITUDE = 11000; // Initial Geopotential Altitude (Meter)
    private static final double STRATOSPHERE_1_ALTITUDE = 20000; // Initial Geopotential Altitude (Meter)
    private static final double STRATOSPHERE_2_ALTITUDE = 32000; // Initial Geopotential Altitude (Meter)
    private static final double STRATOPAUSE_ALTITUDE = 47000; // Initial Geopotential Altitude (Meter)
    private static final double MESOSPHERE_1_ALTITUDE = 51000; // Initial Geopotential Altitude (Meter)
    private static final double MESOSPHERE_2_ALTITUDE = 71000; // Initial Geopotential Altitude (Meter)
    private static final double MESOPAUSE_ALTITUDE = 84852; // Initial Geopotential Altitude (Meter)    
    
    private double calculateGeopotentialAltitude(double GeometricAltitdue){
        return EARTH_RADIUS*GeometricAltitdue/(EARTH_RADIUS+GeometricAltitdue);
    }
    
    private double[] getBaseParameters(double GeopotentialAltitude){
        double[] baseParameters = new double[5];
        double lapseRate = -6.5;
        double baseTemperature = 288.15; 
        double basePressure = 101325;
        double baseDensity =  1.225;
        double baseAltitude = 0; // Base geopotential altitude (m)
        
        if (GeopotentialAltitude>=0 && GeopotentialAltitude<TROPOPAUSE_ALTITUDE){
            lapseRate =-6.5;
            baseTemperature = SEA_LEVEL_TEMPERATURE;
            basePressure = SEA_LEVEL_PRESSURE;
            baseDensity = 1.225;
            baseAltitude = 0;
        } else if(GeopotentialAltitude>=TROPOPAUSE_ALTITUDE && GeopotentialAltitude<STRATOSPHERE_1_ALTITUDE){
            lapseRate = 0;
            baseTemperature = 216.65;
            basePressure = 22632;
            baseDensity = 0.3639;
            baseAltitude = TROPOPAUSE_ALTITUDE;
        } else if(GeopotentialAltitude>=STRATOSPHERE_1_ALTITUDE && GeopotentialAltitude<STRATOSPHERE_2_ALTITUDE){
            lapseRate = 1;
            baseTemperature = 216.65;
            basePressure = 5474.9;
            baseDensity = 0.0880;
            baseAltitude = STRATOSPHERE_1_ALTITUDE;
        } else if(GeopotentialAltitude>=STRATOSPHERE_2_ALTITUDE && GeopotentialAltitude<STRATOPAUSE_ALTITUDE){
            lapseRate = 2.8;
            baseTemperature = 228.65;
            basePressure = 868.02;
            baseDensity = 0.0132;
            baseAltitude = STRATOSPHERE_2_ALTITUDE;
        } else if(GeopotentialAltitude>=STRATOPAUSE_ALTITUDE && GeopotentialAltitude<MESOSPHERE_1_ALTITUDE){
            lapseRate = 0;
            baseTemperature = 270.65;
            basePressure = 110.91;
            baseDensity = 0.0014;
            baseAltitude = STRATOPAUSE_ALTITUDE;
        } else if(GeopotentialAltitude>=MESOSPHERE_1_ALTITUDE && GeopotentialAltitude<MESOSPHERE_2_ALTITUDE){
            lapseRate = -2.8;
            baseTemperature = 270.65;
            basePressure = 66939;
            baseDensity = 0.0009;
            baseAltitude = MESOSPHERE_1_ALTITUDE;
        } else if(GeopotentialAltitude>=MESOSPHERE_2_ALTITUDE && GeopotentialAltitude<MESOPAUSE_ALTITUDE){
            lapseRate = -2;
            baseTemperature = 214.65;
            basePressure = 3.9564;
            baseDensity = 0.0001;
            baseAltitude = MESOSPHERE_2_ALTITUDE;
        } 
        
        lapseRate = lapseRate*Math.pow(10, -3);
        baseParameters[0] = lapseRate;
        baseParameters[1] = baseTemperature;
        baseParameters[2] = basePressure;
        baseParameters[3] = baseDensity;
        baseParameters[4] = baseAltitude;
        
        return baseParameters; //(K/m)
    }
    

    @Override
    public double calculatePressure(double GeometricAltitdue) {
        double geopotentialAltitude = calculateGeopotentialAltitude(GeometricAltitdue);
        double[] baseParameters = getBaseParameters(geopotentialAltitude);
        double lapseRate = baseParameters[0];
        double baseTemperature = baseParameters[1];
        double basePressure = baseParameters[2];
        double baseAltitude = baseParameters[4];
        
        double temperature = calculateTemperature(GeometricAltitdue);
        
        double pressure;
        if(lapseRate==0){
            pressure = basePressure*Math.pow(Math.E, -(EARTH_GRAVITY/(lapseRate*AIR_GAS_CONSTANT))*(geopotentialAltitude-baseAltitude));
        }else{
            pressure = basePressure*Math.pow(temperature/baseTemperature, -EARTH_GRAVITY/(lapseRate*AIR_GAS_CONSTANT));
        }
        
        return pressure;
    }

    @Override
    public double calculateDensity(double GeometricAltitdue) {
        double geopotentialAltitude = calculateGeopotentialAltitude(GeometricAltitdue);
        double[] baseParameters = getBaseParameters(geopotentialAltitude);
        double lapseRate = baseParameters[0];
        double baseTemperature = baseParameters[1];
        double baseDensity = baseParameters[3];
        double baseAltitude = baseParameters[4];
        
        double temperature = calculateTemperature(GeometricAltitdue);
        
        double density;
        if(lapseRate==0){
            density = baseDensity*Math.pow(Math.E, -(EARTH_GRAVITY/(lapseRate*AIR_GAS_CONSTANT))*(geopotentialAltitude-baseAltitude));
        }else{
            density = baseDensity*Math.pow(temperature/baseTemperature, (-EARTH_GRAVITY/(lapseRate*AIR_GAS_CONSTANT))-1);
        }
        
        return density;    
    }

    @Override
    public double calculateTemperature(double GeometricAltitdue) {
        double geopotentialAltitude = calculateGeopotentialAltitude(GeometricAltitdue);
        double[] baseParameters = getBaseParameters(geopotentialAltitude);
        double lapseRate = baseParameters[0];
        double baseTemperature = baseParameters[1];
        double baseAltitude = baseParameters[4];
        
        return baseTemperature+lapseRate*(geopotentialAltitude-baseAltitude);
    }
    
}
