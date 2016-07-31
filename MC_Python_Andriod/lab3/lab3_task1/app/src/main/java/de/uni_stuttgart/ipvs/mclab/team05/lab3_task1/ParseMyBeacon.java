package de.uni_stuttgart.ipvs.mclab.team05.lab3_task1;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import java.net.URL;

/**
 * Created by fangjun on 04/06/16.
 */
public class ParseMyBeacon {
    // The Eddystone Service UUID, 0xFEAA.
    static final byte eddystone_frame_type_uid = 0x00;
    static final byte eddystone_frame_type_url = 0x10;
    static final byte eddystone_frame_type_tlm = 0x20;

    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");





    static public void parseBeacon(MyBeacon beacon, ScanResult result) {
        Log.i(MainActivity.TAG, "try to parse beacon");
        byte[] data = result.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);

        if(data == null)
        {
            Log.i(MainActivity.TAG, "data is null!");
            return;
        }

        beacon.setRssi(result.getRssi());
        beacon.setDeviceAddress(result.getDevice().getAddress());

        // for (byte b: data)
        //     Log.i(MainActivity.TAG, "" + b);


        switch (data[0]) {
            case eddystone_frame_type_uid:
                parse_eddystone_UID_frame(beacon,result);
                break;
            case eddystone_frame_type_url:
                parse_eddystone_URL_frame(beacon, result);
                break;
            case eddystone_frame_type_tlm:
                parse_eddystone_TLM_frame(beacon, result);
                break;
            default:
                Log.i(MainActivity.TAG, "unknown frame type during beacon parsing!");
                break;
        }

    }

    static private void parse_eddystone_UID_frame(MyBeacon beacon, ScanResult result) {

        byte[] data = result.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);

        int txPower0m = data[1];
        beacon.setTxpwr(txPower0m);

        int rssi = result.getRssi();
        double dist = distanceFromRssi(rssi, txPower0m);
        beacon.setDistance(dist);

        byte namespace_id_offset = 2;
        byte namespace_id_len = 10;

        final char[] HEX = "0123456789ABCDEF".toCharArray();

        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte i = 0; i < namespace_id_len; i++) {
            int t = data[namespace_id_offset + i] & 0xff;
            int high_bits = ( t >>> 4) & 0x0f;
            int low_bits = t & 0x0f;

            sb.append(HEX[high_bits]);
            sb.append(HEX[low_bits]);
        }

        beacon.setNamespace_id(sb.toString());

        StringBuilder s = new StringBuilder();
        s.append("0x");
        byte instance_id_offset = 12;
        byte instance_id_len = 6;
        for (byte i = 0; i < instance_id_len; i++) {
            int t = data[instance_id_offset + i] & 0xff;

            int high_bits = ( t >>> 4) & 0x0f;
            int low_bits = t & 0x0f;

            s.append(HEX[high_bits]);
            s.append(HEX[low_bits]);
        }
        beacon.setInstance_id(s.toString());

        byte reserved_offset = 18;
        byte reserved_len = 2;
        StringBuilder rb = new StringBuilder();
        rb.append("0x");
        for (byte i = 0; i < reserved_len; i++) {
            int t = data[reserved_offset + i] & 0xff;

            int high_bits = ( t >>> 4) & 0x0f;
            int low_bits = t & 0x0f;

            rb.append(HEX[high_bits]);
            rb.append(HEX[low_bits]);
        }
        beacon.setReserved(rb.toString());

        beacon.setUIDAvailable(true);

    }

    static private void parse_eddystone_URL_frame(MyBeacon beacon, ScanResult result) {
        byte[] data = result.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);

        int txPower0m = data[1];
        beacon.setTxpwr(txPower0m);

        int rssi = result.getRssi();
        double dist = distanceFromRssi(rssi, txPower0m);
        beacon.setDistance(dist);

        final SparseArray<String> URL_SCHEMES = new SparseArray<String>() {{
            put((byte) 0, "http://www.");
            put((byte) 1, "https://www.");
            put((byte) 2, "http://");
            put((byte) 3, "https://");
        }};

        final SparseArray<String> URL_CODES = new SparseArray<String>() {{
            put((byte) 0, ".com/");
            put((byte) 1, ".org/");
            put((byte) 2, ".edu/");
            put((byte) 3, ".net/");
            put((byte) 4, ".info/");
            put((byte) 5, ".biz/");
            put((byte) 6, ".gov/");
            put((byte) 7, ".com");
            put((byte) 8, ".org");
            put((byte) 9, ".edu");
            put((byte) 10, ".net");
            put((byte) 11, ".info");
            put((byte) 12, ".biz");
            put((byte) 13, ".gov");
        }};

        beacon.setTxpwr(data[1]);
        StringBuilder url = new StringBuilder();
        int url_offset = 3;

        byte scheme_code = data[2];

        String scheme = URL_SCHEMES.get(scheme_code);
        if(scheme != null) {
            url.append(scheme);
            for (int i = url_offset; i < data.length; i++) {
                byte b = data[i];
                if(URL_CODES.get(b) != null) {
                    url.append(URL_CODES.get(data[i]));
                } else {
                    url.append((char) b);
                }
            }
            beacon.setURLAvailable(true);
            beacon.setUrl(url.toString());
            Log.i(MainActivity.TAG, "parse ulr frame successfully!");
        }
    }

    static private void parse_eddystone_TLM_frame(MyBeacon beacon, ScanResult result) {
        byte[] data = result.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);

        byte version = data[1];
        beacon.setVersion(version);

        int high_bits = data[2];
        int low_bits = data[3];

        int voltage = high_bits*256 + low_bits;
        beacon.setBattery_voltage(voltage);

        high_bits = data[4];
        low_bits = data[5];
        float temperature = high_bits + (float)low_bits/256;
        beacon.setBeacon_temperature(temperature);

        beacon.setTLMAvailable(true);

        Log.i(MainActivity.TAG, "parse TLM frame successfully");
    }

    static private double distanceFromRssi(int rssi, int txPower0m) {
        int pathLoss = txPower0m - rssi;
        return Math.pow(10, (pathLoss - 41) / 20.0);
    }
}
