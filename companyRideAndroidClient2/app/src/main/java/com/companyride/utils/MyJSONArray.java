package com.companyride.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Valeriy on 5/6/2016.
 */
public class MyJSONArray extends JSONArray {
    public MyJSONArray(){}

    public MyJSONArray(JSONObject json, String key) throws JSONException {
        super(json.getJSONArray(key).toString());
    }

    @Override
    public Object remove(int index) {

        JSONArray output = new JSONArray();
        int len = this.length();
        for (int i = 0; i < len; i++)   {
            if (i != index) {
                try {
                    output.put(this.get(i));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return output;
        //return this; If you need the input array in case of a failed attempt to remove an item.
    }
}
