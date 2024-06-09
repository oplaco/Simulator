/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.GUI;

import TFM.Core.TrafficSimulated;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public interface TrafficPickedListener {
    void showDetails(TrafficSimulated traffic);
    void hideDetails();
}
