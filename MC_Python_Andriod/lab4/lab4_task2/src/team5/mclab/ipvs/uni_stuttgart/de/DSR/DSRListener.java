package team5.mclab.ipvs.uni_stuttgart.de.DSR;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.DSRMessage;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.DSRRouteTable;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.MessageParser;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.ReceivedMessages;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.GetNextHop;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpReceiver;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpSender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DSRListener implements Runnable {
    private static Logger log = MyLogger.getLogger();

    private int udpPort;
    private boolean isRunnable;

    private UdpReceiver srcSocket;
    private InetAddress selfIP;
    private ReceivedMessages receivedMessages;

    private InetAddress broadcastIP;

    AtomicBoolean hasRoute;
    private Lock lock = null;
    private Condition condVar = null;

    private DSRRouteTable routeTable;


    public DSRListener(int f_udpPort, InetAddress f_selfIP, InetAddress f_broadcastIP,
                       ReceivedMessages f_receivedMessages,
                       AtomicBoolean f_hasRoute, Lock f_lock, Condition f_condVar, DSRRouteTable f_routeTable) {
        udpPort = f_udpPort;
        isRunnable = true;

        srcSocket = new UdpReceiver(udpPort);
        selfIP = f_selfIP;
        broadcastIP = f_broadcastIP;
        receivedMessages = f_receivedMessages;

        hasRoute = f_hasRoute;
        lock = f_lock;
        condVar = f_condVar;

        routeTable = f_routeTable;
    }

    private void forwardData(DSRMessage msg) {
        String nextHop = GetNextHop.getNextHop(selfIP.getHostAddress(), msg.getHostIPs());
        InetAddress nextHopIP  = null;

        try {
            nextHopIP = InetAddress.getByName(nextHop);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        log.info("Forward data to " + nextHop);


        UdpSender sender = new UdpSender(false);

        sender.sendUdpMsg(msg.getMessageContent(), nextHopIP, udpPort);
        sender.close();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Start DSR listener");

        while(isRunnable) {
            try {
                DatagramPacket packet = srcSocket.receiveUdpMsg();

                if(packet != null) {
                    if(packet.getAddress().getHostAddress().equalsIgnoreCase(selfIP.getHostAddress())) {
                        log.fine("*******Receive message from self, do not send response!*******");
                    } else {
                        DSRMessage msg = MessageParser.parsePacket(packet, selfIP.getHostAddress());

                        if(msg.isResposne() == true) {
                            log.fine("It is a response to previous packet! drop it!");
                            continue;
                        }

                        log.fine("Received a udp packet");
                        log.fine("-------------------------");
                        log.fine(msg.toString());
                        log.fine("-------------------------");


                        if(msg.isData()) {
                            if(msg.getDstIP().equalsIgnoreCase(selfIP.getHostAddress())) {
                                log.info("Received message from " + msg.getSrcIP());
                                log.info("\n-------------------------\n" + msg.getMessageContent() + "\n-------------------------\n");
                            } else {
                                forwardData(msg);
                            }
                        } else if(msg.isRREQ() == true) {
                            if(msg.isShouldSendRREP() == true) {
                                log.info(">>>Received RREQ packet\n " + msg.toString());

                                // send RREP message
                                log.info("\nnow send RREP message.");

                                log.fine("append dst ip " + selfIP.getHostAddress() + "to msg");

                                msg.appendHost(selfIP.getHostAddress());
                                log.info("msg is (after appending) " + msg.toString());

                                String str = String.format("%s_%d_%s:%d-%s:%d-%d|%s", "RREP", msg.getMsgCnt(), selfIP.getHostAddress(),
                                        udpPort,
                                        msg.getSrcIP(), udpPort, msg.getTimeSent(), msg.getHostIPs());
                                msg.setMessageContent(str);

                                sendRREP(msg);
                            } else if(receivedMessages.isAlreadyExist(msg) == false){
                                log.fine("##########This message does not exist yet ########");
                                log.info(">>>Received RREQ packet\n " + msg.toString());
                                receivedMessages.addMessage(msg);
                                msg.appendHost(selfIP.getHostAddress());

                                floodPacket(msg);
                            }
                        } else if(msg.isRREP() == true) {
                            if(msg.isReplyForRREQ() == true) {
                                // get the req for reply
                                log.info("Received RREP packet for REQ: " + msg.toString());

                                log.fine(">>>>wake up DSR sender");
                                lock.lock();
                                log.info("add route: " + msg.getSrcIP() + ": " + msg.getHostIPs());
                                routeTable.addRoute(msg.getSrcIP(), msg.getHostIPs());
                                hasRoute.set(true);
                                condVar.signalAll();
                                lock.unlock();
                            } else if(receivedMessages.isAlreadyExist(msg) == true) {
                                log.fine("The message has already been forwarded. Drop it!");
                                continue;
                            } else {
                                receivedMessages.addMessage(msg);
                                log.info(">>>Received RREP packet. \n" + msg.toString());
                                log.info("Now forward it to " + GetNextHop.getNextHopReverse(selfIP.getHostAddress(), msg.getHostIPs()));
                                sendRREP(msg);
                            }

                        } else {
                            log.warning("Unknown message type!");
                        }

        /*
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

         */
                    }
                }
            } finally {

            }

            {
                //log.info("Received exception in DSR listener " );
            }
        }
        srcSocket.close();
        log.fine("DSR request is exited");
    }

    private void sendRREP(DSRMessage msg) {


        String nextHop = GetNextHop.getNextHopReverse(selfIP.getHostAddress(), msg.getHostIPs());
        InetAddress nextHopIP  = null;

        try {
            nextHopIP = InetAddress.getByName(nextHop);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        UdpSender sender = new UdpSender(false);

        sender.sendUdpMsg(msg.getMessageContent(), nextHopIP, udpPort);
        sender.close();
    }

    private void floodPacket(DSRMessage msg) {

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
