/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.googleearth;

import TFM.Coordinates.Coordinate;
import TFM.Core.TrafficSimulated;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms
 */
public class GoogleEarthTraffic {

    private File file;
    private PrintWriter pw;

    public GoogleEarthTraffic(String fileName, TrafficSimulated ts, String altMode, Color color, int lineWidth) {

        double iconScale = 1;
        int extrude = 0;

        this.file = new File("kmls/" + fileName + ".kml");
        try {
            pw = new PrintWriter(new FileWriter(file));
        } catch (IOException ex) {
            Logger.getLogger(GoogleEarth.class.getName()).log(Level.SEVERE, null, ex);
        }

        int alfa = (int) color.getAlpha();
        int red = (int) color.getRed();
        int green = (int) color.getGreen();
        int blue = (int) color.getBlue();

        String col = String.format("%02X%02X%02X%02X", alfa, blue, green, red);
        //Cabecera kml
        pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>");
        pw.print(String.format("\n\t<name>%s</name>", fileName));
        pw.printf("\n<Style id=\"inline1\">");
        pw.printf("\n\t<LineStyle>\n\t\t<color>%s</color>\n\t\t<width>%s</width>\n\t</LineStyle>", col, Integer.toString(lineWidth));
        pw.printf("\n\t<IconStyle>\n\t\t<scale>%s</scale>\n\t\t<Icon><href>%s</href></Icon>\n\t</IconStyle>", iconScale, "http://maps.google.com/mapfiles/kml/shapes/triangle.png");
        pw.printf("\n\t<LabelStyle>\n\t\t<color>%s</color>\n\t\t<scale>%s</scale>\n\t</LabelStyle>", col, 1);
        pw.printf("\n</Style>");
        pw.printf("\n<Placemark>\n\t<name>%s</name>\n\t<styleUrl>#inline1</styleUrl>", ts.getHexCode() + " fly");
        pw.printf("\n\t<MultiGeometry>");
        // Etiqueta
        String lon = String.format("%3.6f", ts.getPosition().getLongitude() + .0001).replace(",", ".");
        String lat = String.format("%3.6f", ts.getPosition().getLatitude() + .0001).replace(",", ".");
        String alt = String.format("%3.6f", ts.getPosition().getAltitude()).replace(",", ".");
        pw.printf("\n\t\t<Point>\n\t\t\t<altitudeMode>%s</altitudeMode>\n\t\t\t<coordinates>%s,%s,%s</coordinates>\n\t\t</Point>", altMode, lon, lat, alt);
        pw.printf("\n\t\t<LineString>\n\t\t\t<extrude>%s</extrude>\n\t\t\t<tessellate>1</tessellate>\n\t\t\t<altitudeMode>%s</altitudeMode>\n\t\t\t<coordinates>", extrude, altMode);
    }

    public void putCoordinateInKml(Coordinate c) {
        String lon = Double.toString(c.getLongitude()).replace(",", ".");
        String lat = Double.toString(c.getLatitude()).replace(",", ".");
        String alt = Double.toString(c.getAltitude()).replace(",", ".");
        pw.printf("\n\t\t\t%s,%s,%s", lon, lat, alt);
    }

    public void closeAndLaunch() {
        pw.printf("\n\t\t\t</coordinates>\n\t\t</LineString>");
        //Cierra la marca de ruta
        pw.printf("\n\t</MultiGeometry>\n</Placemark>");
        //Cierre del archivo .kml
        pw.print("\n</Document>\n</kml>\n");
        pw.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(GoogleEarth.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {    
            File mykml= new File(file.getAbsolutePath());
            Desktop.getDesktop().open(mykml);
        } catch (IOException ex) {
            Logger.getLogger(GoogleEarthTraffic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
