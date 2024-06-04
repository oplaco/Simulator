/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.Routes;
import TFM.Coordinates.Coordinate;
import java.util.List;
import java.util.Map;

/**
 * Represents a pathfinding algorithm used to find the shortest path between two coordinates in a graph.
 * <p>
 * Implementations of this interface provide a method to calculate the shortest path between
 * a departure coordinate and a destination coordinate within a given graph.
 * </p>
 * <p>
 * The {@code getShortestPath} method takes the departure and destination coordinates, along with the graph
 * representing the network of vertices and edges, and returns a list of coordinates representing the shortest path.
 * </p>
 * <p>
 * Implementing classes should provide efficient algorithms to find the shortest path, such as Dijkstra's algorithm or A* search.
 * </p>
 * @author Gabriel Alfonsin Espin
 * @see Coordinate
 * @see Graph
 */
public interface PathfindingAlgorithm {
    /**
     * Finds the shortest path between the departure and destination coordinates in the given graph.
     *
     * @param departure   The starting coordinate of the path.
     * @param destination The destination coordinate of the path.
     * @param graph       The graph representing the network of vertices and edges.
     * @return A list of coordinates representing the shortest path from the departure to the destination.
     */
    List<Coordinate> getShortestPath(Coordinate departure, Coordinate destination, Graph graph);
}

