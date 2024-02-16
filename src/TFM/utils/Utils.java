/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.utils;

/**
 *
 * @author Gabriel
 */
public class Utils {
    public static double dmsToDd(double degrees, double minutes, double seconds) {
        return degrees + (minutes / 60.0) + (seconds / 3600.0);
    }

}
