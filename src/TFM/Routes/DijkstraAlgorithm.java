/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Routes;
import TFM.Coordinates.Coordinate;
import java.util.*;
/**
 * Represents an implementation of Dijkstra's algorithm for finding the shortest path in a graph.
 * <p>
 * This class implements the {@link PathfindingAlgorithm} interface and provides functionality to find
 * the shortest path between two coordinates in a graph using Dijkstra's algorithm.
 * </p>
 * <p>
 * Dijkstra's algorithm works by iteratively relaxing the distances of vertices from the source vertex,
 * updating the shortest path to each vertex until the destination vertex is reached.
 * </p>
 *
 * @author Gabriel Alfonsín Espín
 * @see PathfindingAlgorithm
 * @see Coordinate
 * @see Graph
 */
public class DijkstraAlgorithm implements PathfindingAlgorithm{
    /**
     * Finds the shortest path between the departure and destination coordinates in the given graph.
     * <p>
     * This method implements Dijkstra's algorithm to find the shortest path between the departure
     * and destination coordinates in the provided graph.
     * </p>
     * <p>
     * It initializes all vertices with infinite distance from the departure point, except for the departure
     * point itself, which is initialized with a distance of 0. It then iteratively relaxes the distances
     * of vertices from the departure point until the destination is reached or all vertices are visited.
     * </p>
     *
     * @param departure   The starting coordinate of the path.
     * @param destination The destination coordinate of the path.
     * @param graph       The graph representing the network of vertices and edges.
     * @return A list of coordinates representing the shortest path from the departure to the destination.
     */
    @Override
    public List<Coordinate> getPath(Coordinate departure, Coordinate destination, Graph graph) {
        Map<Coordinate, Double> distances = new HashMap<>();
        Map<Coordinate, Coordinate> predecessors = new HashMap<>();
        PriorityQueue<Coordinate> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        // Initialize all nodes with infinite distance, except for the departure point
        for (Coordinate vertex : graph.getAdjacencyList().keySet()) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
            predecessors.put(vertex, null);
            queue.add(vertex);
        }
        distances.put(departure, 0.0);
        // Reinsert departure to update its position in the priority queue
        queue.remove(departure);
        queue.add(departure);

        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();

            // Early termination if we reach the destination
            if (current.equals(destination)) {
                break;
            }

            // Relaxation step
            for (Coordinate neighbor : graph.getAdjacencyList().get(current)) {
                double alt = distances.get(current) + current.getGreatCircleDistance(neighbor);
                if (alt < distances.get(neighbor)) {
                    distances.put(neighbor, alt);
                    predecessors.put(neighbor, current);
                    // Update priority queue
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return buildPath(destination, predecessors);
    }

    /**
     * Builds the shortest path from the destination to the departure coordinate using predecessors map.
     * <p>
     * This method constructs the shortest path from the destination to the departure coordinate
     * using the predecessors map obtained during the execution of Dijkstra's algorithm.
     * </p>
     *
     * @param destination  The destination coordinate of the path.
     * @param predecessors A map containing the predecessor of each coordinate in the shortest path.
     * @return A list of coordinates representing the shortest path from the destination to the departure.
     */
    private List<Coordinate> buildPath(Coordinate destination, Map<Coordinate, Coordinate> predecessors) {
        LinkedList<Coordinate> path = new LinkedList<>();
        for (Coordinate at = destination; at != null; at = predecessors.get(at)) {
            path.addFirst(at);
        }
        // Check if the path starts at the destination and has more than one node to ensure it's valid
        if (path.size() == 1 && !path.getFirst().equals(destination)) {
            return Collections.emptyList();  // No path found
        }
        return path;
    }
}
