package team5.mclab.ipvs.uni_stuttgart.de;

/**
 * Created by fangjun on 11/06/16.
 */
interface MyConfig {
    int teamNumber = 20;
    int basePort = 5000;
    int udpPort = teamNumber + basePort;
    String interfaceName = "wlan0";

    String srcHostSendData = "192.168.24.31";
    String dstHostReceiveData = "192.168.24.111";

    // String broadcastAddress = "192.168.24.255";
    //String broadcastAddress = "localhost";
}
