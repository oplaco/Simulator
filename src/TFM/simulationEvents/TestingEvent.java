/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package TFM.simulationEvents;

/**
 *
 * @author Gabriel
 */
import java.util.List;
import java.util.Map;

public class TestingEvent {
    public static void main(String[] args) {
        String filePath = "src/simulation files/oneplane.csv"; // Replace with the actual path to your CSV file
        List<SimulationEvent> events = SimulationFileReader.readCSVFile(filePath);

        // Print the parsed simulation events
        for (SimulationEvent event : events) {
            System.out.println("Time: " + event.getTime());
            System.out.println("Command: " + event.getCommand());
            System.out.println("Variables:");
            for (Map.Entry<String, String> entry : event.getVariables().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        }
    }
}

