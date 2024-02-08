/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import TFM.TrafficIcon;
import classes.googleearth.GoogleEarthTraffic;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms
 */
public class TrafficSimulated extends Thread {
    
    private String hexCode;
    private Coordinate position;
    private Coordinate target;
    private double speed; // knots
    private double verticalRate; //fpm
    private double course; // degrees
    private double traveled; // meters
    private boolean moving;
    private TrafficSimulatedListener listener = null;
    
    private double sampleTime; // in minutes
    private int waitTime; // in milliseg
    private int routeMode; // ortho or loxodromic 
    private boolean paintInGoogleEarth;
    private GoogleEarthTraffic ge;
    public static final int FLY_LOXODROMIC = 1;
    public static final int FLY_ORTHODROMIC = 2;
    private static final int THREAD_TIME = 50; // in milliseconds
    public static double SAMPLE_TIME = 0.5; //  in minutes

    //Icon used to display the plane
    private TrafficIcon icon;
       
    //Conversion units
    static private double ftToMeter = 0.3048;
    static private double knotToMs = 0.514444;

    public TrafficSimulated(String hexCode, Coordinate position, double speed, double course) {
        this.hexCode = hexCode;
        this.position = new Coordinate(position);
        this.speed = speed;
        this.course = course;
        this.moving = false;
        this.waitTime = THREAD_TIME;
        this.sampleTime = 0;
        this.paintInGoogleEarth = false;
        this.verticalRate=0;
        
        //Carga la imagen del avión y configura el tamaño
        this.icon = new TrafficIcon(this.hexCode,position.toPosition(1));
        icon.setSize(new Dimension(50, 50));
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(0, 50));
    }
    
    public TrafficSimulated(String hexCode, Coordinate position, Coordinate target,double speed, double course, double sampleTime) {
        TrafficSimulated.SAMPLE_TIME = sampleTime/(1000*60);
        this.hexCode = hexCode;
        this.position = new Coordinate(position);
        this.target = target;
        this.speed = speed;
        this.course = course;
        this.moving = false;
        this.waitTime = THREAD_TIME;
        this.sampleTime = 0;
        this.paintInGoogleEarth = false;
        this.verticalRate=0;
        
        //Carga la imagen del avión y configura el tamaño
        this.icon = new TrafficIcon(this.hexCode,position.toPosition(1));
        icon.setSize(new Dimension(50, 50));
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(0, 50));
    }
    
    public TrafficSimulated(String hexCode, Coordinate position, double speed, double course, TrafficSimulatedListener listener) {
        this.hexCode = hexCode;
        this.position = new Coordinate(position);
        this.speed = speed;
        this.course = course;
        this.moving = false;
        this.waitTime = THREAD_TIME;
        this.sampleTime = 0;
        this.paintInGoogleEarth = false;
        this.listener = listener;
        this.verticalRate=0;
        
        //Carga la imagen del avión y configura el tamaño
        this.icon = new TrafficIcon(this.hexCode,position.toPosition(1));
        icon.setSize(new Dimension(30, 30));
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(0, 50));
    }
    
    private TrafficSimulated(TrafficSimulatedBuilder builder) {
        this.hexCode = builder.hexCode;
        this.position = builder.position;
        this.target = builder.target;
        this.speed = builder.speed;
        this.verticalRate = builder.verticalRate;
        this.course = builder.course;
        this.traveled = builder.traveled;
        this.moving = builder.moving;
        this.listener = builder.listener;
        this.sampleTime = builder.sampleTime;
        this.waitTime = builder.waitTime;
        this.routeMode = builder.routeMode;
    }
    // Static inner Builder class for the CREATE command
    public static class TrafficSimulatedBuilder {
        private String hexCode;
        private Coordinate position;
        private Coordinate target;
        private double speed;
        private double verticalRate;
        private double course;
        private double traveled;
        private boolean moving;
        private TrafficSimulatedListener listener;

        private double sampleTime;
        private int waitTime;
        private int routeMode;

        public TrafficSimulatedBuilder setHexCode(String hexCode) {
            this.hexCode = hexCode;
            return this;
        }

        public TrafficSimulatedBuilder setPosition(Coordinate position) {
            this.position = position;
            return this;
        }

        public TrafficSimulatedBuilder setTarget(Coordinate target) {
            this.target = target;
            return this;
        }

        public TrafficSimulatedBuilder setSpeed(double speed) {
            this.speed = speed;
            return this;
        }

        public TrafficSimulatedBuilder setVerticalRate(double verticalRate) {
            this.verticalRate = verticalRate;
            return this;
        }

        public TrafficSimulatedBuilder setCourse(double course) {
            this.course = course;
            return this;
        }

        public TrafficSimulatedBuilder setTraveled(double traveled) {
            this.traveled = traveled;
            return this;
        }

        public TrafficSimulatedBuilder setMoving(boolean moving) {
            this.moving = moving;
            return this;
        }

        public TrafficSimulatedBuilder setListener(TrafficSimulatedListener listener) {
            this.listener = listener;
            return this;
        }

        public TrafficSimulatedBuilder setSampleTime(double sampleTime) {
            this.sampleTime = sampleTime;
            return this;
        }

        public TrafficSimulatedBuilder setWaitTime(int waitTime) {
            this.waitTime = waitTime;
            return this;
        }

        public TrafficSimulatedBuilder setRouteMode(int routeMode) {
            this.routeMode = routeMode;
            return this;
        }

        public TrafficSimulated build() {
            return new TrafficSimulated(this);
        }
    }

    // Other methods of TrafficSimulated...

    
    //GETTERS
    public boolean isMoving() {
        return moving;
    }
    public double getVerticalRate() {
        return verticalRate;
    }

    public void setVerticalRate(double verticalRateFpm) {
        this.verticalRate = verticalRateFpm; //fpm
    }
    
    public double getCourse() {
        return course;
    }
    
    public double getTraveled() {
        return traveled;
    }
    
    public Coordinate getPosition() {
        return position;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public int getWaitTime() {
        return waitTime;
    }
    
    public String getHexCode() {
        return hexCode;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public void setPaintInGoogleEarth(GoogleEarthTraffic ge) {
        this.ge = ge;
        this.paintInGoogleEarth = true;
    }
    
    public void flyit(Coordinate target, int routeMode, long simulationStepTime) {
        this.target = target;
        this.routeMode = routeMode;
        if (routeMode == FLY_LOXODROMIC) {
            course = position.getRhumbLineBearing(target);
        } else {
            course = position.getGreatCircleInitialBearing(target);
        }
        if (!this.moving) { // if is flying continues            
            this.moving = true;
            //this.start();
            this.move(simulationStepTime);
        }
    }
    
    public void stoppit() {
        this.moving = false;
    }
    
    private void display(double sampleTime, double traveled, double course, Coordinate position) {
        String milesStr, courseStr, latitudeStr, longitudeStr, timeStr,altitudeStr;
        
        if (listener == null) {
            milesStr = String.format("%.2f", traveled / 1852);
            courseStr = String.format("%.2f", course);
            latitudeStr = String.format("%.2f", position.getLatitude());
            longitudeStr = String.format("%.2f", position.getLongitude());
            timeStr = String.format("%.2f", sampleTime);
            altitudeStr = String.format("%.2f", position.getAltitude());
            
            System.out.println("[" + hexCode + "]: " + " TIME: " 
                    + timeStr + " mins, DIST: " + milesStr + " NM, RHUMB: " 
                    + courseStr + " deg ---> Airplane in coordinates (" 
                    + latitudeStr + ", " + longitudeStr +", " 
                    + altitudeStr + " m)");

            if (paintInGoogleEarth) {
                ge.putCoordinateInKml(position); 
            }
        } else {
            listener.planeUpdated(this); // notify to listener
        }
    }
    
    @Override
    public void run() {
        double distance;
        double altitude;
        
        traveled = 0;
        System.out.println(this.hexCode + " plane is running.");
        if (listener != null) {
            listener.planeStarted(this);
        }
        while (moving) {
            display(sampleTime, traveled, course, position);
            distance = (speed * 1852 / 3600) * SAMPLE_TIME * 60; // in meters traveled in each iteration  
            traveled += distance; // total distance
            sampleTime += SAMPLE_TIME;
            position.updateRhumbLinePosition(distance, course);
            if (routeMode == FLY_ORTHODROMIC) {
                course = position.getGreatCircleInitialBearing(target);
            }
            altitude = position.getAltitude() + verticalRate * SAMPLE_TIME * ftToMeter;
            position.setAltitude(altitude); //in meters
            
            System.out.println(this.hexCode + " plane is moving.");
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrafficSimulated.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (listener != null) {
            listener.planeStopped(this);
        }
        
    }
    
    public void move(double simulation_step_time){
        double distance = (speed * 1852 / 3600) * simulation_step_time / 1000; // in meters traveled in each iteration  
        traveled += distance; // total distance
        //sampleTime += SAMPLE_TIME;

        position.updateRhumbLinePosition(distance, course);
        course = position.getRhumbLineBearing(target);
        
        double remaining_distance = position.getRhumbLineDistance(target);
        System.out.println("Remaining distance: "+remaining_distance);
        
        
        
        double altitude = position.getAltitude() + verticalRate * simulation_step_time * ftToMeter;
        position.setAltitude(altitude); //in meters

        System.out.println(this.hexCode + " plane is moving. Traveled: "+ traveled);
    }
            
    
    @Override
    public String toString() {
        return hexCode;
    }
    
    public void println() {
        
        String milesStr = String.format("%.2f", traveled / 1852);
        String courseStr = String.format("%.2f", course);
        String latitudeStr = String.format("%.2f", position.getLatitude());
        String longitudeStr = String.format("%.2f", position.getLongitude());
        
        System.out.println("[" + hexCode + "]: " + " TIME: " + sampleTime + " mins, DIST: " + milesStr + " NM, RHUMB: " + courseStr + " deg ---> Airplane in coordinates (" + latitudeStr + ", " + longitudeStr + ")");
        
    }
    
    /**
     * Obtiene el icono con la posicion actualizada del avión
     * @param altitudScale
     * @return 
     */
    public UserFacingIcon getDisplayer(double altitudScale)
    {
        icon.setPosition(position.toPosition(altitudScale));
        return icon;
    }
    
    
}