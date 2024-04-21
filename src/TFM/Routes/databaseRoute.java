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
import classes.base.WayPoint;
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
 * Represents a route from one Navaid to another, including intermediate waypoints.
 * <p>
 * This class extends the {@link Route} class and specializes in defining routes
 * between Navaids, which are navigational aids used in aviation.
 * It provides functionality to read Navaids, fetch airports, build a graph connecting
 * departure, destination, and intermediate points, and calculate waypoints for the route.
 * </p>
 * <p>
 * The route is constructed based on a route code and a maximum distance for connecting vertices
 * in the graph. It uses a path finding algorithm.
 * </p>
 *
 * @author Gabriel Alfonsín Espín
 * @see Route
 * @see Coordinate
 * @see Navaid
 * @see Graph
 * @see DijkstraAlgorithm
 */

public class DatabaseRoute extends Route {
    private Graph graph;
    Map<Integer, Navaid> navaids;
    private PathfindingAlgorithm pathfinder;

    /**
    * Constructs a DatabaseRoute object with the given route code and maximum distance.
    * <p>
    * This constructor initializes the DatabaseRoute object by initializing necessary data structures
    * and performing the required operations such as reading Navaids, fetching airports, and calculating waypoints.
    * </p>
    *
    * @param routeCode   The code representing the route from departure to destination.
    * @param maxDistance The maximum distance for connecting vertices in the graph.
    */
    public DatabaseRoute(String routeCode, int maxDistance){
        this.navaids = new HashMap<Integer, Navaid>();
        this.graph = new Graph(maxDistance);
        this.readNavaids("NDB","ES");
        this.fetchAirports(routeCode);
        this.pathfinder = new DijkstraAlgorithm();
        this.calculateWP();
    }
    
    /**
    * Calculates waypoints for the flight route.
    * <p>
    * This method calculates waypoints for the flight route using a graph-based approach.
    * It first transforms the Navaids into intermediate points represented by coordinates,
    * then builds a graph connecting departure, destination, and intermediate points,
    * and finally solves the graph using a pathfinder algorithm to find the shortest route.
    * </p>
    *
    * @throws IllegalArgumentException if the route does not contain enough points to exclude the first and last.
    * @see Coordinate
    * @see Navaid
    * @see Graph
    * @see Pathfinder
    */
    private void calculateWP(){
        //First transform  Map<Integer, Navaid> navaids to Map<Integer, Coordinate> intermediatePoints to fit in the buildGraph()
        Map<Integer, Coordinate> intermediatePoints = new HashMap<>();
        for (Map.Entry<Integer, Navaid> entry : this.navaids.entrySet()) {
            Position p = entry.getValue().getPos();
            Coordinate coordinate = new Coordinate(Integer.toString(entry.getValue().getId()),p.getLatitude().getDegrees(),p.getLongitude().getDegrees());
            intermediatePoints.put(entry.getKey(), coordinate);
        }
        
        //Create the Graph
        this.buildGraph(this.departure, this.destination, intermediatePoints);
        
        //Solve the Graph using the pathfinder algorithm.
        List<Coordinate> route =  this.pathfinder.getShortestPath(departure, destination, this.graph);
        
        // Transforming List<Coordinate> to route class Waypoint[] wp. Setting numWaypoints 
        if (route.size() > 2) {
            // Initialize WayPoint array to the size of route - 2 to exclude the first and last
            numWaypoints = route.size() - 2;
            this.wp = new WayPoint[numWaypoints];
            int index = 0;
            // Start loop from 1 to exclude the first element and end at route.size() - 1 to exclude the last element
            for (int i = 1; i < route.size() - 1; i++) {
                Coordinate coord = route.get(i);
                this.wp[index++] = new WayPoint(coord.getLocationName(), WayPoint.FLYBY, coord.getLatitude(), coord.getLongitude());
            }
        } else {
            throw new IllegalArgumentException("Not enough points in the route to exclude first and last");
        }
    }

    /**
     * Establishes a connection to the SQLite database.
     * <p>
     * This method creates a connection to the SQLite database using the specified connection string.
     * </p>
     *
     * @return A Connection object representing the connection to the database.
     *         Returns null if the connection cannot be established.
     */
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
    
    /**
     * Fetches {@link Airport} information for the given route code.
     * <p>
     * This method parses the given route code to obtain departure and destination airport codes.
     * It then fetches the corresponding airport information for both departure and destination airports.
     * </p>
     *
     * @param routeCode The route code in the format "departure-destination" with ICAO identifier .
     *                  For example, "LEBL-LEMB" represents a route from LEBL airport to LEMB airport.
     */
    private void fetchAirports(String routeCode) {
        String[] codes = routeCode.split("-");
        if (codes.length == 2) {
            departure = fetchAirport(codes[0]);
            destination = fetchAirport(codes[1]);
        }
    }
    
    /**
     * Fetches airport information based on the provided airport ICAO code.
     * <p>
     * This method retrieves airport information such as name, latitude, longitude, and elevation
     * from the database based on the given airport code.
     * </p>
     *
     * @param code The ICAO code of the airport to fetch information for.
     *             For example, "LEST" represents Santiago de Compostela Airport.
     * @return An Airport object containing information about the airport if found; otherwise, returns null.
     */
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

    /**
     * Reads navigational aids {@link Navaid} from the database based on the provided type and country codes.
     * <p>
     * This method constructs and executes a SQL query to retrieve Navaids information from the database.
     * The query can be filtered based on the type of Navaids and/or specific country codes.
     * </p>
     *
     * @param type         The type of Navaids to filter by. Can be null or empty to include all types.
     * @param countryCodes A comma-separated string of ISO country codes to filter Navaids by country.
     *                     Codes should be in uppercase. Example: "ES,FR,DE".
     */
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
    
    /**
    * Builds a graph connecting departure, destination, and Navaids coordinates.
    * <p>
    * This method constructs a {@link Graph} by adding departure, destination, and Navaids coordinates
    * as vertices, and connecting them with edges if they are within the maximum distance.
    * </p>
    *
    * @param departure    The {@link Coordinate} representing the departure point.
    * @param destination  The {@link Coordinate} representing the destination point.
    * @param navaids      A map containing Navaids coordinates with their corresponding identifiers.
    *                     Each entry in the map represents a Navaid with its unique identifier.
    * @throws IllegalArgumentException if any of the input coordinates is null or if the map of Navaids is empty.
    */
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
    
    //Main method to test the class if desired.
    public static void main(String[] args) {
    // Assuming these are initialized somewhere
        DatabaseRoute route = new DatabaseRoute("LEST-LEPA",200000);  // Set a max distance threshold in m

        if (route.getWp() != null) {
            for (WayPoint waypoint : route.getWp()) {
                System.out.println(waypoint.getLocationName());
            }
        } else {
            System.out.println("No waypoints are available in the route.");
        }
    }
}


