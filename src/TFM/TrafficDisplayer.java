/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM;

import TFM.AircraftControl.TrafficSimulationMap;
import TFM.GUI.TrafficPickedListener;
import TFM.polygons.TrafficPolygon;
import TFM.Coordinates.Coordinate;
import TFM.AircraftControl.TrafficSimulated;
import TFM.AircraftControl.TrafficSimulatedListener;
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
    private IconLayer trafficLayer; //Layer sobre el que se pinta el trafico
    private RenderableLayer trafficPolygonLayer;
    private AnnotationLayer annotationLayer; //Layer sobre el que se muestran las anotaciones
    private WorldWindow wwd; //Referencia al core de wwd
    private View view;
    
    //Picked Traffic
    TrafficPickedListener trafficPickedListener;
    TrafficPolygon  pickedPolygon;
    TrafficSimulated pickedTraffic;
    
    TrafficIcon lastOver; //Ultimo icono por el que se pasó por encima
    TrafficIcon pickedIcon; //Icono seleccionado
    GlobeAnnotation pickedAnnotation; //Globo selecionado
    boolean enabled = true;
    private double distanceEyePositionToViewCenter; // Meters (m)
    
    private Map<String, TrafficPolygon> trafficPolygonMap = new ConcurrentHashMap<>(); //Map to store TrafficPolygons

    private double altitudScale=1;
    
    /**
    * Constructor de Traffic displayer
    * @param wwd 
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

        
        trafficLayer = new IconLayer();
        trafficLayer.setName("Traffic Old Icon Layer");     
        trafficLayer.setViewClippingEnabled(false);
     
        trafficPolygonLayer = new RenderableLayer();
        trafficPolygonLayer.setName("Traffic Polygon Layer");
        
        
        annotationLayer = new AnnotationLayer();
        annotationLayer.setName("Traffic Annotations");
        
    }
    
    /**
     * Callback cuando se modifica la altitud
     * @param as 
     */
    public void setAltitudeScale(double as)
    {
        this.altitudScale=as;
        System.out.println("Set altitudescale");
        System.out.println(this.wwd.getView().getEyePosition());
        //Update all traffics
        //this.updateTraffic(null);
        
    }
    
//    public void stopit()
//    {
//        try
//        {
//            trafficSimulationMap.stopit();
//            
//        }catch(Exception ex)
//        {
//            
//        }
//    }
    
    public IconLayer getTrafficLayer() {
        return trafficLayer;
    }

    public RenderableLayer getTrafficPolygonLayer() {
        return trafficPolygonLayer;
    }
    
    
        
    public AnnotationLayer getAnnotationLayer() {
        return annotationLayer;
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
     * Busca el icono del avion indicado en la traffics Layer
     * @param icaoCode
     * @return 
     */
    TrafficIcon findIcon(String icaoCode)
    {
        Iterable icons = trafficLayer.getIcons();
        for(Object icon : icons)
        {
            if(((TrafficIcon) icon).getIcaoCode().equals(icaoCode))     
            {
                return ((TrafficIcon) icon);
            }
        }
        return null;
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
        System.out.println("planeStarted method");
        //TrafficIcon icon = new TrafficIcon(
               //trfc.getHexCode(),getNasaPos(trfc));
        //icon.setSize(new Dimension(30,30));
        //trafficLayer.addIcon(icon);
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
        if(event.isRollover())
        {
            highlight(event.getTopObject());
        }
        if(event.isLeftClick())
        {
            pick(event.getTopObject());
        }            
    }
    /**
     * Controlla el highlighting del objeto que se pasa por encima con el raton
     * @param o 
     */
    private void highlight(Object o)
    {
        // Manage highlighting of TrafficIcons.
        if (this.lastOver == o)
            return; //Still same object

        // Turn off highlight if leaves
        if (this.lastOver != null)
        {
            this.lastOver.setHighlighted(false);
            this.lastOver = null;
        }

        // Turn on highlight if object selected.
        if (o != null && o instanceof TrafficIcon)
        {
            this.lastOver = (TrafficIcon) o;
            this.lastOver.setHighlighted(true);
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
                // If a different polygon is clicked, show its details and update pickedPolygon and pickedTraffic
                trafficPickedListener.showDetails(pickedTrafficTemp);
                pickedPolygon = trafficPolygon;
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

    public TrafficSimulationMap getTrafficSimulationmap() {
        return trafficSimulationMap;
    }

    public WorldWindow getWwd() {
        return wwd;
    }
    
    
}
