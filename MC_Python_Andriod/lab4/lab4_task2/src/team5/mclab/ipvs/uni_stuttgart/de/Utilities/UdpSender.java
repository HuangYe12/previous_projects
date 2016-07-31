package team5.mclab.ipvs.uni_stuttgart.de.Utilities;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 */
public class UdpSender {
    private static Logger log = MyLogger.getLogger();

    private DatagramSocket srcSocket;
    private int srcPort;
    private boolean isBroaccast;

    public UdpSender(boolean f_isBroadcast) {
        try {
            isBroaccast = f_isBroadcast;
            srcSocket = new DatagramSocket();
            srcSocket.setBroadcast(isBroaccast);
            srcPort = srcSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "create socket for sending failed!");
            System.exit(-1);
        }
    }

    public DatagramSocket getSrcSocket() {
        return srcSocket;
    }

    public int getSrcPort() {
        return srcPort;
    }


    /**
     *
     * @param msg a string to send
     * @param dstAddress destination ip address
     * @param dstPort destination port
     * @return true if the message is sent successfully, false on errors
     */
    public  boolean sendUdpMsg(String msg, InetAddress dstAddress, int dstPort) {
        byte[] sendData = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, dstAddress, dstPort);

        try {
            srcSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "\nSend packet failed!");
            return false;
        }
        return true;
    }


    public void close() {
        srcSocket.close();
    }

}
