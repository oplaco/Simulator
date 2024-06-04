/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Routes;
import TFM.Coordinates.Coordinate;
import java.util.*;

/**
 * Represents a graph data structure used for pathfinding algorithms.
 * <p>
 * This class provides methods to add vertices (coordinates) and edges between vertices
 * based on a maximum distance threshold. It supports undirected graphs where edges are bidirectional.
 * </p>
 * <p>
 * The graph is represented using an adjacency list, where each vertex is associated with a list of its adjacent vertices.
 * </p>
 *
 * @author Gabriel Alfonsín Espín
 * @see Coordinate
 */
public class Graph {
    private Map<Coordinate, List<Coordinate>> adjacencyList; // Adjacency list representation of the graph
    private double maxDistance; // Maximum distance threshold for connecting vertices

    /**
     * Constructs a new Graph with the specified maximum distance for connecting vertices.
     *
     * @param maxDistance The maximum distance threshold for connecting vertices in the graph.
     */
    public Graph(double maxDistance) {
        this.adjacencyList = new HashMap<>();
        this.maxDistance = maxDistance;
    }

    /**
     * Adds a new coordinate as a vertex to the graph.
     *
     * @param coordinate The coordinate to add as a vertex.
     */
    public void addCoordinate(Coordinate coordinate) {
        if (!adjacencyList.containsKey(coordinate)) {
            adjacencyList.put(coordinate, new ArrayList<>());
            System.out.println("Added coordinate: " + coordinate.getLocationName());
        }
    }

    /**
     * Adds an edge between two vertices (coordinates) in the graph if the distance between them is within the maximum distance.
     * The graph is assumed to be undirected, so the edge is added in both directions.
     *
     * @param from The starting coordinate of the edge.
     * @param to   The ending coordinate of the edge.
     */
    public void addEdge(Coordinate from, Coordinate to) {
        if (from.getGreatCircleDistance(to) <= maxDistance) {
            adjacencyList.get(from).add(to);
            adjacencyList.get(to).add(from); // Assuming an undirected graph
            System.out.println("Connected " + from.getLocationName() + " to " + to.getLocationName() + " with distance " + from.getGreatCircleDistance(to));
        } else {
            // If the distance between coordinates exceeds the maximum distance, no edge is added
            // System.out.println("No connection between " + from.getLocationName() + " and " + to.getLocationName() + " due to distance: " + from.getGreatCircleDistance(to));
        }
    }

    /**
     * Returns the adjacency list representation of the graph.
     *
     * @return The adjacency list representing the graph.
     */
    public Map<Coordinate, List<Coordinate>> getAdjacencyList() {
        return adjacencyList;
    }

    /**
     * Returns the maximum distance threshold for connecting vertices in the graph.
     *
     * @return The maximum distance threshold for connecting vertices.
     */
    public double getMaxDistance() {
        return maxDistance;
    }
}
