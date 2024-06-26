/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Others;

import TFM.GUI.AltitudeScaleListener;
import TFM.Others.TrafficIcon;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwindx.examples.util.SlideShowAnnotation;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opensky.libadsb.Position;
import traffic.Traffic;
import traffic.TrafficListener;
import traffic.TrafficMap;

/**
 *
 * Clase para representar los aviones de la antena
 * 
 */
public class AntennaDisplayer implements TrafficListener,SelectListener,AltitudeScaleListener{

    TrafficMap antenna; //Antena utilizada
    private IconLayer trafficLayer; //Layer sobre el que se pinta el trafico
    private AnnotationLayer annotationLayer; //Layer sobre el que se muestran las anotaciones
    WorldWindow wwd; //Referencia al core de wwd
    
    TrafficIcon lastOver; //Ultimo icono por el que se pasó por encima
    TrafficIcon pickedIcon; //Icono seleccionado
    GlobeAnnotation pickedAnnotation; //Globo selecionado
    boolean enabled = true;
    
    private double altitudScale=1;
    
    /**
    * Constructor de antenna displayer
    * @param wwd 
    */
    public AntennaDisplayer(WorldWindow wwd)
    {
        this.wwd=wwd;
        wwd.getSceneController().getDrawContext().setPickPointFrustumDimension(
                            new Dimension(40, 40));
        wwd.addSelectListener(this);
        
        Position vlc = new Position(0.4727777777777778, 39.489444444444445, 4.0);
        
        try {
            antenna = new TrafficMap("158.42.40.45", 10003, vlc, this);
            //antenna = new TrafficMap("C:\\Users\\Gabriel\\Desktop\\Master\\TFM\\World Wind\\WorldWindJava2\\src\\input.csv",vlc,this);
            //antenna = new TrafficMap("C:\\Users\\Gabriel\\Desktop\\Master\\TFM\\World Wind\\WorldWindJava2\\src\\input.csv",vlc,this);
        } catch (IOException ex) {
            Logger.getLogger(AntennaDisplayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        trafficLayer = new IconLayer();
        trafficLayer.setName("Antenna Traffic");     
        trafficLayer.setViewClippingEnabled(false);
        
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
        
        //Update all traffics
        this.updateTraffic(null);
        
    }
    public void stopit()
    {
        try
        {
            antenna.stopit();
            
        }catch(Exception ex)
        {
            
        }
        
    }
    public IconLayer getTrafficLayer() {
        return trafficLayer;
    }
    public AnnotationLayer getAnnotationLayer() {
        return annotationLayer;
    }
    /**
     * Convierte la posicion de Traffic a posicion del WorldWind
     * @param pos
     * @return 
     */
    public gov.nasa.worldwind.geom.Position getNasaPos(Traffic pos)
    {
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
    public void putTraffic(Traffic trfc) { 
        if(!enabled)
        {
            return;
        }
        TrafficIcon icon = new TrafficIcon(
               trfc.getICAO24(),getNasaPos(trfc));
        icon.setSize(new Dimension(30,30));
        trafficLayer.addIcon(icon);
    }

    /**
     * Elimina el traffic cuando nos avisa el listener
     * @param trfc 
     */
    @Override
    public void removeTraffic(Traffic trfc) {
        if(!enabled)
        {
            return;
        }
        TrafficIcon icon = findIcon(trfc.getICAO24());
        if(icon!=null)
        {
            trafficLayer.removeIcon(icon);
            this.wwd.redraw(); 
        }
    }

    /**
     * Actualiza el traffic modificado o todos si es null
     * @param trfc 
     */
    @Override
    public void updateTraffic(Traffic trfc) {
        if(!enabled)
        {
            return;
        }
        //Actualiza todos los aviones
        if(trfc==null)
        {
            Traffic[] traffics = antenna.getTraffics();
           for(int i = 0 ; i< traffics.length;i++)
           {
               if(traffics[i]!=null)
               {
                    TrafficIcon icon = findIcon(traffics[i].getICAO24());
                    if(icon!=null)
                    {    
                        icon.setPosition(getNasaPos(traffics[i]));
                    }
               }
           }
           this.wwd.redraw();
        }
        else
        {
            TrafficIcon icon = findIcon(trfc.getICAO24());
            //Actualiza solo el avión que se modifica
            if(icon!=null)
            {    
                icon.setPosition(getNasaPos(trfc));
                if(icon.equals(pickedIcon))
                {
                    if(pickedAnnotation!=null)
                    {
                        pickedAnnotation.setText(this.getAnnotationText(pickedIcon));
                        pickedAnnotation.setPosition(pickedIcon.getPosition());
                    }
                }
                this.wwd.redraw();
            }
        }
        

        
    }
    
    @Override
    public void trafficStopped() {
        
    }

    /**
     * Callback cuando se realiza un evento sobre un item
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
    
    /**
     * Muestra el globo del traffic selecionado
     * @param o 
     */
    private void pick(Object o)
    {
              
        if (this.pickedIcon == o)
            return; 

        if (o != null && o instanceof TrafficIcon)
        {
            //Borra todos los globos
            annotationLayer.removeAllAnnotations();
            TrafficIcon icon = (TrafficIcon) o;
            this.pickedIcon=icon;
            Traffic traffic = antenna.get(icon.getIcaoCode());
  
            if(traffic==null)
            {
                return;
            }
            //Crea el globo con la info y añadelo a la layer
            this.pickedAnnotation = new SlideShowAnnotation(            
                    icon.getPosition());
            this.pickedAnnotation.setText(this.getAnnotationText(icon));
            this.pickedAnnotation.setAlwaysOnTop(true);
            pickedAnnotation.getAttributes().setSize(
                new Dimension(280, 0)); 
            annotationLayer.addAnnotation(pickedAnnotation);
        }
        else
        {
            //Limpia toda la selección
            annotationLayer.removeAllAnnotations();
            pickedIcon=null;
            pickedAnnotation=null;
        }
    }
    
    /**
     * Crea el texto que va dentro del globo
     * @param trafficIcon
     * @return 
     */
    private String getAnnotationText(TrafficIcon trafficIcon)
    {

        Traffic traffic = antenna.get(trafficIcon.getIcaoCode());

        double altitude  = traffic.getAltitude();
        double latitude  = traffic.getLatitude();
        double longitude = traffic.getLongitude();
        double groundSpeed = traffic.getGs();
        double heading = traffic.getHeading();
        double verticalRate = traffic.getVr();

        String position = "("+ String.format("%.3f", latitude)+"º, "
                + String.format("%.3f", longitude)+"º, "
                + String.format("%.0f", altitude) +"ft )";
        String gsStr = String.format("%.1f", groundSpeed)+" knots";
        String hStr = String.format("%.1f",heading) + "º";
        String vrStr = String.format("%.1f", verticalRate) + " ft/min";
        
        //El texto va en formato HTML para mostrar diferentes tamaños y colores
        String text = "<p>\n<b><font color=\"#664400\">Traffic Information of: "
                +trafficIcon.getIcaoCode()+"</font></b><br />\n"
                +"<p><b>Position: </b>"+ position+"</p>"
                +"<p><b>Ground Speed: </b>"+ gsStr+"</p>"
                +"<p><b>Heading: </b>"+ hStr+"</p>"
                +"<p><b>verticalRate: </b>"+ vrStr+"</p>"
                +"<p><b>  </b>"+"</p></p>";
                     
        return text;
                
    }
    
}
