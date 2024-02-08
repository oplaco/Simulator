/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import org.opensky.libadsb.msgs.ModeSReply;

/**
 *
 * @author jvila
 */
public interface AntennaListener {
    boolean processMsg(long timestamp, ModeSReply msg);
    
}
