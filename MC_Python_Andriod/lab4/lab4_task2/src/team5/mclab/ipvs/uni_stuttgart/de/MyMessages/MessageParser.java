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


    public static DSRMessage parsePacket(DatagramPacket packet, String selfIP) {
        // RREQ_1_192.168.24.31:44641-192.168.24.111:5020-1466362819473|192.168.24.31|192.168.24.32|192.168.24.33|192.168.24.111
        //

        DSRMessage msg = new DSRMessage();

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

            String msgCode = s[0];
            if(msgCode.equalsIgnoreCase("RREQ")) {
                msg.setRREQ(true);
            } else if(msgCode.equalsIgnoreCase("RREP")) {
                msg.setRREP(true);
            } else if(msgCode.contains("DATA")) {
                log.info("Message is data");
                msg.setData(true);
            }

            int msgCnt = Integer.parseInt(s[1]);
            msg.setMsgCnt(msgCnt);


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

                if(dstIP.equalsIgnoreCase(selfIP)) {
                    if(msg.isRREQ() == true) {
                        msg.setShouldSendRREP(true);
                    } else if(msg.isRREP() == true) {
                        msg.setReplyForRREQ(true);
                    }
                } else {
                    msg.setShouldSendRREP(false);
                }

            } else {
                log.severe("__---___Incorrect message format!___---___");
                return msg;
            }

            if(selfIP.equalsIgnoreCase(srcIP) == true)
                msg.setResposne(true);
            else
                msg.setResposne(false);

            // tmp[1] tmp[2] tmp[3] are ip addresses along the route.
            String[] tmp = IPAndPort[2].split("[|]");
            long timeSent = Long.parseLong(tmp[0]);
            long timeReceived = Calendar.getInstance().getTimeInMillis();
            long linkDelay = timeReceived - timeSent;

            if(tmp.length > 1) {
                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < tmp.length-1; i++) {
                    sb.append(tmp[i]);
                    sb.append("|");
                }
                sb.append(tmp[tmp.length-1]);
                msg.setHostIPs(sb.toString());
            }



            msg.setTimeSent(timeSent);
            msg.setTimeReceived(timeReceived);
            msg.setLinkDelay(linkDelay);


        } else {
            log.severe("__---___Incorrect message format!___---___");
        }

        return msg;
    }
}
