package de.uni_stuttgart.ipvs.mclab.team05.lab3_task2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "lab3_task2_team05";


    private boolean isBound = false;
    IMyGps myGPS = null;

    private TextView txtViewLongitude;
    private TextView txtViewLatitude;
    private TextView txtViewDistance;
    private TextView txtViewAverageSpeed;

    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtViewLongitude = (TextView) findViewById(R.id.longitude);
        txtViewLatitude = (TextView) findViewById(R.id.latitude);
        txtViewDistance = (TextView) findViewById(R.id.distance);
        txtViewAverageSpeed = (TextView) findViewById(R.id.averageSpeed);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            Log.i(TAG, "Try to get gps permission in activity");
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                        MY_PERMISSION_ACCESS_COARSE_LOCATION );
            Log.i(TAG, "Get gps permission successfully");
        }

        if (!isExternalStorageWritable()){
            Log.w(TAG, "SD Card is not writable");
            Toast.makeText(MainActivity.this,"SD Card is not writable",Toast.LENGTH_LONG).show();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isBound) {
            getApplicationContext().unbindService(myConnection);
            isBound = false;
            myGPS = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onBtnStartService(View v) {
        Button btn = (Button)v;
        Log.i(TAG, btn.getText() + " is clicked");

        startService(new Intent(this, MyService.class));
    }

    public void onBtnStopService(View v) {
        Button btn = (Button)v;
        Log.i(TAG, btn.getText() + " is clicked");

        if (isBound) {
            getApplicationContext().unbindService(myConnection);
            isBound = false;
        }

        stopService(new Intent(this, MyService.class));
        myGPS = null;
    }

    public void onBtnUpdateValues(View v) {
        Button btn = (Button)v;
        Log.i(TAG, btn.getText() + " is clicked");

        // bind service
        Intent intent = new Intent(this, MyService.class);
        if (isBound == false)
            isBound = getApplicationContext().bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        else if (myGPS != null) {
            // invoke RPC
            mHandler.sendMessage(Message.obtain(null, 1, myGPS));
        } else {
            Log.i(TAG, "myGps is null! Bind again!");
            isBound = getApplicationContext().bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        }

    }

    public void onBtnExit(View v) {
        Button btn = (Button)v;
        Log.i(TAG, btn.getText() + " is clicked");
        finish();
    }

    public ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(TAG, "service connected");
            myGPS = (IMyGps) binder;

            mHandler.sendMessage(Message.obtain(null, 1, myGPS));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection","disconnected");
            isBound = false;
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.i(TAG, "Received message!");
                    IMyGps myGps = (IMyGps) msg.obj;
                    try {
                        double latitude =  myGps.getLatitude();
                        double longitude = myGps.getLongitude();
                        double distance = myGps.getDistance();
                        double averageSpeed = myGps.getAverageSpeed();
                        String unit = "\u00b0";
                        txtViewLatitude.setText(String.format("%.6f", latitude) + unit);
                        txtViewLongitude.setText(String.format("%.6f", longitude) + unit);
                        txtViewDistance.setText(String.format("%.4f m", distance));
                        txtViewAverageSpeed.setText(String.format("%.4f m/s", averageSpeed));

                    } catch (RemoteException ex) {
                        Log.i(TAG, "Remote exception happend!");
                        Log.e(TAG, ex.getMessage());
                    }
                    break;
                default:
                    Log.e(TAG, "unkown type message received!");
                    break;

            }
        }
    };

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


}
