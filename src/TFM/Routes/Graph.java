/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Routes;
import classes.base.Coordinate;
import java.util.*;

/**
 *
 * @author Gabriel Alfonsín Espín
 */

public class Graph {
    private Map<Coordinate, List<Coordinate>> adjacencyList;
    private double maxDistance;

    public Graph(double maxDistance) {
        this.adjacencyList = new HashMap<>();
        this.maxDistance = maxDistance;
    }

    public void addCoordinate(Coordinate coordinate) {
        if (!adjacencyList.containsKey(coordinate)) {
            adjacencyList.put(coordinate, new ArrayList<>());
            System.out.println("Added coordinate: " + coordinate.getLocationName());
        }
    }

    public void addEdge(Coordinate from, Coordinate to) {
        if (from.getGreatCircleDistance(to) <= maxDistance) {
            adjacencyList.get(from).add(to);
            adjacencyList.get(to).add(from); // Assuming an undirected graph
            System.out.println("Connected " + from.getLocationName() + " to " + to.getLocationName() + " with distance " + from.getGreatCircleDistance(to));
        } else {
            //System.out.println("No connection between " + from.getLocationName() + " and " + to.getLocationName() + " due to distance: " + from.getGreatCircleDistance(to));
        }
    }

    public Map<Coordinate, List<Coordinate>> getAdjacencyList() {
        return adjacencyList;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
    
    
}

