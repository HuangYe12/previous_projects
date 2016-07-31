package de.uni_s.ipvs.mcl.assignment5;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Task2Activity extends AppCompatActivity {
    private static final String TAG = "Task2Activity";

    TextView uuidTemp;
    TextView locAvgTemp;
    private Spinner uuidSpinner;
    private Spinner locationSpinner;

    private ArrayAdapter<String> uuidAdapter;
    private ArrayAdapter<String> locationAdapter;

    MyApplication app;

    private List<String> locationsList = new ArrayList<String>();
    private List<String> uuidList = new ArrayList<String>();

    private String selectedUuid;
    private String selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (MyApplication) getApplicationContext();

        uuidTemp = (TextView) findViewById(R.id.uuidTemp);
        locAvgTemp = (TextView) findViewById(R.id.locationAvgTemp);
        uuidSpinner = (Spinner) findViewById(R.id.uuidSpinner);
        locationSpinner = (Spinner) findViewById(R.id.locationSpinner);

        uuidAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, uuidList);
        locationAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locationsList);

        uuidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        uuidSpinner.setAdapter(uuidAdapter);
        locationSpinner.setAdapter(locationAdapter);

        uuidSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUuid = parent.getItemAtPosition(position).toString();
                Log.i(TAG, "Selected " + selectedUuid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = parent.getItemAtPosition(position).toString();
                Log.i(TAG, "Selected " + selectedLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        app.setTask2Activity(this);
    }

    public List<String> getUuidList() {
        return uuidList;
    }

    public void setUuidList(List<String> uuidList) {
        this.uuidList = uuidList;
        uuidAdapter.notifyDataSetChanged();
    }

    public List<String> getLocationsList() {
        return locationsList;
    }

    public void setLocationsList(List<String> locationsList) {
        this.locationsList = locationsList;
        locationAdapter.notifyDataSetChanged();
    }

    public String getSelectedUuid() {
        return selectedUuid;
    }

    public String getSelectedLocation() {
        return selectedLocation;
    }
}
