/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.polygons;

import TFM.Simulation;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polygon;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import TFM.utils.Config;
import TFM.utils.UnitConversion;

/**
 *
 * @author Gabriel
 */
public class Runway {
    private String identifier;
    private String airport;
    private Polygon polygon;
    private Position runwayStart;
    private Position runwayEnd;
    private double bearing; 
    private double length;
    
    public Runway(String identifier, String airport, Position start, Position end, double width, double length){
        this.runwayStart = start;
        this.runwayEnd = end;
        this.identifier = identifier;
        this.bearing = getBearingFromIdentifier(identifier);
        this.airport = airport;
        this.length = length;

        ArrayList<Position> pathPositions = calculateCorners(start, end,  width);

        this.polygon = new Polygon(pathPositions);
    }
    
 private ArrayList<Position> calculateCorners(Position start, Position end, double widthMeters) {
        ArrayList<Position> corners = new ArrayList<>();

        // Calculate the heading from start to end
        double headingRadians = Math.atan2(end.getLongitude().degrees - start.getLongitude().degrees,
                                           end.getLatitude().degrees - start.getLatitude().degrees);

        // Calculate the perpendicular heading
        double perpendicularHeadingRadians = headingRadians + Math.PI / 2;

        // Convert width from meters to degrees latitude (approximation)
        double widthDegrees = widthMeters / 111320.0; // Rough approximation: 1 degree latitude = 111.32 km

        // Calculate offsets for width using simple trigonometry
        double dLat = Math.cos(perpendicularHeadingRadians) * widthDegrees;
        double dLon = Math.sin(perpendicularHeadingRadians) * widthDegrees;

        // Assuming start and end are the centerline of the runway, calculate corners
        Position corner1 = new Position(Angle.fromDegrees(start.getLatitude().degrees + dLat),
                                        Angle.fromDegrees(start.getLongitude().degrees + dLon),
                                        start.getElevation());
        Position corner2 = new Position(Angle.fromDegrees(end.getLatitude().degrees + dLat),
                                        Angle.fromDegrees(end.getLongitude().degrees + dLon),
                                        end.getElevation());
        Position corner3 = new Position(Angle.fromDegrees(end.getLatitude().degrees - dLat),
                                        Angle.fromDegrees(end.getLongitude().degrees - dLon),
                                        end.getElevation());
        Position corner4 = new Position(Angle.fromDegrees(start.getLatitude().degrees - dLat),
                                        Angle.fromDegrees(start.getLongitude().degrees - dLon),
                                        start.getElevation());

        // Add corners to the list
        corners.add(corner1);
        corners.add(corner2);
        corners.add(corner3);
        corners.add(corner4);

        return corners;
    }
        
    // Convert meters to degrees of latitude
    private double metersToLatitudeDegrees(double meters) {
        final double earthCircumference = 40075017; // Earth's circumference at the equator in meters
        double degrees = (meters / earthCircumference) * 360;
        return degrees;
    }
    
    // Convert meters to degrees of longitude at a given latitude
    private double metersToLongitudeDegrees(double meters, double latitude) {
        double latitudeRadians = Math.toRadians(latitude);
        final double earthCircumference = 40075017; // Earth's circumference at the equator in meters
        double circumferenceAtLatitude = Math.cos(latitudeRadians) * earthCircumference;
        double degrees = (meters / circumferenceAtLatitude) * 360;
        return degrees;
    }
    
    // Helper method to convert DMS to decimal degrees
    private static double dmsToDecimal(String sign,String degrees, String minutes, String seconds) {
        Double decimal = Double.parseDouble(degrees) + 
               Double.parseDouble(minutes.replace(',', '.')) / 60 + 
               Double.parseDouble(seconds) / 3600;
        
        if("S".equals(sign) || "W".equals(sign)){
            decimal = decimal*(-1);
            System.out.println(decimal);
        }
                
        return decimal;
    }

    // Method to read text file and create a Map of Runway objects
       public static List<Runway> createRunwaysFromTextFile(String textFilePath) throws IOException {
        List<Runway> runways = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(textFilePath))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                // Skip the first three lines
                if (lineCount <= 3) continue;

                // Process the data line
                String[] tokens = line.split("\\s+");

                // Assuming tokens[1] is the airport code, tokens[2] is the runway ID, and so on.
                String airportCode = tokens[0];
                String runwayName = tokens[1];
                // Convert DMS to decimal for the start position
                double startElevation = Double.parseDouble(tokens[2])*Simulation.ftToMeter;;
                double startLat = dmsToDecimal(tokens[3],tokens[4], tokens[5], tokens[6]);
                double startLon = dmsToDecimal(tokens[7],tokens[8], tokens[9], tokens[10]);
                // Convert DMS to decimal for the end position
                double endElevation = Double.parseDouble(tokens[11])*Simulation.ftToMeter;
                double endLat = dmsToDecimal(tokens[12],tokens[13], tokens[14], tokens[15]);
                double endLon = dmsToDecimal(tokens[16],tokens[17], tokens[18], tokens[19]);
                
                double width = Double.parseDouble(tokens[20]);

                // Create Position objects for start and end
                Position startPosition = new Position(Angle.fromDegrees(startLat), Angle.fromDegrees(startLon), startElevation);
                Position endPosition = new Position(Angle.fromDegrees(endLat), Angle.fromDegrees(endLon), endElevation);

                // Create the Runway object and add it to the list
                runways.add(new Runway(runwayName,airportCode, startPosition, endPosition, width, 3000));
            }
        }

        return runways;
    }

    public static List<Runway> createRunwaysFromDB(String isoCountryCode) {
        List<Runway> runways = new ArrayList<>();
        String sql = "SELECT runways.* FROM runways " +
                     "JOIN airports ON runways.airport_ref = airports.id " +
                     "WHERE airports.iso_country = ?;";

        try (Connection conn = DriverManager.getConnection(Config.dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String quotedIsoCountryCode = "\"" + isoCountryCode + "\""; // This will for example transform ES into "ES"
            pstmt.setString(1, quotedIsoCountryCode); // Set the modified iso_country code in the query
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Read each field from the current record
                    int id = rs.getInt("id");
                    int airportRef = rs.getInt("airport_ref");
                    String airportIdent = rs.getString("airport_ident");
                    int lengthFt = rs.getInt("length_ft");
                    int widthFt = rs.getInt("width_ft");
                    String surface = rs.getString("surface");
                    int lighted = rs.getInt("lighted");
                    int closed = rs.getInt("closed");
                    String leIdent = rs.getString("le_ident");
                    double leLatitudeDeg = rs.getDouble("le_latitude_deg");
                    double leLongitudeDeg = rs.getDouble("le_longitude_deg");
                    int leElevationFt = rs.getInt("le_elevation_ft");
                    double leHeadingDegT = rs.getDouble("le_heading_degT");
                    int leDisplacedThresholdFt = rs.getInt("le_displaced_threshold_ft");
                    String heIdent = rs.getString("he_ident");
                    double heLatitudeDeg = rs.getDouble("he_latitude_deg");
                    double heLongitudeDeg = rs.getDouble("he_longitude_deg");
                    int heElevationFt = rs.getInt("he_elevation_ft");
                    double heHeadingDegT = rs.getDouble("he_heading_degT");
                    int heDisplacedThresholdFt = rs.getInt("he_displaced_threshold_ft");

                    double width = widthFt*UnitConversion.ftToMeter;
                    Position start = new Position(Angle.fromDegrees(leLatitudeDeg), Angle.fromDegrees(leLongitudeDeg), leElevationFt*UnitConversion.ftToMeter+5);
                    Position end = new Position(Angle.fromDegrees(heLatitudeDeg), Angle.fromDegrees(heLongitudeDeg), heElevationFt*UnitConversion.ftToMeter+5);
                    // Assuming the Runway class has a constructor that matches the data structure
                    Runway runway = new Runway(leIdent,airportIdent,start,end,width, lengthFt*UnitConversion.ftToMeter);

                    runways.add(runway);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return runways;
    }

    public double getBearingFromIdentifier(String identifier){
        identifier = identifier.strip(); // Remove potential blank spaces
        double bearing = Double.parseDouble(identifier.substring(0, 2))*10; //Remain only with the two first digits, for instance in 13L, remain only with 13.
        return bearing;
    }
    
    public Polygon getPolygon() {
        return polygon;
    }

    public String getAirport() {
        return airport;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Position getRunwayStart() {
        return runwayStart;
    }

    public Position getRunwayEnd() {
        return runwayEnd;
    } 

    public double getLength() {
        return length;
    }

    public double getBearing() {
        return bearing;
    }
}
