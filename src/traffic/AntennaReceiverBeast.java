/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package traffic;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.ModeSReply;
import org.opensky.libadsb.tools;

/**
 *
 * @author Nazariy
 */
public class AntennaReceiverBeast extends Thread implements AntennaReceiver {

    private static ModeSDecoder ADSBdecoder = new ModeSDecoder();
    
    //Socket por el que se va a obtener la información
    private Socket phone = null;

    //Tráfico y tabla que se van a ir actualizando
    AntennaListener tm = null;

    //BufferedReader donde se almacena la información que entra por el socket para ser leída
    private final BufferedReader br = null;
    //Streams de los que se obtiene la información
    private InputStream is;
    private DataInputStream dis = null;
    //Writer con el output stream que sale del socket
    private PrintWriter pw = null;
    //Antena funcionando
    private boolean running = false;
    //Arrays de bytes para almacenar por separado la información de cada mensaje Beast
    private final byte[] initial_bytes;
    private final byte[] type_byte;
    private final byte[] timestamp_bytes;
    private final byte[] short_message_bytes;
    private final byte[] long_message_bytes;

    //Buffers donde se almacenan los arrays de bytes correspondientes
    private ByteBuffer bb_timestamp;
    private ByteBuffer bb_message;

    //Timestamp local (desde date_start) y total (local + date_start)
    private long timestamp = 0;
    private long clock_offset;

    //Mensajes Modo S
    private String message;
    private String host;
    private ModeSReply msg;

    // without AntennaListener
    public AntennaReceiverBeast(String host, int port) throws IOException {
        this.host = host;

        initial_bytes = new byte[1];
        type_byte = new byte[1];
        timestamp_bytes = new byte[8];
        short_message_bytes = new byte[7];
        long_message_bytes = new byte[14];
        phone = new Socket(host, port);
        System.out.println("Antenna Receiver: new BEAST receiver for antenna on host: " + host);
        dis = new DataInputStream(phone.getInputStream());
        pw = new PrintWriter(phone.getOutputStream());

        this.running = true;
        this.start();
    }

    // with AntenaListener
    public AntennaReceiverBeast(String host, int port, AntennaListener tm) throws IOException {
        this.host = host;

        initial_bytes = new byte[1];
        type_byte = new byte[1];
        timestamp_bytes = new byte[8];
        short_message_bytes = new byte[7];
        long_message_bytes = new byte[14];

        this.tm = tm;

        phone = new Socket(host, port);
        System.out.println("Antenna Receiver: new BEAST receiver for antenna on host: " + host);
        dis = new DataInputStream(phone.getInputStream());
        pw = new PrintWriter(phone.getOutputStream());

        this.running = true;
        this.start();
    }

    public void stopit() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.isAlive();
    }

    public void run() {
        boolean update, status;
        timestamp = 0;
        while (running) {
            update = false;
            message = "";
            try {
                //http://wiki.modesbeast.com/Mode-S_Beast:Data_Output_Formats     
                do {
                    dis.read(initial_bytes, 0, 1);
                    //System.out.print("."+tools.toHexString(initial_bytes[0]));
                } while (initial_bytes[0] != 0x1a);

                dis.read(type_byte, 0, 1);
                //System.out.println("msg type: "+tools.toHexString(type_byte[0]));
                switch (type_byte[0]) {
                    case 0x31:
                        //System.out.println("Message type 0x31 was received.");
                        dis.skipBytes(9);
                        break;
                    case 0x32:
                        //System.out.println("Message type 0x32 was received.");
                        dis.read(timestamp_bytes, 2, 6);
                        bb_timestamp = ByteBuffer.wrap(timestamp_bytes);
                        timestamp = bb_timestamp.getLong();
                        //System.out.println(" BEAST -> TS:"+timestamp);

                        dis.skipBytes(1);
                        dis.read(short_message_bytes, 0, 7);

                        //bb_message = ByteBuffer.wrap(short_message_bytes);
                        message = tools.toHexString(short_message_bytes);
                        //System.out.println("Message 32: "+message);
                        update = true;
                        break;

                    case 0x33:
                        //System.out.println("Message type 0x33 was received.");
                        dis.read(timestamp_bytes, 2, 6);
                        bb_timestamp = ByteBuffer.wrap(timestamp_bytes);
                        timestamp = bb_timestamp.getLong();

                        dis.skipBytes(1);
                        dis.read(long_message_bytes, 0, 14);

                        //bb_message = ByteBuffer.wrap(long_message_bytes);
                        message = tools.toHexString(long_message_bytes);
                        //System.out.println("Message 33: "+message);
                        update = true;
                        break;

                    case 0x34:
                        dis.skipBytes(21);
                        //System.out.println("Message type 0x34 was received.");
                        break;

                    default:
                        //System.out.print("-"+tools.toHexString(type_byte[0]));
                        break;
                }
                if (clock_offset >= 0) {
                    timestamp = timestamp + clock_offset;
                    //clock_offset = 0;
                }

            } catch (IOException ex) {
                //Logger.getLogger(AntennaReceiver.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println("AntennaReceiverBeast: error in message reception");
            } catch (Exception ex) {
                //Logger.getLogger(AntennaReceiver.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println("AntennaReceiver: error processing message stream. "+ex.getMessage()+" / "+tools.toHexString(type_byte[0])+" TS: "+timestamp+" MSG: "+message);
            }

            try {
                msg = ADSBdecoder.decode(message);
                if (tm != null) {
                    if (update) {
                        synchronized (tm) {
                            status = tm.processMsg(timestamp, msg);
                            //if (status == false) {
                            //System.out.println("AntennaReceiverBeast: error updating message:" + message);
                            //}
                        }
                    }
                } else {
                    System.out.println("MSG " + tools.toHexString(msg.getIcao24()) + " " + msg.getType());
                }

            } catch (BadFormatException e) {
                //System.out.println("Traffic Map-> Malformed message:"+raw_message+" Skipping it. Message: " + e.getMessage());
            } catch (UnspecifiedFormatError e) {
                //System.out.println("Traffic Map-> Unspecified message! Skipping it...");
            } catch (Exception ex) {
                //Logger.getLogger(AntennaReceiver.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("AntennaReceiverBeast: exception while updating message. " + ex.getMessage() + " / " + tools.toHexString(type_byte[0]) + " TS: " + timestamp + " MSG: " + message);
            }
        }

        //Se ejecuta cuando la antena ya no está funcionando   
        try {
            dis.close();
            pw.close();
            phone.close();
        } catch (IOException ex) {
            //Logger.getLogger(AntennaReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("ReceiverThread for " + this.host + " finished");
    }
    
        //Get an static ADSB decoder -----------------------------------------------
    public static ModeSDecoder getADSBdecoder() {
        return ADSBdecoder;
    }


    @Override
    public void setListener(AntennaListener al) {
        this.tm = al;
    }
}
