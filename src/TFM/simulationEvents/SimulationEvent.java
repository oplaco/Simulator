/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Gabriel Alfonsín Espín
 */



public class SimulationEvent {
    public static final Set<String> ALLOWED_COMMANDS = new HashSet<>();

    static {
        ALLOWED_COMMANDS.add("CREATE");
        ALLOWED_COMMANDS.add("UPDATE");
        ALLOWED_COMMANDS.add("DELETE");
    }
    
    private long time;
    private String command;
    private Map<String, String> variables;

    public SimulationEvent(long time, String command, Map<String, String> variables) {
        this.time = time;
        this.command = command;
        this.variables = variables;
    }
    
    
    //Check prior to the builder method that the command actually exists.
    public static SimulationEvent createEvent(long time, String command, Map<String, String> variables) {
        if (!ALLOWED_COMMANDS.contains(command)) {
            throw new IllegalArgumentException("Command '" + command + "' does not exist.");
        }
        return new SimulationEvent(time, command, variables);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(time).append(" ").append(command);
        
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                sb.append(" ").append(entry.getKey()).append(" ").append(entry.getValue());
            }
        }
        
        return sb.toString();
    }

    // Getters for time, command, and variables

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
    
}

