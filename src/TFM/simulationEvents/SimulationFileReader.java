/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.simulationEvents;

/**
 *
 * @author Gabriel Alfonsín Espín
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SimulationFileReader {
    public static List<SimulationEvent> readCSVFile(String filePath) {
        List<SimulationEvent> events = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                SimulationEvent event = getEventFromString(line);
                if (event != null) {
                    events.add(event);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return events;
    }
    
    public static SimulationEvent getEventFromString(String line){
        String[] parts = line.split(",");
        if (parts.length >= 3) {
            long time = Long.parseLong(parts[0]);
            String command = parts[1];
            Map<String, String> variables = new HashMap<>();
            for (int i = 2; i < parts.length; i++) {
                String[] varPair = parts[i].split(":");
                if (varPair.length == 2) {
                    variables.put(varPair[0].trim(), varPair[1].trim());
                }
            }
            SimulationEvent event = SimulationEvent.createEvent(time, command, variables);
            return event;
        } else if(parts.length == 2){
            long time = Long.parseLong(parts[0]);
            String command = parts[1];
            SimulationEvent event = SimulationEvent.createEvent(time, command, null);
            return event;
        }
    
        return null;
    }
    
    public static SimulationEvent createEventFromString(long time,String line){
        String[] parts = line.split(",");
        if (parts.length >= 2) {
            String command = parts[0];
            Map<String, String> variables = new HashMap<>();
            for (int i = 1; i < parts.length; i++) {
                String[] varPair = parts[i].split(":");
                if (varPair.length == 2) {
                    variables.put(varPair[0].trim(), varPair[1].trim());
                }
            }
            SimulationEvent event = SimulationEvent.createEvent(time, command, variables);
            return event;
        } else if (parts.length == 1){
            String command = parts[0];
            SimulationEvent event = SimulationEvent.createEvent(time, command, null);
            return event;
        }
        return null;
    }
}
