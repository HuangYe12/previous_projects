package team5.mclab.ipvs.uni_stuttgart.de.MyMessages;

import java.util.Calendar;

/**
 * Created by fangjun on 18/06/16.
 */
public class FloodMessageGenerator {
    private static int seq = 0;

    /**
     *
     * @param srcIP
     * @param srcPort
     * @param dstIP
     * @param dstPort
     * @param msgContent Hello_3_192.168.24.121:37473-192.168.24.255:5005-1466280086191
     * @return
     */
    public static String getMessage(String srcIP, int srcPort,
                                    String dstIP, int dstPort,
                                    String msgContent) {
        long ms = Calendar.getInstance().getTimeInMillis();

        return String.format("%s_%d_%s:%d-%s:%d-%d", msgContent, seq++,
                                                  srcIP, srcPort,
                                                  dstIP, dstPort,
                                                  ms);
    }
}
