package com.companyride.http;

import android.os.AsyncTask;
import org.json.JSONObject;

/**
 * Created by Valeriy on 3/28/2015.
 */
public class DeleteExecutor extends AsyncTask<String,Void, JSONObject>
{
    @Override
    protected JSONObject doInBackground(String... address)
    {
        HTTPManager.setTimeout(10);
        return HTTPManager.delete(address[0]);
    }
}
