package com.skazerk.locationtodo.Location;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.skazerk.locationtodo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (SecurityException ex) {
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
                String locationFoundName = "";
                for (int i = 0; i < locations.size(); i++) {
                    getCurrentLocation();

                    if (!locationFound) {
                        float results[] = new float[1];
                        Location.distanceBetween(currentLocation.getLatitude(),
                                currentLocation.getLongitude(), locations.get(i).getLatitude(),
                                locations.get(i).getLongitude(), results);
                        if (results[0] <= 15.24) {
                            builder = new NotificationCompat.Builder(MyLocationService.this)
                                    .setSmallIcon(R.drawable.ic_location_notification)
                                    .setContentText("Here is your list!")
                                    .setContentTitle(locationNames.get(i));

                            locationFoundName = locationNames.get(i);

                            int notificationId = 1;

                            NotificationManager notifyMgr =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            notifyMgr.notify(notificationId, builder.build());

                            locationFound = true;
                        }
                    } else {
                        int index = locationNames.indexOf(locationFoundName);
                        float results[] = new float[1];
                        Location.distanceBetween(currentLocation.getLatitude(),
                                currentLocation.getLongitude(), locations.get(index).getLatitude(),
                                locations.get(index).getLongitude(), results);

                        if (results[0] > 15.24) {
                            locationFound = false;
                        }
                    }
                }

                try {
                    Thread.currentThread();
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

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
