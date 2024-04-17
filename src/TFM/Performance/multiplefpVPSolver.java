/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

import TFM.Routes.Route;
import java.util.Map;

/**
 *
 * @author Gabriel
 */
public class multiplefpVPSolver implements VerticalProfileSolver{
    private Route route;
    private VerticalProfile verticalprofile;
    Map<String, FlightPhase> flightPhases;
    
    @Override
    public void updateTOCTOD(double cruiseSpeed) {
        flightPhases = verticalprofile.getFlightPhases();
    }
    
}
