/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.GUI;

import TFM.GUI.AltitudeScaleListener;
import TFM.GUI.TrafficPickedListener;
import TFM.polygons.TrafficPolygon;
import TFM.Coordinates.Coordinate;
import TFM.AircraftControl.TrafficSimulatedListener;
import TFM.Core.TrafficSimulated;
import TFM.Others.TrafficSimulationMap;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwindx.examples.util.SlideShowAnnotation;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opensky.libadsb.Position;

/**
 *
 * Clase para representar los aviones
 * 
 */
public class TrafficDisplayer implements TrafficSimulatedListener,SelectListener,AltitudeScaleListener{

    TrafficSimulationMap trafficSimulationMap; //Map to store Traffic from simulation environment instead of ADSB.
    private Map<String, TrafficPolygon> trafficPolygonMap = new ConcurrentHashMap<>(); //Map to store TrafficPolygons
    
    private RenderableLayer trafficPolygonLayer;
    private AnnotationLayer annotationLayer; //Layer sobre el que se muestran las anotaciones
    private WorldWindow wwd; //Referencia al core de wwd
    private View view;
    
    //Picked Traffic
    private TrafficPickedListener trafficPickedListener;
    private TrafficSimulated pickedTraffic;

    boolean enabled = true;
    private double distanceEyePositionToViewCenter; // Meters (m)
    private double altitudScale=1;

    
    /**
    * Constructor de Traffic displayer
    * @param wwd 
    * @param trafficPickedListener 
    */
    public TrafficDisplayer(WorldWindow wwd, TrafficPickedListener trafficPickedListener)
    {
        this.wwd=wwd;
        this.trafficPickedListener = trafficPickedListener;
        
        wwd.getSceneController().getDrawContext().setPickPointFrustumDimension(
                            new Dimension(40, 40));
        wwd.addSelectListener(this);
        this.view = wwd.getView();
        Position vlc = new Position(0.4727777777777778, 39.489444444444445, 4.0);
        
        try {
            trafficSimulationMap = new TrafficSimulationMap(this,vlc);
            //trafficmap = new TrafficMap("158.42.40.45", 10003, vlc, this);
            //trafficmap = new TrafficMap("C:\\Users\\Gabriel\\Desktop\\Master\\TFM\\World Wind\\WorldWindJava2\\src\\input.csv",vlc,this);
            //trafficmap = new TrafficMap("C:\\Users\\Gabriel\\Desktop\\Master\\TFM\\World Wind\\WorldWindJava2\\src\\input.csv",vlc,this);
        } catch (IOException ex) {
            Logger.getLogger(TrafficDisplayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        trafficPolygonLayer = new RenderableLayer();
        trafficPolygonLayer.setName("Traffic Polygon Layer");
        annotationLayer = new AnnotationLayer();
        annotationLayer.setName("Traffic Annotations");
        
    }
    
    /**
     * Callback cuando se modifica la altitud
     * @param as 
     */
    @Override
    public void setAltitudeScale(double as)
    {
        this.altitudScale=as;
        System.out.println("Set altitudescale");
        System.out.println(this.wwd.getView().getEyePosition());
        //Update all traffics
        //this.updateTraffic(null);
        
    }
    
    /**
     * Convierte la posicion de TrafficSimulated a posicion del WorldWind
     * @param t
     * @return 
     */
    public gov.nasa.worldwind.geom.Position getNasaPos(TrafficSimulated t)
    {
        Coordinate pos = t.getPosition();
        if(pos==null)
        {
            return gov.nasa.worldwind.geom.Position.ZERO;
        }
        gov.nasa.worldwind.geom.Position nasa_pos = 
                gov.nasa.worldwind.geom.Position.fromDegrees(
                        pos.getLatitude(), 
                        pos.getLongitude(),
                        pos.getAltitude()/3.28084*this.altitudScale);
        return nasa_pos;
    }
    
    /**
     * Añade un traffic cuando avisa el listener
     * @param trfc 
     */
    @Override
    public void planeStarted(TrafficSimulated trfc) { 
        if(!enabled)
        {
            return;
        }
        TrafficPolygon trafficPolygon = new TrafficPolygon(trfc,getNasaPos(trfc), distanceEyePositionToViewCenter);
        trafficPolygonMap.put(trfc.getHexCode(), trafficPolygon);
        trafficPolygonLayer.addRenderable(trafficPolygon.getPolygon());
   
        this.wwd.redraw();
    }

    /**
     * Updates the plane variables, removes the old polygon and creates a new one.
     * @param trfc 
     */   
    @Override
    public void planeUpdated(TrafficSimulated trfc) {
        // Check if there is an existing polygon and remove it
        if (trafficPolygonMap.containsKey(trfc.getHexCode())) {
            TrafficPolygon existingPolygon = trafficPolygonMap.get(trfc.getHexCode());
            trafficPolygonLayer.removeRenderable(existingPolygon.getPolygon());
        }

        if (trfc.isFollowed()) {
            System.out.println("Altitude: " + trfc.getPosition().getAltitude());
            view.setEyePosition(new gov.nasa.worldwind.geom.Position(
                Angle.fromDegrees(trfc.getPosition().getLatitude()),
                Angle.fromDegrees(trfc.getPosition().getLongitude()),
                trfc.getPosition().getAltitude() * 2
            ));
            view.setPitch(Angle.fromDegrees(60));
            view.setHeading(Angle.fromDegrees(trfc.getCourse()));
        }

        // Create a new updated polygon
        TrafficPolygon trafficPolygon = new TrafficPolygon(trfc, getNasaPos(trfc), distanceEyePositionToViewCenter);

        // Update the map and layer with the new polygon
        trafficPolygonMap.put(trfc.getHexCode(), trafficPolygon);
        trafficPolygonLayer.addRenderable(trafficPolygon.getPolygon());

        // Check if the updated traffic is the currently picked traffic
        if (pickedTraffic != null && pickedTraffic.getHexCode().equals(trfc.getHexCode())) {
            trafficPickedListener.showDetails(trfc); // Update detail view
        }

        this.wwd.redraw();
    }
    
    public void viewUpdated(double altitude){
        //System.out.println("From Traffic Displayer the altitude is: "+ altitude);
 
        // Current eye position
        gov.nasa.worldwind.geom.Position eyePosition = this.wwd.getView().getCurrentEyePosition();
        Vec4 centerPoint = this.wwd.getView().getCenterPoint();

        // Get the globe from WorldWind to convert geographic positions to Cartesian coordinates
        Globe globe = this.wwd.getModel().getGlobe();

        // Convert eyePosition to Vec4
        Vec4 eyePoint = globe.computePointFromPosition(eyePosition);

        // Now that both points are in the same coordinate system, calculate the distance
        double distance = eyePoint.distanceTo3(centerPoint);     
        System.out.println(distance);
        this.distanceEyePositionToViewCenter = distance;
    }
    
    /**
     * Elimina el traffic cuando nos avisa el listener
     * @param trfc 
     */
    @Override
    public void planeStopped(TrafficSimulated trfc) {
        if(!enabled)
        {
            return;
        }
        TrafficPolygon existingPolygon = trafficPolygonMap.get(trfc.getHexCode());
        trafficPolygonLayer.removeRenderable(existingPolygon.getPolygon());
        this.wwd.redraw(); 
    }

    /**
     * Callback when an action is performed over a wwd item.
     * @param event 
     */
    @Override
    public void selected(SelectEvent event) {      
        if(!enabled)
        {
            return;
        }
        if(event.isLeftClick())
        {
            pick(event.getTopObject());
        }            
    }
          
    public void pick(Object o) {
        if (o instanceof Polygon) {
            Polygon selectedPolygon = (Polygon) o;
            TrafficPolygon trafficPolygon = findTrafficPolygonByPolygon(selectedPolygon);

            if (trafficPolygon == null) {
                System.out.println("No TrafficPolygon associated with the selected polygon.");
                return;
            }

            TrafficSimulated pickedTrafficTemp = trafficSimulationMap.get(trafficPolygon.getIcaoCode());

            // If the same polygon (by ICAO code) is clicked again, toggle the detail view
            if (pickedTraffic != null && pickedTraffic.getHexCode().equals(trafficPolygon.getIcaoCode())) {
                // Toggle visibility of details
                if (pickedTraffic == null) {
                    // Show details if currently hidden
                    trafficPickedListener.showDetails(pickedTrafficTemp);
                    pickedTraffic = pickedTrafficTemp;
                } else {
                    // Hide details if currently shown
                    trafficPickedListener.hideDetails();
                    pickedTraffic = null;
                }
            } else {
                // If a different polygon is clicked, show its details and update pickedTraffic
                trafficPickedListener.showDetails(pickedTrafficTemp);
                pickedTraffic = pickedTrafficTemp;
            }
        } else {
            System.out.println("Selected object is not a Polygon.");
        }
    }
  
    private TrafficPolygon findTrafficPolygonByPolygon(Polygon polygon) {
        for (TrafficPolygon tp : trafficPolygonMap.values()) {
            if (tp.getPolygon().equals(polygon)) {
                return tp;
            }
        }
        return null; // Return null if no match found
    }

    public RenderableLayer getTrafficPolygonLayer() {
        return trafficPolygonLayer;
    }   
           
    public AnnotationLayer getAnnotationLayer() {
        return annotationLayer;
    }
    
    public TrafficSimulationMap getTrafficSimulationmap() {
        return trafficSimulationMap;
    }

    public WorldWindow getWwd() {
        return wwd;
    }
    
    
}
