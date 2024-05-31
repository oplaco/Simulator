/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.GUI;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public interface TrafficPickedListener {
    void showDetails(String icaoCode, String polygonDetails);
    void hideDetails();
}
