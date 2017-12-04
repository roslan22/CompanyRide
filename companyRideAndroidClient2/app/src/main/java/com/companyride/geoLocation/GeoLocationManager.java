package com.companyride.geoLocation;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Valeriy on 5/27/2015.
 */
public  class GeoLocationManager {
    public static final String LOG_TAG = "GeoLocationManager";

    public static Address getAddressLocationFromString(String address, Context context) {
        Address loc = null;
        if (!Geocoder.isPresent()){
            Log.e(LOG_TAG, "No Geocoder service exists on the device!");
            return loc;
        }
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0)
                loc = addresses.get(0);
        } catch (IOException e) {
            //todo: handle exception
            e.printStackTrace();
        }
        if (loc == null) {
            Log.e(LOG_TAG, "No location was found for address: '" + address + "'");
        }
        return loc;
    }

    private static String getAddressStringFromLocation(Double latitude, Double longitude , Context context) {
        String addrStr = null;
        if (!Geocoder.isPresent()){
            Log.e(LOG_TAG, "No Geocoder service exists on the device!");
            return addrStr;
        }
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0)
                addrStr = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAddressLine(1);
        } catch (IOException e) {
            //todo: handle exception
            e.printStackTrace();
        }
        if (addrStr == null || addrStr.isEmpty()) {
            Log.e(LOG_TAG, "No address was found for location: (" + longitude.toString() + ", " +latitude + ")");
        }
        return addrStr;
    }

    public GetAddressLocationTask getAddressLocationTask(Context context) {
        return new GetAddressLocationTask(context);
    }

    public GetAddressStringFromLocationTask getAddressStringFromLocationTask(Context context) {
        return new GetAddressStringFromLocationTask(context);
    }

    public class GetAddressLocationTask extends AsyncTask<String, Void, Address>{
        private Context mContext;
        public GetAddressLocationTask(Context context){
            this.mContext = context;
        }

        @Override
        protected Address doInBackground(String... params) {
            return GeoLocationManager.getAddressLocationFromString(params[0], mContext);
        }
    }


    public class GetAddressStringFromLocationTask extends AsyncTask<Double, Void, String>{
        private Context mContext;
        public GetAddressStringFromLocationTask(Context context){
            this.mContext = context;
        }

        @Override
        protected String doInBackground(Double... params) {
            return GeoLocationManager.getAddressStringFromLocation(params[0], params[1],  mContext);
        }
    }
}
