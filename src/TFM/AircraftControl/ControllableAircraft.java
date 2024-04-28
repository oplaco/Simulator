/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.AircraftControl;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public interface ControllableAircraft {
    public <T> void processCommand(AircraftControlCommand<T> command);
    public void resetPriority();
}

