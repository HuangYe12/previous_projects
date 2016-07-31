package de.uni_s.ipvs.mcl.assignment5;

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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Task1Activity extends AppCompatActivity {
    private static final String TAG = "Task1Activity";

    private static final UUID WEATHER_SERVICE_UUID = UUID.fromString("00000002-0000-0000-fdfd-fdfdfdfdfdfd");
    private static final UUID TEMPERATURE_SENSOR_UUID = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
    private static final UUID TEMPERATURE_SENSOR_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final int MSG_TEMPERATURE = 0;

    private Context context;
    private MyApplication app;

    // flags indicating  which sensor notification service should be enabled
    private enum enumNotification {
        ENABLE_TEMPERATURE_NOTIFICATION,
        ENABLE_UNDEFINED,
    }

    private enumNotification stateNotification = enumNotification.ENABLE_TEMPERATURE_NOTIFICATION;

    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 3000;

    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices; // save the scanned results
    private BluetoothGatt mBluetoothGatt;

    private Map<Integer, String> mapBLEConnectionStates;


    TextView textViewTemperature;
    TextView textViewTime;
    Button task2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // if the smart phone does not support BLE, then exit
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i(TAG, getString(R.string.ble_not_supported));
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.i(TAG, getString(R.string.ble_supported));
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        mapBLEConnectionStates = new HashMap<Integer, String>();
        mapBLEConnectionStates.put(BluetoothProfile.STATE_CONNECTED, "Connected");
        mapBLEConnectionStates.put(BluetoothProfile.STATE_CONNECTING, "Connecting");
        mapBLEConnectionStates.put(BluetoothProfile.STATE_DISCONNECTED, "Disconnected");
        mapBLEConnectionStates.put(BluetoothProfile.STATE_DISCONNECTING, "Disconnecting");


        textViewTemperature = (TextView) findViewById(R.id.temperature);
        textViewTime = (TextView) findViewById(R.id.timeView);
        task2Button = (Button) findViewById(R.id.button);
        task2Button.setOnClickListener(buttonListener);


        app = (MyApplication) this.getApplicationContext();
        app.setTask1Activity(this);
        app.initFirebase();
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Task1Activity.this, Task2Activity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        context = this;

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        // scan device manually
        // scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toast.makeText(context, "Stopped scanning!", Toast.LENGTH_SHORT);
                    Log.i(TAG, "Now stop LE Scan");
                }
            }, SCAN_PERIOD);

            Toast.makeText(context, "Started scanning for devices!", Toast.LENGTH_SHORT);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Device detected!");
                            Log.i(TAG, "    device name is " + device.getName());
//                            Log.i(TAG, "    device name is" + device.getUuids().toString());
                            Log.i(TAG, "    device address is " + device.getAddress());

                            Log.i(TAG, "    Add it to device list");

                            if (mDevices.get(device.hashCode()) == null) {
                                mDevices.put(device.hashCode(), device);
                                Toast.makeText(context, getResources().getString(R.string.found_new_device) + " " + device.getName(), Toast.LENGTH_SHORT).show();
                                invalidateOptionsMenu();
                            }
                        }
                    });
                }
            };


    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    String stateDesc = mapBLEConnectionStates.get(newState);
                    if (null == stateDesc) stateDesc = "Unknown";
                    Log.i(TAG, "Connection state to GATT server changes to " + stateDesc);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            // Continue to service discovery
                            Log.i(TAG, "Do service discoveries");
                            gatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.i(TAG, "Disconnected to GATT sever");
                        }

                    } else {
                        Log.i(TAG, "Connection to GATT server failed!");
                        gatt.disconnect();
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "Service discoveries finished successfully");
                        displayGattServices(gatt.getDevice().getName(), gatt.getServices());

                        BluetoothGattCharacteristic characteristic;
                        BluetoothGattDescriptor descriptor;

                        BluetoothGattService service = gatt.getService(WEATHER_SERVICE_UUID);
                        List<BluetoothGattService> services = gatt.getServices();

                        if (services.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Couldn't find Service.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.e(TAG, "Couldn't find Service.");

                            return;
                        }
                        characteristic = service.getCharacteristic(TEMPERATURE_SENSOR_UUID);
//                        boolean success = gatt.readCharacteristic(characteristic);

                        if (characteristic == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Couldn't find Characteristic of Service.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.e(TAG, "Couldn't find Characteristic of Service.");
                            return;
                        }

                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor = characteristic.getDescriptor(TEMPERATURE_SENSOR_CONFIG_UUID);

                        Log.i(TAG, "Try to enable notification for temperature sensor");
                        stateNotification = enumNotification.ENABLE_TEMPERATURE_NOTIFICATION;
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        Toast.makeText(context, "Successfully connected to " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                        // there should be no code after gatt.writeDescriptor!

                    } else {
                        Log.i(TAG, "Service discoveries failed!");
                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor des, int status) {
                    super.onDescriptorWrite(gatt, des, status);
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    Log.i(TAG, "OnCharacteristicRead: uuid = " + characteristic.getUuid().toString());
                    if (characteristic.getUuid().equals(TEMPERATURE_SENSOR_UUID)) {
                        Log.i(TAG, "Get value from temperature sensor...");
                        if (characteristic.getValue() == null) {
                            Log.i(TAG, "Temperature sensor returns NULL!!1");
                            return;
                        }

                        byte[] sensorValue = characteristic.getValue();
                        Log.i(TAG, "Length of the value is " + sensorValue.length);
                        byte flag = sensorValue[0];
                        Log.i(TAG, "Flag = " + Byte.toString(flag));
                        Float value = parseTemperatureValue(characteristic);
                        mHandler.sendMessage(Message.obtain(null, MSG_TEMPERATURE, value));
//                        textViewTemperature.setText(value.toString());
                    } else {
                        Log.i(TAG, "Unknown characteristic read uuid received : " + characteristic.getUuid().toString());

                    }
                }

                @Override
                // Characteristic notification
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    if (characteristic.getUuid().equals(TEMPERATURE_SENSOR_UUID)) {
                        Log.i(TAG, "Receive temperature notification!");
                        Float value = parseTemperatureValue(characteristic);
                        mHandler.sendMessage(Message.obtain(null, MSG_TEMPERATURE, value));
                    } else {
                        Log.i(TAG, "Unknown notification received!");
                        Log.i(TAG, "UUID is " + characteristic.getUuid().toString());
                    }

                }

                private void readTemperatureSensor(BluetoothGatt gatt) {
                    Log.i(TAG, "Try to read temperature sensor");

                    BluetoothGattCharacteristic characteristic;
                    characteristic = gatt.getService(WEATHER_SERVICE_UUID)
                            .getCharacteristic(TEMPERATURE_SENSOR_UUID);
                    gatt.readCharacteristic(characteristic);
                }

                private Float parseTemperatureValue(BluetoothGattCharacteristic characteristic) {
                    byte[] sensorValue = characteristic.getValue();
                    byte flag = sensorValue[0];
                    String unit = "\u00b0";

                    if (flag % 2 == 0) {
                        // unit = "\u2103"; // celsius
                        unit = unit + "C";

                    } else {
                        // unit = "\u2109"; // fahrenheit;
                        unit = unit + "F";
                    }

                    Float value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1);
                    if (value == null) {
                        Log.e(TAG, "No value could be read!");
                        return -1.0f;
                    }
                    Log.i(TAG, "Temperature is " + value.toString() + unit);
                    return value;

                }
            };

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

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TEMPERATURE:
                    Float value = (Float) msg.obj;
//                    textViewTemperature.setText(value.toString());
                    Log.i(TAG, "Received message: temperature is " + value.toString());

                    app.writeUuidDatabase(value, TEMPERATURE_SENSOR_UUID.toString());
                    app.writeLocationDatabase(value);

                    break;
//                case MSG_HUMIDITY:
//                    Integer value2 = (Integer) msg.obj;
//                    textViewHumidity.setText(value2.toString());
//                    Log.i(TAG, "Received message: humidity is " + value2.toString());
//                    break;
                default:
                    Log.e(TAG, "Unknown message type received!");
                    Log.e(TAG, " message type is " + msg.what);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        for (int i = 0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            Log.i(TAG, "Start to scan!");
            mDevices.clear();
            invalidateOptionsMenu();
            scanLeDevice(true);
            return true;
        } else {
            BluetoothDevice device = mDevices.get(item.getItemId());
            Log.i(TAG, "Try to connect to " + device.getName());

            // false --- do not connect automatically
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

            return super.onOptionsItemSelected(item);
        }
    }
}
