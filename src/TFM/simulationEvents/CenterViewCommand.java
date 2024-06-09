/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

import TFM.Core.Simulation;
import TFM.Others.TrafficSimulationMap;
import TFM.Core.TrafficSimulated;
import java.util.Iterator;
import java.util.Map;
import traffic.Traffic;

/**
 *
 * @author Gabriel
 */
public class CenterViewCommand implements Command {
    @Override
    public void execute(Simulation simulation, SimulationEvent event) {
        TrafficSimulationMap trafficSimulationMap = simulation.getTrafficSimulationMap();
        //TBD. Make the followed aircraft a simulation parameter so we dont have to loop trough each aircraft to unfollow it first.
        unfollowAllPlanes(trafficSimulationMap);
        Map<String, String> variables = event.getVariables();
        if (variables.containsKey("ICAO")) {
            String icaoCode = variables.get("ICAO");
            if (trafficSimulationMap.get(icaoCode)!=null){
                TrafficSimulated followedTraffic = trafficSimulationMap.get(icaoCode);
                followedTraffic.setFollowed(true);
            }else{
                throw new IllegalArgumentException("ICAO code " + icaoCode + " does not exists in the simulation.");
            }                   
        }
    }
    
    private void unfollowAllPlanes(TrafficSimulationMap trafficSimulationMap){
        for (TrafficSimulated traffic : trafficSimulationMap.values()) {
            traffic.setFollowed(false);
        }
    }
}
