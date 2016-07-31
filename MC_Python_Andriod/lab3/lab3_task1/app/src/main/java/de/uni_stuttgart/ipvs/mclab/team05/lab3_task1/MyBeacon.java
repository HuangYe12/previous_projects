package de.uni_stuttgart.ipvs.mclab.team05.lab3_task1;

import android.bluetooth.le.ScanResult;

/**
 * Created by fangjun on 04/06/16.
 */
public class MyBeacon {

    double distance;

    private int txpwr;
    private String url;
    private String deviceAddress;

    private byte version;
    private int battery_voltage;
    private float beacon_temperature;
    private int adv_pdu_cnt;
    private int uptime;

    private String namespace_id;
    private String instance_id;
    private String reserved;
    private String uid;

    private boolean isUIDAvailable = false;
    private boolean isTLMAvailable = false;
    private boolean isURLAvailable = false;

    private int rssi;

    public MyBeacon()
    {
        isTLMAvailable = false;
        isUIDAvailable = false;
        isURLAvailable = false;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isUIDAvailable() {
        return isUIDAvailable;
    }

    public void setUIDAvailable(boolean UIDAvailable) {
        isUIDAvailable = UIDAvailable;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public boolean isURLAvailable() {
        return isURLAvailable;
    }

    public void setURLAvailable(boolean URLAvailable) {
        isURLAvailable = URLAvailable;
    }

    public boolean isTLMAvailable() {
        return isTLMAvailable;
    }

    public void setTLMAvailable(boolean TLMAvailable) {
        isTLMAvailable = TLMAvailable;
    }

    public String getNamespace_id() {
        return namespace_id;
    }

    public void setNamespace_id(String namespace_id) {
        this.namespace_id = namespace_id;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public int getTxpwr() {
        return txpwr;
    }

    public void setTxpwr(int txpwr) {
        this.txpwr = txpwr;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public float getBattery_voltage() {
        return battery_voltage;
    }

    public void setBattery_voltage(int battery_voltage) {
        this.battery_voltage = battery_voltage;
    }

    public float getBeacon_temperature() {
        return beacon_temperature;
    }

    public void setBeacon_temperature(float beacon_temperature) {
        this.beacon_temperature = beacon_temperature;
    }

    public int getAdv_pdu_cnt() {
        return adv_pdu_cnt;
    }

    public void setAdv_pdu_cnt(int adv_pdu_cnt) {
        this.adv_pdu_cnt = adv_pdu_cnt;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
