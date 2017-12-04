package com.companyride.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.companyride.geoLocation.GeoLocationManager;
import com.companyride.http.HTTPManager;
import com.companyride.geoLocation.LocationServiciesChecker;
import com.companyride.parameters.Params;
import org.json.JSONException;
import org.json.JSONObject;

import com.companyride.http.GetExecutor;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by Vlada on 05/04/2015.
 */
public class UtilityFunctions
{
    //format HH:MM
    public static double convertTimeToNumber(String timeStr)
    {
        String utcTimeStr = convertTimeStringToUTCTimeString(timeStr, getTimeOffset());
        String delims = "[:]";
        String[] tokens = utcTimeStr.split(delims);

        double minutes = Integer.parseInt(tokens[1]);
        double expandedMinutes =  minutes/60;
        double hours = Double.parseDouble(tokens[0]);
        return hours + expandedMinutes;
    }

    //format HH:MM
    public static String convertNumberToTime(double timeNum, long offset)
    {
        int hours = (int)timeNum;
        int minutes =  (int)(Math.round((timeNum * 100.0 - hours * 100.0)*0.6));
        String utcTimeStr = String.format("%02d:%02d", hours, minutes);
        return convertUTCTimeStringToEDTTimeString(utcTimeStr, offset);
    }


    public static void showMessageInToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        System.out.println(message);
    }

    public static JSONObject tryGetJsonFromServer(String path)
    {
        JSONObject res = null;
        try {
            res = (new GetExecutor()).execute(path).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // if we have a result
        if (res != null)
        {
            String errorStatus = UtilityFunctions.tryGetStringFromJson(res, "status");
            if(errorStatus != null  && errorStatus.equals("error"))
            {
                res = null;
            }
        }
        return res;
    }

    public static JSONObject tryGetJsonObjectFromArray(MyJSONArray jArr, int index)
    {
        JSONObject res;

        try
        {
            res = jArr.getJSONObject(index);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve object from json array in index:" + index);
            ex.printStackTrace();
            //UtilityFunctions.showMessageInToast(getBaseContext(), "Failed to retrieve object from json array");
            return null;
        }
        return res;
    }

    public static MyJSONArray tryGetMyJSONArray(JSONObject jObj, String str)
    {
        MyJSONArray res;

        try
        {
            res = new MyJSONArray(jObj, str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve json array");
            ex.printStackTrace();
            //UtilityFunctions.showMessageInToast(getBaseContext(), "Failed to retrieve data from json array");
            return null;
        }
        return res;
    }

    public static String tryGetStringFromJson(JSONObject jObj, String str)
    {
        String res = null;

        try
        {
            res = jObj.getString(str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve string: " + str +" from json");
            ex.printStackTrace();
           // UtilityFunctions.showMessageInToast(getBaseContext(), "Failed to retrieve string from json");
        }
        return res;
    }

    public static Double tryGetDoubleFromJson(JSONObject jObj, String str)
    {
        Double res = null;

        try
        {
            res = jObj.getDouble(str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve double: " + str +" from json");
            ex.printStackTrace();
        }
        return res;
    }

    public static long tryGetLongFromJson(JSONObject jObj, String str)
    {
        Long res = null;

        try
        {
            res = jObj.getLong(str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve long: " + str +" from json");
            ex.printStackTrace();
        }
        return res;
    }

    public static int tryGetIntFromJson(JSONObject jObj, String str)
    {
        Integer res = null;

        try
        {
            res = jObj.getInt(str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve int: " + str +" from json");
            ex.printStackTrace();
        }
        return res;
    }


    public static JSONObject tryGetJson(JSONObject json, String str)
    {
        JSONObject res;
        try
        {
            res = json.getJSONObject(str);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve json data from json object");
            ex.printStackTrace();
            //UtilityFunctions.showMessageInToast(getBaseContext(), "Failed to retrieve data in userInfo");
            return null;
        }
        return res;
    }


    public static JSONObject tryRetrieveAddressFromText(Context context, String addressStr)
    {
        JSONObject json = null;
        GeoLocationManager.GetAddressLocationTask locationTask = new GeoLocationManager().getAddressLocationTask(context);
        try {
            Address address = locationTask.execute(addressStr).get();
            if (address != null)
            {
                double lat = address.getLatitude();
                double lon = address.getLongitude();
                json = new JSONObject();
                JSONObject coordinates = new JSONObject();
                json.put("type", "Point");
                json.put("address", address.getAddressLine(1) + " " + address.getAddressLine(0));
                coordinates.put("long", lon);
                coordinates.put("lat", lat);
                json.put("coordinates", coordinates);
            }
            // for emulator only
            // todo: remove at the end
            else if(Build.PRODUCT.contains("sdk_")){
                double lat = 31.1;
                double lon = 35.1;
                json = new JSONObject();
                JSONObject coordinates = new JSONObject();
                json.put("type", "Point");
                json.put("address", addressStr);
                coordinates.put("long", lon);
                coordinates.put("lat", lat);
                json.put("coordinates", coordinates);
            }
        } catch (InterruptedException e) {
            //todo: handle exception
            e.printStackTrace();
        } catch (ExecutionException e) {
            //todo: handle exception
            e.printStackTrace();
        } catch (JSONException e) {
            //todo: handle excepption
            e.printStackTrace();
        }
        return json;
    }

    public static String tryRetrieveAddressFromLangLat(Context context, LatLng location)
    {
        String address = null;
        GeoLocationManager.GetAddressStringFromLocationTask locationTask = new GeoLocationManager().getAddressStringFromLocationTask(context);
        try {
            address = locationTask.execute(location.latitude,location.longitude).get();
        } catch (InterruptedException e) {
            //todo: handle exception
            e.printStackTrace();
        } catch (ExecutionException e) {
            //todo: handle exception
            e.printStackTrace();
        }

        return address;
    }

    public static Boolean isLocationServiciesEnabled(Context context)
    {
        LocationServiciesChecker locationChecker = new LocationServiciesChecker(context);
        return locationChecker.checkLocationServicies();
    }

    public static long getTimeOffset(){
        return TimeZone.getDefault().getOffset(new Date().getTime());
    }

    public static Date utcToDefaultTime(Date utcTime, long offset){
//        return new Date(utcTime.getTime() + TimeZone.getDefault().getOffset(new Date().getTime()));
        return new Date(utcTime.getTime() + offset);
    }

    public static Date defaultToUTCTime(Date defaultTime, long offset){
//        return new Date(defaultTime.getTime() - TimeZone.getDefault().getOffset(new Date().getTime()));
        return new Date(defaultTime.getTime() - offset);
    }

    public static String dateToString(Date date, SimpleDateFormat format){
        format.setTimeZone(TimeZone.getDefault());
        return format.format(date);
    }

    public static Date stringToDate(String str, SimpleDateFormat format){
        format.setTimeZone(TimeZone.getDefault());
        Date date;
        try {
            date = format.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertUTCTimeStringToEDTTimeString(String timeStr, long offset){
        Date utcDate = stringToDate(timeStr, Params.formatTime);
        Date edtDate = utcToDefaultTime(utcDate, offset);
        return dateToString(edtDate, Params.formatTime);
    }

    public static String convertTimeStringToUTCTimeString(String timeStr, long offset){
        Date edtDate = stringToDate(timeStr, Params.formatTime);
        Date utcDate = defaultToUTCTime(edtDate, offset);
        return dateToString(utcDate, Params.formatTime);
    }

    public static String convertUTCFullStringToEDTFullString(String utcDateStr, long offset){
        Date utcDate = stringToDate(utcDateStr, Params.formatDate);
        Date edtDate = utcToDefaultTime(utcDate, offset);
        return dateToString(edtDate, Params.formatDate);
    }

    public static String convertEDTFullStringToUTCFullString(String edtDateStr, long offset){
        Date edtDate = stringToDate(edtDateStr, Params.formatDate);
        Date utcDate = defaultToUTCTime(edtDate, offset);
        return dateToString(utcDate, Params.formatDate);
    }

    public static String convertEDTFormStringToEDTFullString(String edtDateStr){
        Date edtDate = stringToDate(edtDateStr, Params.formDate);
        Date utcDate = defaultToUTCTime(edtDate, getTimeOffset());
        edtDate = utcToDefaultTime(utcDate, 0);
        return dateToString(edtDate, Params.formatDate);
    }

    public static String convertFullUTCStringToFormEDTString(String fullStr, long offset) {
        Date edtDate = stringToDate(fullStr, Params.formatDate);
        Date utcDate = defaultToUTCTime(edtDate, 0);
        edtDate = utcToDefaultTime(utcDate, offset);
        return dateToString(edtDate, Params.formDate);
    }

    public static void removeSoftInputFromCurrentView(Activity activity) {
        // remove soft input if exists
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String buildPictureProfileUrl(String profileId)
    {
        String url;

        url = Params.serverIP + "profilePictures/" + profileId + ".jpg";
        return url;
    }

    public static int compareDatesDay(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static CharSequence[] getCloseYears() {
        Calendar cal = Calendar.getInstance();
        CharSequence[] years = new CharSequence[3];
        years[0] = Integer.toString(cal.get(Calendar.YEAR) - 1);
        years[1] = Integer.toString(cal.get(Calendar.YEAR));
        years[2] = Integer.toString(cal.get(Calendar.YEAR) + 1);
        return years;
    }

    public static int getCurrMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH);
    }
}
