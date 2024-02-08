/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

/**
 *
 * @author fms
 */
public class Airport extends Coordinate {

    private String icaoCode;

    public Airport(String name, String icaoCode, double latitude, double longitude) {
        super(name, latitude, longitude);
        this.icaoCode = icaoCode;
    }

    public Airport(String name, String icaoCode, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
        this.icaoCode = icaoCode;
    }

    public String getIcaoCode() {
        return this.icaoCode;
    }

    @Override
    public String toString() {
        return this.icaoCode;
    }

}
