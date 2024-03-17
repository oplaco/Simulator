/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TCAS;

import TFM.Simulation;
import TFM.TrafficSimulationMap;
import classes.base.Coordinate;
import classes.base.Pilot;
import classes.base.TrafficSimulated;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Gabriel
 */
public class TCASTransponder extends Thread{
    
    private long range = 200000; // meters
    private int sentivityLevel;
    private TrafficSimulationMap trafficSimulationMap; //Need to have access to all the aircrafts in the simulation
    private ConcurrentHashMap<String, TrafficSimulated> trafficWithinRangeMap; //Store all the aircrafts that are in range.
    private WorldWindow wwd;
    private String ownHexCode;
    
    public TCASTransponder(String hexCode,WorldWindow wwd,TrafficSimulationMap trafficSimulationMap){
        this.wwd = wwd;
        this.trafficSimulationMap = trafficSimulationMap;
        this.ownHexCode = hexCode;
    }
    
    
    @Override
    public void run(){
        
        //Check for the positions of all aircrafts in trafficSimulationMap
        
        //Update the trafficWithinRangeMap with the aircrafts which distance is lower than range;
        
        //Para cada uno de ellos calcular el TAU
    }
    public void iteration(){
        for (Map.Entry<String, TrafficSimulated> entry : trafficSimulationMap.entrySet()) {
            String hexCode = entry.getKey();
            TrafficSimulated traffic = entry.getValue();

            // Skip the reference plane itself
            if (!hexCode.equals(ownHexCode)) {
                computeTCASInterrogation(traffic);
            }
        }
    }
    
    private void computeTCASInterrogation(TrafficSimulated traffic2){
        double TCAS,CPA;
        TrafficSimulated traffic1 = trafficSimulationMap.get(ownHexCode);
        
        //Compute NED relative Position. The origin is traffic1.
        Vec4 vec4_traffic1 = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic1));
        Vec4 vec4_traffic2 = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic2));
        double[] relECEFPosition = {vec4_traffic1.getX()-vec4_traffic2.getX(),vec4_traffic1.getY()-vec4_traffic2.getY(),vec4_traffic1.getZ()-vec4_traffic2.getZ()};
        double[] traffic2NED = ecefToNED(relECEFPosition,getNasaPos(traffic1));
        
        //Compute NED relative velocity (i.e closing speed)
        double[] traffic1_NEDspeed,traffic2_NEDspeed;
        traffic1_NEDspeed = computeNEDSpeed(traffic1);
        traffic2_NEDspeed = computeNEDSpeed(traffic2);
                
        double[] relative_Velocity = {
            traffic1_NEDspeed[0]-traffic2_NEDspeed[0],
            traffic1_NEDspeed[1]-traffic2_NEDspeed[1],
            traffic1_NEDspeed[2]-traffic2_NEDspeed[2]
        };
        
        TCAS = calculateTCPA(traffic2NED, relative_Velocity);
        CPA = calculateCPA(traffic2NED, relative_Velocity);
        
        System.out.println("Plane: "+traffic1.getHexCode()+" TCAS: "+TCAS+ "s . CPA: "+ CPA);
    }
    
    /*
        Computes the NED (North East Down) coordinate system velocity from its module and bearing.
    https://www.mathworks.com/help/aeroblks/about-aerospace-coordinate-systems.html
    */
    private double[] computeNEDSpeed(TrafficSimulated traffic){
        Double speed,course,speedNorth,speedEast,speedDown;
        speed = traffic.getSpeed();
        course = traffic.getCourse();
        speedNorth = speed*Math.cos(course)*Simulation.knotToMs;
        speedEast = speed*Math.sin(course)*Simulation.knotToMs;
        speedDown = - traffic.getVerticalRate()*Simulation.ftMinToMs;
        //System.out.println("speedNorth (m/s): "+speedNorth+" speedEast (m/s): "+speedEast+" speedDown (m/s): "+speedDown);
        
        double[] NEDSpeed = {speedNorth, speedEast, speedDown};
        return NEDSpeed;
    }   

    public double calculateCPA(List<Double> relVelocity, List<Double> relPosition, double TCPA) {
        double CPAx = relPosition.get(0) + TCPA * relVelocity.get(0);
        double CPAy = relPosition.get(1) + TCPA * relVelocity.get(1);
        double CPAz = relPosition.get(2) + TCPA * relVelocity.get(2);

        return Math.sqrt(CPAx * CPAx + CPAy * CPAy + CPAz * CPAz);
    }
     
    /*
        Returns the NED coordinates of a ECEF position relative to a origin (latitude, longitude).
        Emphasize that the ecef position must be already the relative position in cartesian coordinates.
    */
    public double[] ecefToNED(double[] ecefPosition, gov.nasa.worldwind.geom.Position originPosition) {
        double xRel,yRel,zRel, latOrigin, lonOrigin;
        xRel = ecefPosition[0];
        yRel = ecefPosition[1];
        zRel = ecefPosition[2];
        latOrigin = originPosition.getLatitude().getDegrees();
        lonOrigin = originPosition.getLongitude().getDegrees();

        // Convert lat and lon from degrees to radians if necessary
        double phi = Math.toRadians(latOrigin);
        double lambda = Math.toRadians(lonOrigin);

        // Transformation matrix from ECEF to NED
        double[][] R = {
            { -Math.sin(phi) * Math.cos(lambda), -Math.sin(lambda), -Math.cos(phi) * Math.cos(lambda) },
            { -Math.sin(phi) * Math.sin(lambda), Math.cos(lambda), -Math.cos(phi) * Math.sin(lambda) },
            { Math.cos(phi), 0, -Math.sin(phi) }
        };

        // Relative ECEF position vector
        double[] relECEF = {xRel, yRel, zRel};

        // Apply the transformation matrix to get NED coordinates
        double[] ned = new double[3];
        for (int i = 0; i < 3; i++) {
            ned[i] = R[i][0] * relECEF[0] + R[i][1] * relECEF[1] + R[i][2] * relECEF[2];
        }

        return ned; // Returns an array [North, East, Down]
    }
    
    
    public double calculateTCPA(double[] relPos, double[] relVel) {
        double dotProduct = relPos[0] * relVel[0] + relPos[1] * relVel[1] + relPos[2] * relVel[2];
        double velMagnitudeSquared = relVel[0] * relVel[0] + relVel[1] * relVel[1] + relVel[2] * relVel[2];
        //System.out.println("Plane: "+ownHexCode+" relative_Velocity: "+Math.sqrt(velMagnitudeSquared));
        return -dotProduct / velMagnitudeSquared;
    }

    private double calculateCPA(double[] relPos, double[] relVel) {
        double relVelMagnitudeSquared = Math.pow(relVel[0], 2) + Math.pow(relVel[1], 2) + Math.pow(relVel[2], 2);
        if (relVelMagnitudeSquared == 0) return Math.sqrt(Math.pow(relPos[0], 2) + Math.pow(relPos[1], 2) + Math.pow(relPos[2], 2)); // No relative velocity, return current distance

        double dotProduct = relPos[0] * relVel[0] + relPos[1] * relVel[1] + relPos[2] * relVel[2];
        double tcpa = -dotProduct / relVelMagnitudeSquared;
        double cpaDistance = Math.sqrt(Math.pow(relPos[0] + tcpa * relVel[0], 2) + Math.pow(relPos[1] + tcpa * relVel[1], 2) + Math.pow(relPos[2] + tcpa * relVel[2], 2));

        return cpaDistance;
    }


    public gov.nasa.worldwind.geom.Position getNasaPos(TrafficSimulated t)
    {
        Coordinate pos = t.getPosition();
        if(pos==null)
        {
            return gov.nasa.worldwind.geom.Position.ZERO;
        }
        gov.nasa.worldwind.geom.Position nasa_pos = 
                gov.nasa.worldwind.geom.Position.fromDegrees(
                        pos.getLatitude(), 
                        pos.getLongitude(),
                        pos.getAltitude()*Simulation.ftToMeter);
        return nasa_pos;
    }
}
