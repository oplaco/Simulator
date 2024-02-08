/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import classes.base.TrafficSimulatedListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author fms
 */
public interface TrafficGenerator {    
    public Traffic[] getTraffics();
    ConcurrentHashMap<String, TrafficListener> getTrafficsMap();
    public byte[] getTrafficsByteArray();
    public void printTrafficMap();
}
