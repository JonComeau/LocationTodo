package com.skazerk.locationtodo.Location;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.skazerk.locationtodo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Skaze on 6/14/17.
 * Uses code from https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
 */

public class MyLocationService extends Service {
    private static final String TAG = "MyLocationService";
    private String locationJson;
    NotificationCompat.Builder builder;
    private JSONObject fullLocation;
    private Location currentLocation;

    private List<Location> locations;
    private List<String> locationNames;

    private LocationManager locationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private IBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        locations = new ArrayList<>();
        locationNames = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        locationJson = intent.getStringExtra("JSON");

        try {
            fullLocation = new JSONObject(locationJson);

            JSONArray locationArray = fullLocation.getJSONArray("location");

            Log.d(TAG, String.valueOf(locationArray.length()));

            for (int i = 0; i < locationArray.length() - 1; i++) {
                JSONObject locationObj = locationArray.getJSONObject(i);
                Location tmpLocation = new Location(LocationManager.GPS_PROVIDER);

                tmpLocation.setLatitude(Float.parseFloat(locationObj.getString("lat")));
                tmpLocation.setLongitude(Float.parseFloat(locationObj.getString("long")));

                locations.add(tmpLocation);

                locationNames.add(locationObj.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean locationFound = false;
                getCurrentLocation();

                builder = new NotificationCompat.Builder(MyLocationService.this)
                        .setSmallIcon(R.drawable.ic_location_notification)
                        .setContentText("Lat: " + locations.get(0).getLatitude()
                                + ", Long: " + locations.get(0).getLongitude())
                        .setContentTitle(locationNames.get(0));

                int notificationId = 001;

                NotificationManager notifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notifyMgr.notify(notificationId, builder.build());
            }
        };

        Thread locationThread = new Thread(runnable);
        locationThread.start();

        return START_STICKY;
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] locationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context
                    .LOCATION_SERVICE);
        }
    }

    public class MyBinder extends Binder {
        public MyLocationService getService() {
            return MyLocationService.this;
        }
    }
}
