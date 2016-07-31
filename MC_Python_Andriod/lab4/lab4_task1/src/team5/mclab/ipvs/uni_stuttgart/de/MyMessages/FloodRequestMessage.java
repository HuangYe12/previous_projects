package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import java.net.InetAddress;

/**
 * Created by fangjun on 11/06/16.
 */
public class FloodRequestMessage {
    private InetAddress fromAddress;
    //private int fromUdpPort;
    private String messageContent;

    private boolean isResposne;

    private String srcIP;
    private int srcPort;
    private String dstIP;
    private int dstPort;

    private long timeSent;
    private long timeReceived;

    /**  milli-second*/
    private long linkDelay;


    public FloodRequestMessage() {

    }

    public FloodRequestMessage(InetAddress f_fromAddress, String f_messageContent, boolean f_isResposne) {
        fromAddress = f_fromAddress;
        messageContent = f_messageContent;
        isResposne = f_isResposne;
    }


    public long getLinkDelay() {
        return linkDelay;
    }

    public void setLinkDelay(long linkDelay) {
        this.linkDelay = linkDelay;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(long timeReceived) {
        this.timeReceived = timeReceived;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public String getDstIP() {
        return dstIP;
    }

    public void setDstIP(String dstIP) {
        this.dstIP = dstIP;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public boolean isResposne() {
        return isResposne;
    }

    public void setResposne(boolean resposne) {
        isResposne = resposne;
    }

    public InetAddress getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(InetAddress fromAddress) {
        this.fromAddress = fromAddress;
    }


    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public String toString(){

        String str = String.format("\nReceived from address: %s\n" +
                                    " Content: %s", fromAddress.getHostAddress(),
                                                    messageContent);
        return str;
/*
        String str = String.format("\nReceived from address: %s\n" +
                        " Port: %d\n" +
                        " Content: %s", fromAddress.getHostAddress(),
                                        fromUdpPort,
                                        messageContent);
 */
        }
}
