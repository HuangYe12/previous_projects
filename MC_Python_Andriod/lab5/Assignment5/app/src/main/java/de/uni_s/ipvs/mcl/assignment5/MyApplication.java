package de.uni_s.ipvs.mcl.assignment5;

import android.app.Application;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private DatabaseReference mRef;
    private DatabaseReference uuids;
    private DatabaseReference locations;


    private Task1Activity task1Activity;
    private Task2Activity task2Activity;

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private Random gen = new Random();

    public void initFirebase() {
        mRef = FirebaseDatabase.getInstance().getReference().child("teams").child("5");
//        mRef = FirebaseDatabase.getInstance().getReference(

        if (mRef == null) {
            Log.e(TAG, "Couldn't get Database Reference");
            Toast.makeText(this, "Couldn't get Database Reference", Toast.LENGTH_SHORT);
        }
        Toast.makeText(this, "Successfully connected to Firebase.", Toast.LENGTH_SHORT);

        uuids = mRef.child("uuids");
        locations = mRef.child("locations");

        //clear subtrees
//        uuids.removeValue();
//        locations.removeValue();

        uuids.addValueEventListener(uuidEventListener);
        locations.addValueEventListener(locationEventListener);
    }

    ValueEventListener uuidEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final String key = dataSnapshot.getKey();
            final HashMap<String, Object> uuidMap = (HashMap<String, Object>) dataSnapshot.getValue();
            if (uuidMap == null) return;
            Log.i(TAG, "Changed Key:" + key + " and value: " + uuidMap.toString());

            for (String uuidString : uuidMap.keySet()) {
                addUuid(uuidString);
                HashMap<String, Object> valueMap = (HashMap<String, Object>) uuidMap.get(uuidString);
                String value = getLatestValue(new ArrayList<>(valueMap.values()));

                final float temp = parseLocationValue(value);
                final String time = parseTimeValue(value);
                task1Activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        task1Activity.textViewTime.setText(time);
                        task1Activity.textViewTemperature.setText(String.valueOf(temp));
                    }
                });
                if (task2Activity != null && uuidString.equals(task2Activity.getSelectedUuid())) {
                    task2Activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            task2Activity.uuidTemp.setText("Temp of UUID: " + String.valueOf(temp));
                        }
                    });
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener locationEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final String key = dataSnapshot.getKey();
            final HashMap<String, Object> locationMap = (HashMap<String, Object>) dataSnapshot.getValue();
            if (locationMap == null) return;
            Log.i(TAG, "Changed Key:" + key + " and value: " + locationMap.toString());
            try {
                for (String locationKey : locationMap.keySet()) {
                    HashMap<String, Object> dateMap = (HashMap<String, Object>) locationMap.get(locationKey);
                    addLocation(locationKey);
                    if (task2Activity == null) break;

                    //Only calculate avg temperature of selected location.
                    if (!locationKey.equals(task2Activity.getSelectedLocation())) continue;
                    for (String dateKey : dateMap.keySet()) {
                        HashMap<String, Object> timestampMap = (HashMap<String, Object>) dateMap.get(dateKey);

                        //Calculate avg temperature of today.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = sdf.parse(dateKey);
                        if (!DateUtils.isToday(date.getTime())) continue;

                        final double avg = getAverage(timestampMap);

                        task2Activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                task2Activity.locAvgTemp.setText(String.format("Average Temp: %1$.4f", avg));
                            }
                        });

                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private double getAverage(HashMap<String, Object> timestampMap) {
        double sum = 0;
        long num = 0;
        for (String ts : timestampMap.keySet()) {
            HashMap<String, Object> valueMap = (HashMap<String, Object>) timestampMap.get(ts);
            for (Object value : valueMap.values()) {
                if (value instanceof Double) {
                    double temp = (Double) value;
                    sum += temp;
                } else if (value instanceof Long) {
                    long temp = (Long) value;
                    sum += temp;
                }
                num++;
            }
        }
        return sum / num;
    }

    private float parseLocationValue(String value) {
        String[] split = value.split(":");
        long ts = Long.valueOf(split[0]);
        float v = Float.valueOf((split[1]));

        return v;
    }

    private String parseTimeValue(String value) {
        String[] split = value.split(":");
        long ts = Long.valueOf(split[0]);
        float v = Float.valueOf((split[1]));

        Date resultdate = new Date(ts);
        return sdf.format(resultdate);
    }

    public void writeUuidDatabase(float value, String uuidString) {
        long ts = System.currentTimeMillis();
        String string = ts + ":" + value;
        uuids.child(uuidString).push().setValue(string);
    }

    public void writeLocationDatabase(float value) {
        long ts = System.currentTimeMillis();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(ts);

        locations.child("Stuttgart").child(date).child(String.valueOf(ts)).push().setValue(value);
    }

    public void setTask2Activity(Task2Activity task2Activity) {
        this.task2Activity = task2Activity;
    }

    public void setTask1Activity(Task1Activity task1Activity) {
        this.task1Activity = task1Activity;
    }

    public void addUuid(String uuid) {
        if (task2Activity != null) {
            List<String> uuidList = task2Activity.getUuidList();
            if (!uuidList.contains(uuid)) {
                uuidList.add(uuid);
            }
            task2Activity.setUuidList(uuidList);
        }

    }

    public void addLocation(String loc) {
        if (task2Activity != null) {
            List<String> locationList = task2Activity.getLocationsList();
            if (!locationList.contains(loc)) {
                locationList.add(loc);
            }
            task2Activity.setLocationsList(locationList);
        }
    }

    public String getLatestValue(List values) {
        List<String> v = values;
        Collections.sort(v, Collections.<String>reverseOrder());
        return v.get(0);
    }
}
