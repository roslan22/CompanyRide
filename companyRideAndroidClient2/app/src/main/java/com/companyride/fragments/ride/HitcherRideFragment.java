package com.companyride.fragments.ride;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.companyride.R;
import com.companyride.activities.Map;
import com.companyride.activities.RideMainScreen;
import com.companyride.fragments.MessagesDialog;
import com.companyride.http.ImageLoaderTask;
import com.companyride.http.PutExecutor;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

public class HitcherRideFragment extends Fragment {
    private Activity parentActivity;
    private View rootView;
    private LinearLayout driverContainer;
    private TextView fullName;
    private TextView occupation;
    private ImageView profilePic;
    private String rideId;
    private JSONObject rideJSON;
    private JSONObject hitcherJSON;
    private TextView textViewMessagesTitle;
    private TextView textViewMessage;
    private TextView textViewFrom;
    private TextView textViewTo;
    private TextView textViewType;
    private TextView textViewTime;
    private TextView startsFrom;
    private TextView endsOn;
    private ListView listViewMessages;
    private  LinearLayout rideDetailsContainer;
    private  LinearLayout approveButtonsContainer;
    private TextView driverMessagesToHitcher;
    private AppSharedData sharedData = AppSharedData.getInstance();
    private String locationsJsonStrForMap;
    boolean driverVisibility = true;
    private static final int MESSAGES_VIEW = 3;
    private TextView message;
    private Button goToMessages;
    private MessagesDialog messagesDialog;
    private String status;
    private String dateStr;
    private Date rideStartDate;
    private Date rideStopDate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rideId = ((RideMainScreen)parentActivity).getRideId();
        rideJSON = ((RideMainScreen)parentActivity).getRideJSON();
        rootView = inflater.inflate(R.layout.page_ride_hitcher, container, false);
        initVariables();
        if (rideJSON != null)
        {
            loadHitcherDetails();
            prepareLocationsJsonForMap();
            loadRideDetails();
            loadMessageFromStatusAndButtonsIfNeeded();
            loadDriver();
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
        setHasOptionsMenu(true);
        return rootView;
    }

    private void prepareLocationsJsonForMap()
    {
        try
        {
            JSONObject locations = new JSONObject();

            locations.put("from", hitcherJSON.getJSONObject("pickUp"));
            locations.put("to", hitcherJSON.getJSONObject("drop"));

            locationsJsonStrForMap = locations.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    //finds information about hitcher that runs application
    private void loadHitcherDetails()
    {
        try {
            String userProfileId = sharedData.getUserProfileId();

            MyJSONArray hitchers = UtilityFunctions.tryGetMyJSONArray(rideJSON, "hitchers");

            if (hitchers != null) {
                JSONObject hitcher;
                for (int i = 0; i < hitchers.length(); i++) {
                    hitcher = (JSONObject) hitchers.get(i);
                    if (hitcher != null) {
                        String hitcherProfileId = UtilityFunctions.tryGetStringFromJson(hitcher, "userProfileId");
                        if (hitcherProfileId != null)
                            if (hitcherProfileId.equals(userProfileId))
                                hitcherJSON = hitcher;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
//        inflater.inflate(R.menu.ride_menu_hitcher, menu);
        inflater.inflate(R.menu.ride_menu_hitcher, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.goToMesView:
                ((RideMainScreen)parentActivity).selectView(getString(R.string.messages));
                break;
            case R.id.goToMapView:
                try {
                    if (locationsJsonStrForMap == null || locationsJsonStrForMap.isEmpty())
                    {
                        UtilityFunctions.showMessageInToast(parentActivity, "Locations failed to load, map cannot be shown");
                    }
                    else
                    {
                        Intent intent = new Intent(parentActivity, Map.class);
                        intent.putExtra(Params.extraLocations, locationsJsonStrForMap);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void initVariables() {
        textViewMessage = (TextView) rootView.findViewById(R.id.textViewMessage);
        listViewMessages = (ListView) rootView.findViewById(R.id.listViewMessages);
        rideDetailsContainer = (LinearLayout) rootView.findViewById(R.id.rideDetailsContainer);
        approveButtonsContainer = (LinearLayout) rootView.findViewById(R.id.approveButtons);
        driverContainer = (LinearLayout) rootView.findViewById(R.id.driverContainer);
        driverMessagesToHitcher = (TextView) rootView.findViewById(R.id.tvDriverMsgsToHitcher);
        initDetailsView();
    }

    private void initDriverView() {
        View view = LayoutInflater.from(parentActivity).inflate(R.layout.driver_part, null);
        driverContainer.addView(view);
        fullName = (TextView) view.findViewById(R.id.textFullName);
        occupation = (TextView) view.findViewById(R.id.textOccupation);
        profilePic = (ImageView) view.findViewById(R.id.imageProfile);
        goToMessages = (Button) view.findViewById(R.id.buttonSendMessageDriver);
    }

    private void initDetailsView()
    {
        textViewFrom = (TextView) rootView.findViewById(R.id.textViewFrom);
        textViewTo = (TextView) rootView.findViewById(R.id.textViewTo);
        textViewType = (TextView) rootView.findViewById(R.id.textViewType);
        textViewTime = (TextView) rootView.findViewById(R.id.textViewTime);
        startsFrom   = (TextView) rootView.findViewById(R.id.startsFromText);
        endsOn       = (TextView) rootView.findViewById(R.id.endsOnText);
    }

    private void loadDriver()
    {
        if (driverVisibility == true)
        {
            initDriverView();
            final String driverProfileId = UtilityFunctions.tryGetStringFromJson(rideJSON, "driverProfileId");
            String driverFullName = UtilityFunctions.tryGetStringFromJson(rideJSON, "driverFullName");
            String driverOccupationTitle = UtilityFunctions.tryGetStringFromJson(rideJSON, "driverOccupationTitle");
            String driverPictureUrl = UtilityFunctions.buildPictureProfileUrl(driverProfileId);
            String driverMessage = getMessagesFromJson(hitcherJSON);
            driverMessagesToHitcher.setText(driverMessage);
            Long timeOffset = UtilityFunctions.tryGetLongFromJson(rideJSON, "timeOffset");
            String startDate     = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "startDate"),
                    timeOffset
            );
            String stopDate      = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "stopDate"),
                    timeOffset
            );
            try {
                rideStartDate = Params.formDate.parse(startDate);
                rideStopDate = Params.formDate.parse(stopDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // today
            Date today = new Date();
            //if today before the start date or
            //   today is after the end date
            if (today.compareTo(rideStartDate) < 0 ||
                    today.compareTo(rideStopDate)  > 0){
                goToMessages.setEnabled(false);
                goToMessages.setVisibility(View.GONE);
            }
            else {
                goToMessages.setEnabled(true);
                goToMessages.setVisibility(View.VISIBLE);
                goToMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String hitcherId;
                        hitcherId = UtilityFunctions.tryGetStringFromJson(hitcherJSON,"userProfileId");
                        showMessageDialog(hitcherId);
                    }
                });
            }

            if (driverFullName != null) fullName.setText(driverFullName);
            else fullName.setText("");

            if (driverOccupationTitle != null) occupation.setText(driverOccupationTitle);
            else occupation.setText("");

            if (driverPictureUrl != null) {
                try {
                    new ImageLoaderTask(profilePic, parentActivity, driverProfileId).execute(driverPictureUrl);
                } catch (Exception ex) {
                    System.out.println("Failed to retrieve picture url");
                    ex.printStackTrace();
                }
            }

            if (driverProfileId != null) {
                driverContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((RideMainScreen) parentActivity).setUserProfileView(driverProfileId);
                    }
                });
            }
        }
    }

    private void loadRideDetails()
    {
        try
        {
            JSONObject pickUpJSON = hitcherJSON.getJSONObject("pickUp");
            JSONObject dropJSON = hitcherJSON.getJSONObject("drop");

            long timeOffset = UtilityFunctions.tryGetLongFromJson(hitcherJSON, "timeOffset");
            String pickUpAddress = UtilityFunctions.tryGetStringFromJson(pickUpJSON,"address");
            String dropAddress =  UtilityFunctions.tryGetStringFromJson(dropJSON,"address");
            String time = UtilityFunctions.tryGetStringFromJson(pickUpJSON,"time");
            String eventType = UtilityFunctions.tryGetStringFromJson(rideJSON,"eventType");

            String startDate     = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "startDate"),
                    timeOffset
            );
            String stopDate      = UtilityFunctions.convertFullUTCStringToFormEDTString(
                    UtilityFunctions.tryGetStringFromJson(rideJSON, "stopDate"),
                    timeOffset
            );

            textViewFrom.setText(pickUpAddress);
            textViewTo.setText(dropAddress);
            textViewTime.setText( UtilityFunctions.convertNumberToTime(Double.valueOf(time), timeOffset));
            textViewType.setText(eventType);
            startsFrom.setText(startDate);
            if (eventType.equals(Params.onTimeEvent))
                stopDate = startDate;
            endsOn.setText(stopDate);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve json location");
            ex.printStackTrace();
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve ride directions");
        }
    }

    private void loadMessageFromStatusAndButtonsIfNeeded()
    {
        try {
            if(status != null && status.equals("approved")){
                return;
            }
            status = (String) UtilityFunctions.tryGetStringFromJson(hitcherJSON, "status");
            message = (TextView) rootView.findViewById(R.id.textViewMessage);

            if (status.equals("waitingForHitcherApprovement"))
            {
                message.setVisibility(View.VISIBLE);
                loadApproveButtons();
                message.setText(parentActivity.getString(R.string.please_approve_pick_up));
            }
            if (status.equals("inPickUpDropDetailsApprove"))
            {
                message.setVisibility(View.VISIBLE);
                loadApproveButtons();
                message.setText(R.string.please_approve_pick_up);
            }
            else if (status.equals("waitingForDriverApprovement"))
            {
                message.setVisibility(View.VISIBLE);
                driverVisibility = false;
                message.setText("Please be patient. Driver need to approve a ride first");
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private void loadApproveButtons()
    {
        try
        {
            LinearLayout buttonsContainer = (LinearLayout)rootView.findViewById(R.id.approveButtons);
            buttonsContainer.setVisibility(View.VISIBLE);
            Button approve = (Button) rootView.findViewById(R.id.approveButton);
            Button disapprove = (Button) rootView.findViewById(R.id.disapproveButton);
            setOnClickListenerOnApprove(approve);
            setOnClickListenerOnDisapprove(disapprove);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void setOnClickListenerOnDisapprove( Button approve)
    {
        approve.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View view)
               {
                   sendToServerHitcherDecision("disapprove");
               }
           }
        );
    }

    private void setOnClickListenerOnApprove( Button approve)
    {
        approve.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View view)
               {
                   String status = (String) UtilityFunctions.tryGetStringFromJson(hitcherJSON, "status");

                   if (status.equals("waitingForHitcherApprovement"))
                   {
                       sendToServerHitcherDecision("approve");
                   }
                   else if (status.equals("inPickUpDropDetailsApprove"))
                   {
                      sendToServerHitcherPickUpApprove();
                   }
               }
           }
        );
    }

    private void sendToServer(String path)
    {
        try
        {
            JSONObject res = new PutExecutor().execute(path, new JSONObject().toString()).get();

            if (res == null) {
                UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), "Failed to send request to server. Try again later");
            } else {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    String message = res.getString("message");
                    UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), message);
                } else {
                    String message = res.getString("message");
                    UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), message);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            UtilityFunctions.showMessageInToast(parentActivity, "Error in sending request to server");
        }
        status = "approved";
        approveButtonsContainer.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
    }


   
//    private void loadMessagesIfNeeded()
//    {
//        try
//        {
//           JSONObject messages =  UtilityFunctions.tryGetJson(hitcherJSON, "messages");
//           JSONArray receivedMessages = UtilityFunctions.tryGetJsonArray(messages, "received");
//
//           if (messages == null || receivedMessages.length() == 0)
//               textViewMessagesTitle.setVisibility(View.GONE);
//        }
//        catch(Exception ex)
//        {
//            ex.printStackTrace();
//        }
//    }
    private void sendToServerHitcherDecision(String approveDecision) {
            String path = Params.serverIP + "ride/" + rideId + "/hitcher/" + sharedData.getUserProfileId() + "/" + approveDecision;
            //retrieve pick up hour and location to json
        sendToServer(path);

    }


    private void sendToServerHitcherPickUpApprove() {
        String path = Params.serverIP + "ride/" + rideId + "/hitcher/" + sharedData.getUserProfileId() + "/pickUpDropDetails/approve";
        //retrieve pick up hour and location to json
        sendToServer(path);

    }


    private String getMessagesFromJson(JSONObject obj)
    {
        JSONObject messagesObj;
        JSONObject receivedMessages;
        MyJSONArray mesArr;
        StringBuilder str = new StringBuilder();

        messagesObj = UtilityFunctions.tryGetJson(obj,"messages");
        mesArr = UtilityFunctions.tryGetMyJSONArray(messagesObj,"received");
        try {
//            str.append("Messages:\n");
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

    private void showMessageDialog(String profileID)
    {
        messagesDialog = MessagesDialog.getInstance(profileID, rideId, false, getResources().getStringArray(R.array.messages), null);
        messagesDialog.show(getFragmentManager(), "Hitcher");
    }

}


