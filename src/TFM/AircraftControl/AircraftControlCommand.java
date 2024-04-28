/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.AircraftControl;

/**
 *
 * @author Gabriel
 * @param <T>
 */
public class AircraftControlCommand<T> {
    public enum CommandType { ALTITUDE, COURSE, ROUTE_MODE, SPEED, VERTICAL_RATE }
    private CommandType type;
    private T value; // Generic value that can be double, boolean, int, etc.
    private int priority;

    public AircraftControlCommand(CommandType type, T value, int priority) {
        this.type = type;
        this.value = value;
        this.priority = priority;
    }

    public CommandType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }
}
