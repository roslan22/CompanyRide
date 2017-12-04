package com.companyride.fragments.ride;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.activities.Map;
import com.companyride.activities.RideMainScreen;
import com.companyride.fragments.MessagesDialog;
import com.companyride.geoLocation.GoogleApiConnection;
import com.companyride.geoLocation.PlaceAutocompleteAdapter;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.CurrentLocationInterface;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DriverRideFragment extends Fragment implements CurrentLocationInterface {
    private Activity parentActivity;
    private View rootView;
    private TextView textViewType;
    private TextView textViewTime;
    private TextView startsFrom;
    private TextView endsOn;
    private TextView from;
    private TextView to;
    private ListView listViewHitchers;
    private HitcherListAdapter hitcherListAdapter;
    private String rideId;
    private JSONObject rideJSON;
    private PickUpDetailsDialogFragment updateDetailsDialog;
    private MessagesDialog messagesDialog;
    private String locationsJsonStrForMap;
    private static final int REQUEST_CODE = 1;
    private static final int MESSAGES_VIEW = 3;
    private long timeOffset;
    private String dateStr;

    // Auto-complete stuff
    private GoogleApiConnection googleApiConnection;
    public PlaceAutocompleteAdapter mAdapter;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;
    private static final LatLngBounds BOUNDS_ISRAEL = new LatLngBounds(
            new LatLng(29, 34), new LatLng(34, 36));
    private Date rideStartDate;
    private Date rideStopDate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rideId = ((RideMainScreen)parentActivity).getRideId();
        rideJSON = ((RideMainScreen)parentActivity).getRideJSON();
        rootView = inflater.inflate(R.layout.page_ride_driver, container, false);
        initVariables();
        if (rideJSON != null)
        {
            prepareLocationsJsonForMap();
            loadRideDetails();
            loadHitchers();
        }
        Intent intent = parentActivity.getIntent();
        String extraDate = intent.getStringExtra(Params.extraDate);
        if(extraDate == null) dateStr = "";
        else
            dateStr    = UtilityFunctions.dateToString(
                    UtilityFunctions.stringToDate(
                            extraDate,Params.formDateJS
                    ),
                    Params.formDate
            );
        parentActivity.setTitle(parentActivity.getTitle() + "  " + dateStr);

        googleApiConnection = new GoogleApiConnection(this);
        googleApiConnection.execute();
        mAdapter = new PlaceAutocompleteAdapter(
                parentActivity,
                googleApiConnection.getGoogleApiClient(),
                BOUNDS_ISRAEL,
                null);

        setHasOptionsMenu(true);
        return rootView;
    }

    private  void prepareLocationsJsonForMap()
    {
        try
        {
            JSONObject locations = new JSONObject();

            locations.put("from", rideJSON.getJSONObject("from"));
            locations.put("to", rideJSON.getJSONObject("to"));
            locations.put("hitchers", rideJSON.getJSONArray("hitchers"));

            locationsJsonStrForMap = locations.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.ride_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.goToMapView:
                try {
                    Intent intent = new Intent(parentActivity, Map.class);
                    intent.putExtra(Params.extraLocations, locationsJsonStrForMap);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.goToMesView:
                //Bundle args = new Bundle();
                //args.putString("profileId","");
                ((RideMainScreen)parentActivity).selectView(getString(R.string.messages));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initVariables() {
        from = (TextView) rootView.findViewById(R.id.textViewFrom);
        to = (TextView) rootView.findViewById(R.id.textViewTo);
        listViewHitchers = (ListView) rootView.findViewById(R.id.listViewHitchers);
        textViewType = (TextView) rootView.findViewById(R.id.textViewType);
        textViewTime = (TextView) rootView.findViewById(R.id.textViewTime);
        startsFrom   = (TextView) rootView.findViewById(R.id.startsFromText);
        endsOn       = (TextView) rootView.findViewById(R.id.endsOnText);
    }
    private void loadRideDetails() {
        try
        {
            timeOffset = UtilityFunctions.tryGetLongFromJson(rideJSON, "timeOffset");
            String startDate     = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "startDate"),
                    timeOffset
            );
            rideStartDate = Params.formDate.parse(startDate);
            String stopDate      = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "stopDate"),
                    timeOffset
            );
            rideStopDate = Params.formDate.parse(stopDate);
            String minPickUpTime = UtilityFunctions.convertNumberToTime(rideJSON.getDouble("minPickUpHour"), timeOffset);
            String maxPickUpTime = UtilityFunctions.convertNumberToTime(rideJSON.getDouble("maxPickUpHour"), timeOffset);
            JSONObject fromObj = rideJSON.getJSONObject("from");
            JSONObject toObj = rideJSON.getJSONObject("to");
            from.setText(fromObj.getString("address"));
            to.setText(toObj.getString("address"));
            String eventType = rideJSON.getString("eventType");
            textViewType.setText(eventType);
            textViewTime.setText(minPickUpTime + " - " + maxPickUpTime);
            startsFrom.setText(startDate);
            if (eventType.equals(Params.onTimeEvent))
                stopDate = startDate;
            endsOn.setText(stopDate);
        } catch (Exception ex) {
            System.out.println("Failed to retrieve json location");
            ex.printStackTrace();
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve ride directions");
        }
    }

    private void loadHitchers() {
        if(rideJSON!=null) {
            MyJSONArray hitchers = UtilityFunctions.tryGetMyJSONArray(rideJSON, "hitchers");

            if (hitchers != null) {
                hitcherListAdapter = new HitcherListAdapter(parentActivity, hitchers, timeOffset);
                listViewHitchers.setAdapter(hitcherListAdapter);
            }
        }
        else
        { //this was last hitcher and was deleted. no ride
            ((SelectViewInterface) parentActivity).selectView(getResources().getString(R.string.user_profile));
        }
    }

    @Override
    public void onCurrentLocationReady() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    private class HitcherListAdapter extends BaseAdapter {
        private Context context;
        private MyJSONArray hitchers;
        private long timeOffset;

        public HitcherListAdapter(Context context, MyJSONArray hitchers, long timeOffset) {
            this.context = context;
            this.hitchers = hitchers;
            this.timeOffset = timeOffset;
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

        public String getStatus(int position) {
            JSONObject hitcher = (JSONObject) getItem(position);
            return UtilityFunctions.tryGetStringFromJson(hitcher, "status");
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView personName;
            final TextView addressPickUp;
            final TextView addressDrop;
            final TextView pickUpTime;
            Button approveButton;
            Button disapproveButton;
            Button updatePickUpDetails;
            Button gotoMessages;
            LinearLayout approveButtons;
            ImageView picture;


            try
            {
                final JSONObject hitcher = (JSONObject) getItem(position);
                final String profileId = getHitcherProfileId(position);

//                long hitcherTimeOffset = UtilityFunctions.tryGetLongFromJson(hitcher, "timeOffset");
                String pictureUrl = UtilityFunctions.buildPictureProfileUrl(profileId);
                String name = UtilityFunctions.tryGetStringFromJson(hitcher, "fullName");
                String status = UtilityFunctions.tryGetStringFromJson(hitcher, "status");
                JSONObject pickUpDetails = UtilityFunctions.tryGetJson(hitcher,"pickUp");
                JSONObject dropDetails = UtilityFunctions.tryGetJson(hitcher,"drop");
                String pickUpHour = UtilityFunctions.convertNumberToTime(pickUpDetails.getDouble("time"), timeOffset);
                String pickUpAddress = pickUpDetails.getString("address");
                String dropAddress = dropDetails.getString("address");


                if (convertView == null)
                {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.hitcher, null);
                }

                // get all items
                personName = (TextView) convertView.findViewById(R.id.hitcherFullName);
                pickUpTime = (TextView) convertView.findViewById(R.id.textPickUpTime);
                addressPickUp = (TextView) convertView.findViewById(R.id.textPickUpLocation);
                addressDrop = (TextView) convertView.findViewById(R.id.textViewDropLocation);
                approveButtons = (LinearLayout) convertView.findViewById(R.id.approveButtons);
                picture = (ImageView) convertView.findViewById(R.id.hitcherImage);
                updatePickUpDetails = (Button) convertView.findViewById(R.id.buttonEditHitcherPickUpDetails);
                gotoMessages        = (Button) convertView.findViewById(R.id.buttonGoToMessages);

                // set values
                personName.setText(name);
                addressDrop.setText(dropAddress);
                addressPickUp.setText(pickUpAddress);
                pickUpTime.setText(pickUpHour);


                // today
                Date today = new Date();
                //if today before the start date or
                //   today is after the end date
                if (today.compareTo(rideStartDate) < 0 ||
                    today.compareTo(rideStopDate)  > 0){
                    gotoMessages.setEnabled(false);
                    gotoMessages.setVisibility(View.GONE);
                    updatePickUpDetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateHitchersPickUpDetails(position, pickUpTime, addressPickUp, addressDrop);
                        }
                    });
                    if(today.compareTo(rideStopDate)  > 0) {
                        updatePickUpDetails.setEnabled(false);
                        updatePickUpDetails.setVisibility(View.GONE);
                    }
                }
                else {
                    gotoMessages.setEnabled(true);
                    gotoMessages.setVisibility(View.VISIBLE);
                    updatePickUpDetails.setEnabled(true);
                    updatePickUpDetails.setVisibility(View.VISIBLE);
                    // set listeners
                    updatePickUpDetails.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateHitchersPickUpDetails(position, pickUpTime, addressPickUp, addressDrop);
                        }
                    });
                    gotoMessages.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showMessageDialog(profileId);
                        }
                    });
                }

                if (pictureUrl != null) {
                    try {
                        new ImageLoaderTask(picture, parentActivity, profileId).execute(pictureUrl);
                    } catch (Exception ex) {
                        System.out.println("Failed to retrieve picture url");
                        ex.printStackTrace();
                    }
                }

                picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((RideMainScreen) parentActivity).setUserProfileView(profileId);
                    }
                });

                approveButton = (Button)approveButtons.findViewById(R.id.approveButton);
                disapproveButton = (Button)approveButtons.findViewById(R.id.disapproveButton);

                if (status.equals("waitingForDriverApprovement"))
                {
                    approveButtons.setVisibility(View.VISIBLE);
                    addListenerOnApprove(approveButton,
                            profileId,
                            pickUpTime.getText().toString(),
                            addressPickUp.getText().toString(),
                            addressDrop.getText().toString());
                    addListenerOnDisapprove(disapproveButton, profileId);
                } else
                {
                    approveButtons.setVisibility(View.GONE);
                    approveButton.setVisibility(View.GONE);
                    disapproveButton.setVisibility(View.GONE);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return convertView;
        }


        private void addListenerOnApprove(Button approve,
                                          final String profileId,
                                          final String pickUpTime,
                                          final String pickUpAddress,
                                          final String dropAddress )
        {
            approve.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) { sendToServerDriverApproveHitcher(profileId, pickUpTime, pickUpAddress, dropAddress); }
            });
        }

        private void addListenerOnDisapprove(Button disapprove, final String profileId)
        {
            disapprove.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {sendToServerDriverDispproveHitcher(profileId); }
            });
        }


        private String getHitcherProfileId(int position) {
            JSONObject hitcher = (JSONObject) getItem(position);
            return UtilityFunctions.tryGetStringFromJson(hitcher, "userProfileId");
        }

        private void sendToServerDriverDispproveHitcher(final String profileId) {
            try {
                String path = Params.serverIP + "ride/" + rideId + "/driver/" + profileId + "/disapprove";
                //retrieve pick up hour and location to json

                JSONObject res = new PutExecutor().execute(path, new JSONObject().toString()).get();

                if (res == null) {
                    UtilityFunctions.showMessageInToast(context, "Failed to send request to server. Try again later");
                } else {
                    if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                    {
                        UtilityFunctions.showMessageInToast(context, "Hitcher disapprove was successfully send to server");
                    } else {
                        String message = res.getString("message");
                        UtilityFunctions.showMessageInToast(context, message);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                UtilityFunctions.showMessageInToast(parentActivity, "Error in sending request to server");
            }

            rideJSON = ((RideMainScreen)parentActivity).reloadRideJSON();
            loadHitchers();
            notifyDataSetChanged();
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

    private void updateHitchersPickUpDetails(int position, TextView pickUpTime, TextView addressPickUp, TextView addressDrop) {
        updateDetailsDialog = PickUpDetailsDialogFragment.newInstance(
                position,
                pickUpTime.getText().toString(),
                addressPickUp.getText().toString(),
                addressDrop.getText().toString()
        );
        updateDetailsDialog.setTargetFragment(this, 1);
        updateDetailsDialog.show(getFragmentManager(), "Update Details Dialog");
    }

    private void showMessageDialog(String profileID)
    {
        messagesDialog = MessagesDialog.getInstance(profileID, rideId,true, getResources().getStringArray(R.array.messages), UtilityFunctions.tryGetStringFromJson(rideJSON, "driverProfileId"));
        messagesDialog.show(getFragmentManager(), "Driver");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == PickUpDetailsDialogFragment.RESULT_CODE){
            int position = data.getIntExtra(PickUpDetailsDialogFragment.POSITION, -1);
            JSONObject hitcher = (JSONObject) hitcherListAdapter.getItem(position);
            final String profileId = UtilityFunctions.tryGetStringFromJson(hitcher, "userProfileId");
            String time = data.getStringExtra(PickUpDetailsDialogFragment.TIME);
            String pickupAddr = data.getStringExtra(PickUpDetailsDialogFragment.PICKUP);
            String dropAddr = data.getStringExtra(PickUpDetailsDialogFragment.DROP);
            // change on the client side before sending to server
//            JSONObject pickUpDetails = UtilityFunctions.tryGetJson(hitcher,"pickUp");
//            JSONObject dropDetails = UtilityFunctions.tryGetJson(hitcher,"drop");
//            try {
//                pickUpDetails.put("time", UtilityFunctions.convertTimeToNumber(time));
//                pickUpDetails.put("address", pickupAddr);
//                dropDetails.put("drop", dropAddr);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            hitcherListAdapter.notifyDataSetChanged();
            hitcherListAdapter.sendToServerDriverApproveHitcher(profileId, time, pickupAddr, dropAddr);
        }
    }
}


