package team5.mclab.ipvs.uni_stuttgart.de.DSR;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.DSRRequestMessageGenerator;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.DSRRouteTable;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.GetNextHop;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.UdpSender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;


public class DSRSender implements  Runnable{

    private static Logger log = MyLogger.getLogger();

    private InetAddress dstIP;
    private int dstPort;

    private InetAddress selfIP;
    private int selfPort;

    private boolean isRunnable = false;

    private Lock lock = null;
    private Condition condVar = null;

    private AtomicBoolean hasRoute;

    private InetAddress broadcastIP;

    private DSRRouteTable routeTable;


    UdpSender sender;
    public DSRSender(InetAddress f_selfIP, //int f_selfPort,
                     InetAddress f_dstIP, int f_dstPort,
                     InetAddress f_broadcastIP,
                     Lock f_lock, Condition f_condVar,
                     AtomicBoolean f_hasRoute,
                     DSRRouteTable f_routeTable) {
        selfIP = f_selfIP;
        //selfPort = f_selfPort;
        dstIP = f_dstIP;
        dstPort = f_dstPort;

        broadcastIP = f_broadcastIP;

        lock = f_lock;
        condVar = f_condVar;

        hasRoute = f_hasRoute;

        isRunnable = true;

        routeTable = f_routeTable;
    }

    Runnable routeDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            // send RREQ to discover route to dst
            String msg = null;
            for(;;) {
                msg = DSRRequestMessageGenerator.getMessage(selfIP.getHostAddress(), sender.getSrcPort(),
                        dstIP.getHostAddress(), dstPort, "RREQ");

                log.info("\n>>>Broadcast RREQ: \n " + msg);
                sender.sendUdpMsg(msg, broadcastIP, dstPort);

                try {
                    Thread.sleep(2 * 1000); // send RREQ every 1 second.
                } catch (InterruptedException e) {
                    break;
                }
            }
            log.info("Now stop sending RREQ");
        }
    };

    @Override
    public void run() {
        log.info("DSR sender is started");
        sender = new UdpSender(true);
        Thread routeDiscoveryThread = null;

        log.info("start to send data to host: " + dstIP);

        boolean isNeedToDiscoverRoute = false;
        long startTime = 0;
        long endTime = 0;

        while (isRunnable) {

            if(hasRoute.get() == false) {

                startTime = Calendar.getInstance().getTimeInMillis();
                isNeedToDiscoverRoute = true;

                log.info("No route information available to host: " + dstIP);
                log.info("Now send RREQ message");

                routeDiscoveryThread = new Thread(routeDiscoveryRunnable);
                routeDiscoveryThread.start();

                // wait for RREP reply.

                lock.lock();
                try {
                    while (hasRoute.get() == false) {
                        try {
                            condVar.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    log.fine("release lock now");
                    lock.unlock();
                    log.fine("stop sending RREQ thread now.");
                    routeDiscoveryThread.interrupt();
                    try {
                        routeDiscoveryThread.join();
                    } catch (InterruptedException e) {
                        log.warning("Interruption occurred when remove DSR RREQ sender");
                    }
                }
            } else {
                log.info("Found route to host: " + dstIP);
                if(isNeedToDiscoverRoute) {
                    endTime = Calendar.getInstance().getTimeInMillis();
                    log.info(String.format("It takes %d ms for route discovery.", endTime - startTime));
                }
                String routes = routeTable.getRoute(dstIP.getHostAddress());
                log.info("\n >>>>> " + routes);

                String s = "DATA Hello " + dstIP.getHostAddress();
                String msg = DSRRequestMessageGenerator.getDataMessage(selfIP.getHostAddress(), sender.getSrcPort(),
                        dstIP.getHostAddress(), dstPort, s, routes);

                String nextHop = GetNextHop.getNextHop(selfIP.getHostAddress(), routes);
                InetAddress nextHopIP  = null;

                try {
                    nextHopIP = InetAddress.getByName(nextHop);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                log.info("Try to send data to dst: " + dstIP);
                log.info(msg);

                log.info("First send to next hop " + nextHop);

                UdpSender sender = new UdpSender(false);

                sender.sendUdpMsg(msg, nextHopIP, dstPort);
                sender.close();

                break;
            }


            /*

            log.info("Try to get lock");
            lock.lock();
            log.info("Now get lock");
            try {
                while(hasRoute.get() == false) {
                    try {
                        log.info("wait for hasRoute to be true");
                        condVar.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("get route to dst!");

            } finally {
                log.info("release lock now");
                lock.unlock();
                log.info("exit dsrSender now");
            }

            */
        }
        sender.close();
        log.fine("now exit DSR Sender");
    }


    public void stop() {
        isRunnable = false;
    }


}
