package team5.mclab.ipvs.uni_stuttgart.de;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;
import team5.mclab.ipvs.uni_stuttgart.de.MyMessages.ReceivedFloodMessages;
import team5.mclab.ipvs.uni_stuttgart.de.Utilities.GetInterfaceIPByName;
import team5.mclab.ipvs.uni_stuttgart.de.flood.FloodRequestListener;
import team5.mclab.ipvs.uni_stuttgart.de.flood.FloodRequestSender;
import team5.mclab.ipvs.uni_stuttgart.de.flood.MyNeighbors;

import java.net.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements MyConfig{
    public static Logger log = MyLogger.getLogger();

    private InetAddress selfIP = null;
    private InetAddress selfBroadcastIP = null;
    private ReceivedFloodMessages receivedMessages;
    private MyNeighbors neighborIPs;

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

        receivedMessages = new ReceivedFloodMessages();
        neighborIPs = new MyNeighbors();
    }

    public void run() {
        int runningTime = 5; // run for 10 seconds

        FloodRequestListener myListener = new FloodRequestListener(udpPort, selfIP, selfBroadcastIP, receivedMessages, neighborIPs);
        FloodRequestSender myRequest = new FloodRequestSender(selfBroadcastIP, udpPort, true, selfIP);

        Thread listeningThread = new Thread(myListener);
        Thread sendingThread = new Thread(myRequest);

        listeningThread.start();
        sendingThread.start();

        try {
            Thread.sleep(runningTime * 2000); // sleep for 20 seconds
            myListener.stop();
            myRequest.stop();

            listeningThread.interrupt();
            sendingThread.interrupt();

            listeningThread.join();
            sendingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        printNeighbors();
        log.log(Level.INFO, "Program is terminated!");
    }

    public void printNeighbors() {
        StringBuilder b = new StringBuilder();
        b.append("\n### neighbor list of " + selfIP.getHostAddress() +": \n");
        Set<String> nn = neighborIPs.getNeighborIPs();
        if (nn.isEmpty()) {
            b.append(" - none");
        } else {
            for(String n : nn) {
                b.append(" - " + n);
                b.append("\n");
            }
        }

        b.append("\n");
        log.info(b.toString());
    }
    public static void main(String[] args) {
        Main t = new Main();
        t.run();
    }
}
