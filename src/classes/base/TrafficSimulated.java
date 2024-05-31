/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import TFM.AircraftControl.AircraftControlCommand;
import static TFM.AircraftControl.AircraftControlCommand.CommandType.ALTITUDE;
import static TFM.AircraftControl.AircraftControlCommand.CommandType.COURSE;
import static TFM.AircraftControl.AircraftControlCommand.CommandType.SPEED;
import static TFM.AircraftControl.AircraftControlCommand.CommandType.VERTICAL_RATE;
import TFM.AircraftControl.ControllableAircraft;
import TFM.Simulation;
import TFM.TrafficIcon;
import TFM.utils.LogEntry;
import TFM.utils.UnitConversion;
import classes.googleearth.GoogleEarthTraffic;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms
 */
public class TrafficSimulated extends Thread  implements ControllableAircraft{
    
    
    private Simulation simulation;
    private String hexCode;
    private Coordinate position;
    private Coordinate target;
    private double speed; // knots
    private double verticalRate; //fpm
    private double course; // degrees
    private double traveled; // meters
    private boolean moving;
    private boolean arrivedAtDestination = false;
    private TrafficSimulatedListener listener = null;
    private int routeMode; // ortho or loxodromic 
    public static final int FLY_LOXODROMIC = 1;
    public static final int FLY_ORTHODROMIC = 2;
    
    //Performance
    private double takeoffAcceleration = 5; // knots/s average A320 takeoff acceleration
    private double climbAcceleration = 3.12; 
    
    //Times
    private double sampleTime; // in minutes
    private static final int THREAD_TIME = 1000; // in milliseconds
    private int waitTime;
    public static double SAMPLE_TIME = 0.5; //  in minutes
    private long timeStepDelta = 0;
    private long lastSimulationTime = 0;

    //Display
    private boolean focused; //View focusing on the aircraft
    private boolean followed; //View following the aircraft
    private TrafficIcon icon;
    private boolean paintInGoogleEarth;
    private GoogleEarthTraffic ge;
    
    //Aircraft Control
    private int currentCommandPriority = Integer.MAX_VALUE;

    //Logging
    private List<LogEntry> logEntries;

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
        this.logEntries = new ArrayList<>();
        
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
        this.logEntries = new ArrayList<>();

        //Carga la imagen del avión y configura el tamaño
        this.icon = new TrafficIcon(this.hexCode,position.toPosition(1));
        icon.setSize(new Dimension(30, 30));
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(0, 50));
    }
    
    private TrafficSimulated(TrafficSimulatedBuilder builder) {
        this.simulation = builder.simulation;
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
        this.routeMode = builder.routeMode;
        this.waitTime = THREAD_TIME;
        // Initialize logEntries
        this.logEntries = new ArrayList<>();
    }
    // Static inner Builder class for the CREATE command
    public static class TrafficSimulatedBuilder  {
        private Simulation simulation;
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
        private int routeMode;
        
        public TrafficSimulatedBuilder setSimulation( Simulation simulation){
            this.simulation = simulation;
            return this;
        }

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

        public TrafficSimulatedBuilder setRouteMode(int routeMode) {
            this.routeMode = routeMode;
            return this;
        }

        public TrafficSimulated build() {
            return new TrafficSimulated(this);
        }
    }
    
    public void setPaintInGoogleEarth(GoogleEarthTraffic ge) {
        this.ge = ge;
        this.paintInGoogleEarth = true;
    }
    
    public void flyit(Coordinate target, int routeMode) {
//        this.target = target;
//        this.routeMode = routeMode;
//        if (routeMode == FLY_LOXODROMIC) {
//            course = position.getRhumbLineBearing(target);
//        } else {
//            course = position.getGreatCircleInitialBearing(target);
//        }
        if (!this.moving) { // if is flying continues            
            this.moving = true;
            this.start();
        }
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
        System.out.println(this.hexCode + " PLANE RUN METHOD !!!!.");
        if (listener != null) {
            listener.planeStarted(this);
        }
        long tmpTime;
        
        while (true) { // Keep the thread alive
            while (!moving) { // Wait if not moving
                try {
                    synchronized (this) {
                        wait(); // Wait until notified to resume
                    }
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted while waiting.");
                    return; // Optionally, handle interruption (e.g., cleanup) before exiting
                }
            }    
            //display(sampleTime, traveled, course, position);

            tmpTime = this.simulation.getSimulationTime();
            timeStepDelta = tmpTime - lastSimulationTime;

            //Adapt waitTime accordingly to simulation speed.
            waitTime = (int)Math.round( (double) THREAD_TIME/simulation.getSpeed());

            distance = speed * UnitConversion.knotToMs * timeStepDelta  /1000; // in meters traveled in each iteration 

            traveled += distance; // total distance

            position.updateRhumbLinePosition(distance, course);
            if (routeMode == FLY_ORTHODROMIC) {
                course = position.getGreatCircleInitialBearing(target);
            }
            altitude = position.getAltitude() + verticalRate * timeStepDelta  / (1000*60)*UnitConversion.ftToMeter;
            System.out.println("[PLANE] "+this.getHexCode() + " Altitude (ft): " +altitude/UnitConversion.ftToMeter + " verticalRate (ft/min)" + verticalRate + " speed (kts)"+ speed);
            position.setAltitude(altitude); //in meters
            System.out.println("Logging data");
            lastSimulationTime = tmpTime;
            //Loggin
            
            logData();
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrafficSimulated.class.getName()).log(Level.SEVERE, null, ex);

            }

            if (arrivedAtDestination) { // Implement logic to set shouldExit when you want to terminate the thread for good
                if (listener != null) {
                    listener.planeStopped(this);
                }
                break;
            }
            
        }   
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
    
    public synchronized void stoppit() {
        moving = false;
        // No need to call notifyAll() here since we want the thread to check the condition and wait.
    }
    
    
    public synchronized void startit() {
        moving = true;
        notifyAll(); // Wake up the thread so it can check the moving condition again.
    }
    
    @Override
    public String toString() {
        return hexCode;
    }

    
    private synchronized void logData() {
        LogEntry entry = new LogEntry(simulation.getSimulationTime());
        entry.setAttribute("Latitude", position.getLatitude());
        entry.setAttribute("Longitude", position.getLongitude());
        entry.setAttribute("Altitude", position.getAltitude());
        entry.setAttribute("Speed", speed);
        entry.setAttribute("Bearing", course);
        logEntries.add(entry);
    }
    
    public void saveLogsToCsv(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(LogEntry.getCsvHeader(logEntries));
            writer.newLine();
            for (LogEntry entry : logEntries) {
                writer.write(entry.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public <T> void processCommand(AircraftControlCommand<T> command) {
        switch (command.getType()) {
            case ALTITUDE:
                if (command.getValue() instanceof Double) {
                    setAltitude((Double) command.getValue(), command.getPriority());
                }
                break;
            case COURSE:
                if (command.getValue() instanceof Double) {
                    setCourse((Double) command.getValue(), command.getPriority());
                }
                break;
            case SPEED:
                if (command.getValue() instanceof Double) {
                    setSpeed((Double) command.getValue(), command.getPriority());
                }
                break;
            case VERTICAL_RATE:
                if (command.getValue() instanceof Double ) {
                    setVerticalRate((Double) command.getValue(), command.getPriority());
                }
                break;
            case ROUTE_MODE:
                if (command.getValue() instanceof Integer) {
                    setRouteMode((Integer) command.getValue(), command.getPriority());
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported command type: " + command.getType());
        }
    }
    
    public synchronized void setCourse(double bearing, int priority) {
        if (priority <= currentCommandPriority) {
            this.course = bearing;
            currentCommandPriority = priority;
            //System.out.println("Course updated to " + bearing + " with priority " + priority);
        }
    }

    public synchronized void setAltitude(double altitude, int priority) {
        if (priority <= currentCommandPriority) {
            this.getPosition().setAltitude(altitude);
            currentCommandPriority = priority;
            //System.out.println("Altitude updated to " + altitude + " with priority " + priority);
        }
    }

    public synchronized void setSpeed(double speed, int priority) {
        if (priority <= currentCommandPriority) {
            this.speed = speed;
            currentCommandPriority = priority;
            //System.out.println("Speed updated to " + speed + " with priority " + priority);
        }
    }  
  
    public synchronized void setVerticalRate(double rate, int priority) {
        if (priority <= currentCommandPriority) {
            this.verticalRate = rate;
            currentCommandPriority = priority;
            //System.out.println("Vertical rate updated to " + rate + " with priority " + priority);
        }
    }  

    public void setRouteMode(int routeMode, int priority) {
        if (priority <= currentCommandPriority) {
            this.routeMode = routeMode;
            currentCommandPriority = priority;
            System.out.println("Route mode updated to " + routeMode + " with priority " + priority);
        }
    }
    
    @Override
    public void resetPriority() {
        currentCommandPriority = Integer.MAX_VALUE;
        System.out.println("[PLANE] "+hexCode+" Command priority reset to normal");
    }
    
    //GETTERS & SETTERS
    
    public synchronized List<LogEntry> getLogEntries() {
        return new ArrayList<>(logEntries); // Return a copy to avoid concurrent modification issues
    }
        
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

    public double getTakeoffAcceleration() {
        return takeoffAcceleration;
    }

    public void setTakeoffAcceleration(double takeoffAcceleration) {
        this.takeoffAcceleration = takeoffAcceleration;
    }

    public double getClimbAcceleration() {
        return climbAcceleration;
    }

    public void setClimbAcceleration(double climbAcceleration) {
        this.climbAcceleration = climbAcceleration;
    }

    public long getLastSimulationTime() {
        return lastSimulationTime;
    }

    public long getTimeStepDelta() {
        return timeStepDelta;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

}
