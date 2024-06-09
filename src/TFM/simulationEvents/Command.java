/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.simulationEvents;
import TFM.Core.Simulation;

/**
 *
 * @author Gabriel
 */
public interface Command {
    void execute(Simulation simulation, SimulationEvent event);
}