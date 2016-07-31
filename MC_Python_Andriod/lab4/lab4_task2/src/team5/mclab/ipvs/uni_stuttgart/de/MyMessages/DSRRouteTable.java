package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by fangjun on 19/06/16.
 */
public class DSRRouteTable {
    private static Logger log = MyLogger.getLogger();

    private Map<String, String> routeTable;

    public DSRRouteTable() {
        routeTable = new HashMap<>();
    }

    public void addRoute(String hostIP, String route) {
        // eg: 192.168.24.111  ---> 192.168.24.31|192.168.24.32|192.168.24.33|192.168.24.111
        log.fine("add rote: " + hostIP + ": " + route);
        routeTable.put(hostIP, route);
    }

    public String getRoute(String hostIP) {
        log.fine("get route for " + hostIP);
        if(routeTable.containsKey(hostIP)) {
            return routeTable.get(hostIP);
        } else {
            return null;
        }
    }
}
