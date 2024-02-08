///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package TFM;
//
////import static TFM.JsonFileReader.readJsonFile;
//import com.fasterxml.jackson.databind.JsonNode;
//import gov.nasa.worldwind.avlist.AVKey;
//import gov.nasa.worldwind.geom.Angle;
//import gov.nasa.worldwind.geom.LatLon;
//import gov.nasa.worldwind.layers.Layer;
//import gov.nasa.worldwind.layers.RenderableLayer;
//import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
//import gov.nasa.worldwind.render.airspaces.Cake;
//import gov.nasa.worldwind.render.airspaces.CappedCylinder;
//import gov.nasa.worldwind.render.airspaces.CappedEllipticalCylinder;
//import gov.nasa.worldwind.render.airspaces.Curtain;
//import gov.nasa.worldwind.render.airspaces.Orbit;
//import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
//import gov.nasa.worldwind.render.airspaces.Polygon;
//import gov.nasa.worldwind.render.airspaces.SphereAirspace;
//import gov.nasa.worldwind.render.airspaces.TrackAirspace;
//import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// *
// * @author Gabriel
// */
//public class AirSpaceLayer {
//    protected RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
//    
//    Layer layer;
//    
//    public AirSpaceLayer(){
//        this.layer = this.makeAGLAirspaces();
//    }
//            public Layer makeAGLAirspaces()
//        {
//            AirspaceAttributes attrs = this.randomAttrs.nextAttributes().asAirspaceAttributes();
//            RenderableLayer layer = new RenderableLayer();
//            layer.setName("AGL Airspaces");
//
//            //Reading FIR Coordinates
//            String [] coordinatesList;
//            String filePath = "C:\\Users\\Gabriel\\Desktop\\Master\\TFM\\Data\\FIR2.json";
//            List<JsonNode> jsonDataList = readJsonFile(filePath);
//            List<JsonNode> jsonDataList2;
//        
//            // Do something with the data stored in jsonDataList
//            int i =0;
//            for (JsonNode jsonData : jsonDataList) {
//                Polygon poly = new Polygon(attrs);
//                ArrayList<LatLon> listTmp = new ArrayList<LatLon>();
//                //System.out.println(jsonData.get("geometry").get("coordinates").get(0));
//                poly.setValue(AVKey.DISPLAY_NAME,jsonData.get("name"));
//                for (JsonNode jsonData2 : jsonData.get("geometry").get("coordinates").get(0)){
//                    //System.out.println(jsonData2);
//                    listTmp.add(LatLon.fromDegrees(jsonData2.get(1).asDouble(),jsonData2.get(0).asDouble()));
//                }
//                //System.out.println(listTmp);
//                poly.setLocations(listTmp);
//                poly.setAltitudes(0, 5000);
//                poly.setAltitudeDatum(AVKey.ABOVE_GROUND_LEVEL, AVKey.ABOVE_GROUND_REFERENCE);
//                layer.addRenderable(poly);
//                listTmp = null;                        
//             }
//            //-------------------------------------------------------------------------------------------//
//            return layer;
//
//        }
//  } 
