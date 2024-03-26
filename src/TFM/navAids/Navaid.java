/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.navAids;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import TFM.utils.Config;
import TFM.utils.UnitConversion;
import gov.nasa.worldwind.geom.Angle;
import java.util.ArrayList;
/**
 *
 * @author Gabriel
 */
public class Navaid extends UserFacingIcon{
    private String icon_path;
    private static String picked_icon_path = "src/images/punto_amarillo.png";
    private static String other_icon_path = "src/images/punto_rosa.png";
    private Position pos;
    private String type;
    private String identifier;
    private String iso_country;
    private int frecuency_khz;
    private double magnetic_variation_deg;
    private String associated_airport;
    
    
    public Navaid(String type, String icon_path,Position pos, String identifier, String iso_country, int frecuency_khz, Double magnetic_variation_deg, String associated_airport){
        super(icon_path,pos);
        this.icon_path = icon_path;
        this.type = type;
        this.pos = pos;
        this.identifier = identifier;
        this.iso_country = iso_country;
        this.frecuency_khz = frecuency_khz;
        this.magnetic_variation_deg = magnetic_variation_deg;
        this.associated_airport = associated_airport;       
    }
    
    public static List<Navaid> createNDBsFromDB(String isoCountryCode) {
        List<Navaid> navaids = new ArrayList<>();
        String sql = "SELECT * FROM navaids " +
                     "WHERE iso_country = ?";

        try (Connection conn = DriverManager.getConnection(Config.dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isoCountryCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String filename = rs.getString("filename");
                    String ident = rs.getString("ident");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int frequencyKhz = rs.getInt("frequency_khz");
                    double latitudeDeg = rs.getDouble("latitude_deg");
                    double longitudeDeg = rs.getDouble("longitude_deg");
                    int elevationFt = rs.getInt("elevation_ft");
                    String isoCountry = rs.getString("iso_country");
                    Double dmeFrequencyKhz = rs.getDouble("dme_frequency_khz");
                    if (rs.wasNull()) {
                        dmeFrequencyKhz = null;
                    }
                    String dmeChannel = rs.getString("dme_channel");
                    Double dmeLatitudeDeg = rs.getDouble("dme_latitude_deg");
                    if (rs.wasNull()) {
                        dmeLatitudeDeg = null;
                    }

                    Double dmeLongitudeDeg = rs.getDouble("dme_longitude_deg");
                    if (rs.wasNull()) {
                        dmeLongitudeDeg = null;
                    }
                    Integer dmeElevationFt = rs.getInt("dme_elevation_ft");
                    if (rs.wasNull()) {
                        dmeElevationFt = null;
                    }

                    Double slavedVariationDeg = rs.getDouble("slaved_variation_deg");
                    if (rs.wasNull()) {
                        slavedVariationDeg = null;
                    }

                    Double magneticVariationDeg = rs.getDouble("magnetic_variation_deg");
                    if (rs.wasNull()) {
                        magneticVariationDeg = null;
                    }

                    String usageType = rs.getString("usageType");
                    String power = rs.getString("power");
                    String associatedAirport = rs.getString("associated_airport");

                    // Now that you've captured all the data, use it as needed, for example, to construct Navaid objects
                    // This is where you would call your Navaid class constructor or builder method
                    String icon_path;
                    switch (type){
                        case "DME":
                            icon_path = "src/images/DME.png";
                            break;                       
                        case "NDB":
                            icon_path = "src/images/NDB.png";
                            break;     
                        case "NDB-DME":
                            icon_path = "src/images/NDB-DME.png";
                            break;
                        case "TACAN":
                            icon_path = "src/images/TACAN.png";
                            break;
                        case "VOR":
                            icon_path = "src/images/VOR.png";
                            break;                            
                        case "VOR-DME":
                            icon_path = "src/images/VOR-DME.png";
                            break;
                        default:
                            icon_path = "src/images/punto_rojo.png";
                            break;
                    }
                    Position pos = new Position(Angle.fromDegrees(latitudeDeg), Angle.fromDegrees(longitudeDeg), elevationFt*UnitConversion.ftToMeter+5);
                    Navaid navaid = new Navaid(type, icon_path, pos, ident, isoCountry, frequencyKhz, magneticVariationDeg, associatedAirport);
                    navaids.add(navaid);
                }
            }catch (Exception e) {
                 System.out.println(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return navaids;
    }
}
