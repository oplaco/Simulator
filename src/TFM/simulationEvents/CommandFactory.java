/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

/**
 *
 * @author Gabriel
 */
public class CommandFactory {
    public static Command getCommand(String commandType) {
        switch (commandType) {
            case "CREATE":
                return new CreateCommand();
            case "UPDATE":
                return new UpdateCommand();
            case "CENTERVIEW":
                return new CenterViewCommand();
            default:
                throw new IllegalArgumentException("Invalid command type: " + commandType);
        }
    }
}
