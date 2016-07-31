package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import com.sun.xml.internal.xsom.impl.scd.Iterators;
import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by fangjun on 12/06/16.
 */
public class ReceivedFloodMessages {
    public static Logger log = MyLogger.getLogger();

    private List<FloodRequestMessage> rcvdMsgs;

    public ReceivedFloodMessages() {
        rcvdMsgs = new ArrayList<>();
    }

    public synchronized void clear() {
        rcvdMsgs.clear();
    }
    public synchronized void addMessage(FloodRequestMessage msg) {
        if (isAlreadyExist(msg) == false) {
            log.info("add " + msg + " to received msgs");
            rcvdMsgs.add(msg);
        }
    }

    public synchronized boolean isAlreadyExist(FloodRequestMessage msg) {
        String content = msg.getMessageContent();
        for (FloodRequestMessage m : rcvdMsgs) {
            if(m.getMessageContent().equalsIgnoreCase(content))
                return true;
        }
        return false;
    }

}
