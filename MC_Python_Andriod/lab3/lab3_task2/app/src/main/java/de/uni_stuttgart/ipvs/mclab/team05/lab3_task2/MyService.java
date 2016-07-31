package de.uni_stuttgart.ipvs.mclab.team05.lab3_task2;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// refer to https://github.com/codepath/android_guides/wiki/Managing-Threads-and-Custom-Services
public class MyService extends Service {
    public static final String TAG = "lab3_task2_team05";

    // The minimum distance to change Updates in meters
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second

    private final File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    private File myFile = new File(sdDir,"Trace_log.gpx");

    private ArrayList<Location> locations;

    LocationManager locationManager;
    LocationListener locationListener;

    Location firstLocation;

    public MyService() {
    }


    public final IMyGps.Stub mBinder = new IMyGps.Stub() {
        @Override
        public double getLatitude() throws RemoteException {
            Log.i(TAG, "getLatitude called!");

            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location.getLatitude();
            } catch (SecurityException e) {
                Log.e(TAG, "permission for gps is not granted!.");
            }
            return -1;
        }

        @Override
        public double getLongitude() throws RemoteException {
            Log.i(TAG, "getLongitude called!");

            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location.getLongitude();
            } catch (SecurityException e) {
                Log.e(TAG, "permission for gps is not granted!.");
            }

            return -1;
        }

        @Override
        public double getDistance() throws RemoteException {
            Log.i(TAG, "getDistance called!");

            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location.distanceTo(firstLocation);
            } catch (SecurityException e) {
                Log.e(TAG, "permission for gps is not granted!.");
            }

            return -1;
        }

        @Override
        public double getAverageSpeed() throws RemoteException {
            Log.i(TAG, "getAverageSpeed called!");
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double dist = location.distanceTo(firstLocation);
                double startTime = firstLocation.getTime();//TODO Change to the service start time
                double endTime = location.getTime();
                double timeSpan = (endTime - startTime)/1000; // convert to second
                if (timeSpan != 0)
                    return dist / timeSpan;
                else
                    return -1;
            } catch (SecurityException e) {
                Log.e(TAG, "permission for gps is not granted!.");
            }

            return -1;
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Location location = (Location) msg.obj;
                    Log.i(TAG, "received location update");

                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();

                    Log.i(TAG, "Longitude is " + longitude);
                    Log.i(TAG, "Latitude is " + latitude);

                    break;
                default:
                    Log.i(TAG, "unknown type message received");
            }
        }
    };

    // Fires when a service is first initialized
    public void onCreate() {
        super.onCreate();
        if (!sdDir.exists()){
            sdDir.mkdir();
        }
    }

    // Fires when a service is started up
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started!");
        createNewFile();
        locations = new ArrayList<>();
        if (locationListener == null) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    mHandler.sendMessage(Message.obtain(null, 1, location));
                    locations.add(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

                firstLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //firstLocation.setTime(System.currentTimeMillis());
                locations.add(firstLocation);

                Log.i(TAG, "Ready for receiving GPS update");
            } else {
                Log.i(TAG, "Permission for gps disabled!");
            }

                /*if(false) {
                    try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Log.i(TAG, "Ready for receiving GPS update");
                }
                catch (SecurityException e) {
                    Log.e(TAG, "Permission error for getting GPS!");
                    dialogGPS();
                }
                }*/
        }

        return START_STICKY;
    }

    // Defines the shutdown sequence
    @Override
    public void onDestroy() {
        if(locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
                Log.i(TAG, "Removed GPS update listener.");
                locationListener = null;
            } catch (SecurityException e) {
                Log.e(TAG, "Error for removing GPS update listener.");
            }
        }
        writePath(myFile,locations);
        Log.i(TAG, "Service stopped!");
    }

    // Binding is another way to communicate between service and activity
    // Not needed here, local broadcasts will be used instead
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");
        return mBinder;
    }

    /**
     * Function to show settings alert dialog
     * */
    public void dialogGPS(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getApplicationContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void createNewFile(){
        myFile.setWritable(true);
        Log.i(TAG, "Log File Path is "+ myFile.getAbsolutePath());
        if (myFile.exists()){
            myFile.delete();
            Log.i(TAG, "Deleting old log file");
        }

        try{
            myFile.createNewFile();
        }catch (IOException e) {
            Log.e(TAG, "Error Writting Path",e);
        }
    }

    private void writePath(File file, List<Location> points) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>MCLabTestLog</name><trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        for (Location location : points) {
            segments += "<trkpt lat=\"" + location.getLatitude()
                    + "\" lon=\"" + location.getLongitude() + "\"><time>"
                    + df.format(new Date(location.getTime())) + "</time></trkpt>\n";
        }
        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            //if (Bitmap.Config.isDEBUG())
            Log.i(TAG, "Saved " + points.size() + " points.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "Error Writting Path",e);
        }
    }
}
