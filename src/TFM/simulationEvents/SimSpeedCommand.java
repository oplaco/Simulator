/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

import TFM.Core.Simulation;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class SimSpeedCommand implements Command{

    @Override
    public void execute(Simulation simulation, SimulationEvent event) {
        String command = event.getCommand();
        Map<String, String> variables = event.getVariables();
        if (variables.containsKey("factor")) {
            String factor = variables.get("factor");
            try {
                double dfactor = Double.parseDouble(factor);
                simulation.setSpeed(dfactor);
            } catch (Exception e) {
                throw new IllegalArgumentException("factor " + factor + " can not be parsed into int.");
            }
        }else{
            throw new IllegalArgumentException("factor variable is missing");
        }
    }
    
}
