package team5.mclab.ipvs.uni_stuttgart.de;

import team5.mclab.ipvs.uni_stuttgart.de.DSR.DSRSender;
import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.DSRRouteTable;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.ReceivedMessages;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.GetInterfaceIPByName;
import team5.mclab.ipvs.uni_stuttgart.de.DSR.DSRListener;

import java.net.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
references:
 Explicit Locks and Condition Variables
    - http://www.math.uni-hamburg.de/doc/java/tutorial/essential/threads/explicitlocks.html
 */
public class Main implements MyConfig{
    public static Logger log = MyLogger.getLogger();

    private InetAddress selfIP = null;
    private InetAddress selfBroadcastIP = null;
    private ReceivedMessages receivedMessages;

    private Lock lock = new ReentrantLock();
    private Condition condVar = lock.newCondition();
    private AtomicBoolean hasRoute = new AtomicBoolean(false);

    private boolean isSource = false;

    private DSRRouteTable routeTable = new DSRRouteTable();

    Main() {

        List<InetAddress> address= GetInterfaceIPByName.getInterfaceIpByName(interfaceName);
        if(address != null) {
            selfBroadcastIP = address.get(0);
            selfIP  = address.get(1);

            log.fine("broadcast address is " + selfBroadcastIP.getHostAddress());
            log.fine("unicast address is " + selfIP.getHostAddress());
        }
        else {
            log.severe("can not find ipv4 address for " + interfaceName);
            System.exit(-1);
        }

        receivedMessages = new ReceivedMessages();

        if(selfIP.getHostAddress().equalsIgnoreCase(srcHostSendData)) {
            isSource = true;
        }

    }

    public void run() {
        int runningTime = 20; // run for 2 seconds

        DSRSender dsrSender = null;

        Thread sendingThread = null;

        if(isSource) {
            InetAddress tmpIP = null;
            try {
                tmpIP = InetAddress.getByName(dstHostReceiveData);
            } catch (UnknownHostException ex) {
                log.info("unknown host " + dstHostReceiveData);
                System.exit(-1);
            }
            dsrSender = new DSRSender(selfIP, tmpIP, udpPort, selfBroadcastIP, lock, condVar, hasRoute, routeTable);
            sendingThread =  new Thread(dsrSender);
        }


        DSRListener myListener = new DSRListener(udpPort, selfIP, selfBroadcastIP, receivedMessages,
                                                 hasRoute, lock, condVar, routeTable);
        Thread listeningThread = new Thread(myListener);
        listeningThread.start();

        if(isSource) {
            sendingThread.start();
        }

        try {
            Thread.sleep(runningTime * 1000); // sleep for runningTime seconds

            myListener.stop();
            listeningThread.interrupt();
            listeningThread.join();


            if(isSource) {
                dsrSender.stop();
                sendingThread.interrupt();
                sendingThread.join();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.log(Level.INFO, "Program is terminated!");
    }


    public static void main(String[] args) {
        Main t = new Main();
        t.run();
    }
}
