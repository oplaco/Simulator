/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.TCAS;

import TFM.AircraftControl.AircraftControlCommand;
import TFM.AircraftControl.ControllableAircraft;
import TFM.AircraftControl.ICommandSource;
import TFM.utils.UnitConversion;
import TFM.utils.Vector2D;
import TFM.Coordinates.Coordinate;
import TFM.Core.Pilot;
import TFM.Core.TrafficSimulated;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Gabriel
 */
public class TCASTransponder implements ICommandSource {
    
    public static int otherTraffic = 0;
    public static int proximateTraffic = 1;
    public static int trafficAdvisory = 2;
    public static int resolutionAdvisory = 3;
    private int trafficType;
    
    //Solver.
    private ResolutionAdvisorySolver RASolver;
    private static final double ALIM = 1000000; // altitude limit for resolution advisories, adjust as necessary
    private double RAverticalSpeed = 1000*UnitConversion.ftMinToMs; // This is the speed that the RA will set for the aircraft ascent and/or descent
    
    private double TCPA, CPA; //time to closes point of approach. Distance to closest point of approach.
    //Distance between aircrafts in closest point of approach is missing as of now.
    private double distanceToTraffic; 
    
    private long range = 200000; // meters
    private int sentivityLevel;
    private ConcurrentHashMap<String, Pilot> pilotMap; //Need to have access to all the aircrafts in the simulation
    private ConcurrentHashMap<String, TrafficSimulated> trafficWithinRangeMap; //Store all the aircrafts that are in range.
    private WorldWindow wwd;
    private String ownHexCode;
    private TrafficSimulated ownTraffic;
    
    private boolean handlingRA = false;
    private boolean currentHandlingRA = false;

    
    public TCASTransponder(String hexCode,WorldWindow wwd,ConcurrentHashMap pilotMap,ResolutionAdvisorySolver RASolver){
        this.wwd = wwd;
        this.pilotMap = pilotMap;
        this.ownHexCode = hexCode;
        this.ownTraffic = this.pilotMap.get(ownHexCode).getPlane();
        this.RASolver = RASolver;
        this.trafficType = 0;
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
                
            }
        
        }
    }
      
    public void updateTrafficType(Pilot otherPilot){
        TCPA = Math.abs(TCPA);
        //Only enforce TA and RA if targets are within 6NM. It should be 6NM horizontally and within 1200ft.
        handlingRA = false;
        if(Math.abs(distanceToTraffic)<6000/UnitConversion.meterToNM){
            if(TCPA<25 && TCPA>0){
                trafficType = resolutionAdvisory;
                handlingRA = true;
            }else if(TCPA<40 && TCPA>=25){
                trafficType = trafficAdvisory;              
            }else if(TCPA>=40){
                trafficType = proximateTraffic;
            }
        }
        
        // If the traffic situation has improved, reset the priority
        if (!handlingRA && currentHandlingRA) {
            ownTraffic.resetPriority();
        }

        currentHandlingRA = handlingRA; // Store the current handling state for comparison in the next iteration
    }
  
    private void computeTCASInterrogation(TrafficSimulated traffic_i){
        TrafficSimulated traffic_o = pilotMap.get(ownHexCode).getPlane();
        
        //Compute NED relative Position. The origin is traffic1.
        Vec4 vec4_traffic_o = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic_o));
        Vec4 vec4_traffic_i = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic_i));
        double[] relECEFPosition = {vec4_traffic_o.getX()-vec4_traffic_i.getX(),vec4_traffic_o.getY()-vec4_traffic_i.getY(),vec4_traffic_o.getZ()-vec4_traffic_i.getZ()};
        
        double[] S_o_NED = ecefToNED(relECEFPosition,getNasaPos(traffic_i));
        double[] S_i_NED = new double[]{0,0,0};
        
        //Compute NED relative velocity (i.e closing speed)
        double[] traffic_o_NEDspeed,traffic_i_NEDspeed;
        traffic_o_NEDspeed = computeNEDSpeed(traffic_o);
        traffic_i_NEDspeed = computeNEDSpeed(traffic_i);
                
        double[] relative_v_o_NED = {
            traffic_o_NEDspeed[0]-traffic_i_NEDspeed[0],
            traffic_o_NEDspeed[1]-traffic_i_NEDspeed[1],
            traffic_o_NEDspeed[2]-traffic_i_NEDspeed[2]
        };
        
        double[] relative_v_i_NED = {0,0,0};
        
        TCPA = calculateTCPA(S_o_NED, relative_v_o_NED);
        CPA = calculateCPA(S_o_NED, relative_v_o_NED);
        
        
        Vector2D S = new Vector2D(S_o_NED[0]-S_i_NED[0],S_o_NED[1]-S_i_NED[1]);
        Vector2D V = new Vector2D(relative_v_o_NED[0],relative_v_o_NED[1]);
        Vector2D S_o = new Vector2D(S_o_NED[0],S_o_NED[1]);
        Vector2D V_o = new Vector2D(traffic_o_NEDspeed[0],traffic_o_NEDspeed[1]);
        Vector2D S_i = new Vector2D(S_i_NED[0],S_i_NED[1]);
        Vector2D V_i = new Vector2D(relative_v_i_NED[0],relative_v_i_NED[1]);
        double s_oz = S_o_NED[2]; 
        double v_oz = traffic_o_NEDspeed[2];
        double s_iz = S_i_NED[1];
        double v_iz = traffic_i_NEDspeed[2];
        double v = RAverticalSpeed;
        double a = Double.POSITIVE_INFINITY;
        
        //System.out.println("[TCAS] "+traffic_o.getHexCode()+ " TCPA "+TCPA+" CPA "+CPA);
                       
        if (trafficType==resolutionAdvisory){
            int solution = RASolver.solve(S_o, s_oz, V_o, v_oz, S_i, s_iz, V_i, v_iz, v, a);
            if (solution == 1){
                System.out.println("[TCAS] "+traffic_o.getHexCode()+ " ordered to ASCEND");
                sendCommand(ownTraffic, new AircraftControlCommand(AircraftControlCommand.CommandType.VERTICAL_RATE, 1000.0, 1));
                System.out.println("[TCAS] "+traffic_o.getHexCode()+ " ordered to ASCEND");
            }else{
                System.out.println("[TCAS] "+traffic_o.getHexCode()+ " ordered to DESCEND"); 
                sendCommand(ownTraffic, new AircraftControlCommand(AircraftControlCommand.CommandType.VERTICAL_RATE, -1000.0, 1)); 
            }
            
        }
    }
    
    /*
        Computes the NED (North East Down) coordinate system velocity from its module and bearing.
    https://www.mathworks.com/help/aeroblks/about-aerospace-coordinate-systems.html
    */
    private double[] computeNEDSpeed(TrafficSimulated traffic){
        Double speed,course,speedNorth,speedEast,speedDown;
        speed = traffic.getSpeed()*UnitConversion.knotToMs;
        course = traffic.getCourse();
        speedNorth = speed*Math.cos(course);
        speedEast = speed*Math.sin(course);
        speedDown = - traffic.getVerticalRate()*UnitConversion.ftMinToMs;
        //System.out.println("speedNorth (m/s): "+speedNorth+" speedEast (m/s): "+speedEast+" speedDown (m/s): "+speedDown);
        
        double[] NEDSpeed = {speedNorth, speedEast, speedDown};
        return NEDSpeed;
    }   
     
    /*
        Returns the NED coordinates of a ECEF position relative to a origin (latitude, longitude).
        Emphasize that the ecef position must be already the relative position in cartesian coordinates to that origin.
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

    // Generic method to send any type of command
    @Override
    public void sendCommand(ControllableAircraft controllableAircraft, AircraftControlCommand command) {
       controllableAircraft.processCommand(command);
    }
    
    public int getTrafficType() {
        return trafficType;
    }

    public boolean isHandlingRA() {
        return handlingRA;
    }
}
