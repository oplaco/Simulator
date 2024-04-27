/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TCAS;

import TFM.Simulation;
import TFM.TrafficSimulationMap;
import TFM.utils.UnitConversion;
import TFM.utils.Utils;
import TFM.utils.Vector2D;
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
    
    //Solver variables.
    private static final double ALIM = 1000000; // altitude limit for resolution advisories, adjust as necessary

    double TCPA, CPA; //time to closes point of approach. Distance to closest point of approach.
    //Distance between aircrafts in closest point of approach is missing as of now.
    double distanceToTraffic; 
    
    private long range = 200000; // meters
    private int sentivityLevel;
    private ConcurrentHashMap<String, Pilot> pilotMap; //Need to have access to all the aircrafts in the simulation
    private ConcurrentHashMap<String, TrafficSimulated> trafficWithinRangeMap; //Store all the aircrafts that are in range.
    private WorldWindow wwd;
    private String ownHexCode;
    private TrafficSimulated ownTraffic;
    private boolean handlingRA = false;

    
    public TCASTransponder(String hexCode,WorldWindow wwd,ConcurrentHashMap pilotMap){
        this.wwd = wwd;
        this.pilotMap = pilotMap;
        this.ownHexCode = hexCode;
        this.ownTraffic = this.pilotMap.get(ownHexCode).getPlane();
        this.trafficType = 0;
    }
    
 
    public void iteration(){
        System.out.println("TCAS iteration");
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
        if(distanceToTraffic<6000/UnitConversion.meterToNM){
            if(TCPA<25 && TCPA>0){
                handlingRA = true;
                trafficType = resolutionAdvisory;
            }else if(TCPA<40 && TCPA>=25){
                trafficType = trafficAdvisory;              
            }else if(TCPA>=40){
                trafficType = proximateTraffic;
            }
        }
    }
  
    private void computeTCASInterrogation(TrafficSimulated traffic_i){
        TrafficSimulated traffic_o = pilotMap.get(ownHexCode).getPlane();
        
        //Compute NED relative Position. The origin is traffic1.
        Vec4 vec4_traffic_o = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic_o));
        Vec4 vec4_traffic_i = wwd.getModel().getGlobe().computePointFromPosition(getNasaPos(traffic_i));
        double[] relECEFPosition = {vec4_traffic_o.getX()-vec4_traffic_i.getX(),vec4_traffic_o.getY()-vec4_traffic_i.getY(),vec4_traffic_o.getZ()-vec4_traffic_i.getZ()};
        
        double[] traffic_o_NED = ecefToNED(relECEFPosition,getNasaPos(traffic_i));
        double[] traffic_i_NED = new double[]{0,0,0};
        
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
        
        TCPA = calculateTCPA(traffic_o_NED, relative_v_o_NED);
        CPA = calculateCPA(traffic_o_NED, relative_v_o_NED);
        
        
        Vector2D S = new Vector2D(traffic_o_NED[0]-traffic_i_NED[0],traffic_o_NED[1]-traffic_i_NED[1]);
        Vector2D V = new Vector2D(relative_v_o_NED[0],relative_v_o_NED[1]);
        Vector2D S_o = new Vector2D(traffic_o_NED[0],traffic_o_NED[1]);
        Vector2D V_o = new Vector2D(traffic_o_NEDspeed[0],traffic_o_NEDspeed[1]);
        Vector2D S_i = new Vector2D(traffic_i_NED[0],traffic_i_NED[1]);
        Vector2D V_i = new Vector2D(relative_v_i_NED[0],relative_v_i_NED[1]);
        double s_oz = traffic_o_NED[2]; 
        double v_oz = traffic_o_NEDspeed[2];
        double s_iz = traffic_i_NED[1];
        double v_iz = traffic_i_NEDspeed[2];
        double v = 1000*UnitConversion.ftMinToMs;
        double a = Double.POSITIVE_INFINITY;
                
        double tau = calculate_tau(S, V);
        double tcpa = calculate_tcpa(S, V);
        System.out.println("[TCAS] "+traffic_o.getHexCode()+" TCPA (s): "+TCPA+ " tau (s): "+tau+" tcpa (s): "+tcpa+" CPA: "+ CPA + " distanceToTraffic "+distanceToTraffic);
        //System.out.println("[TCAS] "+traffic1.getHexCode()+" Vertical Rate: "+ traffic1.getVerticalRate());
        
        if (trafficType==resolutionAdvisory){
            int solution = RA_sense_new(S_o, s_oz, V_o, v_oz, S_i, s_iz, V_i, v_iz, v, a);
            if (solution == 1){
                System.out.println("[TCAS] "+traffic_o.getHexCode()+ " ordered to ASCEND");
            }else{
                System.out.println("[TCAS] "+traffic_o.getHexCode()+ " ordered to DESCEND"); 
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

    
    /* PAPER*/

    
    private double calculate_tcpa(Vector2D S, Vector2D V){
        if(!V.equals(new Vector2D(0, 0))){
             return -(S.dotProduct(V))/(V.dotProduct(V));           
        }else{
            return 0.;
        }
    }
    
    /**
    * Calculates tau, this time is an estimate (BUT NOT THE SAME) of the time of closest point of approach.
    */
    private double calculate_tau(Vector2D S, Vector2D V){
        if(!V.equals(new Vector2D(0, 0))){
             return -(S.dotProduct(V))/(S.dotProduct(S));           
        }else{
            return 0.;
        }
    }
    /**
     * predicts the vertical separation between the aircraft at time tau assuming a target vertical speed v for the ownship.
     * @param S
     * @param V
     * @param s_oz
     * @param v_oz
     * @param s_iz
     * @param v_iz
     * @param v Target vertical speed v
     * @param a The ownship is assumed to y at constant ground speed and constant vertical acceleration a.
     * @param epsilon  The parameter epsilon specifes a possible direction for the vertical ownship maneuver, 
     *  which is upward when epsilon = 1 and downward when epsilon = -1.
     */
    private double sep_at(Vector2D S,Vector2D V,double s_oz, double v_oz, double s_iz,double v_iz,double v, double a,double epsilon){
        double alt_o = own_alt_at(s_oz, v_oz, Math.abs(v), a, epsilon*Utils.sign(v), calculate_tau(S, V));
        double alt_i = s_iz + calculate_tau(S, V)*v_iz;
        return epsilon*(alt_o-alt_i);
    }
    
    /**
     * Computes the vertical altitude of the ownship at any time t given a target vertical speed v and acceleration a.
     */
    private double own_alt_at(double s_oz, double v_oz, double v, double a, double epsilon, double t){
        double s = stop_accel(v_oz, v, a, epsilon, t);
        double q = Math.min(t, s);
        double l = Math.max(0,t-s);
        if(a==Double.POSITIVE_INFINITY){
            return q*v_oz + s_oz + epsilon*l*s;   
        }else{
            return epsilon*q*q*a*0.5 + q*v_oz + s_oz + epsilon*l*s; 
        }
    }
   
    /**
     * Computes the time at which the ownship reaches the target vertical speed v.
    */
    private double stop_accel(double v_oz, double v, double a, double epsilon, double t){
        if(t <= 0 || v_oz >=v){
            return 0;
        } else{
            return (epsilon*v - v_oz)/(epsilon*a);
        }
    }
   
   /**
    * Computes wheter an aircraft must go up or down
    * @param S_o
    * @param s_oz
    * @param V_o
    * @param v_oz
    * @param S_i
    * @param s_iz
    * @param V_i
    * @param v_iz
    * @param v
    * @param a
    * @return 
    */
    private int RA_sense_new(Vector2D S_o, double s_oz, Vector2D V_o, double v_oz, Vector2D S_i, double s_iz, Vector2D V_i, double v_iz, double v, double a){
        double ALIM_l = 100000.0;
        double tau_o = calculate_tau(S_o, V_o);
        double tau_i = calculate_tau(S_i, V_i);
        
        double o_up = own_alt_at(s_oz, v_oz, v, a, 1,  tau_o);
        double o_down = own_alt_at(s_oz, v_oz, v, a, -1,  tau_o);     
        double i_up = own_alt_at(s_iz, v_iz, v, a, 1,  tau_i);
        double i_down = own_alt_at(s_iz, v_iz, v, a, -1,  tau_i);
        
        double alt_o = s_oz + tau_o*v_oz;
        double alt_i = s_iz + tau_i*v_iz;
        
        double dist_o_up = o_up - alt_i;
        double dist_o_down = alt_i - o_down;
        double dist_i_up = i_up - alt_o;
        double dist_i_down = alt_o - i_down;
        
        if( Utils.sign(s_oz-s_iz)==1 && Math.min(dist_o_up, dist_i_down)>= ALIM_l){
            return 1;
        }else if(Utils.sign(s_oz-s_iz)==-1 && Math.min(dist_o_down, dist_i_up)>= ALIM_l){
            return -1;
        }else if(Math.min(dist_o_up,dist_i_down)>=Math.min(dist_o_down,dist_i_up)){
            return 1;
        }else{
            return -1;
        }
   }
    
    public int getTrafficType() {
        return trafficType;
    }

    public boolean isHandlingRA() {
        return handlingRA;
    }
}
