package com.companyride.fragments.rideRequest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.companyride.geoLocation.PlaceAutocompleteAdapter;
import com.companyride.http.DeleteExecutor;
import com.companyride.utils.MyJSONArray;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;
import com.companyride.R;
import com.companyride.geoLocation.GoogleApiConnection;
import com.companyride.activities.RideRequestMainScreen;
import com.companyride.fragments.DatePickerFragment;
import com.companyride.fragments.TimePickerFragment;
import com.companyride.interfaces.CurrentLocationInterface;
import com.companyride.http.PostExecutor;
import com.companyride.http.PutExecutor;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class RideRequestFragment extends Fragment implements View.OnClickListener, CurrentLocationInterface{
    private static final String LOG_TAG = "RideRequest";
    private Menu optionsMenu;
    private Activity parentActivity;
    private View rootView;
    private RadioGroup eventType;
    private RadioButton radioOneTime;
    private RadioButton radioWeekly;
    private Button submitButton;
    private Button potRidesButton;
    private EditText rideDateText;
    private EditText stopRideDateText;
    //private TextView stopRideDateLabel;
    private EditText startTime;
    private EditText stopTime;
    private String rideType;
    //private LinearLayout customZone;
    private AutoCompleteTextView fromAddr, toAddr;
    private Spinner maxNumOfHitchers;           //for driver only
    private boolean isDriver;
    private TextView messagesLabel;
    private SeekBar maxDistanceToGoSeekBar;     //for hitcher only
    private EditText maxDistanceToGoEditText;   //for hitcher only
    private boolean disabledMode;
    private HashSet<String> diffKeys;
    private String rideReqId;
    private JSONObject rideReqJSON;
	private ImageButton currentLocation;
    private GoogleApiConnection googleApiConnection;

    // Auto-complete stuff
    private PlaceAutocompleteAdapter mAdapter;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;
    private static final LatLngBounds BOUNDS_ISRAEL = new LatLngBounds(
            new LatLng(29, 34), new LatLng(33, 36));
    private LinearLayout maxDistToGoLayout;
    private LinearLayout maxHitchNumLayout;
    private TextInputLayout loFromAddr;
    private TextInputLayout loToAddr;
    private ImageButton currentLocationTo;
    private boolean fromAddrWaits;
    private boolean toAddrWaits;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.page_ride_request, container, false);
        rideReqId = ((RideRequestMainScreen)parentActivity).getRideRequestId();
        rideReqJSON = ((RideRequestMainScreen)parentActivity).getRideRequestJSON();
        initVariables();
        addListenerOnDateField(rideDateText, "startDate");
        addListenerOnDateField(stopRideDateText, "stopDate");
        addListenerOnTimeField(startTime, "preferredRideTime");
        addListenerOnTimeField(stopTime, "preferredRideTime");
        retrieveEventType();

        eventType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if(checkedId == radioOneTime.getId())
                {
                    //stopRideDateLabel.setVisibility(View.GONE);
                    stopRideDateText.setVisibility(View.GONE);
                }
                else  if(checkedId == radioWeekly.getId())
                {
                    stopRideDateText.setVisibility(View.VISIBLE);
                    //stopRideDateLabel.setVisibility(View.VISIBLE);
                }
                diffKeys.add("eventType");
            }
        });

        googleApiConnection = new GoogleApiConnection(this);
        googleApiConnection.execute();
        // Auto-complete stuff
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        fromAddr = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteFrom);
        toAddr   = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTo);

        // Register a listener that receives callbacks when a suggestion has been selected
        fromAddr.setOnItemClickListener(mAutocompleteClickListener);
        toAddr.setOnItemClickListener(mAutocompleteClickListener);

        // Retrieve the TextViews that will display details and attributions of the selected place.
//        mPlaceDetailsText = (TextView) parentActivity.findViewById(R.id.place_details);
//        mPlaceDetailsAttribution = (TextView) parentActivity.findViewById(R.id.place_attribution);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(parentActivity, googleApiConnection.getGoogleApiClient(), BOUNDS_ISRAEL,
                null);
        fromAddr.setAdapter(mAdapter);
        toAddr.setAdapter(mAdapter);

        submitButton.setOnClickListener(this);
        potRidesButton.setOnClickListener(this);
		currentLocation.setOnClickListener(this);
        currentLocationTo.setOnClickListener(this);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        // fill in the data if called from calendar
        customizeRequestView();
        // add listeners
        addListeners();
        super.onResume();
    }

    @Override
    public void onPause() {
        UtilityFunctions.removeSoftInputFromCurrentView(parentActivity);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.edit_menu, menu);
        optionsMenu = menu;
        if (rideReqId == null) {
            optionsMenu.findItem(R.id.editMenuItem).setVisible(false);
            optionsMenu.findItem(R.id.editMenuCancel).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.editMenuItem:
                makeViewEditableWithSubmitButton();
                break;
            case R.id.editMenuCancel:
                cancelRideRequest();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addListeners() {
        fromAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("from");
            }
        });
        toAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("to");
            }
        });
        if (maxDistanceToGoEditText != null) {
            maxDistanceToGoEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    diffKeys.add("radius");
                }
            });
        }
    }

    private void initVariables()
    {
        fromAddrWaits = false;
        toAddrWaits   = false;
        diffKeys = new HashSet<>();
        diffKeys.add("eventType");

        eventType = (RadioGroup) rootView.findViewById(R.id.radioGroupEventTypes);
        radioOneTime = (RadioButton) rootView.findViewById(R.id.radioOneTime);
        radioWeekly = (RadioButton) rootView.findViewById(R.id.radioWeekly);
        submitButton = (Button) rootView.findViewById(R.id.buttonSubmit);
        potRidesButton = (Button) rootView.findViewById(R.id.potRidesBtn);
        rideDateText = (EditText) rootView.findViewById(R.id.editTextRideDate);
		currentLocation = (ImageButton) rootView.findViewById(R.id.buttonLocationRideRequest);
        currentLocationTo = (ImageButton) rootView.findViewById(R.id.buttonLocationRideRequestTo);
		
        stopRideDateText = (EditText) rootView.findViewById(R.id.editTextStopRideDate);

        //customZone = (LinearLayout)rootView.findViewById(R.id.customZone);
        maxDistToGoLayout = (LinearLayout)rootView.findViewById(R.id.maxDistanceToGo);
        maxHitchNumLayout = (LinearLayout)rootView.findViewById(R.id.maxNumOfHitchers);
        startTime = (EditText) rootView.findViewById(R.id.editStartHour);

        stopTime = (EditText) rootView.findViewById(R.id.editStopHour);

//        loFromAddr = (TextInputLayout) rootView.findViewById(R.id.input_layout_from_req);
//        loFromAddr.setErrorEnabled(true);
//        loFromAddr.setError(getString(R.string.must_have_from_addr));
        fromAddr = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteFrom);
//        fromAddr.setError("Required");

//        loToAddr = (TextInputLayout) rootView.findViewById(R.id.input_layout_to_req);
//        loToAddr.setErrorEnabled(true);
//        loToAddr.setError(getString(R.string.must_have_to_addr));
        toAddr = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTo);
//        toAddr.setError("Required");

        //stopRideDateLabel = (TextView)rootView.findViewById(R.id.textViewStopRideDate);
        messagesLabel = (TextView)rootView.findViewById(R.id.textViewMessage);

        startTime.setFocusable(false);
        startTime.setClickable(true);
        stopTime.setFocusable(false);
        stopTime.setClickable(true);

        rideDateText.setFocusable(false);
        rideDateText.setClickable(true);
        stopRideDateText.setFocusable(false);
        stopRideDateText.setClickable(true);

    }

    private void addListenerOnDateField(final EditText dateField, final String fieldName) {
        dateField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setTextField(dateField);
                newFragment.show(getFragmentManager(), "DatePicker");
                diffKeys.add(fieldName);
            }
        });
    }

    private void addListenerOnTimeField(final EditText timeField, final String fieldName)
    {
        timeField.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setTextField(timeField);
                newFragment.show(getFragmentManager(), "TimePicker");
                diffKeys.add(fieldName);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.buttonSubmit:
            {
                try{
                    clearMessage();
                    if (validateAllFieldsAreFull())
                    {
                        JSONObject json = toJSONObject();
                        if (json != null)
                            sendRideRequestToServer(json);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.potRidesBtn:
            {
                try{
                    clearMessage();
                    ((RideRequestMainScreen)parentActivity).gotoPotentialRides();
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
			case R.id.buttonLocationRideRequest:
            {
                if(UtilityFunctions.isLocationServiciesEnabled(parentActivity)) {
                    //googleApiConnection.execute();
                    fromAddrWaits = true;
                    googleApiConnection.fetchLastKnownLocationFromAPI();
                }
                break;
            }
            case R.id.buttonLocationRideRequestTo:
            {
                if(UtilityFunctions.isLocationServiciesEnabled(parentActivity)) {
                    //googleApiConnection.execute();
                    toAddrWaits = true;
                    googleApiConnection.fetchLastKnownLocationFromAPI();
                }
                break;
            }
        }
    }

    public void showMessageInLabel(String message)
    {
        messagesLabel.setText(message);
        Log.e(LOG_TAG, message);
    }

    public void clearMessage()
    {
        messagesLabel.setText("");
    }


    private void cancelRideRequest() {
        String path = Params.serverIP + Params.rideRequestPath + "/" + rideReqId;
        JSONObject res = null;
        try {
            res = new DeleteExecutor().execute(path).get();
            if (res == null) {
                showMessageInLabel("Failed to send cancel request to server. Try again later");
                return;
            }

            if (res.getInt("code") == 200)
            {
                parentActivity.onBackPressed();
                String message = res.getString("message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
            }
            else
            {
                String message = res.getString("message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
            }
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "Unexpected exception: ");
            ex.printStackTrace();
        }
    }

    private void sendRideRequestToServer(JSONObject json)
    {
        try {
            String path = Params.serverIP + Params.rideRequestPath;
            JSONObject res = null;
            // if the submit is actually update of current request
            if (rideReqId != null) {
                if (diffKeys.size() == 0){
                    UtilityFunctions.showMessageInToast(parentActivity, "Request was not changed!");
                    makeViewReadOnlyWithEditButton();
                    return;
                }
                JSONObject diffJson = new JSONObject();
                for( String key : diffKeys){
                    if (key.equals("startDate")){
                        diffJson.put("weekday",json.get("weekday"));
                    }
                    diffJson.put(key, json.get(key));
                }
                path = path + "/" + rideReqId;
                res = new PutExecutor().execute(path, diffJson.toString()).get();
            }
            // else - it is a new request
            else
                res = new PostExecutor().execute(path, json.toString()).get();
            submitButton.setEnabled(false);

            if (res == null) {
                showMessageInLabel("Failed to send request to server. Try again later");
                submitButton.setEnabled(true);
                return;
            }



            if (res.getInt("code") == 200)
            {
                parentActivity.onBackPressed();
                String message = res.getString("message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
            }
            else
            {
                String message = res.getString("message");
                UtilityFunctions.showMessageInToast(parentActivity,message);
                submitButton.setEnabled(true);
            }
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "Unexpected exception: ");
            ex.printStackTrace();
        }
    }

    private void fromJSONObject(JSONObject json)
    {
        Log.d(LOG_TAG, "Loading from ride request json: " + json.toString());
        long timeOffset = UtilityFunctions.tryGetLongFromJson(json,"timeOffset");
        String fromStr = getAddressFromGeoLocationJSON(UtilityFunctions.tryGetJson(json, "from"));
        if (fromStr == null) {
            UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve coordinates for address in \"From\" field");
        } else {
            fromAddr.setText(fromStr);
            String toStr = getAddressFromGeoLocationJSON(UtilityFunctions.tryGetJson(json, "to"));
            if (toStr == null) {
                UtilityFunctions.showMessageInToast(parentActivity, "Failed to retrieve coordinates for address in \"To\" field");
            } else {
                toAddr.setText(toStr);
                if (isDriver) {
                    maxNumOfHitchers.setSelection(UtilityFunctions.tryGetIntFromJson(json, "maxNumOfHitchers")-1);
                } else {
                    String radius = UtilityFunctions.tryGetStringFromJson(json, "radius");
                    maxDistanceToGoEditText.setText(radius);
                    maxDistanceToGoSeekBar.setProgress(Integer.parseInt(radius));
                }
                if (UtilityFunctions.tryGetStringFromJson(json, "eventType").equals(Params.onTimeEvent))
                    radioOneTime.setChecked(true);
                else
                    radioWeekly.setChecked(true);

                rideDateText.setText(UtilityFunctions.convertFullUTCStringToFormEDTString(UtilityFunctions.tryGetStringFromJson(json, "startDate"), timeOffset));
                if (radioWeekly.isChecked()) {
                    stopRideDateText.setText(UtilityFunctions.convertFullUTCStringToFormEDTString((UtilityFunctions.tryGetStringFromJson(json, "stopDate")), timeOffset));
                }
                JSONObject preferredHoursJson = UtilityFunctions.tryGetJson(json, "preferredRideTime");
                startTime.setText(UtilityFunctions.convertNumberToTime(UtilityFunctions.tryGetDoubleFromJson(preferredHoursJson, "fromHour"), timeOffset));
                stopTime.setText(UtilityFunctions.convertNumberToTime(UtilityFunctions.tryGetDoubleFromJson(preferredHoursJson, "toHour"), timeOffset));
            }
        }
    }

    private void customizeRequestView() {
        if (rideReqId != null){
            // get the request from the server
            fromJSONObject(rideReqJSON);
            makeViewReadOnlyWithEditButton();
        }
        else{
            makeViewEditableWithSubmitButton();
            setValuesFromIntent();
        }
    }

    private void setValuesFromIntent() {
        String date = ((RideRequestMainScreen)parentActivity).getCurrentDateStr();
        if (date != null)
            rideDateText.setText(date);
    }

    private void makeViewReadOnlyWithEditButton() {
        disabledMode = true;
        // disable / change irrelevant stuff
        if (optionsMenu != null) {
            MenuItem item = optionsMenu.findItem(R.id.editMenuItem);
            if(item != null) item.setVisible(true);
        }
        // set autocomplete disable
        fromAddr.setOnClickListener(null);
        toAddr.setOnClickListener(null);
        submitButton.setEnabled(false);
        submitButton.setVisibility(View.GONE);
        // if you are the hitcher and the ride request is not in the past
        if( !isDriver &&
                UtilityFunctions.compareDatesDay(
                        ((RideRequestMainScreen) parentActivity).getCurrentDate(),
                        new Date()
                ) >= 0) {
            potRidesButton.setEnabled(true);
            potRidesButton.setVisibility(View.VISIBLE);
        }
        LinearLayout dataLayout = (LinearLayout) rootView.findViewById(R.id.rideRequestDataLayout);
        disableEnableRecursively(dataLayout, false);
    }

    private void makeViewEditableWithSubmitButton() {
        disabledMode = false;
        // disable / change irrelevant stuff
        if (optionsMenu != null)
            optionsMenu.findItem(R.id.editMenuItem).setVisible(false);
        // set autocomplete enable
        fromAddr.setOnItemClickListener(mAutocompleteClickListener);
        toAddr.setOnItemClickListener(mAutocompleteClickListener);
        submitButton.setEnabled(true);
        submitButton.setVisibility(View.VISIBLE);
        potRidesButton.setEnabled(false);
        potRidesButton.setVisibility(View.GONE);
        LinearLayout dataLayout = (LinearLayout) rootView.findViewById(R.id.rideRequestDataLayout);
        disableEnableRecursively(dataLayout, true);
    }

    private void disableEnableRecursively(ViewGroup group, boolean action){
        for ( int i=0; i < group.getChildCount(); i++){
            View view = group.getChildAt(i);
            if (view instanceof Spinner){
                View selectedView = ((Spinner) view).getSelectedView();
                if (selectedView != null)
                    selectedView.setEnabled(false);
                view.setEnabled(action);
            }
            else if ( view instanceof ViewGroup || view instanceof TextInputLayout ){
                disableEnableRecursively((ViewGroup) view, action);
            }
            else if( view instanceof Button      ||
                     view instanceof EditText    ||
                     view instanceof ProgressBar ||
                     view instanceof AutoCompleteTextView
                    ) {
                view.setEnabled(action);
                //view.setFocusableInTouchMode(action);
                if (view instanceof AutoCompleteTextView
                        || (view instanceof  EditText &&
                            !(     ((EditText) view).getInputType() == InputType.TYPE_CLASS_DATETIME
                                || ((EditText) view).getInputType() == InputType.TYPE_DATETIME_VARIATION_NORMAL
                                || ((EditText) view).getInputType() == InputType.TYPE_DATETIME_VARIATION_DATE
                                || ((EditText) view).getInputType() == InputType.TYPE_DATETIME_VARIATION_TIME
                                || ((EditText) view).getInputType() == (InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE)
                                || ((EditText) view).getInputType() == (InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME)
                            ))) {
                    view.setFocusableInTouchMode(action);
                }
                //view.setFocusable(action);
            }
        }
    }

    private void retrieveEventType()
    {
        Intent  i = parentActivity.getIntent();
        rideType = i.getStringExtra(Params.extraRideType);


        if (rideType.equals(Params.extraRideTypeHitcher))
        {
            parentActivity.setTitle(getString(R.string.hitchRequest));
//            customZone.removeAllViewsInLayout();
//            View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout.maxdistancetogo, null);
//
            maxDistToGoLayout.setVisibility(View.VISIBLE);
            maxHitchNumLayout.setVisibility(View.GONE);
            maxDistanceToGoSeekBar = (SeekBar)rootView.findViewById(R.id.seekBarMaxDistanceToGo);
            maxDistanceToGoEditText = (EditText)rootView.findViewById(R.id.editTextMaxDistanceToGo);
			maxDistanceToGoEditText.setText(Integer.toString(Params.maxDistanceToWalk));
            maxDistanceToGoSeekBar.setProgress(Params.maxDistanceToWalk);

            addListenerToSeekBarMaxDistanceToGo();
            isDriver = false;
        }
        else if (rideType.equals(Params.extraRideTypeDriver))
        {
            parentActivity.setTitle(getString(R.string.driveProposal));
//            customZone.removeAllViewsInLayout();
//            View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout.maxnumofhitchers, null);
//            customZone.addView(view);
            maxDistToGoLayout.setVisibility(View.GONE);
            maxHitchNumLayout.setVisibility(View.VISIBLE);
            maxNumOfHitchers = (Spinner) rootView.findViewById(R.id.spinnerMaxNumOfHitchers);
            maxNumOfHitchers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    diffKeys.add("maxNumOfHitchers");
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            isDriver = true;
        }
    }

    private void addListenerToSeekBarMaxDistanceToGo()
    {
        maxDistanceToGoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                maxDistanceToGoEditText.setText(Integer.toString(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }



    private boolean validateAllFieldsAreFull()
    {
        if (checkIfTextFieldIsEmpty(fromAddr, "\"From\" field can't be empty")) return false;
        if (checkIfTextFieldIsEmpty(toAddr, "\"To\" field can't be empty")) return false;
        if (checkIfTextFieldIsEmpty(rideDateText, "\"Ride date\" field can't be empty")) return false;
        if (radioWeekly.isSelected() && checkIfTextFieldIsEmpty(stopRideDateText, "\"From\" field can't be empty")) return false;
        if (checkIfTextFieldIsEmpty(startTime, "\"Start time\" field can't be empty")) return false;
        if (checkIfTextFieldIsEmpty(stopTime, "\"Stop time\" field can't be empty")) return false;
        // if (checkIfTextFieldIsEmpty(maxDistanceToGoEditText, "\"Maximum distance\" field can't be empty")) return false;

        return true;
    }


    private JSONObject getGeoLocationJSONFromAddress(EditText textField)
    {
        return UtilityFunctions.tryRetrieveAddressFromText(parentActivity, textField.getText().toString());
    }

    private String getAddressFromGeoLocationJSON(JSONObject json)
    {
        String addrStr = null;
        try {
            addrStr = json.getString("address");
        } catch (JSONException e) {
            // todo: handle exception
            e.printStackTrace();
        }
        return addrStr;
    }

    private boolean checkIfTextFieldIsEmpty(EditText field, String textToPrintIfEmpty)
    {
        if(field.getText().toString().trim().equals(""))
        {
            Toast.makeText(parentActivity, textToPrintIfEmpty, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private JSONObject toJSONObject()
    {
        JSONObject json = null;
        try {
            json = new JSONObject();

            JSONObject fromJson = getGeoLocationJSONFromAddress(fromAddr);
            if (fromJson == null) {
                Toast.makeText(parentActivity, "Failed to retrieve coordinates for address in \"From\" field", Toast.LENGTH_LONG).show();
                return null;
            } else {
                JSONObject toJson = getGeoLocationJSONFromAddress(toAddr);
                if (toJson == null) {
                    Toast.makeText(parentActivity, "Failed to retrieve coordinates for address in \"To\" field", Toast.LENGTH_LONG).show();
                    return null;
                } else {
                    if (isDriver) {
                        json.put("rideType", Params.extraRideTypeDriver);
                        json.put("maxNumOfHitchers", maxNumOfHitchers.getSelectedItem().toString());
                    } else {
                        json.put("rideType", Params.extraRideTypeHitcher);
                        json.put("radius", maxDistanceToGoEditText.getText().toString());
                    }
                    json.put("userProfileId", AppSharedData.getInstance().getUserProfileId());
                    json.put("inconvenientUsers", new MyJSONArray());
                    json.put("blockedUsers", AppSharedData.getInstance().getBlockedUsersAsMyJSONArray());
                    if (radioOneTime.isChecked())
                        json.put("eventType", Params.onTimeEvent);
                    else
                        json.put("eventType", Params.weeklyEvent);
                    Date startDate = UtilityFunctions.stringToDate(rideDateText.getText().toString(), Params.formDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(startDate);
                    json.put("weekday",cal.get(Calendar.DAY_OF_WEEK));
                    String startDateStr = UtilityFunctions.convertEDTFormStringToEDTFullString(rideDateText.getText().toString());
                    json.put("startDate", startDateStr);
                    if (radioWeekly.isChecked()) {
                        String stopDateStr = UtilityFunctions.convertEDTFormStringToEDTFullString(stopRideDateText.getText().toString());
                        json.put("stopDate", stopDateStr);
                    }
                    json.put("status", Params.requestType_NEW);
                    json.put("from", fromJson);
                    json.put("to", toJson);
                    JSONObject preferredHoursJson = new JSONObject();
                    preferredHoursJson.put("fromHour", (double)UtilityFunctions.convertTimeToNumber(startTime.getText().toString()));
                    preferredHoursJson.put("toHour", (double)UtilityFunctions.convertTimeToNumber(stopTime.getText().toString()));
                    json.put("preferredRideTime", preferredHoursJson);
                    json.put("timeOffset", UtilityFunctions.getTimeOffset());
                }
            }
        }
        catch (JSONException ex)
        {
            //todo: handle exception
            ex.printStackTrace();
            json = null;
        }
        Log.d(LOG_TAG, "Ride request json created: " + json.toString());
        return json ;
    }

    private void checkAndUpdateWithAddress(String addrStr, final AutoCompleteTextView addrView, Boolean waiting){
        if(addrView.isEnabled() && waiting) {
            addrView.setText(addrStr);
            addrView.post(new Runnable() {
                public void run() {
                    addrView.dismissDropDown();
                }
            });
        }
    }

	@Override
    public void onCurrentLocationReady() {
        LatLng lastKnownLocation = googleApiConnection.getLastKnownLocation();
        if (lastKnownLocation != null) {
            String address = UtilityFunctions.tryRetrieveAddressFromLangLat(parentActivity,
                        lastKnownLocation);
            checkAndUpdateWithAddress(address, fromAddr, fromAddrWaits);
            fromAddrWaits = false;
            checkAndUpdateWithAddress(address, toAddr, toAddrWaits);
            toAddrWaits = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //do nothing
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(LOG_TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
//                    .getPlaceById(googleApiConnection.getGoogleApiClient(), placeId);
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
//
//            Toast.makeText(parentActivity.getApplicationContext(), "Clicked: " + primaryText,
//                    Toast.LENGTH_SHORT).show();
//            Log.i(LOG_TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

//    /**
//     * Callback for results from a Places Geo Data API query that shows the first place result in
//     * the details view on screen.
//     */
//    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
//            = new ResultCallback<PlaceBuffer>() {
//        @Override
//        public void onResult(PlaceBuffer places) {
//            if (!places.getStatus().isSuccess()) {
//                // Request did not complete successfully
//                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
//                places.release();
//                return;
//            }
//            // Get the Place object from the buffer.
//            final Place place = places.get(0);
//
//            // Format details of the place for display and show it in a TextView.
//            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                    place.getId(), place.getAddress(), place.getPhoneNumber(),
//                    place.getWebsiteUri()));
//
//            // Display the third party attributions if set.
//            final CharSequence thirdPartyAttribution = places.getAttributions();
//            if (thirdPartyAttribution == null) {
//                mPlaceDetailsAttribution.setVisibility(View.GONE);
//            } else {
//                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
//            }
//
//            Log.i(LOG_TAG, "Place details received: " + place.getName());
//
//            places.release();
//        }
//    };
//
//    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
//                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
//        Log.e(LOG_TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
//                websiteUri));
//        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
//                websiteUri));
//
//    }
}


