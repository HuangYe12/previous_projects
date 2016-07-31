package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import java.net.InetAddress;

/**
 * Created by fangjun on 11/06/16.
 */
public class DSRMessage {
    private InetAddress fromAddress = null;
    //private int fromUdpPort;
    private String messageContent = null;

    private boolean isResposne = false;

    private boolean isRREQ = false;
    private boolean isRREP = false;
    private boolean isReplyForRREQ = false;

    private boolean isData = false;

    private boolean shouldSendRREP = false;

    private int msgCnt;

    private String srcIP;
    private int srcPort;
    private String dstIP;
    private int dstPort;

    private String hostIPs;

    private long timeSent;
    private long timeReceived;

    /**  milli-second*/
    private long linkDelay;


    public DSRMessage() {

    }

    public DSRMessage(InetAddress f_fromAddress, String f_messageContent, boolean f_isResposne) {
        fromAddress = f_fromAddress;
        messageContent = f_messageContent;
        isResposne = f_isResposne;
    }

    public boolean isData() {
        return isData;
    }

    public void setData(boolean data) {
        isData = data;
    }

    public void appendHost(String hostIP) {
        hostIPs = hostIPs + ("|" + hostIP);
        messageContent += ("|" + hostIP);
    }

    public String getHostIPs() {
        return hostIPs;
    }

    public void setHostIPs(String hostIPs) {
        this.hostIPs = hostIPs;
    }

    public boolean isReplyForRREQ() {
        return isReplyForRREQ;
    }

    public void setReplyForRREQ(boolean replyForRREQ) {
        isReplyForRREQ = replyForRREQ;
    }

    public int getMsgCnt() {
        return msgCnt;
    }

    public void setMsgCnt(int msgCnt) {
        this.msgCnt = msgCnt;
    }

    public boolean isShouldSendRREP() {
        return shouldSendRREP;
    }

    public void setShouldSendRREP(boolean shouldSendRREP) {
        this.shouldSendRREP = shouldSendRREP;
    }

    public boolean isRREQ() {
        return isRREQ;
    }

    public void setRREQ(boolean RREQ) {
        isRREQ = RREQ;
    }

    public boolean isRREP() {
        return isRREP;
    }

    public void setRREP(boolean RREP) {
        isRREP = RREP;
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

        String str = String.format("Received from address: %s\n" +
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
