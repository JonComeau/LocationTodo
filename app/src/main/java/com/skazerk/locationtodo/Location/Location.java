package com.skazerk.locationtodo.Location;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.skazerk.locationtodo.R;
import com.skazerk.locationtodo.ui.ListActivity;
import com.skazerk.locationtodo.ui.Main;

import org.json.JSONException;
import org.json.JSONObject;

public class Location extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Location.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private CameraPosition cameraPosition;
    private android.location.Location lastKnownLocation;

    public static boolean mapIsTouched;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private TextView address, lat, lng;

    /**
     * sets up all needed values. The googleApiClient sets up the location services
     * for the map and Places for the search bar. The layout is also set up with the
     * "Search" onClick set with the Places search bar. the "save" button sends a
     * result back to the previous activity with information about the lat, long, and
     * address.
     *
     * @param savedInstanceState previous state of activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        } else {
            Log.d(TAG, "No saved data");
        }

        setContentView(R.layout.activity_location);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleApiClient.connect();

        Log.d(TAG, "1");

        address = (TextView) findViewById(R.id.address);
        lat = (TextView) findViewById(R.id.lat);
        lng = (TextView) findViewById(R.id.lng);

        Button search = (Button) findViewById(R.id.FindPlaceButton);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(Location.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        Button save = (Button) findViewById(R.id.location_save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ListActivity.class);
                JSONObject locationJSONObj = new JSONObject();

                try {
                    locationJSONObj.put("lat", lat.getText().toString());
                    locationJSONObj.put("long", lng.getText().toString());
                    locationJSONObj.put("address", address.getText());
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                intent.putExtra("JSON", locationJSONObj.toString());
                intent.putExtra(Main.EXTRA_LISTFILE, getIntent().getStringExtra(Main
                        .EXTRA_LISTFILE));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        Log.d(TAG, "2");
    }

    /**
     * Overrides the back button to send back a result_cancelled result.
     */
    public void onBackPressed() {
        Log.d(TAG, "Back button was pressed");
        cancelLocation();
    }

    /**
     * cancels the location request. puts together an intent for the previous activity to
     * use.
     */
    private void cancelLocation() {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra(Main.EXTRA_LISTFILE, getIntent().getStringExtra(Main
                .EXTRA_LISTFILE));
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    /**
     *  Handles the returned values place picker activity that pos up on the screen.
     *  From that data, the lat, long, and address field and set and the maps position
     *  is moved to view the address.
     *
     * @param requestCode The type of request set back to the activity
     * @param resultCode the state of the result
     * @param data the returned intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                address.setText(place.getName() + "\n" + place.getAddress());
                LatLng tmp = place.getLatLng();
                lat.setText(tmp.latitude + "");
                lng.setText(tmp.longitude + "");

                map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().target(tmp).zoom(19).build()
                        ));
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * saves the camera location across different iterations of this exact activity.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);

            Log.d("Location", "LastKnownLocation: " + lastKnownLocation.toString());
            Log.d("Location", "CameraPosition: " + map.getCameraPosition().toString());

            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Connects the map fragment to google play services if the phone is connected
     *
     * @param bundle bundled data for the function
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        MySupportMapFragment supportMapFragment = (MySupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    /**
     * Here because it's needed
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Here because it's needed
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Sets the map when everything is loaded. The location is updated and the
     * user's location is grabbed
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        updateLocationUI();

        getDeviceLocation();
    }

    /**
     * asks google location services for the gps coordinates. initially moves the camera to
     * reflect that
     */
    private void getDeviceLocation() {
        /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        if (locationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (cameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else if (lastKnownLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Asks the user if they want to grant location services to the app.
     * if yes, Then the users location can be stored, if not, the default is sydney, Aus
     *
     * @param requestCode type of request
     * @param permissions list of permissions asked for
     * @param grantResults the results of asking
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults){
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Check for permissions, then if granted, update settings in activity
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (locationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            lastKnownLocation = null;
        }
    }
}
