package com.example.qatestingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class BarometerTesting extends AppCompatActivity {

    private TextView txt, underground;
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private int published = 0;
    Button back;
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            float pressure = (float) (values[0] * 0.0009869233);
            txt.setText( String.format("%.2f", pressure)+ " atm");
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://hellomotumdemo-default-rtdb.firebaseio.com/");
            boolean isUnderground = false;
            if(pressure > 1.01f){
                isUnderground = true;
            }
            String undergrnd = isUnderground == true ? "You are underground" : "You are at the surface";
            underground.setText(undergrnd);

            if(published == 0) {
                databaseRef.child("atms").child(Calendar.getInstance().getTime().toString()).child("Pressure").setValue(String.format("%.2f", pressure));
                databaseRef.child("atms").child(Calendar.getInstance().getTime().toString()).child("isUnderground").setValue(isUnderground);
                published = 1;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barometer_testing);
        FirebaseApp.initializeApp(this); // Initialize Firebase

        txt = findViewById(R.id.txt);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        underground = findViewById(R.id.underground);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BarometerTesting.this, MainActivity.class));
                finish();
            }
        });

        //        if (pressureSensor == null) {
//            Toast.makeText(this, "This device does not have a pressure sensor", Toast.LENGTH_SHORT).show();
//        } else {
        // Check if permission is granted
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED)){
            // Permission is not granted, ask for it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            // Permission is already granted, register listener
            registerPressureSensorListener();
        }
        //}
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, register listener
                registerPressureSensorListener();
            } else {
                // Permission is not granted
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerPressureSensorListener() {
        sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }
}