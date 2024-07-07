/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

import TFM.Core.Simulation;

/**
 *
 * @author Gabriel
 */
public class ResumeCommand implements Command{

    @Override
    public void execute(Simulation simulation, SimulationEvent event) {
        System.out.println("ResumeCommand executed");
        simulation.play();
    }
    
}
