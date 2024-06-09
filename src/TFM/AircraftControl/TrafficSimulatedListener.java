/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFM.AircraftControl;

import TFM.Core.TrafficSimulated;

/**
 *
 * @author fms
 */
public interface TrafficSimulatedListener {
     public void planeStarted(TrafficSimulated ts);
     public void planeUpdated(TrafficSimulated ts);
     public void planeStopped(TrafficSimulated ts);
}