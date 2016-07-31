package test.hy.mclabapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class FanControlActivity extends AppCompatActivity {
    private final static String TAG = FanControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private SeekBar mSeekBar;
    private TextView mTextView;
    private boolean fanEnable = false;

    private BluetoothGattCharacteristic fanCharacteristic;
    private BluetoothGattService fanService;

    private static final UUID FAN_SERVICE_UUID =
            UUID.fromString("00000001-0000-0000-FDFD-FDFDFDFDFDFD");
    private static final UUID FAN_CHARACTERISTICS_UUID =
            UUID.fromString("10000001-0000-0000-FDFD-FDFDFDFDFDFD");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_control);

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

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.i(TAG, mDeviceAddress);

        mTextView = (TextView)findViewById(R.id.fanStatus);
        mSeekBar = (SeekBar) findViewById(R.id.fanSeekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "Progress changed!");
                if(mBluetoothGatt==null){
                    Toast.makeText(FanControlActivity.this,"You haven't connected to the service.",Toast.LENGTH_LONG).show();
                    return;
                }
                if (fanEnable){
                    Toast.makeText(FanControlActivity.this,"set value to "+progress,Toast.LENGTH_SHORT).show();
                    writeLightControl(mBluetoothGatt, progress);
                }
                else
                    Toast.makeText(FanControlActivity.this,"Please wait.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        connect(mDeviceAddress);


    }

    private void writeLightControl(BluetoothGatt gatt, int value){
        fanEnable = false;
        Log.i(TAG, "Try to write fan value: "+value);
        boolean status;
        Log.i(TAG,"Fan uuid: "+ fanCharacteristic.getUuid().toString());
        fanCharacteristic.setValue(value,BluetoothGattCharacteristic.FORMAT_UINT16,0);
        status = gatt.writeCharacteristic(fanCharacteristic);

        if(status)
            Log.i(TAG, "Success!");
        else
            Log.i(TAG, "Failed...");
        fanEnable = true;
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
                fanService = gatt.getService(FAN_SERVICE_UUID);
                fanCharacteristic = fanService.getCharacteristic(FAN_CHARACTERISTICS_UUID);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setText(R.string.fan_connected);
                    }
                });
                fanEnable = true;
                //writeLightControl(gatt,0);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.w(TAG, "onCharacteristicWrite success. " );
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };



}
