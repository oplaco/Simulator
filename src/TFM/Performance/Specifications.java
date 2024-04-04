/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TFM.Performance;

/**
 *
 * @author Gabriel Alfonsín Espín
 * Class for storing aircraft specifications
 */
public class Specifications {
    //TAKE-OFF
    private double V2IAS; //safe speed of takeoff (kts)
    
    //INITIAL CLIMB
    private double initialClimbIAS; // (kts)
    private double initialClimbClimbRate; // (ft/min)
    
    //CLIMB (to FL 150)
    private double climbFL150IAS;// (kts)
    private double climbFL150ClimbRate;//(ft/min)
    
    //CLIMB (to FL240)
    private double climbFL240IAS;// (kts)
    private double climbFL240ClimbRate;//(ft/min)
    
    //MACH CLIMB
    private double machClimbMach;//
    private double machClimbClimbRate;//(ft/min)
    
    //CRUISE
    private double cruiseMach;//
    private double cruiseTAS;//(kts)
    private double cruiseCeiling;//Flight Level
    
    //INITIAL DESCENT (to FL240)
    private double initialDescentMach;
    private double initialDescentClimbRate;//(ft/min)
    
    //DESCENT (to FL100)
    private double descentFL100IAS;//(kts)
    private double descentFL100ClimbRate;//(ft/min)
    
    //APPROACH
    private double approachIAS;//(kts)
    private double approachClimbRate; //(ft/min)
    
    //LANDING
    private double VatIAS; // Indicated airspeed at threshold (kts)
    
    //Technical Specifications
    private double wingSpan; // m
    private double lenght; // m
    private double height; // m
    
    /**
     * A320 specifications as an example in an empty constructor.
     * Data retrieved from https://contentzone.eurocontrol.int/aircraftperformance/details.aspx?ICAO=A320 at 03/04/2024
     */
    public Specifications(){
            //TAKE-OFF
    this.V2IAS = 145;

    this.initialClimbIAS = 175; // (kts)
    this.initialClimbClimbRate = 2500; // (ft/min)

    this.climbFL150IAS = 290;// (kts)
    this.climbFL150ClimbRate = 2000;//(ft/min)

    this.climbFL240IAS = 290;// (kts)
    this.climbFL240ClimbRate = 1400;//(ft/min)

    this.machClimbMach = 0.78;//
    this.machClimbClimbRate = 1000;//(ft/min)

    this.cruiseMach = 0.79;//
    this.cruiseTAS = 450;//(kts)
    this.cruiseCeiling = 390;//Flight Level

    this.initialDescentMach = 0.78;
    this.initialDescentClimbRate = 1000;//(ft/min)
    
    this.descentFL100IAS = 290;//(kts)
    this.descentFL100ClimbRate = 3500;//(ft/min)
    
    this.approachIAS = 250;//(kts)
    this.approachClimbRate = 210; //(ft/min)

    this.VatIAS = 137; // Indicated airspeed at threshold (kts)

    this.wingSpan = 34.1; // m
    this.lenght = 37.57; // m
    this.height = 11.76; // m       
    }

    public Specifications(double V2IAS, double initialClimbIAS, double initialClimbClimbRate, double climbFL150IAS, double climbFL150ClimbRate, double climbFL240IAS, double climbFL240ClimbRate, double machClimbMach, double machClimbClimbRate, double cruiseMach, double cruiseTAS, double cruiseCeiling, double initialDescentMach, double initialDescentClimbRate, double descentFL100IAS, double descentFL100ClimbRate, double approachIAS, double approachClimbRate, double VatIAS, double wingSpan, double lenght, double height) {
        this.V2IAS = V2IAS;
        this.initialClimbIAS = initialClimbIAS;
        this.initialClimbClimbRate = initialClimbClimbRate;
        this.climbFL150IAS = climbFL150IAS;
        this.climbFL150ClimbRate = climbFL150ClimbRate;
        this.climbFL240IAS = climbFL240IAS;
        this.climbFL240ClimbRate = climbFL240ClimbRate;
        this.machClimbMach = machClimbMach;
        this.machClimbClimbRate = machClimbClimbRate;
        this.cruiseMach = cruiseMach;
        this.cruiseTAS = cruiseTAS;
        this.cruiseCeiling = cruiseCeiling;
        this.initialDescentMach = initialDescentMach;
        this.initialDescentClimbRate = initialDescentClimbRate;
        this.descentFL100IAS = descentFL100IAS;
        this.descentFL100ClimbRate = descentFL100ClimbRate;
        this.approachIAS = approachIAS;
        this.approachClimbRate = approachClimbRate;
        this.VatIAS = VatIAS;
        this.wingSpan = wingSpan;
        this.lenght = lenght;
        this.height = height;
    }

    public double getV2IAS() {
        return V2IAS;
    }

    public void setV2IAS(double V2IAS) {
        this.V2IAS = V2IAS;
    }

    public double getInitialClimbIAS() {
        return initialClimbIAS;
    }

    public void setInitialClimbIAS(double initialClimbIAS) {
        this.initialClimbIAS = initialClimbIAS;
    }

    public double getInitialClimbClimbRate() {
        return initialClimbClimbRate;
    }

    public void setInitialClimbClimbRate(double initialClimbClimbRate) {
        this.initialClimbClimbRate = initialClimbClimbRate;
    }

    public double getClimbFL150IAS() {
        return climbFL150IAS;
    }

    public void setClimbFL150IAS(double climbFL150IAS) {
        this.climbFL150IAS = climbFL150IAS;
    }

    public double getClimbFL150ClimbRate() {
        return climbFL150ClimbRate;
    }

    public void setClimbFL150ClimbRate(double climbFL150ClimbRate) {
        this.climbFL150ClimbRate = climbFL150ClimbRate;
    }

    public double getClimbFL240IAS() {
        return climbFL240IAS;
    }

    public void setClimbFL240IAS(double climbFL240IAS) {
        this.climbFL240IAS = climbFL240IAS;
    }

    public double getClimbFL240ClimbRate() {
        return climbFL240ClimbRate;
    }

    public void setClimbFL240ClimbRate(double climbFL240ClimbRate) {
        this.climbFL240ClimbRate = climbFL240ClimbRate;
    }

    public double getMachClimbMach() {
        return machClimbMach;
    }

    public void setMachClimbMach(double machClimbMach) {
        this.machClimbMach = machClimbMach;
    }

    public double getMachClimbClimbRate() {
        return machClimbClimbRate;
    }

    public void setMachClimbClimbRate(double machClimbClimbRate) {
        this.machClimbClimbRate = machClimbClimbRate;
    }

    public double getCruiseMach() {
        return cruiseMach;
    }

    public void setCruiseMach(double cruiseMach) {
        this.cruiseMach = cruiseMach;
    }

    public double getCruiseTAS() {
        return cruiseTAS;
    }

    public void setCruiseTAS(double cruiseTAS) {
        this.cruiseTAS = cruiseTAS;
    }

    public double getCruiseCeiling() {
        return cruiseCeiling;
    }

    public void setCruiseCeiling(double cruiseCeiling) {
        this.cruiseCeiling = cruiseCeiling;
    }

    public double getInitialDescentMach() {
        return initialDescentMach;
    }

    public void setInitialDescentMach(double initialDescentMach) {
        this.initialDescentMach = initialDescentMach;
    }

    public double getInitialDescentClimbRate() {
        return initialDescentClimbRate;
    }

    public void setInitialDescentClimbRate(double initialDescentClimbRate) {
        this.initialDescentClimbRate = initialDescentClimbRate;
    }

    public double getDescentFL100IAS() {
        return descentFL100IAS;
    }

    public void setDescentFL100IAS(double descentFL100IAS) {
        this.descentFL100IAS = descentFL100IAS;
    }

    public double getDescentFL100ClimbRate() {
        return descentFL100ClimbRate;
    }

    public void setDescentFL100ClimbRate(double descentFL100ClimbRate) {
        this.descentFL100ClimbRate = descentFL100ClimbRate;
    }

    public double getApproachIAS() {
        return approachIAS;
    }

    public void setApproachIAS(double approachIAS) {
        this.approachIAS = approachIAS;
    }

    public double getApproachClimbRate() {
        return approachClimbRate;
    }

    public void setApproachClimbRate(double approachClimbRate) {
        this.approachClimbRate = approachClimbRate;
    }

    public double getVatIAS() {
        return VatIAS;
    }

    public void setVatIAS(double VatIAS) {
        this.VatIAS = VatIAS;
    }

    public double getWingSpan() {
        return wingSpan;
    }

    public void setWingSpan(double wingSpan) {
        this.wingSpan = wingSpan;
    }

    public double getLenght() {
        return lenght;
    }

    public void setLenght(double lenght) {
        this.lenght = lenght;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
    
    
}
