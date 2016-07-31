package test.hy.mclabapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import java.util.UUID;

public class WeatherActivity extends AppCompatActivity {
    private final static String TAG = WeatherActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private final static String NEWLINE = System.getProperty("line.separator");
    private TextView mTextView;
    StringBuilder stringBuilder = new StringBuilder();
    private boolean notifyEnable = false;

    private static final UUID WEATHER_SERVICE_UUID =
            UUID.fromString("00000002-0000-0000-FDFD-FDFDFDFDFDFD");
    private static final UUID TEMPERATURE_SENSOR_UUID =
            UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
    private static final UUID TEMPERATURE_SENSOR_CONFIG_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID HUMIDITY_SENSOR_UUID =
            UUID.fromString("00002a6f-0000-1000-8000-00805f9b34fb");
    private static final UUID HUMIDITY_SENSOR_CONFI_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    BluetoothGattService weatherService;
    BluetoothGattCharacteristic temperatureCharacteristic,humidityCharacteristic;
    BluetoothGattDescriptor tDescriptor, hDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "BluetoothAdapter initialize failed.");
            return;
        }

        mTextView = (TextView) findViewById(R.id.weatherTextView);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.i(TAG, mDeviceAddress);
        connect(mDeviceAddress);


    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }


    @Override
    protected void onStop() {
        super.onStop();
        close();
    }

    public boolean connect(final String address) {
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        Log.i(TAG, "Now connecting Gatt connection");
        mBluetoothGatt = device.connectGatt(this,false,mGattCallback);
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.i(TAG, "Now disconnect Gatt connection");
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.i(TAG, "Now close Gatt connection");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private void parseTemperatureValue(BluetoothGattCharacteristic characteristic) {
        byte[] sensorValue = characteristic.getValue();
        byte flag = sensorValue[0];
        String unit = "\u00b0";
        String mString;
        if (flag%2 == 0) {
            // unit = "\u2103"; // celsius
            unit = unit + "C";
        } else {
            // unit = "\u2109"; // fahrenheit;
            unit = unit + "F";
        }
        Float value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
        Log.i(TAG, "Temperature is " + value.toString() + unit);
        mString = "Temperature is " + value.toString() + unit + NEWLINE;
        stringBuilder.append(mString);

    }

    private void parseHumidityValue(BluetoothGattCharacteristic characteristic) {
        String mString;
        Integer value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        Log.i(TAG, "Humidity is " + value.toString() + "%");
        mString = "Humidity is " + value.toString() + "%" + NEWLINE;
        stringBuilder.append(mString) ;
    }



    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered success. " );
                //displayGattServices(gatt.getDevice().getName() ,gatt.getServices());
                weatherService = gatt.getService(WEATHER_SERVICE_UUID);
                temperatureCharacteristic = weatherService.getCharacteristic(TEMPERATURE_SENSOR_UUID);
                humidityCharacteristic = weatherService.getCharacteristic(HUMIDITY_SENSOR_UUID);
                readWeatherData(gatt);
                Log.i(TAG, "Try to read temperature sensor");

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (characteristic.getUuid().equals(TEMPERATURE_SENSOR_UUID)) {
                parseTemperatureValue(characteristic);
                Log.i(TAG, "Try to read humidity sensor");
                gatt.readCharacteristic(humidityCharacteristic);
            }
            else if (characteristic.getUuid().equals(HUMIDITY_SENSOR_UUID)) {
                parseHumidityValue(characteristic);
                if (!notifyEnable){
                    Log.i(TAG, "Try to enable notification for humidity sensor");
                    notifyEnable = true;
                    gatt.setCharacteristicNotification(characteristic, true);
                    hDescriptor = characteristic.getDescriptor(HUMIDITY_SENSOR_CONFI_UUID);
                    hDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(hDescriptor);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText(stringBuilder.toString());
                    }
                });
            }

            Log.w(TAG, "onCharacteristicRead success. " );
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getUuid().equals(HUMIDITY_SENSOR_UUID)) {
                Log.i(TAG, "Receive humidity notification!");
                readWeatherData(gatt);
            }
            else {
                Log.i(TAG, "Unknown notification received!");
            }
        }
    };

    private void readWeatherData(BluetoothGatt gatt){
        stringBuilder.delete(0,stringBuilder.length());
        gatt.readCharacteristic(temperatureCharacteristic);
    }

    /*
    private void displayGattServices(String deviceName, List<BluetoothGattService> gattServices) {
        Log.i(TAG, "Device " + deviceName + " supports the following services:");
        if (gattServices == null) {
            Log.i(TAG, "Services is null !!!");
            return;
        }

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();

            Log.i(TAG, "\tService uuid is " + uuid);
            // Loops through available Characteristics.
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String uuidChara = gattCharacteristic.getUuid().toString();
                Log.i(TAG, "\t\tCharacteristic uuid is " + uuidChara);

            }
        }
    }*/

}
