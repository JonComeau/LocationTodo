package com.skazerk.locationtodo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.skazerk.locationtodo.Location.Location;
import com.skazerk.locationtodo.Location.MyLocationService;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {
    private LatLng broulims;
    private LatLng home;
    private List<String> locationJson;
    private String json;
    MyLocationService boundService;
    boolean serviceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        home = new LatLng(43.814753, -111.794798);
        broulims = new LatLng( 43.827312, -111.787406);

        locationJson = new ArrayList<String>();

        locationJson.add("{" +
                "\"name\": \"Broulim's\", " +
                "\"lat\": \"43.827312\", " +
                "\"long\": \"-111.787406\"" +
                "}");

        locationJson.add("{" +
                "\"name\": \"Home\", " +
                "\"lat\": \"43.814753\", " +
                "\"long\": \"-111.794798\"" +
                "}");

        json = "{ 'location': [";

        for (int i = 0; i < locationJson.size(); i++) {
            json += locationJson.get(i) + ", ";
        }

        json += "] }";

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Location.class);
                startActivity(intent);
            }
        });
    }

    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyLocationService.class);
        intent.putExtra("JSON", json);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder myBinder = (MyLocationService.MyBinder) service;
            boundService = myBinder.getService();
            serviceBound = true;
        }
    };
}
