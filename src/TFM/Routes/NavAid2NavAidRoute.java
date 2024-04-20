/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Routes;
import TFM.navAids.Navaid;
import static TFM.utils.Config.dbUrl;
import TFM.utils.UnitConversion;
import classes.base.Airport;
import classes.base.Coordinate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Gabriel Alfonsín Espín
 */

public class NavAid2NavAidRoute extends Route {
    private Graph graph;
    Map<Integer, Navaid> navaids;
    private PathfindingAlgorithm pathfinder;

    public NavAid2NavAidRoute(String routeCode){
        this.navaids = new HashMap<Integer, Navaid>();
        this.graph = new Graph(175000);
        this.readNavaids("NDB","ES");
        this.fetchAirports(routeCode);
        this.pathfinder = new DijkstraAlgorithm();
        // Assume DijkstraAlgorithm has been implemented as described
    }

    private Connection connect() {
        // SQLite connection string
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
    private void fetchAirports(String routeCode) {
        String[] codes = routeCode.split("-");
        if (codes.length == 2) {
            departure = fetchAirport(codes[0]);
            destination = fetchAirport(codes[1]);
        }
    }
    
    private Airport fetchAirport(String code) {
        String quotedCode = "\"" + code + "\"";  // Adding double quotes
        String sql = "SELECT ident, name, latitude_deg, longitude_deg, elevation_ft FROM airports WHERE ident = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)) {

            pstmt.setString(1, quotedCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Airport(rs.getString("name"), rs.getString("ident"),
                                       rs.getDouble("latitude_deg"), rs.getDouble("longitude_deg"),
                                        rs.getDouble("elevation_ft")*UnitConversion.ftToMeter);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    // Example method to read navaids from SQLite
    private void readNavaids(String type, String countryCodes) {
        StringBuilder sql = new StringBuilder("SELECT * FROM navaids");

        // Lists to hold parameters for prepared statement
        List<String> parameters = Arrays.stream(countryCodes.split(","))
                                        .filter(code -> !code.isEmpty())
                                        .collect(Collectors.toList());

        boolean hasType = type != null && !type.isEmpty();
        boolean hasCountryCodes = !parameters.isEmpty();

        // Construct the WHERE clause based on provided type and country codes
        if (hasType || hasCountryCodes) {
            sql.append(" WHERE ");
            boolean needAnd = false;

            if (hasType) {
                sql.append("type = ?");
                needAnd = true;
            }

            if (hasCountryCodes) {
                if (needAnd) {
                    sql.append(" AND ");
                }
                sql.append("iso_country IN (");
                sql.append(String.join(", ", Collections.nCopies(parameters.size(), "?")));
                sql.append(")");
            }
        }

        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (hasType) {
                stmt.setString(paramIndex++, type);
            }

            if (hasCountryCodes) {
                for (String code : parameters) {
                    stmt.setString(paramIndex++, code);
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                double latitudeDeg = rs.getDouble("latitude_deg");
                double longitudeDeg = rs.getDouble("longitude_deg");
                int elevationFt = rs.getInt("elevation_ft");

                // Assuming Angle and Position are defined elsewhere with appropriate constructors
                Position pos = new Position(Angle.fromDegrees(latitudeDeg), Angle.fromDegrees(longitudeDeg), elevationFt * UnitConversion.ftToMeter + 5);
                Navaid navaid = new Navaid(pos, rs.getString("type"), id);
                navaids.put(id, navaid);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public void buildGraph(Coordinate departure, Coordinate destination, Map<Integer, Coordinate> navaids) {
        List<Coordinate> allCoordinates = new ArrayList<>();
        allCoordinates.add(departure);
        allCoordinates.add(destination);
        allCoordinates.addAll(navaids.values());

        // Initialize the graph with all coordinates without connecting them yet
        for (Coordinate coordinate : allCoordinates) {
            graph.addCoordinate(coordinate);
        }

        // Now connect the nodes that are within the maximum distance
        for (Coordinate from : allCoordinates) {
            for (Coordinate to : allCoordinates) {
                if (!from.equals(to)) {
                    graph.addEdge(from, to);
                }
            }
        }
    }
    
    
    public static void main(String[] args) {
    // Assuming these are initialized somewhere
        NavAid2NavAidRoute route = new NavAid2NavAidRoute("LEBL-LEMD");  // Set a max distance threshold in m
        Map<Integer, Coordinate> intermediatePoints = new HashMap<>();
        for (Map.Entry<Integer, Navaid> entry : route.navaids.entrySet()) {
            Position p = entry.getValue().getPos();
            Coordinate coordinate = new Coordinate(Integer.toString(entry.getValue().getId()),p.getLatitude().getDegrees(),p.getLongitude().getDegrees());
            intermediatePoints.put(entry.getKey(), coordinate);
        }
        route.buildGraph(route.departure, route.destination, intermediatePoints);
        List<Coordinate> shortestRoute = route.pathfinder.getShortestPath(route.departure, route.destination,route.graph);

        System.out.println("Solution");
        System.out.println(shortestRoute.size());
        for (Coordinate step : shortestRoute) {
            System.out.println(step.getLocationName());
        }
    }
    
    public static void mainss(String[] args) {
    // Assuming these are initialized somewhere
        Coordinate departure = new Coordinate("Departure", 40.4, -3.6);  // Example: Madrid
        Coordinate destination = new Coordinate("Destination", 39.5, 2.5);  // Example: Palma
        Map<Integer, Coordinate> navaids = new HashMap<>();
        navaids.put(1, new Coordinate("Cuenca", 40, -2.2));  // Example: Cuenca
        navaids.put(2, new Coordinate("Valencia", 39.5, -0.4));  // Example: Valencia
        navaids.put(3, new Coordinate("Ibiza", 39, 1.5));  // Example: Ibiza
        navaids.put(4, new Coordinate("Toledo", 39.9, -4));  // Example: Toledo
        navaids.put(5, new Coordinate("Avila", 40.7, -4.8));  // Example: Avila
        NavAid2NavAidRoute r = new NavAid2NavAidRoute("LEBL-LEMD");  // Set a max distance threshold in km
        
        r.buildGraph(departure, destination, navaids);
        List<Coordinate> route =  r.pathfinder.getShortestPath(departure, destination, r.graph);
        System.out.println("Solution");
        System.out.println(route.size());
        for (Coordinate step : route) {
            System.out.println(step.getLocationName());
        }
    }
}


