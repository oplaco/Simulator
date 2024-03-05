/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

import TFM.Simulation;
import classes.base.TrafficSimulated;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class CenterViewCommand implements Command {
    @Override
    public void execute(Simulation simulation, SimulationEvent event) {
        Map<String, String> variables = event.getVariables();
        if (variables.containsKey("ICAO")) {
            String icaoCode = variables.get("ICAO");
            if (simulation.getTrafficSimulationMap().get(icaoCode)!=null){
                TrafficSimulated followedTraffic = simulation.getTrafficSimulationMap().get(icaoCode);
                followedTraffic.setFollowed(true);
            }else{
                throw new IllegalArgumentException("ICAO code " + icaoCode + " does not exists in the simulation.");
            }                   
        }
    }
}
