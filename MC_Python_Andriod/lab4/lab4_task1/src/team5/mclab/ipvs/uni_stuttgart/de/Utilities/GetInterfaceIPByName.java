package team5.mclab.ipvs.uni_stuttgart.de.Utilities;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by fangjun on 12/06/16.
 */
public class GetInterfaceIPByName {
    final static String fixedPart = "192.168.24";

    /**
     * Get the ipv4 address with prefix 192.168.24 of a specified interface.
     *  0 - broadcast address
     *  1 - ipv4 address
     * Return null if can not find such address.
     * @param interfaceName Interface name
     * @return null if it can not find one.
     */
    public static List<InetAddress>  getInterfaceIpByName(String interfaceName) {
        List<InetAddress> myAddress = new ArrayList<>();

        NetworkInterface net = null;

        try {
            net = NetworkInterface.getByName(interfaceName);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if(net != null) {
            List<InterfaceAddress> addrs = net.getInterfaceAddresses();
            for (InterfaceAddress addr : addrs) {
                InetAddress inetAddress = addr.getAddress();
                if (inetAddress instanceof Inet4Address) {
                    if (inetAddress.getHostAddress().contains(fixedPart)) {
                        myAddress.add(addr.getBroadcast());
                        myAddress.add(addr.getAddress());
                        return myAddress;
                    }
                }
            }
        }

            return null;
    }
}
