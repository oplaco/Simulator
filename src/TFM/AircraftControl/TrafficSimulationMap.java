/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFM.AircraftControl;

import TFM.AircraftControl.TrafficSimulated;
import TFM.AircraftControl.TrafficSimulatedListener;
import traffic.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.msgs.*;
import org.opensky.libadsb.tools;

/* TrafficMap that reads information from simulation file instead of adsb messages.*/
public class TrafficSimulationMap extends ConcurrentHashMap<String, TrafficSimulated> {

    protected Position ref;
    
    private final long SEC = 1000000000L; //ns

    private long  clock = 0;
    private long  max_timestamp = 0;
    private long  system_clock=0;
    private long  clock_offset =0;
    private long  max_timestamp_prev=0;
    private long  system_clock_prev=0;
    private long  last_housekeeping = 0;
    private int midnight_count=0;
    private boolean need_offset=true;
    private boolean vez1=true;
    
    int traffic_count=0;
  
    protected TrafficSimulatedListener listener = null; // The listener must be the TrafficDisplayer.
    
    private boolean saveData=false;


    public TrafficSimulationMap(TrafficSimulatedListener listener, Position ref) throws IOException {
        this.ref = ref;
        this.listener = listener;
    }

    public TrafficSimulationMap(){
    }
    

//    public void stopit() {
//
//        if (this.ar != null) {
//            this.ar.stopit();
//            while (ar.isRunning());
//        }
//        if (this.td != null) {
//            this.td.stopit();
//            while (td.isAlive());
//        }
//        
//         if (this.cleaner != null) {
//            this.cleaner.stopit();
//            while (cleaner.isAlive());
//        }
//
//        System.out.println("TrafficMap finished");
//        if (listener != null) {
//            listener.trafficStopped();
//        }
//    }



    public void removeTraffic(TrafficSimulated tr) {
        this.remove(tr.getHexCode());
        if (listener != null) {
            listener.planeStopped(tr); // notify
        }
    }
    
    public void putTraffic(TrafficSimulated tr) {
        this.put(tr.getHexCode(),tr);
        if (listener != null) {
            listener.planeStarted(tr); // notify
        }
    }
    
    public void updateTraffic(TrafficSimulated tr) {
        //update the traffic also.
        if (listener != null) {
            listener.planeUpdated(tr); // notify
        }
    }
    


//    public void printTrafficMap() {
//        // Iterate Traffic Map calling the print method for each traffic
//
//        java.util.Enumeration<String> enumeration = this.keys();
//        String key;
//        TrafficSimulated t;
//        int i = 1;
//        DecimalFormat df1 = new DecimalFormat("00");
//
//        System.out.println();
//        System.out.println("Traffic listing: (Total: " + this.size() + ")");
//        while (enumeration.hasMoreElements()) {
//            key = enumeration.nextElement();
//            t = (Traffic) this.get(key);
//            long last_time = System.currentTimeMillis() - t.getTimestamp();
//            System.out.print(df1.format(i) + " - ");
//            t.println();
//            i++;
//        }
//    }

    
    //--------------------------------------------------------------------------
    
    public void printTrafficSimulationMap() {
        System.out.println("TrafficSimulated Map:");
        for (Map.Entry<String, TrafficSimulated> entry : entrySet()) {
            String key = entry.getKey();
            TrafficSimulated value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }
 
    public TrafficSimulated[] getTraffics() {
        List<TrafficSimulated> trafficList = new ArrayList<>(values());
        TrafficSimulated[] trafficsArray = new TrafficSimulated[trafficList.size()];
        return trafficList.toArray(trafficsArray);
    }

    public TrafficSimulatedListener getListener() {
        return listener;
    }


    
    
 
}
