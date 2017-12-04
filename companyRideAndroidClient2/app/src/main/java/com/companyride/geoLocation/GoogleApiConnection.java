package com.companyride.geoLocation;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.companyride.geoLocation.LocationUpdater;
import com.companyride.interfaces.CurrentLocationInterface;
import com.companyride.observer.Observer;
import com.companyride.observer.Subject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ruslan on 30-Sep-15.
 */
public class GoogleApiConnection extends AsyncTask<Void, Void, Void>
             implements GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener, Subject
{
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private GoogleApiClient mGoogleApiClient = null;
    private Location mCurrentLocation;
    private Context mContext;
    private CurrentLocationInterface mCurrentInterface;
    private ArrayList<Observer> m_ObserversArray = new ArrayList<Observer>();
    private LocationRequest mLocationRequest;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public GoogleApiConnection(Context context) {
        mContext = context;
        mCurrentInterface = (CurrentLocationInterface)context;
        InitializeConnection(context);
    }

    public GoogleApiConnection(Fragment fragment) {
        mContext = fragment.getActivity();
        mCurrentInterface = (CurrentLocationInterface)fragment;
        InitializeConnection(mContext);
    }

    private void InitializeConnection(Context context){
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                //.enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        createLocationRequest();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        notifyObservers();
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

    public void fetchLastKnownLocationFromAPI()
    {
        mCurrentLocation =  LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        notifyObservers();
    }

    public LatLng getLastKnownLocation()
    {
        LatLng retLastLocation;
        if(mCurrentLocation==null)
            retLastLocation = null;
        else
            retLastLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        return retLastLocation;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    public void Disconnect() { //onStop disconnect
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
                mGoogleApiClient.connect();
        }

    }
    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        //dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
        return null;
    }

    @Override
    public void registerObserver(Observer observer) {
        m_ObserversArray.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        m_ObserversArray.remove(observer);
    }

    @Override
    public void notifyObservers() {
        mCurrentInterface.onCurrentLocationReady();
        for(Observer observ : m_ObserversArray)
        {
            observ.Update(); //Update that GoogleApiClient is connected
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            //((GoogleApiConnection) getActivity()).onDialogDismissed();
        }
    }
}
