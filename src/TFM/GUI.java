/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package TFM;

import TFM.polygons.Runway;
import TFM.polygons.TakeOffSurface;
import TFM.simulationEvents.SimulationEvent;
import TFM.simulationEvents.SimulationFileReader;
import TFM.utils.Utils;
import gov.nasa.worldwindx.examples.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.*;
import gov.nasa.worldwind.view.BasicView;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

public class GUI {

    public static class AppPanel extends JPanel {

        protected WorldWindow wwd;
        protected StatusBar statusBar;
        protected ToolTipController toolTipController;
        protected HighlightController highlightController;
        Configuration configuration;

        public AppPanel(Dimension canvasSize, boolean includeStatusBar) {
            super(new BorderLayout());

            this.wwd = this.createWorldWindow();
            ((Component) this.wwd).setPreferredSize(canvasSize);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));

            this.add((Component) this.wwd, BorderLayout.CENTER);
            if (includeStatusBar) {
                this.statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                this.statusBar.setEventSource(wwd);
            }
            
            // Add controllers to manage highlighting and tool tips.
            this.toolTipController = new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);
            this.highlightController = new HighlightController(this.getWwd(), SelectEvent.ROLLOVER);
        }

        protected WorldWindow createWorldWindow() {
            return new WorldWindowGLCanvas();
        }

        public WorldWindow getWwd() {
            return wwd;
        }

        public StatusBar getStatusBar() {
            return statusBar;
        }
    }

    protected static class AppFrame extends JFrame {

        private Dimension canvasSize = new Dimension(1000, 800);

        protected AppPanel wwjPanel;
        protected JPanel controlPanel;
        protected LayerPanel layerPanel;
        protected StatisticsPanel statsPanel;

        //RouteController
        RouteController routeController;
        
        // Timer to manage debouncing
        private Timer debounceTimer;
        private double viewAltitude;
        //Simulation simulation;
        
            // Map to hold multiple simulations
        private Map<String, Simulation> simulationMap = new HashMap<>();
        // Attribute to keep track of the currently active simulation
        private Simulation activeSimulation;

        //Antenna displayer
        private TrafficDisplayer trafficDisplayer;

        public AppFrame() {
            
            this.initialize(true, true, false);
            
        }

        public AppFrame(Dimension size) {
            this.canvasSize = size;
            this.initialize(true, true, false);
        }

        public AppFrame(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
            this.initialize(includeStatusBar, includeLayerPanel, includeStatsPanel);
        }
        
        private void debounceUpdate(double altitude) {
            // If there's an existing scheduled update, cancel it
            if (debounceTimer.isRunning()) {
                debounceTimer.stop();
            }
            // Update the action to perform with the latest altitude
            this.viewAltitude = altitude;
            
            debounceTimer.restart(); // Restart the timer
        }

        protected void initialize(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
            // Create the WorldWindow.
            this.wwjPanel = this.createAppPanel(this.canvasSize, includeStatusBar);
            this.wwjPanel.setPreferredSize(canvasSize);

            //Reading the events from testing file to create the default simulation.
            String filePath = "src/simulation files/twoplaneTCAS.csv"; 
            List<SimulationEvent> events = SimulationFileReader.readCSVFile(filePath);
            this.trafficDisplayer = new TrafficDisplayer(this.getWwd());
            addSimulation("DefaultSimulation", new Simulation(events, this.trafficDisplayer));
            setActiveSimulation("DefaultSimulation");
             
            
            this.getWwd().getView().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    Object newValue = evt.getNewValue();
                    if (newValue instanceof BasicView) {
                        BasicView view = (BasicView) newValue;
                        Position eyePosition = view.getEyePosition();
                        
                        // Debounce updates to trafficDisplayer
                        debounceUpdate(eyePosition.getAltitude());
                    }
                }
            });
                  
            // Initialize the debounce timer with a delay and the action to perform
            debounceTimer = new Timer(100, e -> { // 100 ms delay
                // Perform your update action here. This block will only execute after 500ms of inactivity.
                // Since the actionListener cannot directly access `eyePosition` from here,
                // you need to store it somewhere accessible on the last call to `debounceUpdate`.
                trafficDisplayer.viewUpdated(viewAltitude);
            });
            debounceTimer.setRepeats(false); // Ensure the timer only fires once after the last event

            
            
            // Put the pieces together.
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            
            //Include all the menus.
            if (includeLayerPanel) {
                this.controlPanel = new JPanel(new BorderLayout(10, 10));
                this.layerPanel = new LayerPanel(this.getWwd());
                this.controlPanel.add(this.layerPanel, BorderLayout.CENTER);
                
                //Añade el controllador de la ruta al menu lateral
                //this.controlPanel.add(new RoutesMenu(routeController),BorderLayout.AFTER_LAST_LINE); //add custom menu
                
                // Create a vertical BoxLayout
                Box controlBox = Box.createVerticalBox();
                // Time Management Menu
                TimeManagementMenu timeManagementPanel = new TimeManagementMenu(this.activeSimulation);
                this.activeSimulation.setTimeUpdateListener(timeManagementPanel);
   
                //Console Menu
                Console console = new Console(this.activeSimulation);
                
                // Add timeManagementPanel and console to the vertical BoxLayout
                controlBox.add(timeManagementPanel);
                controlBox.add(console);

                // Add the vertical BoxLayout to the layerPanel using BorderLayout.CENTER
                this.controlPanel.add(controlBox, BorderLayout.AFTER_LAST_LINE);
                
                this.controlPanel.add(new FlatWorldPanel(this.getWwd()), BorderLayout.NORTH);
                this.getContentPane().add(this.controlPanel, BorderLayout.WEST);
            }

            if (includeStatsPanel || System.getProperty("gov.nasa.worldwind.showStatistics") != null) {
                this.statsPanel = new StatisticsPanel(this.wwjPanel.getWwd(), new Dimension(250, canvasSize.height));
                this.getContentPane().add(this.statsPanel, BorderLayout.EAST);
            }

            // Create and install the view controls layer and register a controller for it with the WorldWindow.
            ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
            insertBeforeCompass(getWwd(), viewControlsLayer);
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

            // Register a rendering exception listener that's notified when exceptions occur during rendering.
            this.wwjPanel.getWwd().addRenderingExceptionListener((Throwable t) -> {
                if (t instanceof WWAbsentRequirementException) {
                    String message = "Computer does not meet minimum graphics requirements.\n";
                    message += "Please install up-to-date graphics driver and try again.\n";
                    message += "Reason: " + t.getMessage() + "\n";
                    message += "This program will end when you press OK.";

                    JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(-1);
                }
            });

            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers()) {
                if (layer instanceof SelectListener) {
                    this.getWwd().addSelectListener((SelectListener) layer);
                }
            }

            

            // Center the application on the screen.
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
            

            //Insert layers in wwmap
            insertAfterPlacenames(this.getWwd(), trafficDisplayer.getAnnotationLayer());
            insertAfterPlacenames(this.getWwd(), trafficDisplayer.getTrafficLayer());
            insertAfterPlacenames(this.getWwd(), trafficDisplayer.getTrafficPolygonLayer());
            
            RenderableLayer airportLayer = new RenderableLayer();
            airportLayer.setName("Airport Renderable Layer");
                    
            try {
                List<Runway> runwayList = Runway.createRunwaysFromTextFile("src/airports/runways.txt");
                for (Runway runway : runwayList) {      
                    airportLayer.addRenderable(runway.getPolygon());
                }
                insertAfterPlacenames(this.getWwd(), airportLayer);
                
                RenderableLayer servitudesLayer = new RenderableLayer();
                airportLayer.setName("Servitudes Layer");

                TakeOffSurface takeOffSurface = new TakeOffSurface(3000,runwayList.get(0).getRunwayStart(),200);
                servitudesLayer.addRenderable(takeOffSurface.getPolygon());
                insertAfterPlacenames(this.getWwd(), servitudesLayer);
                
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            

            
            
            
            //insertAfterPlacenames(this.getWwd(), new LatLonGraticuleLayer());             
          
            //insertAfterPlacenames(this.getWwd(), routeController.getPlanesLayer());
            //insertAfterPlacenames(this.getWwd(), routeController.getRouteLayer());
            //insertAfterPlacenames(this.getWwd(), routeController.getAirportLayer());
            //insertAfterPlacenames(this.getWwd(), new AirSpaceLayer().layer);
            
            
            // Limpia todos los threads antes de salir de la app
            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                    System.out.println("CERRANDO LA VENTANA");
                    //routeController.stopit();
                    //antennaDisplayer.stopit();
                    activeSimulation.stopit(); //We should stop all simulations for the moment just the activeSimulation
                }
            });
    
            //System.out.println(this.wwjPanel.getWwd().getView().getEyePosition().getAltitude());
            System.out.println(this.wwjPanel.getWwd().getSceneController().getView().getEyePosition());
            this.pack();
            
             
                       
        }

        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar) {
            return new AppPanel(canvasSize, includeStatusBar);
        }

        public Dimension getCanvasSize() {
            return canvasSize;
        }

        public AppPanel getWwjPanel() {
            return wwjPanel;
        }

        public WorldWindow getWwd() {
            return this.wwjPanel.getWwd();
        }

        public StatusBar getStatusBar() {
            return this.wwjPanel.getStatusBar();
        }

        /**
         * @deprecated Use getControlPanel instead.
         * @return This application's layer panel.
         */
        @Deprecated
        public LayerPanel getLayerPanel() {
            return this.layerPanel;
        }

        public JPanel getControlPanel() {
            return this.controlPanel;
        }

        public StatisticsPanel getStatsPanel() {
            return statsPanel;
        }

        public void setToolTipController(ToolTipController controller) {
            if (this.wwjPanel.toolTipController != null) {
                this.wwjPanel.toolTipController.dispose();
            }

            this.wwjPanel.toolTipController = controller;
        }

        public void setHighlightController(HighlightController controller) {
            if (this.wwjPanel.highlightController != null) {
                this.wwjPanel.highlightController.dispose();
            }

            this.wwjPanel.highlightController = controller;
        }
        
        // Method to add simulations to the map
        public void addSimulation(String name, Simulation simulation) {
            simulationMap.put(name, simulation);
        }

        // Method to set the active simulation
        public void setActiveSimulation(String name) {
            activeSimulation = simulationMap.get(name);
            // Optionally handle if the simulation name doesn't exist in the map
        }
    }

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    public static void insertAfterPlacenames(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just after the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof PlaceNameLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition + 1, layer);
    }

    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName) {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l.getName().contains(targetName)) {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (Configuration.isWindowsOS()) {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, Class<?> appFrameClass) {
 
        if (Configuration.isMacOS() && appName != null) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try {
            final AppFrame frame = (AppFrame) appFrameClass.getConstructor().newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocation(new Point(300,300));
            java.awt.EventQueue.invokeLater(() -> {
                frame.setVisible(true);
            });

            return frame;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        GUI.start("Visualizador de Rutas", AppFrame.class);
    }
}
