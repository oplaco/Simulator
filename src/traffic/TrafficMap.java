/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.msgs.*;
import org.opensky.libadsb.tools;

/*Clase que contiene los mapas de tráfico, lista de posiciones y método para actualizarlo todo*/
public class TrafficMap extends ConcurrentHashMap<String, Traffic> implements AntennaListener, TrafficGenerator {

    private AntennaReceiver ar;
    private TrafficDisplay td;
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
    
    private MrClean cleaner;
    protected TrafficListener listener = null;
    
    private boolean saveData=false;


    public TrafficMap(String host, int port, Position ref) throws IOException {
        this.ref = ref;
        this.ar = new AntennaReceiverBeast(host, port, this);
        this.td = new TrafficDisplay(this, 5000);
        this.cleaner= new MrClean(1500); 
    }

    public TrafficMap(String host, int port, Position ref, TrafficListener listener) throws IOException {
        this.ref = ref;
        this.ar = new AntennaReceiverBeast(host, port, this);
        this.cleaner= new MrClean(1500);
        this.listener = listener;
    }

    public TrafficMap(String file, Position ref) {
        this.ref = ref;
        this.ar = new AntennaEmulator(file, this);
        this.td = new TrafficDisplay(this, 5000);
        this.cleaner= new MrClean(1500);
    }

    public TrafficMap(String file, Position ref, TrafficListener listener) {
        this.ref = ref;
        this.ar = new AntennaEmulator(file, this);
        this.cleaner= new MrClean(1500);
        this.listener = listener;
    }

    public void stopit() {

        if (this.ar != null) {
            this.ar.stopit();
            while (ar.isRunning());
        }
        if (this.td != null) {
            this.td.stopit();
            while (td.isAlive());
        }
        
         if (this.cleaner != null) {
            this.cleaner.stopit();
            while (cleaner.isAlive());
        }

        System.out.println("TrafficMap finished");
        if (listener != null) {
            listener.trafficStopped();
        }
    }

    public AntennaReceiver getAntenna() {
        return ar;
    }

    public void removeTraffic(Traffic tr) {
        this.remove(tr.getICAO24());
        if (listener != null) {
            listener.removeTraffic(tr); // notify
        }
    }

    public synchronized boolean processMsg(long timestamp, ModeSReply msg) {
        Traffic tr;
        boolean ok = false;
        String icao24 = "";
        byte ftc = 0;
        int subtype = 0;
        boolean newTraffic;
        
        processTimestamp(timestamp);

        icao24 = tools.toHexString(msg.getIcao24());

        if (this.containsKey(icao24)) {
            tr = this.get(icao24);
            newTraffic = false;
        } else {
            tr = new Traffic(icao24, ref);
            newTraffic = true;
        }
        // Decode message and update Traffic
        ok = tr.updateTraffic(msg, timestamp);
        if (!ok) {
            return ok; // Corrupted messagges are not proccessed
        }
        if (newTraffic) {
            if (tr.isPositioned()) {
                this.put(icao24, tr);
                if (listener != null) {
                    listener.putTraffic(tr); // notify
                }
            }
        } else {
            if (listener != null) {
                listener.updateTraffic(tr); // notify
            }
        }
        // Housekeeping: handle aircraft removal, aircraft extrapolations
        if ((this.max_timestamp - last_housekeeping) > 1.5 * SEC || (last_housekeeping > clock)) {
            //System.out.println("Housekeeping dT="+(global_clock-last_housekeeping));
            this.houseKeeping();
            last_housekeeping = this.max_timestamp;
        }
        return ok;
    }

    protected void processTimestamp(long timestamp) {
        long diff_sc, diff_ts;
        
        if (vez1) {
            max_timestamp=timestamp-1;
            vez1 = false;
        }
 
        if (need_offset) {
            //Adjust initial clock offset
            clock_offset = System.nanoTime() - max_timestamp;
            system_clock = (System.nanoTime() - clock_offset);           
            System.out.println("SYSTEM CLOCK OFFSET: " + clock_offset);          
            need_offset = false;
        }
               
        system_clock_prev = system_clock;
        system_clock = (System.nanoTime() - clock_offset);
        diff_sc= system_clock-system_clock_prev;
        
        max_timestamp_prev = max_timestamp;
        diff_ts = timestamp - max_timestamp_prev;
        
        if (timestamp > max_timestamp) {
            // Update max_timestamp
            // if not too much difference with previous one OR
            // long time passed without receiving a traffic message
            if (diff_ts < 6e10 || diff_sc > 6e10) {
                this.max_timestamp = timestamp;
            }
            // Update clock at the highest frequency between timestamps and system_clock
            clock = Math.max(timestamp, system_clock);
            
        } else if ((max_timestamp - timestamp) > 4.3e13) { // if diff > 1/2 day
            //Suspicious message. Is it midinight?
            midnight_count++;  // Check for 10 suspicious mesages and reset

            if ((midnight_count > 9) && (max_timestamp > 7e13)) { // Midinight is detected
                
                long sec = (long) (max_timestamp / 1e9);
                long min = sec / 60;
                long hr = min / 60;
                long rem_min = min - hr * 60;
                long rem_sec = sec - rem_min * 60 - hr * 3600;               
                System.out.println("Resetting Max_TS to: " + timestamp + "TIME: " + hr + ":" + rem_min + ":" + rem_sec); 

                // Reset max_timestamp
                max_timestamp = timestamp;

                // Reset all timestamps in PositionedTraffics
                Iterator it;
                Traffic tr;
                String key;
                it = this.keySet().iterator();
                while (it.hasNext()) {
                    key = (String) it.next();
                    tr = this.get(key);
                    tr.resetTimestamps(max_timestamp);
                }                            
                need_offset=true;
                last_housekeeping = timestamp;
                midnight_count = 0;
            }
        }
        
        if (diff_sc<0) { // system clock has restarted
            need_offset=true;         
        }

    }

    // Iterate for all traffics: 
    // a) extrapolate if they did not report a new position recently (1.5 sec)
    // b) clean if we do not hear from them long time ago (60 sec)
    protected void houseKeeping() {
        Iterator it;
        Traffic tr;
        String key;
        long ts, tsp, tsa;
        long pv=0;
        List<String> victims;        
  
        it = this.keySet().iterator();
        while (it.hasNext()) {
            key = (String) it.next();
            tr = this.get(key);
            ts  = tr.getTimestamp();
            tsp = tr.getTimestampPos();
            tsa = tr.getTimestampAlt();

            long diff = this.max_timestamp - ts;

            if (diff > 30 * SEC) {
                this.removeTraffic(tr);
                //System.out.println("Removed: " + tr.getCallsign()+ " Max. TS:"+max_timestamp+"/ AC TS: "+ts+ " #: "+" diff: "+diff+this.victims.size());
            }
            // Extrapolate pos if no position message was received 2 sec ago
            if ((this.max_timestamp - tsp) > 1.5e9) { //1.5 sec
                tr.extrapolatePosition(this.max_timestamp);
            }
            if ((this.max_timestamp - tsa) > 1.5e9) { //1.5 sec
                tr.extrapolateAltitude(this.max_timestamp);
            }
        }
        
//        if (traffic_count != this.size()) {
//            traffic_count = this.size();
//            System.out.println(traffic_count);
//        }
        
    }

    public void printTrafficMap() {
        // Iterate Traffic Map calling the print method for each traffic

        java.util.Enumeration<String> enumeration = this.keys();
        String key;
        Traffic t;
        int i = 1;
        DecimalFormat df1 = new DecimalFormat("00");

        System.out.println();
        System.out.println("Traffic listing: (Total: " + this.size() + ")");
        while (enumeration.hasMoreElements()) {
            key = enumeration.nextElement();
            t = (Traffic) this.get(key);
            long last_time = System.currentTimeMillis() - t.getTimestamp();
            System.out.print(df1.format(i) + " - ");
            t.println();
            i++;
        }
    }

    
    //--------------------------------------------------------------------------
    public class MrClean extends Thread {

        private int period;
        boolean running = true;

        public MrClean(int period) {
            this.period = period;
            this.start();
        }

        // Add your task here
        public void run() {
            while (running) {
                try {
                    Thread.sleep(period);
                } catch (InterruptedException ex) {
                }
                houseKeeping();
            }
            System.out.println("Cleaner finished");
        }

        public void stopit() {
            running = false;
        }
    }
    
    @Override
    public ConcurrentHashMap<String, TrafficListener> getTrafficsMap() {
        return new ConcurrentHashMap();
    }
    

    @Override
    public byte[] getTrafficsByteArray() {
        byte[] return_value = null;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintWriter pwaos = new java.io.PrintWriter(baos);
        java.util.Enumeration<String> enumeration = this.keys();

        //Bucle per imprimir la hashtable
        Traffic i = null;

        while (enumeration.hasMoreElements()) {
            i = this.get(enumeration.nextElement());
            pwaos.println(i.getICAO24() + "," + i.getCallsign() + "," + i.getLongitude() + "," + i.getLatitude() + "," + i.getAltitude() + "," + i.getGs() + "," + i.getTrack() + "," + i.getVr() + "," + i.getSquawk());
        }

        pwaos.flush();
        return_value = baos.toByteArray();
        return return_value;
    }

    public TrafficListener getListener() {
        return listener;
    }

    @Override
    public Traffic[] getTraffics() {
        return (this.values().toArray(new Traffic[0]));
    }
 
}
