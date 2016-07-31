package team5.mclab.ipvs.uni_stuttgart.de.Utilities;

import team5.mclab.ipvs.uni_stuttgart.de.Logger.MyLogger;

import java.util.logging.Logger;

/**
 * Created by fangjun on 19/06/16.
 */
public class GetNextHop {
    private static Logger log = MyLogger.getLogger();

    public static String getNextHop(String selfIP, String routes) {
        String res = null;

        log.fine(selfIP + ": " + routes);
        String[] s = routes.split("[|]");
        log.fine("length = " + s.length);

        for (int i = 0; i < s.length-1; i++) {

            if(s[i].equalsIgnoreCase(selfIP)) {
                log.fine("i = " + i + " length =  " + s.length );
                res = s[i+1];
                break;
            }
        }

        return res;
    }

    public static String getNextHopReverse(String selfIP, String routes) {
        String res = null;
        String[] s = routes.split("[|]");
        for (int i = s.length-1; i > 0; i--) {
            if(s[i].equalsIgnoreCase(selfIP)) {
                res = s[i-1];
                break;
            }
        }
        return res;
    }
}
