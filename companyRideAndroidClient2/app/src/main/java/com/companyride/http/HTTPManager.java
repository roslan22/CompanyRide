package com.companyride.http;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by Valeriy on 5/2/2015.
 */
public class HTTPManager {
    static final String LOG_TAG = "HTTPManager";
    static HttpClient httpClient = new DefaultHttpClient();

    public static void setTimeout(int seconds){
        final HttpParams httpParameters = httpClient.getParams();

        HttpConnectionParams.setConnectionTimeout(httpParameters, seconds * 1000);
        HttpConnectionParams.setSoTimeout        (httpParameters, seconds * 1000);
    }

    private static HttpResponse executeHTTPCmdAndGetResponse(HttpEntityEnclosingRequestBase request, String data){
        try
        {
            String utfString = new String(data.getBytes(), "UTF-8");
            StringEntity se = new StringEntity(utfString,HTTP.UTF_8);
            request.setEntity(se);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json; charset=utf-8");

            return httpClient.execute(request);
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "Network fail");
            ex.printStackTrace();
        }
        return null;
    }

    public static JSONObject post(String url, String postData){
        HttpResponse res = null;
        JSONObject resJSON = null;
        HttpPost httpPost = new HttpPost(url);
        res = executeHTTPCmdAndGetResponse(httpPost, postData);

        if (res != null)
        {
            resJSON = getJSONFromResponse(res);
        }

        return resJSON;
    }

    public static JSONObject put(String url, String putData){
        HttpResponse res = null;
        JSONObject resJSON = null;
        HttpPut httpPut = new HttpPut(url);
        Log.d("TAG", "Put Url: " + url);
        res = executeHTTPCmdAndGetResponse(httpPut, putData);

        if (res != null)
        {
            resJSON = getJSONFromResponse(res);
        }

        return resJSON;
    }

    public static JSONObject postFile(String url, String filePath){
        HttpResponse res = null;
        JSONObject resJSON = null;
        HttpPost httppost = new HttpPost(url);
        Log.d("TAG", "Post Url: " + url);
        try
        {
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("picture", new File(filePath));
            httppost.setEntity(reqEntity);
//            httpPut.setHeader("Accept", "application/json");
//            httpPut.setHeader("Content-type", "application/json; charset=utf-8");
            res = httpClient.execute(httppost);
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "Network fail");
            ex.printStackTrace();
        }

        if (res != null)
        {
            resJSON = getJSONFromResponse(res);
        }

        return resJSON;
    }

    public  static JSONObject get(String url){
        HttpResponse res = null;
        JSONObject resJSON = null;
        HttpGet httpGet = new HttpGet(url);
        Log.d("TAG", "Url: " + url);
        try
        {
            res = httpClient.execute(httpGet);
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "Network fail");
            ex.printStackTrace();
        }

        if (res != null)
        {
            resJSON = getJSONFromResponse(res);
        }

        return resJSON;
    }

    private static JSONObject getJSONFromResponse(HttpResponse res) {
        try
        {
            int status = res.getStatusLine().getStatusCode();
            if (status == 404)
                throw new HttpException(res.getStatusLine().getReasonPhrase());
            HttpEntity entity = res.getEntity();
            String data = EntityUtils.toString(entity);
            JSONObject resJSON = new JSONObject(data);
            resJSON.put("code", status);
            return resJSON;
        }
        catch (HttpException e){
            Log.e(LOG_TAG, "Failed to execute HTTP request");
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Failed to parse request");
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject delete(String url) {
        HttpResponse res = null;
        JSONObject resJSON = null;
        HttpDelete httpDelete = new HttpDelete(url);
        Log.d("TAG", "Url: " + url);
        try
        {
            res = httpClient.execute(httpDelete);
        }
        catch(Exception ex)
        {
            Log.e(LOG_TAG, "Network fail");
            ex.printStackTrace();
        }

        if (res != null)
        {
            resJSON = getJSONFromResponse(res);
        }

        return resJSON;
    }

    public static void main(String[] args)
    {
        String url = "http://52.4.64.206:3000/rideRequest";
        String data = "{\"rideType\":\"driver\",\"maxNumOfHitchers\":\"1\",\"userProfileId\":\"53f45f26780bfa040b36dfa3\",\"inconvenientUsers\":[],\"blockedUsers\":[\"53f45f26780bfa040b36dfa3\"],\"eventType\":\"one-time\",\"weekday\":4,\"startDate\":\"2017-07-11T22:00:00.000Z\",\"status\":\"new\",\"from\":{\"type\":\"Point\",\"address\":\"נתניה נורדאו 15\",\"coordinates\":{\"long\":34.8575285,\"lat\":32.2845256}},\"to\":{\"type\":\"Point\",\"address\":\"ישראל תל אביב יפו\",\"coordinates\":{\"long\":34.7817676,\"lat\":32.0852999}},\"preferredRideTime\":{\"fromHour\":12.5,\"toHour\":16.5},\"timeOffset\":7200000}";
        HTTPManager.post(url, data);
    }


}
