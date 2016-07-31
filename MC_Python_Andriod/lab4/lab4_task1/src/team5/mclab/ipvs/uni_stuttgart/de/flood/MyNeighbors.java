package team5.mclab.ipvs.uni_stuttgart.de.flood;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by fangjun on 12/06/16.
 */
public class MyNeighbors {
    public static Logger log = MyLogger.getLogger();

    Set<String> neighborIPs;
    Map<String, List<Long> > linkDelay;

    public MyNeighbors() {
        neighborIPs = new HashSet<>();
        linkDelay = new HashMap<>();
    }

    public synchronized Set<String> getNeighborIPs() {
        Set<String> res = new HashSet<>();
        for (String s : neighborIPs) {
            if(linkDelay.containsKey(s) == true) {
                List<Long> delay = linkDelay.get(s);
                long ms = 0;
                for (Long d : delay) {
                    ms += d;
                }
                if(delay.size() != 0) {
                    ms = ms / delay.size();
                }

                res.add(String.format("%s\t%dms", s, ms));
            }
        }
        return res;
    }

    public synchronized void addNeighbors(String ip, long f_linkDelay) {
        neighborIPs.add(ip);

        if(linkDelay.containsKey(ip) == false) {
            log.info(String.format("\n\n@@@@@@@ add new link delay for %s @@@@@@@", ip));

            linkDelay.put(ip, new ArrayList<Long>());
        }

        linkDelay.get(ip).add(f_linkDelay);
    }

    public synchronized boolean hasNeighbor(String ip) {
        return neighborIPs.contains(ip);
    }

}
