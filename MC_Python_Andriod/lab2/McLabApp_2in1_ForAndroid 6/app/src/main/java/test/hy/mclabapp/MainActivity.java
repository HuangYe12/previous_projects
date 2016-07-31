package test.hy.mclabapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList <BluetoothDevice> mDevices; // save the scanned results
    private final static int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private boolean mScanning = false;

    private TextView mTextView;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    ListView list;
    LeDeviceListAdapter mAdapter;

    private static final String WEATHER_DEVICE_NAME = "IPVSWeather";
    private static final String FAN_DEVICE_NAME = "IPVS-LIGHT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "running onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mDevices = new ArrayList<>();
        mTextView = (TextView) findViewById(R.id.textView);
        mHandler = new Handler();

        list = (ListView) findViewById(R.id.listView);
        mAdapter = new LeDeviceListAdapter();

        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mAdapter.getDevice(position);

                if (device == null) {
                    Log.w(TAG, "No device found. From onItemClick()");
                    return;
                }
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                    invalidateOptionsMenu();
                }
                Log.i(TAG, "You Click item " + String.valueOf(position) + " And name is "+ device.getName());
                if (WEATHER_DEVICE_NAME.equals(device.getName())){
                    Intent intentWeather = new Intent(MainActivity.this,WeatherActivity.class);
                    intentWeather.putExtra(WeatherActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    startActivity(intentWeather);
                }
                else if (FAN_DEVICE_NAME.equals(device.getName())){
                    Intent intentFan = new Intent(MainActivity.this,FanControlActivity.class);
                    intentFan.putExtra(FanControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    startActivity(intentFan);
                }



            }
        });

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            //Android M permission Check
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan BLE");
                builder.setPositiveButton(R.string.ok_button,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        Log.i(TAG, "Finish onCreate()");
    }


    @Override
    protected void onResume() {
        Log.i(TAG, "running onResume()");
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        mTextView.setText(R.string.ready_text);

        Log.i(TAG, "Finish onResume()");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "running onPause()");
        super.onPause();
        scanLeDevice(false);
        mDevices.clear();
        Log.i(TAG, "Finish onPause()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "running onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (mScanning)
            menu.findItem(R.id.action_scan).setVisible(false);
        else menu.findItem(R.id.action_scan).setVisible(true);
        Log.i(TAG, "Finish onCreateOptionsMenu()");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "running onOptionsItemSelected()");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            scanLeDevice(true);
            Log.i(TAG, "Finish onOptionsItemSelected()");
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    //Scan function.
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mDevices.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                    mTextView.setText(R.string.ready_text);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mTextView.setText(R.string.scan_text);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mTextView.setText(R.string.ready_text);
        }
        invalidateOptionsMenu();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Device detected!");
                            Log.i(TAG, "device name is " + device.getName());
                            Log.i(TAG, "device address is " + device.getAddress());
                            if (!mDevices.contains(device)) {
                                Log.i(TAG, "Device added!");
                                mDevices.add(device);
                                addListItem(mDevices);
                            }
                        }
                    });
                }
            };

    private void addListItem(final ArrayList<BluetoothDevice> mDevices){
        Log.i(TAG, "addListItem running!");
        for(int i=0;i<mDevices.size();i++)
            mAdapter.addDevice(mDevices.get(i));
        mAdapter.notifyDataSetChanged();
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.ItemAddress);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.ItemName);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
