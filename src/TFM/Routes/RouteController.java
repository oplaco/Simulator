package TFM.Routes;

import TFM.AltitudeScaleListener;
import classes.base.TrafficSimulated;
import classes.base.TrafficSimulatedListener;
import classes.simulator.FlightPlan;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.ArrayList;


/**
 *<a href="https://es.vecteezy.com/vectores-gratis/icono-avi%C3%B3n">Icono Avión Vectores por Vecteezy</a>
 * @author Sergio Alexandro Costea Andronache
 * Esta clase sirve para manejar los planes de vuelo y actualizar los diferentes displayers
 * 
 */
public class RouteController implements TrafficSimulatedListener{
    
    //Lista de los planes de vuelo que existen
    ArrayList<FlightPlan> flightPlans;    
    
    //Additional layers
    RenderableLayer routeLayer;
    IconLayer airportLayer;
    IconLayer planesLayer;

    //AltitudListeners
    AltitudeScaleListener altitudScaleListener;
    
    //Aplciacion NASA
    WorldWindow wwd;
    
    private double altitudeScale=1; // Escala de la altitud, para ve mejor las rutas
    

    public void setAltitudeScale(double altitudeScale) {
        this.altitudeScale = altitudeScale;
        if(altitudScaleListener!=null)
        {
            altitudScaleListener.setAltitudeScale(altitudeScale);
        }
        
        updatePaths();
        updatePlaneDisplayers();
        wwd.redraw();
    }
    
    /**
     * Constructor del controlador de las rutas, si no se quiere usar listener
     * poner como null
     * @param wwd
     * @param listener 
     */
    public RouteController(WorldWindow wwd, AltitudeScaleListener listener)        
    {
        this.wwd=wwd;
        flightPlans = new ArrayList<>();
        
        //Crea los diferentes layers y les asigna un nombre
        routeLayer = new RenderableLayer();
        routeLayer.setName("Routes");

        planesLayer = new IconLayer();
        planesLayer.setName("Planes");

        airportLayer = new IconLayer();
        airportLayer.setName("Airports & WayPoints");
        
        this.altitudScaleListener=listener;
    }
    
    /**
     * Crea e inicia un nuevo plan de vuelo con el nombre de archivo
     * @param fpname
     * @param route
     * @param cruiseAltitude
     * @param ascentRate
     * @param descentRate 
     */
    void startNew(String fpname, String route,double cruiseAltitude,double ascentRate,double descentRate)
    {
        FlightPlan fp;

        fp = new FlightPlan(fpname, route, this);               
        //fp.setVerticalProfile(cruiseAltitude,ascentRate, descentRate);
               
        fp.start();
        
        flightPlans.add(fp);
                      
    }
    /**
     * Busca si ya existe un plan de vuelo con ese nombre
     * @param name
     * @return true si el plan de vuelo ya existe
     */
    public boolean containsNamedPlan(String name)
    {
        for(int i = 0;i<flightPlans.size();i++)
        {
            FlightPlan fp = flightPlans.get(i);
            if(fp.getPlanName().equals(name))
            {
                return true;
            }           
        }
        return false;
    }
    /**
     * Actualiza los displayers de la ruta
     */
    private void updatePaths()
    {
        routeLayer.removeAllRenderables();
        airportLayer.removeAllIcons();
        for(int i = 0;i<flightPlans.size();i++)
        {
            FlightPlan fp = flightPlans.get(i);
            if(fp!=null)
            {
//                routeLayer.addRenderable(
//                        fp.getRoute().getPathDisplayer(this.altitudeScale,
//                        fp.getPilot().getTopOfClimb(),
//                        fp.getPilot().getTopOfDescent(),
//                        fp.getPilot().getRouteMode()));
                
                airportLayer.addIcons(fp.getRoute().getWpIndicators(this.altitudeScale));
            }
           
        }
    }
    
    /**
     * Actualiza el displayer del avion
     */
    private void updatePlaneDisplayers()
    {
        planesLayer.removeAllIcons();
        for(int i = 0;i<flightPlans.size();i++)
        {
            FlightPlan fp = flightPlans.get(i);
            if(fp==null)
            {
                continue;
            }
            
            TrafficSimulated plane = fp.getPlane();
         
            if(plane==null)
            {
                continue;
            }
            
            planesLayer.addIcon(plane.getDisplayer(this.altitudeScale));
        }
        wwd.redraw(); //Manda repintar la ventana
               
    }

    void stopit()
    {
        try
        {  
            for(int i = 0; i<flightPlans.size();i++)
            {
                if(flightPlans.get(i)!=null)
                {
                    flightPlans.get(i).stopit();
                }
            }
        }
        catch(Exception e)
        {
        
        }
    }
    
    

    //----------TRAFFIC LISTENER-------------//
    @Override
    public void planeStarted(TrafficSimulated ts) {
        this.updatePaths();
    }

    @Override
    public void planeUpdated(TrafficSimulated ts) {
        updatePlaneDisplayers();
    }

    @Override
    public void planeStopped(TrafficSimulated ts) {
        for(int i =0;i<flightPlans.size();i++)
        {
            if(flightPlans.get(i).getPlane()==ts)
            {
                flightPlans.remove(i);
            }
        }
        this.updatePaths();
        this.updatePlaneDisplayers();
    }
    
    
    //------------GETTERS--------------//
    
    public double getAltitudeScale() {
        return altitudeScale;
    }
    public RenderableLayer getRouteLayer() {
        return routeLayer;
    }

    public IconLayer getAirportLayer() {
        return airportLayer;
    }

    public IconLayer getPlanesLayer() {
        return planesLayer;
    }


}
