package com.companyride.http;

import android.os.AsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Valeriy on 3/28/2015.
 */
public class GetExecutor extends AsyncTask<String,Void, JSONObject>
{
    @Override
    protected JSONObject doInBackground(String... address)
    {
        HTTPManager.setTimeout(10);
        return HTTPManager.get(address[0]);
    }
}
