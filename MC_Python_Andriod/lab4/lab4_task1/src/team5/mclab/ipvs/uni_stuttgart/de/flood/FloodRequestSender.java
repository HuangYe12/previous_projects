package team5.mclab.ipvs.uni_stuttgart.de.flood;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.FloodMessageGenerator;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpSender;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 *
 * Send a flood request every 1 second
 */
public class FloodRequestSender implements Runnable {
    private static Logger log = MyLogger.getLogger();

    private InetAddress dstIP;
    private int dstPort;
    private String dstIPstr;

    private String msg;


    private boolean isRunnable;

    private UdpSender srcSocket;
    private int srcPort;


    private InetAddress selfIP;
    private String selfIPstr;

    private String msgContent = "Hello";


    public FloodRequestSender(InetAddress f_dstAddress, int f_dstPort,
                              boolean f_isBroadcast, InetAddress f_selfIP) {
        dstIP = f_dstAddress;
        dstPort = f_dstPort;
        dstIPstr = dstIP.getHostAddress();

        isRunnable = true;

        srcSocket = new UdpSender(f_isBroadcast);
        srcPort = srcSocket.getSrcPort();

        selfIP = f_selfIP;
        selfIPstr = selfIP.getHostAddress();
    }

    private String generateMessage() {
        return FloodMessageGenerator.getMessage(selfIPstr, srcPort,
                dstIPstr, dstPort,
                msgContent);
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Start request");
        int interval = 1; // number of seconds between two packets

        while(isRunnable) {
            try {
                msg = generateMessage();
                srcSocket.sendUdpMsg(msg, dstIP, dstPort);
                log.log(Level.INFO, String.format("send udp packet '%s' to %s:%d successfully.",
                                            msg, dstIP.getHostAddress(), dstPort));

                Thread.sleep(interval * 1000); //
            } catch (Exception e) {
                //e.printStackTrace();
                log.log(Level.SEVERE, e.getMessage());
                log.log(Level.INFO, "Received exception in flood request");
            }
        }

        srcSocket.close();
        log.log(Level.INFO, "flood request is exited");
    }

    public void stop() {
        isRunnable = false;
    }
}
