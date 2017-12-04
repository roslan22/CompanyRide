package com.companyride.activities;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.*;
import com.companyride.R;
import com.companyride.fragments.calendar.CalendarFragment;
import com.companyride.fragments.ride.DriverRideFragment;
import com.companyride.fragments.rideRequest.PotentialRidesFragment;
import com.companyride.fragments.rideRequest.RideRequestFragment;
import com.companyride.http.GetExecutor;
import com.companyride.http.HTTPManager;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by Valeriy on 3/28/2015.
 */
public class RideRequestMainScreen extends AppCompatActivity implements SelectViewInterface {
    private CharSequence mTitle;
    private JSONObject rideReqJSON;
    private String rideReqId;
    private String mode;
    private String currentDate;
    private Fragment currFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_req_screen);
        loadExtraFromCallingActivity();
        selectView(mode);
        mTitle = getTitle();
        // Customize the action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count < 2 ) {
            finish();
        } else {
            goBackView();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public String getCurrentDateStr(){
        return currentDate;
    }

    public Date getCurrentDate(){
        return UtilityFunctions.stringToDate(currentDate, Params.formDateJS);
    }

    public void goBackView(){
        FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(0);
        setTitle(backStackEntry.getName());
        getFragmentManager().popBackStack();
    }

    @Override
    public void selectView(String viewName, Bundle args) {
        Fragment fragment = null;
        if (viewName.equals(getString(R.string.rideRequestTitle)))
        {
            fragment = new RideRequestFragment();
        }
        else if(viewName.equals(getString(R.string.potRidesTitle)))
        {
            fragment = new PotentialRidesFragment();
        }
        else{
            return;
        }
        currFragment = fragment;
        if(args != null) fragment.setArguments(args);
        //add flag show messages then showView
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.ride_req_frame, fragment)
                .addToBackStack(viewName);
        UtilityFunctions.removeSoftInputFromCurrentView(this);
        transaction.commit();
        setTitle(viewName);
    }

    public void selectView(String viewName){
        selectView(viewName, null);
    }

    private void loadExtraFromCallingActivity() {
        Intent i = getIntent();
        String callInActivity = i.getStringExtra(Params.extraCallingActivity);
        String date =i.getStringExtra(Params.extraDate);
        if (date != null){
            currentDate = date;
        }
        // if called from calendar
        if (callInActivity !=null && callInActivity.equals(CalendarFragment.class.toString())){
            // get the request from DB
            rideReqId = i.getStringExtra(Params.extraID);
            if (rideReqId != null)
                // get the request from the server
                loadRideRequestFromServer(rideReqId);
        }
        if(callInActivity ==null || callInActivity.equals(CalendarFragment.class.toString())){
            mode = getString(R.string.rideRequestTitle);
        }
        else{
            mode = getString(R.string.potRidesTitle);
        }
    }

    public void loadRideRequestFromServer(String id)
    {
        String path = Params.serverIP + Params.rideRequestPath + "/" + id;
        JSONObject res = UtilityFunctions.tryGetJsonFromServer(path);
        if (res == null) {
            UtilityFunctions.showMessageInToast(getApplicationContext(), "Failed to get request from server. Try again later");
            return;
        }
        // fill in the data
        try {
            rideReqJSON = res.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getRideRequestId() {
        return rideReqId;
    }

    public JSONObject getRideRequestJSON() {
        return rideReqJSON;
    }

    public void gotoPotentialRides() {
        selectView(getString(R.string.potRidesTitle));
    }

    public void gotoRideRequest() {
        selectView(getString(R.string.rideRequestTitle));
    }
}
