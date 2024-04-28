/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.AircraftControl;

/**
 *
 * @author Gabriel Alfonsín Espín
 * 
 * Blueprint of how a potential ATC class could give orders directly to aircrafts.
 * This class should be instantiated where there is access to the trafficSimulationMap probably at simulation class.
 */
import TFM.TrafficSimulationMap;
import classes.base.Coordinate;
import classes.base.TrafficSimulated;

public class ATC implements ICommandSource {
    private TrafficSimulationMap trafficSimulationMap;
    private double controlRadius;  // ATC's range of influence in kilometers
    private Coordinate location; // ATC location 

    public ATC(TrafficSimulationMap trafficSimulationMap, double controlRadius) {
        this.trafficSimulationMap = trafficSimulationMap;
        this.controlRadius = controlRadius;
    }

    // Generic method to send any type of command
    @Override
    public void sendCommand(ControllableAircraft controllableAircraft, AircraftControlCommand command) {
       controllableAircraft.processCommand(command);
    }

    // Method to issue a specific course correction command to a specific aircraft
    public void directAircraft(String hexCode, double newBearing) {
        if (trafficSimulationMap.containsKey(hexCode)) {
            TrafficSimulated traffic = trafficSimulationMap.get(hexCode);
            sendCommand(traffic, new AircraftControlCommand<>(AircraftControlCommand.CommandType.COURSE, newBearing, 2));
            sendCommand(traffic, new AircraftControlCommand<>(AircraftControlCommand.CommandType.ROUTE_MODE, TrafficSimulated.FLY_LOXODROMIC, 2));
        }
    }

    // Method to issue altitude adjustment command to a specific aircraft
    public void updateAircraftAltitude(String hexCode, double newAltitude) {
        if (trafficSimulationMap.containsKey(hexCode)) {
            TrafficSimulated traffic = trafficSimulationMap.get(hexCode);
            sendCommand(traffic, new AircraftControlCommand<>(AircraftControlCommand.CommandType.ALTITUDE, newAltitude, 2));
        }
    }
    
}

