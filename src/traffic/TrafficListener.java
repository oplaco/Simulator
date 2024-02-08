/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import org.opensky.libadsb.ModeSDecoder;

/**
 *
 * @author fms
 */
public interface TrafficListener {
    public  void putTraffic(Traffic t);
    public  void removeTraffic(Traffic t);
    public  void updateTraffic(Traffic t);   
    public  void trafficStopped();
}

//    public  void updateFlights();
//    public  void addCombo(String s); 
//    public  void removeCombo(String s);
//    public  void setFlightDeleted(String s);
//    public  void setFlightAdded(String s);  
