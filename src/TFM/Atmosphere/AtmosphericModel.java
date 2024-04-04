/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package TFM.Atmosphere;

/**
 *
 * @author Gabriel Alfonsín Espín
 */
public interface AtmosphericModel {
    public double calculatePressure(double GeometricAltitdue);
    public double calculateDensity(double GeometricAltitdue);
    public double calculateTemperature(double GeometricAltitdue);
    public double calculateSpeedOfSound(double AbsoluteTemperature);
}
