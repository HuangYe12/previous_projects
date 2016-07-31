package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by fangjun on 12/06/16.
 */
public class ReceivedMessages {
    public static Logger log = MyLogger.getLogger();

    private List<DSRMessage> rcvdMsgs;

    public ReceivedMessages() {
        rcvdMsgs = new ArrayList<>();
    }

    public synchronized void clear() {
        rcvdMsgs.clear();
    }
    public synchronized void addMessage(DSRMessage msg) {
        if (isAlreadyExist(msg) == false) {
            log.fine("add " + msg + " to received msgs");
            rcvdMsgs.add(msg);
        }
    }

    public synchronized boolean isAlreadyExist(DSRMessage msg) {
        String content = msg.getMessageContent().split("[|]")[0];
        for (DSRMessage m : rcvdMsgs) {
            if(m.getMessageContent().split("[|]")[0].equalsIgnoreCase(content))
                return true;
        }
        return false;
    }

}
