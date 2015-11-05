package com.sfsu.utils.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sfsu.entities.AppConfig;

// TODO: implement the LocationService to get LocationUpdates using FusedLocation and also set the BroadcastReceiver to send
// the data to Activity once the service gets the Location
public class LocationService extends Service implements LocationListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String BROADCAST_ACTION = "com.droid.slate.locationMaster";

    // Location updates intervals in sec
    private static int _SECONDS = 60 * 1000;
    private static int UPDATE_INTERVAL = 5 * _SECONDS; // 5 mins
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // Handler communicate between this Thread and UI thread.
    private final Handler handler = new Handler();

    // getting the LocationObject.
    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    // getting the Location requests
    private LocationRequest mLocationRequest;
    private Intent intent;
    private int counter = 0;
    /**
     *
     */
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            broadcastLocationInfo();
            handler.postDelayed(this, 5000); // 5 seconds
        }
    };

    // Deafault Constructor
    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*initialize the Params in the onCreate of the Service. Since building GoogleApiClient and creating LocationRequests
        needs to be done only once,
         */
        intent = new Intent(BROADCAST_ACTION);
        // check availability of play services
        if (isGooglePlayServicesAvailable()) {
            Log.i(AppConfig.LOGTAG, "play services available and so initializing the api, location requests.");
            // Building the GoogleApi client
            buildGoogleApiClient();
            // initialize the LocationRequest
            createLocationRequest();
            // finally onConnect, handle the LocationUpdates
            mGoogleApiClient.connect();
        }
    }

    /**
     * util method to check if GooglePlayServicesAvailable or not.
     *
     * @return
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.i(AppConfig.LOGTAG, " Error in resultCode");
            } else {
                Log.i(AppConfig.LOGTAG, " This device is not supported");
            }
            return false;
        }
        return true;
    }

    /**
     * start the Fused location updates
     */
    protected void startLocationUpdates() {
        Log.i(AppConfig.LOGTAG, "starting location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * stop the Fused location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    /**
     * Build the GoogleApi client.
     */
    public synchronized void buildGoogleApiClient() {
        Log.i(AppConfig.LOGTAG, "Building the Google API Client.");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * create the LocationRequest object by specifying all the params needed to initialize the object.
     */
    public void createLocationRequest() {
        Log.i(AppConfig.LOGTAG, "Creating Location Requests.");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(AppConfig.LOGTAG, "onStart called");
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second delay
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Methods to handle the LocationListener for the
    @Override
    public void onConnected(Bundle bundle) {
        // Once connected with google api, get the location
        Log.i(AppConfig.LOGTAG, "onConnected called");
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    // Callback for ConnectionCallbacks interface
    @Override
    public void onConnectionSuspended(int i) {

    }

    // Callbacks for LocationListener interface.
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        Log.i(AppConfig.LOGTAG, "Location changed: " + mLastLocation.getLatitude() + " : " + mLastLocation.getLongitude());
        // Displaying the new location on UI
        // TODO: notify to the BroadcastReceiver
    }

    // call back for OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }


    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    /**
     * send the location broadcast to the Activity
     */
    private void broadcastLocationInfo() {
        Log.i(AppConfig.LOGTAG, "entered broadcast logging info method");
        if (intent != null) {
            intent.putExtra("locINFO", mLastLocation);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(AppConfig.LOGTAG, "onDestroy");
        handler.removeCallbacks(sendUpdatesToUI);
    }
}
