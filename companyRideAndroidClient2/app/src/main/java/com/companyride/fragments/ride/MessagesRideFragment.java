package com.companyride.fragments.ride;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.companyride.R;
import com.companyride.activities.Map;
import com.companyride.activities.RideMainScreen;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ruslan on 07-Jul-15.
 */
public class MessagesRideFragment extends Fragment{
    private Activity parentActivity;
    private View rootView;
    private ListView listViewDriverHitchersMsgs;
    private HitcherListAdapter hitcherListAdapter;
    private String rideId;
    private JSONObject rideJSON;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rideId = ((RideMainScreen)parentActivity).getRideId();
        String path = Params.serverIP + "ride/" + rideId;
        rideJSON =  UtilityFunctions.tryGetJsonFromServer(path);
        rideJSON = UtilityFunctions.tryGetJson(rideJSON,"data");
        rootView = inflater.inflate(R.layout.ride_messages_view, container, false);
        initVariables();
        if (rideJSON != null)
        {
            loadHitchers();
        }
        setHasOptionsMenu(true);
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        //inflater.inflate(R.menu.messages_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                ((SelectViewInterface) parentActivity).goBackView();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initVariables() {
        listViewDriverHitchersMsgs = (ListView) rootView.findViewById(R.id.listViewDriversHitcherMessg);
    }

    private void loadHitchers() {
        MyJSONArray hitchers = UtilityFunctions.tryGetMyJSONArray(rideJSON, "hitchers");

        if (hitchers != null) {
            hitcherListAdapter = new HitcherListAdapter(parentActivity, hitchers);
            listViewDriverHitchersMsgs.setAdapter(hitcherListAdapter);
        }
    }

    private class HitcherListAdapter extends BaseAdapter {
        private Context context;
        private MyJSONArray hitchers;

        public HitcherListAdapter(Context context, MyJSONArray hitchers) {
            this.context = context;
            this.hitchers = hitchers;
        }

        @Override
        public int getCount() {
            return hitchers.length();
        }

        @Override
        public Object getItem(int position) {
            return UtilityFunctions.tryGetJsonObjectFromArray(hitchers, position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView personName;
            TextView textViewMessages;
            ImageView picture;
            String messages;

            try
            {
                final JSONObject hitcher = (JSONObject) getItem(position);
                final String profileId = getHitcherProfileId(position);

                String pictureUrl = UtilityFunctions.buildPictureProfileUrl(profileId);
                String name = UtilityFunctions.tryGetStringFromJson(hitcher, "fullName");
                messages = getMessagesFromJson(hitcher);

                if (convertView == null)
                {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.ride_message_box, null);
                }

                // get all items
                personName = (TextView) convertView.findViewById(R.id.hitcherFullName);
                textViewMessages = (TextView) convertView.findViewById(R.id.tvMsgs);
                picture = (ImageView) convertView.findViewById(R.id.hitcherImage);

                // set values
                personName.setText(name);
                if(messages!=null) {
                    textViewMessages.setText(messages);
                }

                if (pictureUrl != null) {
                    try {
                        new ImageLoaderTask(picture,parentActivity, profileId).execute(pictureUrl);
                    } catch (Exception ex) {
                        System.out.println("Failed to retrieve picture url");
                        ex.printStackTrace();
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return convertView;
        }


        private String getMessagesFromJson(JSONObject obj)
        {
            JSONObject messagesObj;
            JSONObject receivedMessages;
            MyJSONArray mesArr;
            StringBuilder str = new StringBuilder();

            messagesObj = UtilityFunctions.tryGetJson(obj,"messages");
            mesArr = UtilityFunctions.tryGetMyJSONArray(messagesObj,"sent");
       //TODO     // mesArr = UtilityFunctions.tryGetJsonArray(messagesObj,"received"); //receive messages from driver
            try {
                for(int i=0; i<mesArr.length(); i++) {
                    str.append(mesArr.getString(i));
                    str.append('\n');
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

          return str.toString();
        }

        private String getHitcherProfileId(int position) {
            JSONObject hitcher = (JSONObject) getItem(position);
            return UtilityFunctions.tryGetStringFromJson(hitcher, "userProfileId");
        }

        private void sendToServerDriverApproveHitcher(final String profileId,
                                                      final String pickUpTime,
                                                      final String pickUpAddress,
                                                      final String dropAddress) {
            try {
                String path = Params.serverIP + "ride/" + rideId + "/driver/" + profileId + "/approve";
                //retrieve pick up hour and location to json
                JSONObject pickUpDetails = retrieveDropPickUpTimeAndLocation(pickUpTime, pickUpAddress, dropAddress);
                if (pickUpDetails != null)
                {
                    JSONObject res = new PutExecutor().execute(path, pickUpDetails.toString()).get();

                    if (res == null) {
                        UtilityFunctions.showMessageInToast(context, "Failed to send request to server. Try again later");
                    } else {
                        if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                        {
                            UtilityFunctions.showMessageInToast(context, "Hitcher approve was successfully send to server");
                        } else {
                            String message = res.getString("message");
                            UtilityFunctions.showMessageInToast(context, message);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UtilityFunctions.showMessageInToast(parentActivity, "Error in sending request to server");
            }
            // update adapter data
            rideJSON = ((RideMainScreen)parentActivity).reloadRideJSON();
            loadHitchers();
            notifyDataSetChanged();
        }

        private JSONObject retrieveDropPickUpTimeAndLocation(String pickUpTime,
                                                             String pickUpAddress,
                                                             String dropAddress) throws JSONException
        {
            JSONObject pickUpDropDetails = null;
            JSONObject pickUp = new JSONObject();
            JSONObject drop = new JSONObject();

            JSONObject coordinatesPickUp = getGeoLocationJSONFromAddress(pickUpAddress);
            JSONObject coordinatesDrop = getGeoLocationJSONFromAddress(dropAddress);

            if (coordinatesPickUp == null)
                UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve coordinates for \"Pick up address\"");
            else if (coordinatesDrop == null)
                UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve coordinates for \"Pick up address\"");
            else
            {
                pickUp.put("address", UtilityFunctions.tryGetStringFromJson(coordinatesPickUp, "address"));
                pickUp.put("coordinates", UtilityFunctions.tryGetJson(coordinatesPickUp, "coordinates"));
                try
                {
                    pickUp.put("time", UtilityFunctions.convertTimeToNumber(pickUpTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                drop.put("address", UtilityFunctions.tryGetStringFromJson(coordinatesDrop, "address"));
                drop.put("coordinates", UtilityFunctions.tryGetJson(coordinatesDrop, "coordinates"));

                pickUpDropDetails = new JSONObject();
                pickUpDropDetails.put("pickUp",pickUp).put("drop", drop);
            }

            return pickUpDropDetails;
        }

        private JSONObject getGeoLocationJSONFromAddress(String addrText) {
            return UtilityFunctions.tryRetrieveAddressFromText(parentActivity, addrText);
        }
    }
}
