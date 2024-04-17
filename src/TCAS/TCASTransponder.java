/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TCAS;

import TFM.Simulation;
import TFM.TrafficSimulationMap;
import TFM.utils.UnitConversion;
import classes.base.Coordinate;
import classes.base.Pilot;
import classes.base.TrafficSimulated;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Gabriel
 */
public class TCASTransponder{
    
    public static int otherTraffic = 0;
    public static int proximateTraffic = 1;
    public static int trafficAdvisory = 2;
    public static int resolutionAdvisory = 3;
    private int trafficType;
    
    double TCPA, CPA;
    double distanceToTraffic;
    
    private long range = 200000; // meters
    private int sentivityLevel;
    private ConcurrentHashMap<String, Pilot> pilotMap; //Need to have access to all the aircrafts in the simulation
    private ConcurrentHashMap<String, TrafficSimulated> trafficWithinRangeMap; //Store all the aircrafts that are in range.
    private WorldWindow wwd;
    private String ownHexCode;
    private TrafficSimulated ownTraffic;

    
    public TCASTransponder(String hexCode,WorldWindow wwd,ConcurrentHashMap pilotMap){
        this.wwd = wwd;
        this.pilotMap = pilotMap;
        this.ownHexCode = hexCode;
        this.ownTraffic = this.pilotMap.get(ownHexCode).getPlane();
    }
    
 
    public void iteration(){
        for (Map.Entry<String, Pilot> entry : pilotMap.entrySet()) {
            String hexCode = entry.getKey();
            Pilot otherPilot = entry.getValue();

            // Skip the reference plane itself
            if (!hexCode.equals(ownHexCode)) {
                // Calculate TCPA and CPA between the two planes.
                computeTCASInterrogation(otherPilot.getPlane());
                
                distanceToTraffic = ownTraffic.getPosition().getGreatCircleDistance(otherPilot.getPlane().getPosition());
                //update the traffic type (traffic advisory, resolution advisory based on TCPA)
                updateTrafficType(otherPilot);
                
                 
                if (trafficType==resolutionAdvisory){
                    if(!pilotMap.get(ownHexCode).isOtherTCASSolvingRA()){
                        handleResolutionAdvisory(otherPilot);
                    }
                }
            }
        
        }
    }
    
    public void updateTrafficType(Pilot otherPilot){
        TCPA = Math.abs(TCPA);
        //Only enforce TA and RA if targets are within 6NM. It should be 6NM horizontally and within 1200ft.
        if(distanceToTraffic<6/UnitConversion.meterToNM){
            if(TCPA<25 && TCPA>0){
                trafficType = resolutionAdvisory;
            }else if(TCPA<40 && TCPA>=25){
                trafficType = trafficAdvisory;              
            }else if(TCPA>=40){
                trafficType = proximateTraffic;
            }
        }
    }

    
    private void handleResolutionAdvisory(Pilot otherPilot) {
        //Avoid both the TCAS systems to solve the same advisory.
        if(!pilotMap.get(ownHexCode).isOtherTCASSolvingRA()){
            otherPilot.setOtherTCASSolvingRA(true);
        }
        
        
        TrafficSimulated otherPlane = otherPilot.getPlane();
        double myAltitude = ownTraffic.getPosition().getAltitude();
        double otherAltitude = otherPlane.getPosition().getAltitude();

        if (myAltitude > otherAltitude) {
            // I'm higher, I should climb, other descends
            ownTraffic.setVerticalRate(1000);
            otherPlane.setVerticalRate(-1000); //descent
        } else {
            // I'm lower or equal, I should descend, other climbs
            ownTraffic.setVerticalRate(-1000);//descent
            otherPlane.setVerticalRate(1000); 
        }

        // Logging for debug purposes
        System.out.println("[TCAS] RA. Altitude" + ownTraffic.getHexCode()+" :"+ ownTraffic.getPosition().getAltitude() + " "+ ownTraffic.getVerticalRate()+" Altitude 2: " + otherPlane.getPosition().getAltitude() +" "+ otherPlane.getVerticalRate());
    }
    
    private void computeTCASInterrogation(TrafficSimulated traffic2){
        TrafficSimulated traffic1 = pilotMap.get(ownHexCode).getPlane();
        
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
        
        TCPA = calculateTCPA(traffic2NED, relative_Velocity);
        CPA = calculateCPA(traffic2NED, relative_Velocity);
        //System.out.println("[TCAS] "+traffic1.getHexCode()+" TCPA: "+TCPA+ "s . CPA: "+ CPA);
        //System.out.println("[TCAS] "+traffic1.getHexCode()+" Vertical Rate: "+ traffic1.getVerticalRate());
    }
    
    /*
        Computes the NED (North East Down) coordinate system velocity from its module and bearing.
    https://www.mathworks.com/help/aeroblks/about-aerospace-coordinate-systems.html
    */
    private double[] computeNEDSpeed(TrafficSimulated traffic){
        Double speed,course,speedNorth,speedEast,speedDown;
        speed = traffic.getSpeed();
        course = traffic.getCourse();
        speedNorth = speed*Math.cos(course)*UnitConversion.knotToMs;
        speedEast = speed*Math.sin(course)*UnitConversion.knotToMs;
        speedDown = - traffic.getVerticalRate()*UnitConversion.ftMinToMs;
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
                        pos.getAltitude()*UnitConversion.ftToMeter);
        return nasa_pos;
    }

    public int getTrafficType() {
        return trafficType;
    }
    
}
