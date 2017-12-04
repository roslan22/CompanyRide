package com.companyride.fragments.rideRequest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.activities.RideRequestMainScreen;
import com.companyride.http.PutExecutor;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;

public class PotentialRidesFragment extends Fragment{
    private Activity parentActivity;
    private View rootView;
    private ListView listViewRides;
    private RidesListAdapter ridesListAdapter;
    private String rideReqId;
    private JSONObject rideReqJSON;
    private String TAG = "Potential Rides";
    private PotentialRideDialogFragment potentialRidesDialog;
    private String POT_RIDE_DIALOG = "Potential Rides";
    private int REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rideReqId = ((RideRequestMainScreen)parentActivity).getRideRequestId();
        rideReqJSON = ((RideRequestMainScreen)parentActivity).getRideRequestJSON();
        rootView = inflater.inflate(R.layout.page_ride_request_pot_rides, container, false);
        listViewRides = (ListView) rootView.findViewById(R.id.listViewPotRides);
        loadPotentialRides();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == PotentialRideDialogFragment.RESULT_CODE) {
            String reqId = data.getStringExtra(PotentialRideDialogFragment.REQ_ID);
            Log.i(TAG, "Chosen req id: " + reqId);
            JSONObject potRide = ridesListAdapter.getItemForRideId(reqId);
            String newReqId = updateRideRequestToServer(potRide);
            ((RideRequestMainScreen)parentActivity).loadRideRequestFromServer(newReqId);
        }
    }

    private String updateRideRequestToServer(JSONObject potentialRide)
    {
        JSONObject res = null;
        try {
            String path = Params.serverIP + Params.rideRequestOneTimePath + "/" + rideReqId;
            JSONObject diffJson = new JSONObject();
            diffJson.put("origReq", rideReqJSON);
            diffJson.put("weekday", UtilityFunctions.tryGetLongFromJson(potentialRide, "weekday"));
            diffJson.put("startDate", UtilityFunctions.convertEDTFullStringToUTCFullString(
                    UtilityFunctions.tryGetStringFromJson(potentialRide, "eventDate"),
                    UtilityFunctions.tryGetLongFromJson(rideReqJSON, "timeOffset")));
            //diffJson.put("stopDate", UtilityFunctions.tryGetStringFromJson(potentialRide, "eventDate"));
            diffJson.put("toHour", UtilityFunctions.tryGetDoubleFromJson(potentialRide, "rideTimeTo"));
            diffJson.put("fromHour", UtilityFunctions.tryGetDoubleFromJson(potentialRide, "rideTimeFrom"));
            diffJson.put("to", UtilityFunctions.tryGetJson(potentialRide, "to"));
            diffJson.put("eventType", Params.onTimeEvent);
            Log.d(TAG, diffJson.toString());
            res = new PutExecutor().execute(path, diffJson.toString()).get();

            if (res == null) {
                UtilityFunctions.showMessageInToast(parentActivity, "Failed to send request to server. Try again later");
                return null;
            }

            if (UtilityFunctions.tryGetIntFromJson(res, "code") == 200)
            {
                parentActivity.onBackPressed();
                String message = UtilityFunctions.tryGetStringFromJson(res,"message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
            }
            else
            {
                String message = UtilityFunctions.tryGetStringFromJson(res, "message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Unexpected exception: ");
            ex.printStackTrace();
        }
        return UtilityFunctions.tryGetStringFromJson(UtilityFunctions.tryGetJson(res, "data"), "newReqId");
    }

    public void loadPotentialRides(){
        // get potential rides from server
        MyJSONArray potRides = getPotentialRidesForRequest();
        if(potRides != null && potRides.length() > 0) {
            // create an adapter for rides
            ridesListAdapter = new RidesListAdapter(
                    parentActivity,
                    potRides,
                    UtilityFunctions.tryGetLongFromJson(rideReqJSON, "timeOffset")
            );
            listViewRides.setAdapter(ridesListAdapter);
            listViewRides.setOnItemClickListener(ridesListAdapter.onItemClickListener);
        }
        else
        {
            Log.i(TAG, "No potential rides were found!");
            UtilityFunctions.showMessageInToast(this.parentActivity, "No potential rides were found!");
            ((RideRequestMainScreen)this.parentActivity).goBackView();
        }
    }

    private MyJSONArray getPotentialRidesForRequest() {
        //return UtilityFunctions.tryGetJsonArray(rideReqJSON, Params.potentialRides);
        String date = ((RideRequestMainScreen)parentActivity).getCurrentDateStr();
        String executionPath = Params.serverIP + Params.potRideRequestPath + "/" + rideReqId + "/" + date;
        JSONObject res = UtilityFunctions.tryGetJsonFromServer(executionPath);
        if(res == null) return null;
        return UtilityFunctions.tryGetMyJSONArray(res, "data");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        //inflater.inflate(R.menu.ride_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class RidesListAdapter extends BaseAdapter {
        private Context context;
        private MyJSONArray rides;
        private long timeOffset;

        public RidesListAdapter(Context context, MyJSONArray rides, long timeOffset) {
            this.context = context;
            this.rides = rides;
            this.timeOffset = timeOffset;
        }

        @Override
        public int getCount() {
            return rides.length();
        }

        @Override
        public Object getItem(int position) {
            return UtilityFunctions.tryGetJsonObjectFromArray(rides, position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public String getItemStrId(int position){
            JSONObject json = (JSONObject)getItem(position);
            return UtilityFunctions.tryGetStringFromJson(json, "_id");
        }

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TextView date = (TextView) v.findViewById(R.id.date);
                TextView time = (TextView) v.findViewById(R.id.pickUpTime);
                PotentialRideDialogFragment potRideDialog =  PotentialRideDialogFragment.newInstance(
                        parentActivity,
                        date.getText().toString(),
                        time.getText().toString(),
                        getItemStrId(position)
                        );
                potRideDialog.setTargetFragment(PotentialRidesFragment.this, REQUEST_CODE);
                potRideDialog.show(getFragmentManager(), POT_RIDE_DIALOG);
            }
        };

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView date;
            final TextView addressDrop;
            final TextView pikUpTimeView;

            try {
                final JSONObject ride = (JSONObject) getItem(position);

                JSONObject dropDetails = UtilityFunctions.tryGetJson(ride, "to");
                String dateText = UtilityFunctions.convertFullUTCStringToFormEDTString(
                        UtilityFunctions.tryGetStringFromJson(ride, "eventDate"),
                        timeOffset
                );
                String fromHour = UtilityFunctions.convertNumberToTime(
                        UtilityFunctions.tryGetDoubleFromJson(ride, "rideTimeFrom"),
                        timeOffset
                );
                String toHour = UtilityFunctions.convertNumberToTime(
                        UtilityFunctions.tryGetDoubleFromJson(ride, "rideTimeTo"),
                        timeOffset
                );
                //String pickUpAddress = UtilityFunctions.tryGetStringFromJson(pickUpDetails, "address");
                String dropAddress = UtilityFunctions.tryGetStringFromJson(dropDetails, "address");

                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.potential_ride_details, null);
                }

                // get all items
                pikUpTimeView = (TextView) convertView.findViewById(R.id.pickUpTime);
                date = (TextView) convertView.findViewById(R.id.date);
                addressDrop = (TextView) convertView.findViewById(R.id.textViewTo);

                // set values
                addressDrop.setText(dropAddress);
                date.setText(dateText);
                pikUpTimeView.setText(fromHour + "-" + toHour);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return convertView;
        }

        public JSONObject getItemForRideId(String reqId) {
            for(int position=0; position < rides.length(); position++){
                JSONObject item = (JSONObject)getItem(position);
                if(UtilityFunctions.tryGetStringFromJson(item, "_id") == reqId)
                    return item;
            }
            return null;
        }
    }
}


