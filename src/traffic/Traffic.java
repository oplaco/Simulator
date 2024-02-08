/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import java.text.DecimalFormat;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.msgs.AirborneOperationalStatusV1Msg;
import org.opensky.libadsb.msgs.AirborneOperationalStatusV2Msg;
import org.opensky.libadsb.msgs.AirbornePositionV0Msg;
import org.opensky.libadsb.msgs.AirbornePositionV1Msg;
import org.opensky.libadsb.msgs.AirbornePositionV2Msg;
import org.opensky.libadsb.msgs.AirspeedHeadingMsg;
import org.opensky.libadsb.msgs.AllCallReply;
import org.opensky.libadsb.msgs.AltitudeReply;
import org.opensky.libadsb.msgs.CommBAltitudeReply;
import org.opensky.libadsb.msgs.CommBIdentifyReply;
import org.opensky.libadsb.msgs.CommDExtendedLengthMsg;
import org.opensky.libadsb.msgs.EmergencyOrPriorityStatusMsg;
import org.opensky.libadsb.msgs.ExtendedSquitter;
import org.opensky.libadsb.msgs.IdentificationMsg;
import org.opensky.libadsb.msgs.IdentifyReply;
import org.opensky.libadsb.msgs.LongACAS;
import org.opensky.libadsb.msgs.MilitaryExtendedSquitter;
import org.opensky.libadsb.msgs.ModeSReply;
import static org.opensky.libadsb.msgs.ModeSReply.subtype.COMM_B_IDENTIFY_REPLY;
import static org.opensky.libadsb.msgs.ModeSReply.subtype.IDENTIFY_REPLY;
import org.opensky.libadsb.msgs.OperationalStatusV0Msg;
import org.opensky.libadsb.msgs.ShortACAS;
import org.opensky.libadsb.msgs.SurfaceOperationalStatusV1Msg;
import org.opensky.libadsb.msgs.SurfaceOperationalStatusV2Msg;
import org.opensky.libadsb.msgs.SurfacePositionV0Msg;
import org.opensky.libadsb.msgs.SurfacePositionV1Msg;
import org.opensky.libadsb.msgs.SurfacePositionV2Msg;
import org.opensky.libadsb.msgs.TCASResolutionAdvisoryMsg;
import org.opensky.libadsb.msgs.VelocityOverGroundMsg;
import org.opensky.libadsb.tools;

/**
 *
 * @author jvila
 */
public class Traffic {

    private final long MSEC = 1000000L;
    DecimalFormat df1 = new DecimalFormat(" #.0;-0.0");
    DecimalFormat df4 = new DecimalFormat(" 00.0000;-00.0000");
    DecimalFormat df3 = new DecimalFormat(" 000.0;-000.0");
    DecimalFormat df5 = new DecimalFormat(" 00000;-00000");

    //ICAO 24 transponder identifier
    private final String icao24;//***************************

    private String callsign = " ";//*****************************
    private String squawk = " ";//*****************************

    private double latitude = 0.0, longitude = 0.0;//*****************************
    private double altitude = 0.0;  // Barometric in ft//*****************************
    private double height = 0.0;    // Geometric  in ft
    private double ias, tas;        // kts //*****************************
    private double vr;              // fpm//*****************************
    private double gs;              // kts  //*****************************
    private double heading;         // deg
    private double track;           // deg//*****************************
    private boolean isOnGround = false, hasAlert = false, hasSPI = false; //*****************************
    private boolean isVrBaro, isAltBaro;

    // Position attributes 
    private Position position = null;
    private boolean isPositioned = false;

    //Position extrapolation
    private boolean isExtrapolated;//*****************************
    private double lat_ext, lon_ext, alt_ext;

    // Reference position
    private Position ref = null;

    //Timestamp from last received msg (date_start + msg timestamp)
    private long timestamp = 0;//*****************************
    //Timestamp from last position msg
    private long timestamp_pos;
    private long timestamp_alt;

    // -------------------------------------------------------------------------
    // Other attributes---------------------------------------------------------
    // -------------------------------------------------------------------------
    byte adsbVersion;
    /**
     * the version number of the formats and protocols in use on the aircraft
     * installation.<br>
     * 0: Conformant to DO-260/ED-102 and DO-242<br>
     * 1: Conformant to DO-260A and DO-242A<br>
     * 2: Conformant to DO-260B/ED-102A and DO-242B<br>
     * 3-7: reserved
     */

    double horizontalContainmentRadiusLimit;  // m (based on NIC)
    int geometricVerticalAccuracy;         // m (based on GVA)
    double positionUncertainty;               // m (based on NACp)
    double gsResolution;                      // kts


    String categoryDescription;     // Light, Small, Large, Heavy, Rotorcraft
    String interrogatorCode;        // Either the interrogator id or the surveillance id.
    String surveillanceStatus;      // As defines in DO-260B, No condition information, Permanent alert, SPI condition
    String emergencyStateText;      // no emergency, general emergency, lifeguard/medical, minimum fuel, ...
    String extendedSquitterMessage; //  7 byte Extended Squitter Message
    String militaryMessage;         // 13 byte Military Message
    String commDELMmessage;         // 10 byte Comm-D Extended Length message

    byte NIC;                  // Navigation Integrity Category -> horizontalContainmentRadiusLimit
    // According to DO-260B Table 2-14
    boolean hasNICSupplementA; // Supplement from operational status message.
    boolean hasNICSupplementB; // To refine horizontal containment radius limit and navigation integrity category
    boolean hasNICSupplementC;

    byte NACp;                // Navigation Accuracy Category for position (NACp) ->  positionUncertainty
    byte NACv;                // Navigation Accuracy Category for velocity (NACv)

    byte SIL;                 // Surveillance Integrity Level (SIL)
    // indicates the propability of exceeding the NIC containment radius
    // (see table A-15 in RTCA DO-260B)
    boolean hasSILSupplement;

    byte GVA;                // Geometric Vertical Accuracy. 1-> 150 m 2-> 45 m

    byte[] ModeACode;           // Four-digit Mode A (4096) code (only ADS-B version 2)
    byte systemDesignAssurance; // For interpretation see Table 2-65 in DO-260B

    byte militaryApplicationCode; // Code from the AF field

    boolean isMagneticNorth;

    boolean hasTCAS;
    boolean hasTCAS_RA;
    boolean hasRATerminated;
    byte tcasThreatType;      // (Annex 10 V4, 4.3.8.4.2.2.1.5)
    int tcasThreatIdentity;  // (Annex 10 V4, 4.3.8.4.2.2.1.6)
    boolean hasOperatingACAS;
    byte acasSsensitivityLevel;

    boolean hasCDTI;
    boolean hasBarometricAltitudeIntegrityCode;
    boolean has1090ESIn;
    boolean hasActiveIDENTSwitch; // IDENT switch active:
    boolean hasUATIn;             // UAT receiver
    boolean hasSingleAntenna;
    boolean hasAirReferencedVelocity; // Supports air-referenced velocity reports
    boolean hasLowTxPower;            // Whether transponder has less than 70 Watts transmit power
    boolean hasTrackHeadingInfo;
    boolean hasIFRCapability; // Iff aircraft has equipage class A1 or higher (ADS-B version 1 transponders only)

    int AirplaneLength; // m
    double AirplaneWidth;  // m

    byte GPSAntennaOffset; // Encoded longitudinal distance of the GPS Antenna from the NOSE

    byte commDELMsequenceNumber;
    boolean commDELMisAck;

    // -------------------------------------------------------------------------
    //Constructor
    
    public Traffic(String icao24, Position ref) {
        this.icao24 = icao24;
        this.ref = ref;
    }

    public Traffic(String icao24, String callsign, String squawk, long timestamp) {
        this.icao24 = icao24;
        this.callsign = callsign;
        this.squawk = squawk;
        this.timestamp = timestamp;
    }

    public Traffic(String icao24,
            String callsign,
            String squawk,
            Double lon,
            Double lat,
            Double alt,
            Double gs,
            Double track,
            Double vr,
            Boolean alert,
            Boolean SPI,
            Boolean iog,
            long timestamp) {

        //--------------------------------------------
        this.icao24 = icao24; //HexId
        this.callsign = callsign;
        this.squawk = squawk;
//        this.isExtrapolated = values[5].equals("1");
        this.longitude = lon;
        this.latitude = lat;
        this.altitude = alt;
        this.gs = gs;
//        this.ias = Double.parseDouble(values[10]);
//        this.tas = Double.parseDouble(values[11]);
        this.track = track;
        this.vr = vr;
        this.hasAlert = alert;
        this.hasSPI = SPI;
        this.isOnGround = iog;
        this.timestamp = timestamp;

    }

    // -------------------------------------------------------------------------
    public boolean updateTraffic(ModeSReply msg, long timestamp) {
        boolean retval = true;

        this.timestamp = timestamp;

        if (tools.isZero(msg.getParity()) || msg.checkParity()) { // CRC is ok
            // now check the message type

            switch (msg.getType()) {
                case ADSB_AIRBORN_POSITION_V0:
                case ADSB_AIRBORN_POSITION_V1:
                case ADSB_AIRBORN_POSITION_V2:
                    AirbornePositionV0Msg ap0 = (AirbornePositionV0Msg) msg;

                    // use CPR to decode position
                    // CPR needs at least 2 positions or a reference, otherwise we get null here
                    Position c0 = AntennaReceiverBeast.getADSBdecoder().decodePosition(timestamp / MSEC, ap0, ref);
                    if (c0 != null) {
                        this.latitude = c0.getLatitude();
                        this.longitude = c0.getLongitude();
                        this.horizontalContainmentRadiusLimit = ap0.getHorizontalContainmentRadiusLimit();

                        this.isPositioned = true;
                        this.timestamp_pos = getTimestamp();

                        this.lon_ext = this.longitude;
                        this.lat_ext = this.latitude;
                        this.isExtrapolated = false;
                        //System.out.println(icao24+" POSITIONED");
                    }

                    this.isAltBaro = ap0.isBarometricAltitude();
                    if (ap0.isBarometricAltitude()) {
                        if (ap0.hasAltitude()) {
                            this.altitude = ap0.getAltitude().doubleValue(); //ft
                        }
                    } else {
                        if (ap0.hasAltitude()) {
                            this.height = ap0.getAltitude().doubleValue(); //ft
                        }
                    }

                    Integer geoMinusBaro = AntennaReceiverBeast.getADSBdecoder().getGeoMinusBaro(msg);
                    if (ap0.hasAltitude() && ap0.isBarometricAltitude() && geoMinusBaro != null) {
                        this.height = ap0.getAltitude().doubleValue() + geoMinusBaro.doubleValue();
                    }

                    this.NIC = ap0.getNIC();
                    this.surveillanceStatus = ap0.getSurveillanceStatusDescription();

                    // we want to inspect fields for ADS-B of different versions
                    switch (msg.getType()) {
                        case ADSB_AIRBORN_POSITION_V0:
                            // NACp and SIL for newer ADS-B versions contained in operational status message
                            this.NACp = ap0.getNACp();
                            this.positionUncertainty = ap0.getPositionUncertainty();
                            this.SIL = ap0.getSIL();
                            break;
                        case ADSB_AIRBORN_POSITION_V1:
                            AirbornePositionV1Msg ap1 = (AirbornePositionV1Msg) msg;
                            this.hasNICSupplementA = ap1.hasNICSupplementA();
                            break;
                        case ADSB_AIRBORN_POSITION_V2:
                            AirbornePositionV2Msg ap2 = (AirbornePositionV2Msg) msg;
                            // NIC supplement A contained in operational status messages is set by the decoder if present
                            this.hasNICSupplementA = ap2.hasNICSupplementA();
                            this.hasNICSupplementB = ap2.hasNICSupplementB();
                            break;
                    }
                    break;

                case ADSB_SURFACE_POSITION_V0:
                case ADSB_SURFACE_POSITION_V1:
                case ADSB_SURFACE_POSITION_V2:
                    SurfacePositionV0Msg sp0 = (SurfacePositionV0Msg) msg;

                    Position sPos0 = AntennaReceiverBeast.getADSBdecoder().decodePosition(timestamp / MSEC, sp0, ref);
                    // decode the position if possible; prior position needed
                    if (sPos0 != null) {
                        this.latitude = sPos0.getLatitude();
                        this.longitude = sPos0.getLongitude();

                        this.isPositioned = true;
                        this.timestamp_pos = getTimestamp();

                        this.lon_ext = this.longitude;
                        this.lat_ext = this.latitude;
                        this.isExtrapolated = false;
                    }
                    this.horizontalContainmentRadiusLimit = sp0.getHorizontalContainmentRadiusLimit();

                    if (sp0.hasValidHeading()) {
                        this.track = sp0.getHeading();
                        this.heading = this.track;
                    }

                    if (sp0.hasGroundSpeed()) {
                        this.gs = sp0.getGroundSpeed(); //kts
                        this.gsResolution = sp0.getGroundSpeed(); //kts
                        this.tas = this.gs;
                    }

                    // we want to inspect fields for ADS-B of different versions
                    switch (msg.getType()) {
                        case ADSB_SURFACE_POSITION_V0:
                            // NACp and SIL for newer ADS-B versions contained in operational status message
                            // Use the following only with version 0 as the others are more accurate
                            this.NACp = sp0.getNACp();
                            this.positionUncertainty = sp0.getPositionUncertainty();
                            this.SIL = sp0.getSIL();
                            break;
                        case ADSB_SURFACE_POSITION_V1:
                            SurfacePositionV1Msg sp1 = (SurfacePositionV1Msg) msg;
                            this.hasNICSupplementA = sp1.hasNICSupplementA();
                            break;
                        case ADSB_SURFACE_POSITION_V2:
                            SurfacePositionV2Msg sp2 = (SurfacePositionV2Msg) msg;
                            // NIC supplement C contained in operational status messages is set by the decoder if present
                            this.hasNICSupplementA = sp2.hasNICSupplementA();
                            this.hasNICSupplementC = sp2.hasNICSupplementC();
                            break;
                    }
                    this.isOnGround = true;
                    break;

                case ADSB_EMERGENCY:
                    EmergencyOrPriorityStatusMsg status = (EmergencyOrPriorityStatusMsg) msg;
                    this.emergencyStateText = status.getEmergencyStateText();
                    this.ModeACode = status.getModeACode();
                    break;

                case ADSB_AIRSPEED:
                    AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg) msg;
                    if (airspeed.hasAirspeedInfo()) {
                        if (airspeed.isTrueAirspeed()) {
                            this.tas = airspeed.getAirspeed();
                        } else {
                            this.ias = airspeed.getAirspeed();
                        }
                    }

                    if (AntennaReceiverBeast.getADSBdecoder().getAdsbVersion(msg) == 0) {
                        // version 0 flag indicates true or magnetic north
                        try {
                            this.heading = airspeed.getHeading();
                            this.isMagneticNorth = (airspeed.hasHeadingStatusFlag() ? true : false);
                        } catch (Exception e) {
                        }
                        //} else {
                        //    // version 1+ flag indicates if heading is available at all
                        //    System.out.println("          Heading: "
                        //            + (airspeed.hasHeadingStatusFlag() ? airspeed.getHeading() + "°" : "unkown"));
                    }

                    if (airspeed.hasVerticalRateInfo()) {
                        this.vr = airspeed.getVerticalRate().doubleValue();
                        this.isVrBaro = airspeed.isBarometricVerticalSpeed();
                    }
                    break;

                case ADSB_IDENTIFICATION:
                    IdentificationMsg ident = (IdentificationMsg) msg;
                    this.callsign = new String(ident.getIdentity());
                    this.categoryDescription = ident.getCategoryDescription();
                    break;

                case ADSB_STATUS_V0:
                    OperationalStatusV0Msg opstat0 = (OperationalStatusV0Msg) msg;
                    adsbVersion = opstat0.getVersion();
                    hasTCAS = opstat0.hasOperationalTCAS();
                    hasCDTI = opstat0.hasOperationalCDTI();
                    break;

                case ADSB_AIRBORN_STATUS_V1:
                case ADSB_AIRBORN_STATUS_V2:
                    AirborneOperationalStatusV1Msg opstatA1 = (AirborneOperationalStatusV1Msg) msg;
                    adsbVersion = opstatA1.getVersion();
                    this.GVA = opstatA1.getGVA();
                    this.hasBarometricAltitudeIntegrityCode = opstatA1.getBarometricAltitudeIntegrityCode();
                    this.geometricVerticalAccuracy = opstatA1.getGeometricVerticalAccuracy(); // m
                    if (opstatA1.getHorizontalReferenceDirection()) {
                        this.isMagneticNorth = false;
                    }
                    this.NACp = opstatA1.getNACp();
                    this.positionUncertainty = opstatA1.getPositionUncertainty();
                    this.hasNICSupplementA = opstatA1.hasNICSupplementA();
                    this.SIL = opstatA1.getSIL();
                    this.systemDesignAssurance = opstatA1.getSystemDesignAssurance();
                    this.has1090ESIn = opstatA1.has1090ESIn();
                    this.hasActiveIDENTSwitch = opstatA1.hasActiveIDENTSwitch();
                    this.hasTCAS = opstatA1.hasOperationalTCAS();
                    this.hasTCAS_RA = opstatA1.hasTCASResolutionAdvisory();
                    this.hasUATIn = opstatA1.hasUATIn();
                    this.hasSingleAntenna = opstatA1.hasSingleAntenna();
                    this.hasAirReferencedVelocity = opstatA1.hasAirReferencedVelocity();

                    // SIL supplement in version 2
                    if (msg instanceof AirborneOperationalStatusV2Msg) {
                        this.hasSILSupplement = ((AirborneOperationalStatusV2Msg) msg).hasSILSupplement();
                    }
                    break;

                case ADSB_SURFACE_STATUS_V1:
                case ADSB_SURFACE_STATUS_V2:
                    SurfaceOperationalStatusV1Msg opstatS1 = (SurfaceOperationalStatusV1Msg) msg;

                    this.adsbVersion = opstatS1.getVersion();
                    this.geometricVerticalAccuracy = opstatS1.getGeometricVerticalAccuracy(); // m

                    if (opstatS1.getHorizontalReferenceDirection()) {
                        this.isMagneticNorth = false;
                    }

                    this.NACp = opstatS1.getNACp();
                    this.positionUncertainty = opstatS1.getPositionUncertainty();
                    this.hasNICSupplementA = opstatS1.hasNICSupplementA();
                    this.hasNICSupplementC = opstatS1.getNICSupplementC();
                    this.SIL = opstatS1.getSIL();
                    this.systemDesignAssurance = opstatS1.getSystemDesignAssurance();
                    this.has1090ESIn = opstatS1.has1090ESIn();
                    this.hasActiveIDENTSwitch = opstatS1.hasActiveIDENTSwitch();
                    this.hasTCAS_RA = opstatS1.hasTCASResolutionAdvisory();
                    this.hasUATIn = opstatS1.hasUATIn();
                    this.hasSingleAntenna = opstatS1.hasSingleAntenna();
                    this.AirplaneLength = opstatS1.getAirplaneLength();
                    this.AirplaneWidth = opstatS1.getAirplaneWidth();
                    this.NACv = opstatS1.getNACv();
                    this.hasLowTxPower = opstatS1.hasLowTxPower();
                    this.GPSAntennaOffset = opstatS1.getGPSAntennaOffset();
                    this.hasTrackHeadingInfo = opstatS1.hasTrackHeadingInfo();

                    // SIL supplement in version 2
                    if (msg instanceof SurfaceOperationalStatusV2Msg) {
                        this.hasSILSupplement = ((SurfaceOperationalStatusV2Msg) msg).hasSILSupplement();
                    }
                    break;

                case ADSB_TCAS:
                    TCASResolutionAdvisoryMsg tcas = (TCASResolutionAdvisoryMsg) msg;

                    this.hasRATerminated = tcas.hasRATerminated();
                    this.tcasThreatType = tcas.getThreatType();

                    if (tcas.getThreatType() == 1) // it's a icao24 address
                    {
                        this.tcasThreatIdentity = tcas.getThreatIdentity();
                    }
                    break;

                case ADSB_VELOCITY:
                    VelocityOverGroundMsg veloc = (VelocityOverGroundMsg) msg;

                    if (veloc.hasVelocityInfo()) {
                        this.gs = veloc.getVelocity();;
                    }

                    if (veloc.hasVelocityInfo()) {
                        this.gs = veloc.getVelocity();
                        this.heading = veloc.getHeading();
                        this.track = this.heading;
                    }

                    if (veloc.hasVerticalRateInfo()) {
                        this.vr = veloc.getVerticalRate();
                    }

                    // the IFR flag is only used in ADS-B version 1. Although equipage is low, we still support it
                    if (AntennaReceiverBeast.getADSBdecoder().getAdsbVersion(veloc) == 1) {
                        this.hasIFRCapability = veloc.hasIFRCapability();
                    }
                    this.isVrBaro = veloc.isBarometricVerticalSpeed();
                    break;

                case EXTENDED_SQUITTER:
                    ExtendedSquitter extsq = (ExtendedSquitter) msg;
                    this.extendedSquitterMessage = tools.toHexString(extsq.getMessage());
                    //System.out.println("Traffic -> [" + icao24 + "]: Extended squitter message: "+extendedSquitterMessage);                  
                    //System.out.println("Traffic -> [" + icao24 + "]: Unknown extended squitter with type code " + ((ExtendedSquitter) msg).getFormatTypeCode() + "!");
                    break;
                case MODES_REPLY:
                    break;
                case SHORT_ACAS:
                    break;
                case ALTITUDE_REPLY:
                    break;
                case IDENTIFY_REPLY:
                    break;
                case ALL_CALL_REPLY:
                    break;
                case LONG_ACAS:
                    break;
                case MILITARY_EXTENDED_SQUITTER:
                    break;
                case COMM_B_ALTITUDE_REPLY:
                    break;
                case COMM_B_IDENTIFY_REPLY:
                    break;
                case COMM_D_ELM:
                    break;
                default:
                    throw new AssertionError(msg.getType().name());
            }
        } else if (msg.getDownlinkFormat() != 17) { // CRC failed
            switch (msg.getType()) {
                case MODES_REPLY:
                    retval = false;
                    //System.out.println("Traffic -> [" + icao24 + "]: Unknown message with DF " + msg.getDownlinkFormat());
                    break;

                case SHORT_ACAS:
                    ShortACAS acas = (ShortACAS) msg;
                    try {
                        this.altitude = acas.getAltitude();

                        this.timestamp_alt = getTimestamp();
                        this.alt_ext = this.altitude;
                        //this.isExtrapolated=false;

                    } catch (Exception e) {
                    }
                    this.isOnGround = !acas.isAirborne();
                    this.hasOperatingACAS = acas.hasOperatingACAS();
                    this.acasSsensitivityLevel = acas.getSensitivityLevel();
                    break;

                case ALTITUDE_REPLY:
                    AltitudeReply alti = (AltitudeReply) msg;
                    try {
                        this.altitude = alti.getAltitude();
                        this.isOnGround = alti.isOnGround();
                        this.hasAlert = alti.hasAlert();
                        this.hasSPI = alti.hasSPI();

                        this.timestamp_alt = getTimestamp();
                        this.alt_ext = this.altitude;
                        //this.isExtrapolated=false;

                    } catch (Exception e) {
                        //e.printStackTrace();
                        //System.out.println(alti);
                    }
                    break;

                case IDENTIFY_REPLY:
                    IdentifyReply identify = (IdentifyReply) msg;
                    this.squawk = identify.getIdentity();
                    this.isOnGround = identify.isOnGround();
                    this.hasAlert = identify.hasAlert();
                    this.hasSPI = identify.hasSPI();
                    //System.out.println(msg.toString());
                    //System.out.println("IDENTIFY REPLY: "+getSquawk()+" AID: "+antennaId + " positioned: "+isPositioned+" ICAO: "+icao24);
                    break;

                case ALL_CALL_REPLY:
                    AllCallReply allcall = (AllCallReply) msg;
                    if (allcall.hasValidInterrogatorCode()) {
                        this.interrogatorCode = tools.toHexString(allcall.getInterrogatorCode());
                    }

                    //System.out.println("Traffic ->  [" + icao24 + "]: All-call reply for " + tools.toHexString(allcall.getInterrogatorCode())
                    //        + " (" + (allcall.hasValidInterrogatorCode() ? "valid" : "invalid") + ")");
                    break;

                case LONG_ACAS:
                    LongACAS long_acas = (LongACAS) msg;
                    try {
                        this.altitude = long_acas.getAltitude();

                        this.timestamp_alt = getTimestamp();
                        this.alt_ext = this.altitude;
                        //this.isExtrapolated=false;     
                    } catch (Exception e) {
                    }
                    this.isOnGround = !long_acas.isAirborne();
                    if (long_acas.hasValidRAC());
                    this.hasAlert = long_acas.hasMultipleThreats();

                    //System.out.println("[" + icao24 + "]: Altitude is " + long_acas.getAltitude() + "ft and ACAS is "
                    //        + (long_acas.hasOperatingACAS() ? "operating." : "not operating."));
                    //System.out.println("          A/C is " + (long_acas.isAirborne() ? "airborne" : "on the ground")
                    //        + " and sensitivity level is " + long_acas.getSensitivityLevel());
                    //System.out.println("          RAC is " + (long_acas.hasValidRAC() ? "valid" : "not valid")
                    //        + " and is " + long_acas.getResolutionAdvisoryComplement() + " (MTE=" + long_acas.hasMultipleThreats() + ")");
                    //System.out.println("          Maximum airspeed is " + long_acas.getMaximumAirspeed() + "kn.");                   
                    break;

                case MILITARY_EXTENDED_SQUITTER:
                    MilitaryExtendedSquitter mil = (MilitaryExtendedSquitter) msg;
                    this.militaryApplicationCode = mil.getApplicationCode();
                    this.militaryMessage = tools.toHexString(mil.getMessage());
                    break;

                case COMM_B_ALTITUDE_REPLY:
                    CommBAltitudeReply commBaltitude = (CommBAltitudeReply) msg;
                    try {
                        this.altitude = commBaltitude.getAltitude();

                        this.timestamp_alt = getTimestamp();
                        this.alt_ext = this.altitude;
                        //this.isExtrapolated=false;
                    } catch (Exception e) {
                        //e.printStackTrace();
                        //System.out.println(commBaltitude);
                    }
                    this.isOnGround = commBaltitude.isOnGround();
                    this.hasAlert = commBaltitude.hasAlert();
                    this.hasSPI = commBaltitude.hasSPI();
                    break;

                case COMM_B_IDENTIFY_REPLY:
                    CommBIdentifyReply commBidentify = (CommBIdentifyReply) msg;
                    this.squawk = commBidentify.getIdentity();
                    this.isOnGround = commBidentify.isOnGround();
                    this.hasAlert = commBidentify.hasAlert();
                    this.hasSPI = commBidentify.hasSPI();
                    //System.out.println(msg.toString());
                    //System.out.println("IDENTIFY REPLY: "+getSquawk()+" AID: "+antennaId + " positioned: "+isPositioned+" ICAO: "+icao24);
                    break;

                case COMM_D_ELM:
                    CommDExtendedLengthMsg commDELM = (CommDExtendedLengthMsg) msg;
                    this.commDELMsequenceNumber = commDELM.getSequenceNumber();
                    this.commDELMisAck = commDELM.isAck();
                    this.commDELMmessage = tools.toHexString(commDELM.getMessage());
                    break;

                default:
                    retval = false;
                    break;
            }

        } else {
            //System.out.println("Traffic -> Message contains biterrors.\n" + msg);
            retval = false;
        }

        return retval;
    }

    public void resetTimestamps(long ts) {
        timestamp = ts;
        timestamp_pos = ts;
        timestamp_alt = ts;
    }

    //--------------------------------------------------------------------------
    public void extrapolatePosition(long ts) {
        //Extrapolate position. Loxo algorithm

        double dist, R = 6371000;
        double t_extpos = (new Long(ts).doubleValue() - new Long(timestamp_pos).doubleValue()) / (MSEC * 1000);

        double q, dlat, lat1, lat2, dPhi, dlon, alpha, lon1, lon2;
        double trackr;

        if (gs > 0 && !isOnGround && t_extpos > 0 && isPositioned) {
            dist = gs * 0.514444 * t_extpos;  // gs knots -> m/s

            //if (gs < 600 && t_extpos < 10) {
            alpha = dist / R;
            trackr = track * Math.PI / 180;
            lat1 = latitude * Math.PI / 180;
            lon1 = longitude * Math.PI / 180;

            dlat = alpha * Math.cos(trackr);
            lat2 = lat1 + dlat;

            dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4) / Math.tan(lat1 / 2 + Math.PI / 4));

            if (Double.isInfinite(dlat / dPhi)) {
                q = dlat / dPhi;
            } else {
                q = Math.cos(lat1);
            }

            dlon = alpha * Math.sin(trackr) / q;

            if (Math.abs(lat2) > Math.PI / 2) {
                if (lat2 > 0) {
                    lat2 = Math.PI - lat2;
                } else {
                    lat2 = -Math.PI - lat2;
                }
            }

            lon2 = ((lon1 + dlon + Math.PI) % (2 * Math.PI)) - Math.PI;

            lat_ext = lat2 * 180 / Math.PI;
            lon_ext = lon2 * 180 / Math.PI;

            isExtrapolated = true;

            //if (t_extpos > 2) {
            //System.out.println("Extrapolation for: " + icao24 + "- " +  latitude + "/ "+ lat_ext + " " + longitude +"/"+ lon_ext + " " + altitude+"/"+alt_ext);
            //System.out.print("Extr. POS for: " + getCallsign() + " - " + " t_extpos: " + t_extpos + " " + " dist: " + dist);
            //System.out.print(" dlat: "+  dlat*180/(2*Math.PI) + " dlon: "+  dlon*180/(2*Math.PI));
            //System.out.println(" lat / lat_ext "+  latitude + "/ "+ lat_ext + " lon/lon_ext "+  longitude + "/"+ lon_ext + " alt/alt_ext: "+  altitude + "/"+ alt_ext);
            //}
            //}
        }

    }

    public void extrapolateAltitude(long ts) {

        double t_extalt = (new Long(ts).doubleValue() - new Long(timestamp_alt).doubleValue()) / (MSEC * 1000);
        if (vr > 0 && vr < 4000 && !isOnGround && t_extalt > 0) {
            alt_ext = Math.round(altitude + vr / 60 * t_extalt); //vr -> fpm to fps

            isExtrapolated = true;

            //System.out.println("Extr. ALT for: " + getCallsign() + " lat / lat_ext "+  latitude + "/ "+ lat_ext + " lon/lon_ext "+  longitude + "/"+ lon_ext + " alt/alt_ext: "+  altitude + "/"+ alt_ext);
        }

    }

    private String fixedLengthString(String string, int length) {
        return String.format("%1$" + length + "s", string);
    }

    //println method------------------------------------------------------------
    public void println() {
        System.out.print("hex:" + fixedLengthString(this.icao24, 6));
        System.out.print(", cs:" + fixedLengthString(this.callsign, 8));
        System.out.print(", sq:" + fixedLengthString(this.squawk, 4));
        System.out.print(", lon:" + df4.format(this.longitude));
        System.out.print(", lat:" + df4.format(this.latitude));
        System.out.print(", alt:" + df5.format(this.altitude));
        System.out.print(", gs:" + df3.format(this.gs));
        System.out.print(", trk:" + df3.format(this.track));
        System.out.print(", vr:" + df5.format(this.vr));
        System.out.print(", alr:" + this.hasAlert);
        System.out.print(", spi:" + this.hasSPI);
        System.out.println(", iog:" + this.isOnGround);
    }

    //Timestamp methods---------------------------------------------------------
    public long getTimestamp() {
        return timestamp;
    }

    public long getTimestampPos() {
        return this.timestamp_pos;
    }

    public long getTimestampAlt() {
        return this.timestamp_alt;
    }

    //Getters ------------------------------------------------------------------
    public String getCallsign() {
        if (callsign == " ") {
            return icao24;
        } else {
            return callsign;
        }
    }

    public String getSquawk() {
        return this.squawk;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getIas() {
        return ias;
    }

    public double getTas() {
        return tas;
    }

    public double getHeading() {
        return heading;
    }

    public double getGs() {
        return gs;
    }

    public double getVr() {
        return vr;
    }

    public boolean isVr_baro() {
        return isVrBaro;
    }

    public boolean isAlt_baro() {
        return isAltBaro;
    }

    public double getTrack() {
        return track;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public boolean hasAlert() {
        return hasAlert;
    }

    public boolean hasSPI() {
        return hasSPI;
    }

    public String getICAO24() {
        return icao24;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isPositioned() {
        return isPositioned;
    }

    //Extrapolation methods-----------------------------------------------------
    public boolean isExtrapolation() {
        return isExtrapolated;
    }

    public double getLat_ext() {
        return lat_ext;
    }

    public double getLon_ext() {
        return lon_ext;
    }

    public double getAlt_ext() {
        return alt_ext;
    }

    public byte getAdsbVersion() {
        return adsbVersion;
    }

    //Clone method--------------------------------------------------------------
    public Traffic clone() {
        return new Traffic(icao24, callsign, squawk, longitude, latitude, altitude, gs, track, vr, hasAlert, hasSPI, isOnGround, timestamp);
    }

    @Override
    public String toString() {
        return icao24;
    }

    public double getFPA() {
        double slope = Math.atan2(this.vr * 0.00987473, this.gs) * 180 / Math.PI;
        return slope;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
}
