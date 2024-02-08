/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.ModeSReply;
import org.opensky.libadsb.tools;

/**
 *
 * @author fms
 */
public class AntennaEmulator extends java.lang.Thread implements AntennaReceiver {

    private AntennaListener tm;
    private String file;
    private boolean running = false, change = false;

    public AntennaEmulator(String file, AntennaListener tm) {
        this.file = file;
        this.tm = tm;
        this.running = true;
        this.start();
    }

    public void startit() {
        this.running = true;
        this.start();
    }

    public void stopit() {
        if (this.running) {
            this.running = false;
        }
    }
    
    public boolean isRunning(){
        return this.isAlive();
    }

    @Override
    public synchronized void run() {
        this.running = true;
        try {
            readFlights(file);
            // TODO: exercise 2. Replace the previous instruction by readFlightsTraceOpt(file);
        } catch (IOException ex) {
            //Logger.getLogger(AntennaEmulator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Could not read file: " + file);
        }
    }

    public void readFlights(String archivo) throws FileNotFoundException, IOException {
        String cadena, value[];
        FileReader f = new FileReader(archivo);
        BufferedReader b = new BufferedReader(f);
        ModeSReply msg = null;
        boolean status;
        long timestamp;

        while (this.running && (cadena = b.readLine()) != null) {
            cadena = cadena.trim();
            value = cadena.split(",");
            timestamp = Long.valueOf(value[0]);
            try {
                msg = AntennaReceiverBeast.getADSBdecoder().decode(value[1]);
            } catch (BadFormatException ex) {
                //Logger.getLogger(AntennaEmulator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnspecifiedFormatError ex) {
                //Logger.getLogger(AntennaEmulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (tm != null) {
                synchronized (tm) {
                    //System.out.println(timestamp+ " " + value[1]);
                    status = tm.processMsg(timestamp, msg);
                    //if (status == false) {
                    //System.out.println("AntennaEmulator: error updating message:" + message);
                    //}
                }
            } else {
                System.out.println("MSG " + tools.toHexString(msg.getIcao24()) + " " + msg.getType());
            }

            try {
                Thread.sleep((long) (1));
            } catch (InterruptedException ex) {
                //Logger.getLogger(AntennaEmulator.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        b.close();

        System.out.println("AntennaEmulator: finished.");

    }

    @Override
    public void setListener(AntennaListener al) {
        this.tm = al;
    }
}
