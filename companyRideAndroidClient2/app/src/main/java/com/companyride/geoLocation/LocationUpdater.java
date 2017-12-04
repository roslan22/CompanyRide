package com.companyride.geoLocation;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.companyride.interfaces.CurrentLocationInterface;
import com.companyride.observer.Observer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Ruslan on 04-Oct-15.
 */
public class LocationUpdater implements Observer, com.google.android.gms.location.LocationListener {
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private CurrentLocationInterface mContext;

    public Location getCurrentLocation() {
        return CurrentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        CurrentLocation = currentLocation;
    }

    public Location CurrentLocation;


    public LocationUpdater(GoogleApiClient googleApiClient, CurrentLocationInterface context) {
        mGoogleApiClient = googleApiClient;
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mContext = context;

        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onLocationChanged(Location location) {
        //if (mCurrentLocation == null) {
           // mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            mContext.onLocationChanged(location);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
            mRequestingLocationUpdates = false;
        }
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    public void fetchLastKnownLocationFromAPI()
    {
        CurrentLocation =  LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void Update() {
        //google Api is connected now
        mRequestingLocationUpdates = false;
        startLocationUpdates();
    }
}
