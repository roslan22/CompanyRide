package com.companyride.http;

import android.os.AsyncTask;
import org.json.JSONObject;

/**
 * Created by Valeriy on 3/28/2015.
 */
public class PutExecutor extends AsyncTask<String,Void, JSONObject>
{
    @Override
    protected JSONObject doInBackground(String... address) {
        HTTPManager.setTimeout(10);
        //pass the url as the first arg and the data as the second arg
        return HTTPManager.put(address[0], address[1]);
    }
}
