package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * Created by fangjun on 11/06/16.
 */
public class MessageParser {
    public static Logger log = MyLogger.getLogger();


    public static FloodRequestMessage parsePacket(DatagramPacket packet, String selfIP) {
        // Hello_3_192.168.24.121:37473-192.168.24.255:5005-1466280086191

        FloodRequestMessage msg = new FloodRequestMessage();

        InetAddress fromAddress = packet.getAddress();
        msg.setFromAddress(fromAddress);

        //int fromPort = packet.getPort();
        //msg.setFromUdpPort(fromPort);

        byte[] data = data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

        String contents = new String(data);
        msg.setMessageContent(contents);

        String srcIP;
        int srcPort;

        String dstIP;
        int dstPort;

        // String[] s = contents.split("[|]");
        String[] s = contents.split("[_]");
        if(s != null && s.length == 3) {
            String[] IPAndPort = s[2].split("[-]");

            String[] srcIPAndPort = IPAndPort[0].split("[:]");
            if(srcIPAndPort != null && srcIPAndPort.length == 2) {
                srcIP = srcIPAndPort[0];
                srcPort = Integer.parseInt(srcIPAndPort[1]);
                msg.setSrcIP(srcIP);
                msg.setSrcPort(srcPort);
            } else {
                log.severe("__---___Incorrect message format!___---___");
                return msg;
            }

            String[] dstIPAndPort = IPAndPort[1].split("[:]");
            if(dstIPAndPort != null && dstIPAndPort.length == 2) {
                dstIP = dstIPAndPort[0];
                dstPort = Integer.parseInt(dstIPAndPort[1]);
                msg.setDstIP(dstIP);
                msg.setDstPort(dstPort);
            } else {
                log.severe("__---___Incorrect message format!___---___");
                return msg;
            }

            if(selfIP.equalsIgnoreCase(srcIP) == true)
                msg.setResposne(true);
            else
                msg.setResposne(false);

            long timeSent = Long.parseLong(IPAndPort[2]);
            long timeReceived = Calendar.getInstance().getTimeInMillis();
            long linkDelay = timeReceived - timeSent;

            msg.setTimeSent(timeSent);
            msg.setTimeReceived(timeReceived);
            msg.setLinkDelay(linkDelay);


        } else {
            log.severe("__---___Incorrect message format!___---___");
        }

        return msg;
    }
}
