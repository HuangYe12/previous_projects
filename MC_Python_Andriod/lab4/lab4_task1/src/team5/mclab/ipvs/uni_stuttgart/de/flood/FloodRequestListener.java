package team5.mclab.ipvs.uni_stuttgart.de.flood;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.FloodRequestMessage;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.MessageParser;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.ReceivedFloodMessages;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpReceiver;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpSender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 */
public class FloodRequestListener implements Runnable {
    private static Logger log = MyLogger.getLogger();

    private int udpPort;
    private boolean isRunnable;

    private UdpReceiver srcSocket;
    private InetAddress selfIP;
    private ReceivedFloodMessages receivedMessages;

    private InetAddress broadcastIP;
    private MyNeighbors neighborIPs;



    public FloodRequestListener(int f_udpPort, InetAddress f_selfIP, InetAddress f_broadcastIP,
                                ReceivedFloodMessages f_receivedMessages, MyNeighbors f_neighborIPs) {
        udpPort = f_udpPort;
        isRunnable = true;

        srcSocket = new UdpReceiver(udpPort);
        selfIP = f_selfIP;
        broadcastIP = f_broadcastIP;
        receivedMessages = f_receivedMessages;
        neighborIPs = f_neighborIPs;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Start flood request listener");

        while(isRunnable) {
            try {
                DatagramPacket packet = srcSocket.receiveUdpMsg();

                if(packet != null) {
                    if(packet.getAddress().getHostAddress().equalsIgnoreCase(selfIP.getHostAddress())) {
                        log.info("*******Receive message from self, do not send response!*******");
                    } else {
                        FloodRequestMessage msg = MessageParser.parsePacket(packet, selfIP.getHostAddress());
                        log.log(Level.INFO, "Received a udp packet");
                        log.log(Level.INFO, "-------------------------");
                        log.log(Level.INFO, msg.toString());
                        log.log(Level.INFO, "-------------------------");

                        if(msg.isResposne() == true) {
                            log.info("##########Received response! Add to neighbor list########");
                            neighborIPs.addNeighbors(msg.getFromAddress().getHostAddress(), msg.getLinkDelay());
                        } else if(receivedMessages.isAlreadyExist(msg) == false) {
                            log.info("##########This message does not exist yet ########");
                            receivedMessages.addMessage(msg);
                            floodPacket(msg);
                        } else {
                            log.info("This message is already saved or is a response!");
                        }
                    }
                }
            } catch(Exception ex) {
                log.log(Level.INFO, "Received exception in flood listener");
            }
        }
        srcSocket.close();
        log.log(Level.INFO, "flood request is exited");
    }

    private void floodPacket(FloodRequestMessage msg) {

        log.log(Level.INFO, String.format("Flood packet to to %s:%d", broadcastIP.getHostAddress(),
                                                      udpPort));

        UdpSender sender = new UdpSender(true);
        sender.sendUdpMsg(msg.getMessageContent(), broadcastIP, udpPort);
        sender.close();
    }

    public void stop() {
        isRunnable = false;
    }
}
