package team5.mclab.ipvs.uni_stuttgart.de.Utilities;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 */
public class UdpReceiver {
    private static Logger log = MyLogger.getLogger();
    private DatagramSocket srcSocket;

    public UdpReceiver(DatagramSocket f_srcSocket) {
        srcSocket = f_srcSocket;
    }

    public UdpReceiver(int port) {
        try {
            srcSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "\nCreate udp socket failed");
            System.exit(-1);
        }
    }


    /**
     *
     * @param port receive udp message sent to this port
     * @return return received udp packet. Return null on error.
     */
    public DatagramPacket receiveUdpMsg() {

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            // TODO: delete this timeout statement
            srcSocket.setSoTimeout(2000); // comment out this! It is only for test!
            srcSocket.receive(packet);
        } catch (SocketTimeoutException e) {
            log.log(Level.INFO, e.getMessage());
            log.log(Level.INFO, "Timeout for receiving");
            return null;
        } catch (Exception e) {
            // e.printStackTrace();
            log.log(Level.SEVERE, e.getMessage());
            log.log(Level.SEVERE, "\nReceive udp packet failed");
            return null;
        }
        return packet;
    }

    public void close() {
        srcSocket.close();
    }
}
