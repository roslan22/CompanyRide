package com.companyride.activities;

/**
 * Created by Ruslan on 02-May-15.
 */
import android.app.ActionBar;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.companyride.R;
import com.companyride.geoLocation.GoogleApiConnection;
import com.companyride.geoLocation.LocationServiciesChecker;
import com.companyride.geoLocation.LocationUpdater;
import com.companyride.interfaces.CurrentLocationInterface;
import com.companyride.mathFunctions.MathFunctions;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Map extends AppCompatActivity
        implements OnMapReadyCallback, CurrentLocationInterface {

    private LocationManager mLocationManager;
    Location mLocation;
    private GoogleMap m_Map;
    ArrayList<Coordinate> fromtoCoordinates = new ArrayList<>();
    ArrayList<Coordinate> hitchersCoordinates = new ArrayList<>();
    private int currHitcherIndex;
    private GoogleApiConnection mGoogleApiConnection;
    private Marker mCurrentLocationMarker;
    private LatLng mPrevLastKnownLocation;
    private float mLastRotation;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mp);

        try {
            retrieveCoordinates();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mapFragment.getMapAsync(this);
        // Customize the action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mGoogleApiConnection = new GoogleApiConnection(this);
        mGoogleApiConnection.registerObserver(new LocationUpdater(mGoogleApiConnection.getGoogleApiClient(), this));
        mPrevLastKnownLocation = new LatLng(0,0);
        mLastRotation = 0;
        mGoogleApiConnection.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void retrieveCoordinates() throws JSONException {
        try
        {
            Intent i = getIntent();
            String extra = i.getStringExtra(Params.extraLocations);
            JSONObject locationData = new JSONObject(extra);

            if (locationData != null)
            {
                retrieveFromToCoordinates(locationData);
                retrieveHitcherCoordinatesIfPresent(locationData);
            }
        }
        catch ( Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void retrieveFromToCoordinates(JSONObject rideJson)
    {
        try {
            JSONObject fromJSON = UtilityFunctions.tryGetJson(rideJson, "from");
            JSONObject toJSON = UtilityFunctions.tryGetJson(rideJson, "to");

            JSONObject fromCoordinates = UtilityFunctions.tryGetJson(fromJSON, "coordinates");
            JSONObject toCoordinates = UtilityFunctions.tryGetJson(toJSON, "coordinates");

            Double fromLat = UtilityFunctions.tryGetDoubleFromJson(fromCoordinates, "lat");
            Double fromLong = UtilityFunctions.tryGetDoubleFromJson(fromCoordinates, "long");
            String fromAddress = UtilityFunctions.tryGetStringFromJson(fromJSON, "address");

            Double toLat = UtilityFunctions.tryGetDoubleFromJson(toCoordinates, "lat");
            Double toLong = UtilityFunctions.tryGetDoubleFromJson(toCoordinates, "long");
            String toAddress = UtilityFunctions.tryGetStringFromJson(toJSON, "address");

            fromtoCoordinates.add(new Coordinate(fromLat,fromLong,fromAddress,"Start point"));
            fromtoCoordinates.add(new Coordinate(toLat,toLong,toAddress,"End point"));
        }
        catch  (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void retrieveHitcherCoordinatesIfPresent(JSONObject locationData)
    {
        try
        {
            MyJSONArray hitchers  = UtilityFunctions.tryGetMyJSONArray(locationData, "hitchers");

            if (hitchers != null)
            {
                for (int i = 0; i < hitchers.length(); i++)
                {
                    currHitcherIndex = i;
                    retrieveCoordinateForOneHitcher((JSONObject) hitchers.get(i));

                }
            }
        }
        catch  (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void retrieveCoordinateForOneHitcher (JSONObject hitcher)
    {
        try
        {
            JSONObject pickUp = UtilityFunctions.tryGetJson(hitcher, "pickUp");
            JSONObject drop = UtilityFunctions.tryGetJson(hitcher, "drop");

            JSONObject pickupCoordinates = UtilityFunctions.tryGetJson(pickUp, "coordinates");
            JSONObject dropCoordinates = UtilityFunctions.tryGetJson(drop, "coordinates");

            String pickUpAddr = UtilityFunctions.tryGetStringFromJson(pickUp, "address");
            String dropAddr = UtilityFunctions.tryGetStringFromJson(drop, "address");

            String hitcherFullName = UtilityFunctions.tryGetStringFromJson(hitcher, "fullName");

            Double pickUpLat = UtilityFunctions.tryGetDoubleFromJson(pickupCoordinates, "lat");
            Double pickUpLong = UtilityFunctions.tryGetDoubleFromJson(pickupCoordinates, "long");

            Double dropLat = UtilityFunctions.tryGetDoubleFromJson(dropCoordinates, "lat");
            Double dropLong = UtilityFunctions.tryGetDoubleFromJson(dropCoordinates, "long");

            BitmapDescriptor icon = getMarkerIcon(currHitcherIndex);
            hitchersCoordinates.add(new Coordinate(pickUpLat,pickUpLong,pickUpAddr,  hitcherFullName + " pick up point", icon ));
            hitchersCoordinates.add(new Coordinate(dropLat,dropLong,dropAddr,  hitcherFullName + " drop point", icon));
        }
        catch  (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        LocationManager lm;
        m_Map = map;

        if (fromtoCoordinates.size() == 2) {
            map.addMarker(new MarkerOptions()
                    .title(fromtoCoordinates.get(0).getTitle())
                    .snippet(fromtoCoordinates.get(0).getAddress())
                    .position(fromtoCoordinates.get(0).getLatLong())).showInfoWindow();

            map.addMarker(new MarkerOptions()
                    .title(fromtoCoordinates.get(1).getTitle())
                    .snippet(fromtoCoordinates.get(1).getAddress())
                    .position(fromtoCoordinates.get(1).getLatLong()));
        }

        for (int i = 0; i < hitchersCoordinates.size(); i++) {
            map.addMarker(new MarkerOptions()
                    .title(hitchersCoordinates.get(i).getTitle())
                    .snippet(hitchersCoordinates.get(i).getAddress())
                    .position(hitchersCoordinates.get(i).getLatLong())
                    .icon(hitchersCoordinates.get(i).getBitmapDescriptor()));
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fromtoCoordinates.get(0).getLatLong(), 13));
    }

    @Override
    public void onCurrentLocationReady() {
        if(isLastKnownLocationAvailable()) {
            initialMarkerToImageOfLastKnownLocation();
        }
        else
        {
           new LocationServiciesChecker(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //mGoogleApiConnection.fetchLastKnownLocationFromAPI();
        updateMarkerToLastKnownLocation(location);
        //animateLocationChange(); //TODO check this function
    }

    private void initialMarkerToImageOfLastKnownLocation() {
        LatLng lastKnownLocation = mGoogleApiConnection.getLastKnownLocation();
            mCurrentLocationMarker = addGreenCarMarkerToLastKnownLocation(lastKnownLocation);
        mCurrentLocationMarker.showInfoWindow();
        //mPrevLastKnownLocation = lastKnownLocation;
    }

    private Marker addGreenCarMarkerToLastKnownLocation(LatLng lastKnownLocation) {
        Marker returnMarker;
        float rotation = (float) MathFunctions.calcRotationAngleInDegrees(mPrevLastKnownLocation, lastKnownLocation);

        returnMarker = m_Map.addMarker(new MarkerOptions()
                .position(lastKnownLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_car))
                .anchor(0.5f, 0.5f)
                .rotation(rotation - mLastRotation));
        mPrevLastKnownLocation = lastKnownLocation;
        mLastRotation = (float) MathFunctions.calcRotationAngleInDegrees(mPrevLastKnownLocation, lastKnownLocation);
        //check to simplify location
        return returnMarker;
    }

    private void updateMarkerToLastKnownLocation(Location location) {
        mCurrentLocationMarker.remove();
        mCurrentLocationMarker =  addGreenCarMarkerToLastKnownLocation(
                                  new LatLng(location.getLatitude(),location.getLongitude()));
        mCurrentLocationMarker.showInfoWindow();
    }

    private boolean isLastKnownLocationAvailable() {
        if(mGoogleApiConnection.getLastKnownLocation() != null ) {
            return true;
        }
        else
        {
            return false;
        }
    }


    private BitmapDescriptor getMarkerIcon(int i)
    {
        BitmapDescriptor res = BitmapDescriptorFactory.defaultMarker();
        int mod = i % 6;

        switch (mod)
        {
            case 0: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); break;
            case 1: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN); break;
            case 2: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA); break;
            case 3: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW); break;
            case 4: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE); break;
            case 5: res =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE); break;
        }

        return res;
    }

    private class Coordinate
    {
        private double lat;
        private double longi;
        private String address;
        private LatLng latLong;
        private String title;
        private BitmapDescriptor icon;

        public Coordinate(double lat,double longi, String address, String title, BitmapDescriptor icon)
        {
            this.lat = lat;
            this.longi = longi;
            this.address = address;
            this.title = title;
            this.latLong = new LatLng(lat, longi);
            this.icon = icon;
        }

        public Coordinate(double lat,double longi, String address, String title)
        {
            this.lat = lat;
            this.longi = longi;
            this.address = address;
            this.title = title;
            this.latLong = new LatLng(lat, longi);
            icon = BitmapDescriptorFactory.defaultMarker();
        }

        public double getLat(){return lat;}
        public double getLongi(){return longi;}
        public BitmapDescriptor getBitmapDescriptor(){return icon;}
        public String getAddress(){return address;}
        public String getTitle(){return title;}
        public void setAddress(String address){ this.address = address;}
        public void setLat(double lat){ this.lat = lat;}
        public void setLongi(double longi){ this.longi = longi;}
        public void setBitmapDescriptor(BitmapDescriptor icon){this.icon = icon;}

        public LatLng getLatLong()
        {
            return latLong;
        }
    }
}