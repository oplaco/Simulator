/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import java.io.IOException;
import java.util.logging.*;

/**
 *
 * @author jvila
 */
public class TrafficDisplay extends Thread {

    TrafficMap t = null;
    public boolean running = true;
    long period=1000;

    public TrafficDisplay(TrafficMap t, long period) {
        super();
        this.t = t;
        this.period=period;
        this.start();
        System.out.println("Started TrafficMap Display with period= "+period+" msec.");
    }

    public void stopit() {
        if (this.running) {
            this.running = false;
        }
    }
        
    public void run() {
        while (running) {
            
            t.printTrafficMap();
            try {
                Thread.sleep(period);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrafficDisplay.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("TrafficDisplay finished");
    }

}


