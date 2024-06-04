package TFM.Others;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.UserFacingIcon;

/**
 * Clase que extiende UserFacingIcon añadiendo la información del codigo icao
 * @author Alfredo Torres Pons
 */
public class TrafficIcon extends UserFacingIcon{
    
    private String icaoCode; //Codigo icao del avion
    
    //Path de las imagenes utilizadas
    //private static String icon_path = "src/images/punto_rojo.png";
    private static String icon_path = "src/images/avion24_azul.png";
    private static String picked_icon_path = "src/images/punto_amarillo.png";
    private static String other_icon_path = "src/images/punto_rosa.png";
    
    /**
     * Contructor
     * @param icaoCode
     * @param pos 
     */
    public TrafficIcon(String icaoCode,Position pos)
    {
        //Llama al constructor de UserFacing Icon
        super(icon_path,pos);
        
        //Asigna el codigo icao y configura highligh y tool tip (texto)
        this.icaoCode=icaoCode;
        this.setHighlightScale(1.2);
        this.setToolTipText(icaoCode);
        this.setShowToolTip(true);
    }

    /**
     * Cambia el color cuando se pasa por encima con el ratón
     * @param highlighted 
     */
    @Override
    public void setHighlighted(boolean highlighted)
    {
        super.setHighlighted(highlighted);
        
        if(highlighted)
        {
            this.setImageSource(picked_icon_path);
        }
        else
        {
            this.setImageSource(icon_path);
        }
    }
    /**
     * Getter icao Code
     * @return 
     */
    public String getIcaoCode() {
        return icaoCode;
    }
    
    
}
