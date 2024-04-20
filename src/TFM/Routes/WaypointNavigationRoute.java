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
 *
 * @author Gabriel Alfonsín Espín
 * 
 */
public class WaypointNavigationRoute extends Route{
    
    public WaypointNavigationRoute(String archivo, double cruiseSpeed) throws FileNotFoundException, IOException {
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
    
    public WaypointNavigationRoute(String archivo) throws FileNotFoundException, IOException {
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
