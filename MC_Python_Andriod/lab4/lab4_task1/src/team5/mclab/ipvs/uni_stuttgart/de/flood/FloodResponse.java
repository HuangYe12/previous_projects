package team5.mclab.ipvs.uni_stuttgart.de.flood;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.FloodRequestMessage;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.MessageParser;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpReceiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 *
 * Process received response for a flood request
 */
public class FloodResponse implements Runnable {
    private static Logger log = MyLogger.getLogger();

    private int srcPort;
    private UdpReceiver srcSocket;

    private boolean isRunnable;

    public FloodResponse(DatagramSocket f_srcSocket) {
        srcSocket = new UdpReceiver(f_srcSocket);

        isRunnable = true;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Start flood response listener");
        while (isRunnable) {
            try {
                DatagramPacket packet = srcSocket.receiveUdpMsg();
                if (packet != null) {
                    FloodRequestMessage msg = MessageParser.parsePacket(packet, " ");
                    log.log(Level.INFO, "Received a udp response packet");
                    log.log(Level.INFO, "-------");
                    log.log(Level.INFO, msg.toString());
                    log.log(Level.INFO, "-------");
                }
            } catch (Exception ex) {
                log.log(Level.INFO, ex.getMessage());
                log.log(Level.INFO, "Received exception!");
            }
        }
        srcSocket.close();
        log.log(Level.INFO, "flood response is exited");
    }

    public void stop() {
        isRunnable = false;
    }
}
