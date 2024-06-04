/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFM.Coordinates;

/**
 *
 * @author fms
 */
public class WayPoint extends Coordinate {

    private int type;
    public static final int FLYBY = 0;
    public static final int FLYOVER = 1;

    public WayPoint(String name, int type, double latitude, double longitude) {
        super(name, latitude, longitude);
        this.type = type;
    }

    public WayPoint(String name, int type, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
        this.type = type;
    }
}
