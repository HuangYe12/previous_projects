package de.uni_stuttgart.ipvs.mclab.team05.lab3_task1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "lab3_task1_team05";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    // FD:57:E0:DC:63:40

    private final String DEVICE_MAC = "FD:57:E0:DC:63:40";

    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");


    TextView txtViewRssi;
    TextView txtViewDeviceAddress;
    TextView txtViewDistance;
    TextView txtViewNamespaceId;
    TextView txtViewInstanceId;
    TextView txtViewTxPowerAt0m;

    TextView txtViewTemperature;
    TextView txtViewBatteryVolmV;

    TextView txtViewURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();

        txtViewRssi = (TextView) findViewById(R.id.rssi);
        txtViewDeviceAddress = (TextView) findViewById(R.id.deviceAddress);
        txtViewDistance = (TextView) findViewById(R.id.distance);
        txtViewNamespaceId = (TextView) findViewById(R.id.uidNamespace);
        txtViewInstanceId = (TextView) findViewById(R.id.uidInstance);
        txtViewTxPowerAt0m = (TextView) findViewById(R.id.txPowerAt0m);
        txtViewTemperature = (TextView) findViewById(R.id.temperature);
        txtViewBatteryVolmV = (TextView) findViewById(R.id.batteryVolmV);
        txtViewURL = (TextView) findViewById(R.id.url);


    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBluetoothLeScanner != null)
            stopScan();

        mBluetoothLeScanner = null;

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        init();
        startScan();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // parse scan result and display it
                    ScanResult result = (ScanResult) msg.obj;
                    Log.i(TAG, "BLE Device: ");
                    Log.i(TAG, "    Device name: " + result.getDevice().getName());
                    Log.i(TAG, "    MAC addres: " + result.getDevice().getAddress());
                    Log.i(TAG, "    RSSI: " + result.getRssi());

                    MyBeacon beacon = new MyBeacon();
                    ParseMyBeacon.parseBeacon(beacon, result);

                    if(beacon.isUIDAvailable() ||
                            beacon.isURLAvailable() ||
                            beacon.isTLMAvailable()) {

                        txtViewRssi.setText(beacon.getRssi() + " dBm");
                        txtViewDeviceAddress.setText(beacon.getDeviceAddress());
                    }

                    if(beacon.isUIDAvailable() == true) {

                        txtViewNamespaceId.setText(beacon.getNamespace_id());
                        txtViewInstanceId.setText(beacon.getInstance_id());

                        Log.i(TAG, "ID Namespace: " + beacon.getNamespace_id());
                        Log.i(TAG, "ID Instance: " + beacon.getInstance_id());
                        Log.i(TAG, "Reserved: " + beacon.getReserved());
                    }

                    if(beacon.isURLAvailable()) {
                        txtViewURL.setText(beacon.getUrl());
                        Log.i(TAG, "url is: " + beacon.getUrl());
                    }

                    if(beacon.isUIDAvailable() || beacon.isURLAvailable()) {
                        txtViewDistance.setText(String.format("%.2f m", beacon.getDistance()));
                        txtViewTxPowerAt0m.setText(String.format("%d dBm", beacon.getTxpwr()));
                        Log.i(TAG, "distance is " + beacon.getDistance());
                    }

                    if(beacon.isTLMAvailable()) {
                        Log.i(TAG, "temperature is " + beacon.getBeacon_temperature());
                        Log.i(TAG, "Voltage is " + beacon.getBattery_voltage());
                        float temperature = beacon.getBeacon_temperature();
                        String unit = "\u00b0" + "C";

                        String tempFormat = String.format("%.2f ", temperature);
                        txtViewTemperature.setText(tempFormat + unit);
                        txtViewBatteryVolmV.setText(String.format("%4.0f mV", beacon.getBattery_voltage()));
                    }

                    break;
                default:
                    Log.e(TAG, "Unknown message type received!");
                    Log.e(TAG, " message type is " + msg.what);
            }
        }
    };


    private void startScan() {

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        if (mBluetoothLeScanner == null)
        {
            Log.e(TAG, "Scanner is null! Check that Bluetooth is enabled or not.");
            Toast.makeText(this, "Scanner is null! Check that Bluetooth is enabled or not.", Toast.LENGTH_SHORT).show();
            finish();

        }
        mBluetoothLeScanner.startScan(null, settings, mScanCallback);
        Log.i(TAG, "Start to scan");
    }

    private void stopScan() {
        Log.i(TAG, "Stop to scan");
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: "+errorCode);
        }

        private void processResult(ScanResult result) {

            /*
             * Create a new beacon from the list of obtains AD structures
             * and pass it up to the main thread
             */
            //TemperatureBeacon beacon = new TemperatureBeacon(result.getScanRecord(),
            //        result.getDevice().getAddress(),
            //        result.getRssi());


            if(DEVICE_MAC.equalsIgnoreCase(result.getDevice().getAddress()))
                mHandler.sendMessage(Message.obtain(null, 1, result));
        }
    };

    // initialise Bluetooth stuff
    private void init() {
        // if the smart phone does not support BLE, then exit
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i(TAG, getString(R.string.ble_not_supported));
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else {
            Log.i(TAG, getString(R.string.ble_supported));
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

    }



}
