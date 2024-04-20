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

public class DijkstraAlgorithm implements PathfindingAlgorithm{

    @Override
    public List<Coordinate> getShortestPath(Coordinate departure, Coordinate destination, Graph graph) {
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
