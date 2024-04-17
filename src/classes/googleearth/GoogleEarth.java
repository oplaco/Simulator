/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.googleearth;

import classes.base.Coordinate;
import TFM.Routes.Route;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fms
 */
public class GoogleEarth {

    private static File file;
    private static PrintWriter pw;

    public static void PaintCoordinate(String fileName, Coordinate c) {
        String lat, lon, alt;

        lat = Double.toString(c.getLatitude()).replace(",", ".");
        lon = Double.toString(c.getLongitude()).replace(",", ".");
        alt = Double.toString(c.getAltitude()).replace(",", ".");

        String iconFile = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";
        double iconScale = 1;
        file = new File("kmls/" + fileName + ".kml");
        try {
            pw = new PrintWriter(new FileWriter(file));
        } catch (IOException ex) {
            Logger.getLogger(GoogleEarth.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Cabecera kml
        pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>");
        pw.print(String.format("\n\t<name>%s</name>", fileName));
        pw.print("\n\t<open>1</open>");
        //Bucle principal
        //Escritura del objeto en el archivo .kml
        pw.print("\n\t<Placemark>");
        pw.print(String.format("\n\t\t<name>%s</name>", c));
        pw.print("\n\t\t<open>1</open>");
        pw.print(String.format("\n\t\t<description>%s</description>", fileName));
        //Estilo
        pw.print("\n\t\t<Style id=\"1\">");
        pw.print("\n\t\t\t<LabelStyle>");
        pw.print("\n\t\t\t\t<scale>0.8</scale>");
        pw.print("\n\t\t\t</LabelStyle>");
        pw.print("\n\t\t\t<IconStyle>");
        pw.print("\n\t\t\t\t<heading>0</heading>");
        pw.print(String.format("\n\t\t\t\t<scale>%g</scale>", iconScale).replace(",", "."));
        pw.print(String.format("\n\t\t\t\t<Icon>%s</Icon>", iconFile));
        pw.print("\n\t\t\t</IconStyle>");
        pw.print("\n\t\t</Style>");
        //Control de camara
        pw.print("\n\t\t<LookAt>");
        pw.print(String.format("\n\t\t\t<longitude>%s</longitude>", lon));
        pw.print(String.format("\n\t\t\t<latitude>%s</latitude>", lat));
        pw.print(String.format("\n\t\t\t<altitude>%g</altitude>", c.getAltitude() * 0.3048).replace(",", "."));
        pw.print("\n\t\t\t<altitudeMode>absolute</altitudeMode>");
        pw.print("\n\t\t\t<heading>0</heading>");
        // pw.print("\n\t\t\t<tilt>60</tilt>");
        // pw.print("\n\t\t\t<range>15000</range>");
        pw.print("\n\t\t\t<tilt>0</tilt>");
        pw.print(String.format("\n\t\t\t<range>%g</range>", c.getAltitude() * 0.3048 + 8000).replace(",", "."));
        pw.print("\n\t\t</LookAt>");
        //Pone el punto
        pw.print("\n\t\t<Point>");
        pw.print(String.format("\n\t\t\t<coordinates>%s,%s,%s</coordinates>", lon, lat, alt));
        pw.print("\n\t\t\t<altitudeMode>absolute</altitudeMode>");
        pw.print("\n\t\t</Point>");
        //Cierra el objeto
        pw.print("\n\t</Placemark>");
        //Cierre del archivo .kml
        pw.print("\n</Document>\n</kml>\n");
        closeAndLaunch();

    }

    public static void PaintRoute(String fileName, Route r, String altMode, Color color, int lineWidth) {

        double iconScale = 1;
        int extrude = 0;

        file = new File("kmls/" + fileName + ".kml");
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
        pw.printf("\n<Placemark>\n\t<name>%s</name>\n\t<styleUrl>#inline1</styleUrl>", r.getDeparture() + "-" + r.getDestination());
        pw.printf("\n\t<MultiGeometry>");

        String lon, lat, alt;
        // Etiqueta
        if (r.getNumWaypoints() != 0) {
            
            int m = Math.round(r.getWp().length / 2);
            lon = String.format("%3.6f", r.getWp()[m].getLongitude() + .0001).replace(",", ".");
            lat = String.format("%3.6f", r.getWp()[m].getLatitude() + .0001).replace(",", ".");
            alt = String.format("%3.6f", r.getWp()[m].getAltitude()).replace(",", ".");
        } else {
            lon = String.format("%3.6f", (r.getDeparture().getLongitude()+ r.getDestination().getLongitude()) / 2).replace(",", ".");
            lat = String.format("%3.6f", (r.getDeparture().getLatitude() + r.getDestination().getLatitude()) / 2).replace(",", ".");
            alt = String.format("%3.6f", (r.getDeparture().getAltitude()+ r.getDestination().getAltitude()) / 2).replace(",", ".");
        }
        pw.printf("\n\t\t<Point>\n\t\t\t<altitudeMode>%s</altitudeMode>\n\t\t\t<coordinates>%s,%s,%s</coordinates>\n\t\t</Point>", altMode, lon, lat, alt);
        pw.printf("\n\t\t<LineString>\n\t\t\t<extrude>%s</extrude>\n\t\t\t<tessellate>1</tessellate>\n\t\t\t<altitudeMode>%s</altitudeMode>\n\t\t\t<coordinates>", extrude, altMode);

        lon = Double.toString(r.getDeparture().getLongitude()).replace(",", ".");
        lat = Double.toString(r.getDeparture().getLatitude()).replace(",", ".");
        alt = Double.toString(r.getDeparture().getAltitude()).replace(",", ".");
        pw.printf("\n\t\t\t%s,%s,%s", lon, lat, alt);

        //Bucle que recorre los puntos de la ruta.
        for (int i = 0; i < r.getNumWaypoints(); i++) {
            lon = Double.toString(r.getWp()[i].getLongitude()).replace(",", ".");
            lat = Double.toString(r.getWp()[i].getLatitude()).replace(",", ".");
            alt = Double.toString(r.getWp()[i].getAltitude()).replace(",", ".");
            pw.printf("\n\t\t\t%s,%s,%s", lon, lat, alt);
        }
        lon = Double.toString(r.getDestination().getLongitude()).replace(",", ".");
        lat = Double.toString(r.getDestination().getLatitude()).replace(",", ".");
        alt = Double.toString(r.getDestination().getAltitude()).replace(",", ".");
        pw.printf("\n\t\t\t%s,%s,%s", lon, lat, alt);

        pw.printf("\n\t\t\t</coordinates>\n\t\t</LineString>");
        //Cierra la marca de ruta
        pw.printf("\n\t</MultiGeometry>\n</Placemark>");

        // Waypoint labels
        putCoordinateInKml(r.getDeparture(), altMode);
        //Bucle que recorre los puntos de la ruta.
        for (int i = 0; i < r.getNumWaypoints(); i++) {
            putCoordinateInKml(r.getWp()[i], altMode);
        }

        putCoordinateInKml(r.getDestination(), altMode);

        //Cierre del archivo .kml
        pw.print("\n</Document>\n</kml>\n");
        closeAndLaunch();
    }

    private static void putCoordinateInKml(Coordinate c, String altMode) {
        String lon = String.format("%3.6f", c.getLongitude() + .0001).replace(",", ".");
        String lat = String.format("%3.6f", c.getLatitude() + .0001).replace(",", ".");
        String alt = String.format("%3.6f", c.getAltitude()).replace(",", ".");
        pw.printf("\n<Placemark>");
        pw.printf("\n\t<name>%s</name>", c);
        pw.printf("\n\t<description>%s</description>", c.getLocationName());
        pw.printf("\n\t<styleUrl>inline1</styleUrl>");
        pw.printf("\n\t<Point>");
        pw.printf("\n\t\t<altitudeMode>%s</altitudeMode>", altMode);
        pw.printf("\n\t\t<coordinates>");
        pw.printf("%s,%s,%s ", lon, lat, alt);
        pw.printf("</coordinates>");
        pw.printf("\n\t</Point>\n</Placemark>");
    }

    private static void closeAndLaunch() {
        pw.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(GoogleEarth.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            File mykml = new File(file.getAbsolutePath());
            Desktop.getDesktop().open(mykml);
        } catch (IOException ex) {
            Logger.getLogger(GoogleEarthTraffic.class.getName()).log(Level.SEVERE, null, ex);
        }
//        String sop = System.getProperty("os.name");
//        String path = "";
//        if (sop.equals("Windows 10")) {
//            path = "C:/Program Files/Google/Google Earth Pro/client/googleearth.exe";
//        } else {
//            path = "/Applications/Google Earth Pro.app/Contents/MacOS/Google Earth";
//        }
//        try {
//            Process p = Runtime.getRuntime().exec(new String[]{path, file.getAbsolutePath()});
//        } catch (IOException ex) {
//            Logger.getLogger(GoogleEarth.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
