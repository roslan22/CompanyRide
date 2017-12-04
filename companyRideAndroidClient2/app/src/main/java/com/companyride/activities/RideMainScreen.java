package com.companyride.activities;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.*;
import android.util.Log;
import com.companyride.R;
import com.companyride.fragments.UserProfileFragment;
import com.companyride.fragments.ride.DriverRideFragment;
import com.companyride.fragments.ride.HitcherRideFragment;
import com.companyride.fragments.ride.MessagesRideFragment;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;

public class RideMainScreen extends AppCompatActivity implements SelectViewInterface{
    private CharSequence mTitle;
    private String rideId;
    private JSONObject rideJSON;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_screen);
        // load ride
        loadRide();
        // which fragment to show
        defineRideMode();
        // set view for appropriate fragment
        selectView(mode);
        mTitle = getTitle();
        // Customize the action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void loadRide()
    {
        if(rideId == null) {
            Intent i = getIntent();
            rideId = i.getStringExtra(Params.extraID);
        }
        // Load ride from server
        JSONObject res = getRideFromServer(rideId);

        if (res != null)
        {
            JSONObject data = UtilityFunctions.tryGetJson(res, "data");
            if (data != null) rideJSON = data;
            else rideJSON = null;
        }
        else
        {
            rideJSON = null;
            String message = UtilityFunctions.tryGetStringFromJson(res, "message");
            if (message != null && !message.isEmpty())
                UtilityFunctions.showMessageInToast(getBaseContext(), message);
        }
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

    public void goBackView(){
        FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(0);
        setTitle(backStackEntry.getName());
        getFragmentManager().popBackStack();
    }

    private JSONObject getRideFromServer(String rideId)
    {
        String path = Params.serverIP + "ride/" + rideId;
        JSONObject res = UtilityFunctions.tryGetJsonFromServer(path);
        return res;
    }

    private void defineRideMode()
    {
        String  driverProfileId = UtilityFunctions.tryGetStringFromJson(rideJSON, "driverProfileId");

        if (driverProfileId != null)
        {
            if (driverProfileId.equals(AppSharedData.getInstance().getUserProfileId())) mode = getString(R.string.driver_ride);
            else if (!driverProfileId.equals(null)) mode = getString(R.string.hitcher_ride);
        }
        else
        {
            mode = null;
            Log.d("Ride", "Failed to load driver profile id");
        }
    }


    public void setUserProfileView(String userProfileId)
    {
        Bundle args = new Bundle();
        args.putString(Params.extraUserProfileId, userProfileId);
        selectView(getString(R.string.user_profile), args);
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public String getRideId() {
        return rideId;
    }

    public JSONObject getRideJSON() {
        return rideJSON;
    }

    public JSONObject reloadRideJSON(){
        loadRide();
        return rideJSON;
    }

    @Override
    public void selectView(String viewName, Bundle args) {
        Fragment fragment = null;
        if (viewName.equals(getString(R.string.driver_ride)))
        {
            fragment = new DriverRideFragment();
        }
        else if(viewName.equals(getString(R.string.hitcher_ride)))
        {
            fragment = new HitcherRideFragment();
        }
        else if(viewName.equals(getString(R.string.messages)))
        {
            fragment = new MessagesRideFragment();
        }
        else if( viewName.equals(getString(R.string.user_profile))) {
            fragment = new UserProfileFragment();
        }
        else{
            return;
        }
        if(args != null) fragment.setArguments(args);
        //add flag show messages then showView
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.ride_frame, fragment)
                .addToBackStack(viewName);
        UtilityFunctions.removeSoftInputFromCurrentView(this);
        transaction.commit();
        setTitle(viewName);
    }

    public void selectView(String viewName){
        selectView(viewName, null);
    }
}
