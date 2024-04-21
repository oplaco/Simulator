/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Routes;

import classes.base.Airport;
import classes.base.WayPoint;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a route for navigating between waypoints, including departure and destination airports read from a txt file.
 * <p>
 * This class extends the {@link Route} class and specializes in defining routes
 * for navigating between waypoints. It provides functionality to parse a file containing
 * information about waypoints and airports, extract relevant data, and initialize waypoints,
 * departure, and destination airports accordingly.
 * </p>
 * <p>
 * Waypoints are extracted from the file, along with departure and destination airports,
 * and are stored in an array of {@link WayPoint} objects. The route can be initialized
 * with or without specifying a cruise speed for the flight.
 * </p>
 * <p>
 * This class supports the reading of waypoint information from a file, parsing of coordinates,
 * and initialization of route data based on the provided file. Default cruise speed is set to 250 knots.
 * </p>
 * <p>
 * This class handles exceptions related to file operations such as {@link FileNotFoundException} and {@link IOException}.
 * </p>
 *
 * @author Alfredo Torres Pons, Gabriel Alfonsín Espín
 * @see Route
 * @see WayPoint
 * @see Airport
 * @see FileNotFoundException
 * @see IOException
 */
public class InputTxtRoute extends Route{
    
    /**
    * Constructs a InputTxtRoute object by parsing a file containing waypoint and airport information,
    * and initializes departure, destination, and waypoints for navigation.
    * <p>
    * This constructor reads the waypoint and airport information from the specified file.
    * It initializes the departure and destination airports based on the first and last entries in the file,
    * and initializes the intermediate waypoints using the remaining entries.
    * The number of waypoints is determined by the total number of entries in the file minus 3 (departure, destination, and header).
    * </p>
    * <p>
    * This constructor allows specifying the cruise speed for the flight.
    * </p>
    *
    * @param archivo     The path to the file containing waypoint and airport information.
    * @param cruiseSpeed The cruise speed for the flight, in knots.
    * @throws FileNotFoundException If the specified file is not found.
    * @throws IOException           If an I/O error occurs while reading the file.
    */
    public InputTxtRoute(String archivo, double cruiseSpeed) throws FileNotFoundException, IOException {
        String cadena, value[];
        FileReader f = new FileReader(archivo);
        Airport airport;
        BufferedReader b = new BufferedReader(f);// (new InputStreamReader(System.in));

        Path path = Paths.get(archivo);
        long lines = Files.lines(path).count();

        b.readLine(); // salta la cabecera
        cadena = b.readLine(); // aeropuerto origen

        departure = extractAirport(cadena);

        numWaypoints = (int) (lines - 3);
        wp = new WayPoint[numWaypoints];

        for (int i = 0; i < numWaypoints; i++) { // waypoints   
            cadena = b.readLine();
            wp[i] = extractWayPoint(cadena);
        }

        cadena = b.readLine(); // aeropuerto destino
        destination = extractAirport(cadena);
        cruiseSpeed = cruiseSpeed;

    }
    
    /**
    * Constructs a InputTxtRoute object by parsing a file containing waypoint and airport information,
    * and initializes departure, destination, and waypoints for navigation.
    * <p>
    * This constructor reads the waypoint and airport information from the specified file.
    * It initializes the departure and destination airports based on the first and last entries in the file,
    * and initializes the intermediate waypoints using the remaining entries.
    * The number of waypoints is determined by the total number of entries in the file minus 3 (departure, destination, and header).
    * </p>
    * <p>
    * This constructor uses a default cruise speed of 250 knots for the flight.
    * </p>
    *
    * @param archivo The path to the file containing waypoint and airport information.
    * @throws FileNotFoundException If the specified file is not found.
    * @throws IOException           If an I/O error occurs while reading the file.
    */
    public InputTxtRoute(String archivo) throws FileNotFoundException, IOException {
        String cadena, value[];
        FileReader f = new FileReader(archivo);
        Airport airport;
        BufferedReader b = new BufferedReader(f);// (new InputStreamReader(System.in));

        Path path = Paths.get(archivo);
        long lines = Files.lines(path).count();

        b.readLine(); // salta la cabecera
        cadena = b.readLine(); // aeropuerto origen

        departure = extractAirport(cadena);

        numWaypoints = (int) (lines - 3);
        wp = new WayPoint[numWaypoints];

        for (int i = 0; i < numWaypoints; i++) { // waypoints   
            cadena = b.readLine();
            wp[i] = extractWayPoint(cadena);
        }

        cadena = b.readLine(); // aeropuerto destino
        destination = extractAirport(cadena);
        cruiseSpeed = 250; // 250 knots por defecto

    }
    
    /**
    * Extracts airport information from the provided string and creates an {@link Airport} object.
    * <p>
    * This method parses the provided string to extract airport information such as ICAO code, name, latitude, and longitude.
    * It then creates an {@link Airport} object with the extracted information.
    * </p>
    *
    * @param cad The string containing airport information.
    * @return An {@link Airport} object representing the extracted airport information.
    */
    private Airport extractAirport(String cad) {
        Airport aip = null;
        String[] value;

        cad = cad.trim();
        value = cad.split("\\s+"); // separa cadenas con uno o varios espacios
        int i = 0;
        boolean seguir = true;
        do {
            seguir = value[i].charAt(0) != 'N' && value[i].charAt(0) != 'S';
            i++;
        } while (seguir);

        String icao = value[0];
        String name = "";
        for (int j = 0; j < value.length - i - 1; j++) { // por si el nombre tiene espacios en blanco
            name = name + " " + value[i + 1 + j];  //  tomamos todas las palabras 
        }
        double lat = DmsToDecimal(value[i - 1], 1); // latitud
        double lon = DmsToDecimal(value[i], 2); // longitud

        aip = new Airport(name, icao, lat, lon); // creamnos el aeropuerto
        return aip;
    }
    
    /**
    * Extracts waypoint information from the provided string and creates a {@link WayPoint} object.
    * <p>
    * This method parses the provided string to extract waypoint information such as name, type, latitude, and longitude.
    * It then creates a {@link WayPoint} object with the extracted information.
    * </p>
    *
    * @param cad The string containing waypoint information.
    * @return A {@link WayPoint} object representing the extracted waypoint information.
    */
    private WayPoint extractWayPoint(String cad) {
        WayPoint wayPoint;
        String[] value;

        cad = cad.trim();
        value = cad.split("\\s+"); // separa cadenas con uno o varios espacios
        int i = 1; // comenzamos despues del nombre ya que éste puede comenzar por "N" "S" "E" "W"
        boolean seguir = true;
        do { // buscamos una cadena que comience por N, S, E o W pues ésta contendrá las coordenadas
            seguir = value[i].charAt(0) != 'N' && value[i].charAt(0) != 'S' && value[i].charAt(0) != 'E' && value[i].charAt(0) != 'W';
            i++;
        } while (seguir);
        // latitud detectada en i - 1
        String name = value[0];
        int type = WayPoint.FLYBY; // por simplicidad dejamos este modo
        double lat = DmsToDecimal(value[i - 1], 1); // la primera es la latitud
        double lon = DmsToDecimal(value[i], 2); // la sefunda cadena es la longitud

        wayPoint = new WayPoint(name, type, lat, lon);  // creamos el wp

        return wayPoint;
    }
    
}
