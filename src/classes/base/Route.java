/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes.base;

import classes.googleearth.GoogleEarth;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author fms & Alfredo Torres Pons
 */
public class Route {

    private Airport departure;
    private Airport destination;
    private int numWaypoints;
    private WayPoint[] wp;
    private double speed;
    
    private Coordinate tocPos;
    private Coordinate todPos;
    
    /**
     * Set the TOD or TOC positions
     * @param H altitude
     * @param wp1 last wp -1 if departure
     * @param wp2 nex wp >=length if destination
     * @param distToWp1 distance to wp 1
     * @param TOC true if set TOC false if set TOD
     */
    public void setTOCTOD(double H,int wp1,int wp2,double distToWp1,boolean TOC)
    {
        Coordinate c1,c2;
        
        if(wp1<0)
        {
            c1=departure;
        }
        else
        {
            c1=wp[wp1];
        }
        if(wp2>=wp.length)
        {
            c2=destination;
        }
        else
        {
            c2=wp[wp2];
        }
        
        double lat = c1.getLatitude() + distToWp1 *(c2.getLatitude()-c1.getLatitude())/(c1.getGreatCircleDistance(c2));
        double lon = c1.getLongitude()+ distToWp1 *(c2.getLongitude()-c1.getLongitude())/(c1.getGreatCircleDistance(c2));
        
        if(TOC)
        {
            tocPos = new Coordinate("TOC", lat, lon, H);
        }
        else
        {
            todPos = new Coordinate("TOD", lat, lon, H);
        }
        

    }
    
    
    public Route(Airport departure, Airport destination, WayPoint[] wp) {
        this.departure = departure;
        this.destination = destination;
        this.numWaypoints = wp.length;
        this.wp = wp;
        this.speed = 100; // default 100 knots 
    }

    public Route(Airport departure, Airport destination, WayPoint[] wp, double speed) {
        this.departure = departure;
        this.destination = destination;
        this.numWaypoints = wp.length;
        this.wp = wp;
        this.speed = speed;
    }

    public Route(Airport departure, Airport destination) {
        this.departure = departure;
        this.destination = destination;
        this.numWaypoints = 0;
        this.wp = null;
        this.speed = 100; // default 100 knots 
    }
    
    public Route(){
        this.departure = new Airport("Valencia","VLC",39.486998052,-0.475664);
        this.destination = new Airport("Sao Paulo","SBGR",-23.42583163,-46.46833);;
        this.numWaypoints = 0;
        this.wp = null;
        this.speed = 100; // default 100 knots 
    }

    public Route(Airport departure, Airport destination, double speed) {
        this.departure = departure;
        this.destination = destination;
        this.numWaypoints = 0;
        this.wp = null;
        this.speed = speed;
        
    }

    public Route(String archivo) throws FileNotFoundException, IOException {
        String cadena, value[];
        FileReader f = new FileReader(archivo);
        Airport airport;
        BufferedReader b = new BufferedReader(f);// (new InputStreamReader(System.in));

        Path path = Paths.get(archivo);
        long lines = Files.lines(path).count();

        b.readLine(); // salta la cabecera
        cadena = b.readLine(); // aeropuerto origen

        departure = extractAirport(cadena);

        numWaypoints = (int) (lines - 3);
        wp = new WayPoint[numWaypoints];

        for (int i = 0; i < numWaypoints; i++) { // waypoints   
            cadena = b.readLine();
            wp[i] = extractWayPoint(cadena);
        }

        cadena = b.readLine(); // aeropuerto destino
        destination = extractAirport(cadena);
        speed = 100; // 100 knots por defecto

    }

    public Route(String archivo, double speed) throws FileNotFoundException, IOException {
        String cadena, value[];
        FileReader f = new FileReader(archivo);
        Airport airport;
        BufferedReader b = new BufferedReader(f);// (new InputStreamReader(System.in));

        Path path = Paths.get(archivo);
        long lines = Files.lines(path).count();

        b.readLine(); // salta la cabecera
        cadena = b.readLine(); // aeropuerto origen

        departure = extractAirport(cadena);

        numWaypoints = (int) (lines - 3);
        wp = new WayPoint[numWaypoints];

        for (int i = 0; i < numWaypoints; i++) { // waypoints   
            cadena = b.readLine();
            wp[i] = extractWayPoint(cadena);
        }

        cadena = b.readLine(); // aeropuerto destino
        destination = extractAirport(cadena);
        speed = speed;

    }

    private WayPoint extractWayPoint(String cad) {
        WayPoint wp = null;
        String[] value;

        cad = cad.trim();
        value = cad.split("\\s+"); // separa cadenas con uno o varios espacios
        int i = 1; // comenzamos despues del nombre ya que éste puede comenzar por "N" "S" "E" "W"
        boolean seguir = true;
        do { // buscamos una cadena que comience por N, S, E o W pues ésta contendrá las coordenadas
            seguir = value[i].charAt(0) != 'N' && value[i].charAt(0) != 'S' && value[i].charAt(0) != 'E' && value[i].charAt(0) != 'W';
            i++;
        } while (seguir);
        // latitud detectada en i - 1
        String name = value[0];
        int type = WayPoint.FLYBY; // por simplicidad dejamos este modo
        double lat = DmsToDecimal(value[i - 1], 1); // la primera es la latitud
        double lon = DmsToDecimal(value[i], 2); // la sefunda cadena es la longitud

        wp = new WayPoint(name, type, lat, lon);  // creamos el wp

        return wp;
    }

    private Airport extractAirport(String cad) {
        Airport aip = null;
        String[] value;

        cad = cad.trim();
        value = cad.split("\\s+"); // separa cadenas con uno o varios espacios
        int i = 0;
        boolean seguir = true;
        do {
            seguir = value[i].charAt(0) != 'N' && value[i].charAt(0) != 'S';
            i++;
        } while (seguir);

        String icao = value[0];
        String name = "";
        for (int j = 0; j < value.length - i - 1; j++) { // por si el nombre tiene espacios en blanco
            name = name + " " + value[i + 1 + j];  //  tomamos todas las palabras 
        }
        double lat = DmsToDecimal(value[i - 1], 1); // latitud
        double lon = DmsToDecimal(value[i], 2); // longitud

        aip = new Airport(name, icao, lat, lon); // creamnos el aeropuerto
        return aip;
    }

    public double DmsToDecimal(String coordenada, int type) {
        // convierte de grados, minutos, segundos a decimal
        // considera las diferencias de formato entre lat y lon
        String grados = null;
        String minutos = null;
        String segundos = null;
        String direccion = null;

        switch (type) {
            case 1: // latitude
                grados = coordenada.substring(1, 3);
                minutos = coordenada.substring(4, 6);
                segundos = coordenada.substring(7, coordenada.length() - 1);
                break;
            case 2: // longitude
                grados = coordenada.substring(1, 4);
                minutos = coordenada.substring(5, 7);
                segundos = coordenada.substring(8, coordenada.length() - 1);
                break;
            default:
        }
        direccion = coordenada.substring(0);
        double decimal = Math.abs(Double.parseDouble(grados)) + (Double.parseDouble(minutos) / 60.0) + (Double.parseDouble(segundos) / 3600.0);
        if (direccion.substring(0, 1).equals("S") || direccion.substring(0, 1).equals("W")) {
            decimal *= -1;
        }
        return decimal;
    }

    public double greatCircleLength() { // meters
        int i;
        double distance;

        if (wp != null) {
            distance = departure.getGreatCircleDistance(wp[0]);
            for (i = 0; i < numWaypoints - 1; i++) {
                distance += wp[i].getGreatCircleDistance(wp[i + 1]);
            }
            distance = distance + wp[i].getGreatCircleDistance(destination);
        } else {
            distance = departure.getGreatCircleDistance(destination);
        }
        return distance;
    }

    public double greatCircleTime() { // mins 

        return greatCircleLength() / (speed * 1852 / 3600);

    }

    public double rhumbLength() { // meters
        int i;
        double distance;

        if (wp != null) {
            distance = departure.getRhumbLineDistance(wp[0]);
            for (i = 0; i < numWaypoints - 1; i++) {
                distance += wp[i].getRhumbLineDistance(wp[i + 1]);
            }
            distance = distance + wp[i].getRhumbLineDistance(destination);
        } else {
            distance = departure.getRhumbLineDistance(destination);
        }
        return distance;

    }


    public void print() {
        // route characteristics 
        double geoDistance, geoBearing, rhumbDistance, rhumbBearing, ortoLength, loxoLength;
        String geoDistanceStr, geoBearingStr, rhumbDistanceStr, rhumbBearingStr, ortoLengthStr, loxoLengthStr;

        geoDistance = departure.getGreatCircleDistance(destination) / 1852;
        geoDistanceStr = String.format("%.2f", geoDistance);
        geoBearing = departure.getGreatCircleInitialBearing(destination);
        geoBearingStr = String.format("%.2f", geoBearing);

        System.out.println("GreatCircle distance from " + departure.getLocationName() + " to " + destination.getLocationName() + " is " + geoDistanceStr + " NM");
        System.out.println("Initial Bearing: " + geoBearingStr);
        rhumbDistance = departure.getRhumbLineDistance(destination) / 1852;
        rhumbDistanceStr = String.format("%.2f", rhumbDistance);
        rhumbBearing = departure.getRhumbLineBearing(destination);
        rhumbBearingStr = String.format("%.2f", rhumbBearing);
        rhumbBearingStr = String.format("%.2f", rhumbBearing);
        System.out.println("Rhumb line distance from " + departure.getLocationName() + " to " + destination.getLocationName() + " is " + rhumbDistanceStr + " NM");
        System.out.println("Bearing: " + rhumbBearingStr);

        ortoLength = greatCircleLength() / 1852;
        ortoLengthStr = String.format("%.2f", ortoLength);
        System.out.println("ORTHODROMIC. Route distance : " + ortoLengthStr + " NM, Fly time: " + Math.round(greatCircleTime() / 60) + " Mins");

        loxoLength = rhumbLength() / 1852;
        loxoLengthStr = String.format("%.2f", loxoLength);
        System.out.println("LOXODROMIC. Route distance : " + loxoLengthStr + " NM, Fly time: " + Math.round(rhumbTime() / 60) + " Mins");
    }

    public void paint(String fileName, String altMode, Color col, int tick) {
        GoogleEarth.PaintRoute(fileName, this, altMode, col, tick);
    }

    public Coordinate computeCoordinateOverTime(TrafficSimulated a, double mins) {
        // Calcula la coordenada alcanzada por un avión que vuela esta ruta 
        // ortodrómicamente transcurridos los minutos indicados
        double metros, distancia, bearing;
        int i;
        Coordinate coord = null;

        metros = (a.getSpeed() * 1852 / 3600) * mins * 60;
        distancia = departure.getGreatCircleDistance(wp[0]);
        if (metros < distancia) {
            // la coordenada está en el primer tramo
            bearing = departure.getGreatCircleInitialBearing(wp[0]);
            coord = departure.getGreatCircleDestination(metros, bearing);

        } else { // la coordenada se encuentra más allá del primer tramo
            for (i = 0; i < numWaypoints - 1; i++) {
                metros = metros - distancia;
                distancia = wp[i].getGreatCircleDistance(wp[i + 1]);
                if (metros < distancia) { // la coordenada se encuentra en este tramo
                    bearing = wp[i].getGreatCircleInitialBearing(wp[i + 1]);
                    coord = wp[i].getGreatCircleDestination(metros, bearing);
                    break;
                }
            }
            if (i == numWaypoints - 1) { // último tramo
                metros = metros - distancia;
                distancia = wp[i].getGreatCircleDistance(destination);
                if (metros < distancia) {
                    bearing = wp[i].getGreatCircleInitialBearing(destination);
                    coord = wp[i].getGreatCircleDestination(metros, bearing);
                } else { // coordenada más allá de la ruta
                    System.out.println("Error: el tiempo indicado excede al de la ruta");
                    coord = destination;
                }
            }
        }
        return coord;
    }



    public Coordinate computeIntersection(Route r) {
        // Calcula la coordenada en la que la ruta actual intersecta con la ruta r
        //
        TrafficSimulated plane1;
        TrafficSimulated plane2;
        // plane1 para recorrer this y plane2 para recorrer r

        // necesito uniformizar la velocidad para que las rutas sean homogeneas
        double speed1, speed2;

        // speed1 será para la ruta más corta y sppeed2 para la larga
        if (greatCircleLength() < r.greatCircleLength()) {
            // la ruta que me pasan es más corta
            speed1 = 20; // precisión mayor para la ruta más corta y la otra escalada
            speed2 = (speed1 * r.greatCircleLength()) / greatCircleLength();
        } else {
            // al revés
            speed2 = 20; // precisión mayor para la ruta más corta y la otra escalada
            speed1 = (speed2 * greatCircleLength()) / r.greatCircleLength();
        }

        plane1 = new TrafficSimulated("AAAAAA", getDeparture(), speed1, 0);
        plane2 = new TrafficSimulated("BBBBBB", r.getDeparture(), speed2, 0);

        double t, min = Double.MAX_VALUE, minTime = 0;
        Coordinate pos1, pos2, minPos = null;
        double timeRoute;

        timeRoute = (greatCircleLength() / (plane1.getSpeed() * 1852 / 3600)) / 60; // mins
        System.out.println(timeRoute);
        for (int i = 0; i < timeRoute; i++) {
            //    System.out.println(i);
            pos1 = computeCoordinateOverTime(plane1, i);
            for (int j = 0; j < timeRoute; j++) {
                pos2 = r.computeCoordinateOverTime(plane2, j);
                if (pos1.getGreatCircleDistance(pos2) < min) {
                    min = pos1.getGreatCircleDistance(pos2);
                    minPos = pos1;
                    minTime = i;
                }
            }
        }

        System.out.println("Min dist: " + min + " Min time: " + minTime);

        return minPos;
    }
    
    
    /**
     * Returns last wp index based on distance specified, return -1 if next wp is departure and -2 if distance is > than route distance
     * @param distanceMeter
     * @param routeMode
     * @return 
     */
    public int getLastWpIndexByDistance(double distanceMeter, int routeMode)
    {
        int i;
        double distance;

        
        if (wp != null) {
            if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
            {
                distance = departure.getGreatCircleDistance(wp[0]);
            }
            else
            {
                distance = departure.getRhumbLineDistance(wp[0]);
            }
            
            if(distance > distanceMeter)
            {
                return -1;
            }
            for (i = 0; i < numWaypoints - 1; i++) 
            {     
                if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
                {
                    distance += wp[i].getGreatCircleDistance(wp[i + 1]);
                }
                else
                {
                    distance += wp[i].getRhumbLineDistance(wp[i + 1]);
                }
                
                if(distance > distanceMeter)
                {
                    return i;
                }
            }

            if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
            {
                distance += wp[i].getGreatCircleDistance(destination);
            }
            else
            {
                distance += wp[i].getRhumbLineDistance(destination);
            }           
            
            if(distance > distanceMeter)
            {
                return i; //LAST ONE
            }
            
        } else {
            
            if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
            {
                distance =departure.getGreatCircleDistance(destination);
            }
            else
            {
                distance = departure.getRhumbLineDistance(destination);
            } 
            
            if(distance> distanceMeter)
            {
                return -1;
            }
        }
        return -2; 
        
    }
    
    
    /**
     * Returns Distance from start to specified wp index
     * @param wpIndex
     * @param routeMode
     * @return 
     */
    public double getDistanceToWp(int wpIndex, int routeMode)
    {
        int i;
        double distance=0;
        
        
        if (wp != null) 
        {
            if(wpIndex==-1) return 0;
            if(wpIndex>=wp.length)
            {
                 if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
                {
                    return this.greatCircleLength();
                }
                else
                {
                    return this.rhumbLength();
                }
                
            }
            if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
            {
                distance += departure.getGreatCircleDistance(wp[0]);
            }
            else
            {
                distance += departure.getRhumbLineDistance(wp[0]);
            }
            
            for (i = 0; i < wpIndex; i++) 
            {     
                if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
                {
                    distance += wp[i].getGreatCircleDistance(wp[i + 1]);
                }
                else
                {
                    distance += wp[i].getRhumbLineDistance(wp[i + 1]);
                }
                
            }
        }
        return distance;
    }
    
    /**
     * Return route length based on route mode
     * @param routeMode
     * @return 
     */
    double getRouteLength(int routeMode)
    {
        if(routeMode==TrafficSimulated.FLY_ORTHODROMIC)
        {
            return this.greatCircleLength();
        }
        return this.rhumbLength();     
    }
    
    /**
     * Obtiene los iconos de los wp y toc tod
     * @param altitudeScale
     * @return 
     */
    public ArrayList<WWIcon> getWpIndicators(double altitudeScale)
    {
        
        ArrayList<WWIcon> icons = new ArrayList<>();
        for(int i = 0; i< this.getWp().length;i++)
        {
            WayPoint wp = this.getWp()[i];
            UserFacingIcon icon = new UserFacingIcon("src/images/waypoint.png",wp.toPosition(altitudeScale));
            icon.setSize(new Dimension(20, 20));
            icon.setShowToolTip(true);
            icon.setToolTipText(wp.getLocationName());
            icon.setDragEnabled(false);
            icon.setToolTipOffset(new Vec4(0, -10));
            
            icons.add(icon);       
        }
        
        //Aeropuerto salida
        UserFacingIcon icon = new UserFacingIcon("src/images/airport.png",this.getDeparture().toPosition(altitudeScale));
        icon.setSize(new Dimension(40, 40));
        icon.setShowToolTip(true);
        icon.setToolTipText(this.getDeparture().getIcaoCode());
        icon.setDragEnabled(false);
        //icon.setToolTipOffset(new Vec4(0, -10));

        icons.add(icon);  
        
        //Aeropuerto llegada
        icon = new UserFacingIcon("src/images/airport.png",this.getDestination().toPosition(altitudeScale));
        icon.setSize(new Dimension(40, 40));
        icon.setShowToolTip(true);
        icon.setToolTipText(this.getDestination().getIcaoCode());
        icon.setDragEnabled(false);
        //icon.setToolTipOffset(new Vec4(0, -10));

        icons.add(icon);
        
        //TOC
        icon = new UserFacingIcon("src/images/TOCTOD.png",this.getTocPos().toPosition(altitudeScale));
        icon.setSize(new Dimension(40, 40));
        icon.setShowToolTip(true);
        icon.setToolTipText(this.getTocPos().getLocationName());
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(-40, 40));

        icons.add(icon);
        
        //TOD
        icon = new UserFacingIcon("src/images/TOCTOD.png",this.getTodPos().toPosition(altitudeScale));
        icon.setSize(new Dimension(40, 40));
        icon.setShowToolTip(true);
        icon.setToolTipText(this.getTodPos().getLocationName());
        icon.setDragEnabled(false);
        icon.setToolTipOffset(new Vec4(0, +40));

        icons.add(icon);
        
        
        return icons;
    }
    /**
     * Obtiene el displayer de la ruta
     * @param altitudScale
     * @param toc
     * @param tod
     * @param routeMode
     * @return 
     */
    public gov.nasa.worldwind.render.Path getPathDisplayer(double altitudScale,double toc, double tod, int routeMode)
    {
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(new Material(Color.YELLOW));
        attrs.setOutlineWidth(2d);
        
        
        //Add points to list
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        Coordinate coord = this.getDeparture();
        Position pos = coord.toPosition(altitudScale);
        
        pathPositions.add(pos);
        
        for(int i = 0; i< this.getWp().length;i++)
        {
            Coordinate wp = this.getWp()[i];
            double dist1 = this.getDistanceToWp(i-1, routeMode);
            double dist2 = this.getDistanceToWp(i, routeMode);
            
            
            if(dist1<=toc && dist2>=toc)
            {
                pathPositions.add(tocPos.toPosition(altitudScale));
            }
            if(dist1<=tod && dist2>=tod)
            {
                pathPositions.add(todPos.toPosition(altitudScale));
            }
            pathPositions.add(wp.toPosition(altitudScale));
                        
        }
        if(this.getDistanceToWp(this.getWp().length-1, routeMode)<tod)
        {
            pathPositions.add(todPos.toPosition(altitudScale));
        }
        coord = this.getDestination();
        pathPositions.add(coord.toPosition(altitudScale));
        
        
        //Create path
        gov.nasa.worldwind.render.Path path = new gov.nasa.worldwind.render.Path(pathPositions);
        path.setAttributes(attrs);
        path.setVisible(true);
        path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        path.setPathType(AVKey.GREAT_CIRCLE);
        
        
        return path;
        
    }
    
    //GETTERS//
    public Coordinate getTocPos() {
        return tocPos;
    }

    public Coordinate getTodPos() {
        return todPos;
    }
    public double rhumbTime() { // seconds
        return rhumbLength() / (speed * 1852 / 3600);
    }

    public int getNumWaypoints() {
        return numWaypoints;
    }

    public WayPoint[] getWp() {
        return wp;
    }

    public Airport getDeparture() {
        return departure;
    }

    public Airport getDestination() {
        return destination;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        speed = speed;
    }

}
