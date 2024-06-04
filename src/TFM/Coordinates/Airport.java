/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFM.Coordinates;

import TFM.polygons.Runway;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Cylinder;
import java.util.List;

/**
 *
 * @author Gabriel Alfonsin Espin
 */
public class Airport extends Coordinate {

    private String icaoCode;
    private List<Runway> runwayList;
    private Position ARP; //Airport Reference Point
    private Cylinder cylinder;

    public Airport(String name, String icaoCode, double latitude, double longitude) {
        super(name, latitude, longitude);
        this.icaoCode = icaoCode;
        this.ARP =  Position.fromDegrees(latitude, longitude);
        this.cylinder = this.createAirpotPerimeter(ARP, name);
    }

    public Airport(String name, String icaoCode, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
        this.icaoCode = icaoCode;
        this.cylinder = this.createAirpotPerimeter(ARP, name);
    }
    
    private Cylinder createAirpotPerimeter(Position center, String name){
        // Cylinder with default orientation.
            Cylinder cylinder = new Cylinder(Position.ZERO, 60000, 60000, 60000);
            cylinder.setAltitudeMode(WorldWind.ABSOLUTE);
            cylinder.setValue(AVKey.DISPLAY_NAME, name);
            return cylinder;
    } 
   

    public String getIcaoCode() {
        return this.icaoCode;
    }

    @Override
    public String toString() {
        return this.icaoCode;
    }

}
