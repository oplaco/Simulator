/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.Routes;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
import classes.base.Coordinate;
import java.util.List;
import java.util.Map;

public interface PathfindingAlgorithm {
    List<Coordinate> getShortestPath(Coordinate departure, Coordinate destination, Graph graph);
}

