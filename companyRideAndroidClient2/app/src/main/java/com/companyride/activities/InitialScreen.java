package com.companyride.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.companyride.R;
import com.companyride.fragments.ExitConfirmationDialog;
import com.companyride.fragments.FeedbackFragment;
import com.companyride.fragments.inital.*;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.services.notifications.RegistrationIntentService;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;


public class InitialScreen extends AppCompatActivity implements SelectViewInterface {
    private CharSequence mTitle;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
//    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final String TAG = "InitialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                SharedPreferences sharedPreferences =
//                        PreferenceManager.getDefaultSharedPreferences(context);
////                boolean sentToken = sharedPreferences
////                        .getBoolean(Params.SENT_TOKEN_TO_SERVER, false);
//            }
//        };


        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            registerToUsersTopics();
        }
        // bypass login if previously logged in
        loginPreferences = getSharedPreferences(Params.PREF_FILE, MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Intent intent = getIntent();
        if (getIntent().getBooleanExtra(getString(R.string.exit), false)) {
            finish();
            return;
        }
        if (intent.getBooleanExtra(getString(R.string.logout), false)) {
            loginPrefsEditor.putBoolean(Params.SAVE_LOGIN, false);
//            loginPrefsEditor.remove(Params.USER_ID);
            loginPrefsEditor.commit();
            AppSharedData.getInstance().setUserId(null);
            registerToUsersTopics();
        }

        setContentView(R.layout.main_screen);

        mTitle = getTitle();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            selectView(getString(R.string.login));
        }
    }

    private void registerToUsersTopics() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        this.startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                new IntentFilter(Params.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        File storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        File file[] = storageDir.listFiles();
        for (int i=0; i < file.length; i++)
        {
            file[i].delete();
        }
        super.onDestroy();
    }

    public void goBackView(){
        FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(0);
        setTitle(backStackEntry.getName());
        getFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if (mTitle.equals(getString(R.string.app_name)))
            exitApp();
        else
            goBackView();
//        int count = getFragmentManager().getBackStackEntryCount();

//        if (count < 2 ) {
//            exitApp();
//        } else {
//            goBackView();
//        }
    }

    /** Swaps fragments in the main content view */
    public void selectView(String viewName, Bundle args) {
        Fragment fragment = null;
        if (viewName.equals(getString(R.string.login))){
            fragment = new LoginFragment();
        }
        else if (viewName.equals(getString(R.string.sign_up))){
            fragment = new SignUpFragment();
        }
        else if(viewName.equals(getString(R.string.account))) {
            fragment = new AccountFragment();
        }
        else{
            return;
        }
        if(args != null)  fragment.setArguments(args);
        setTitle(viewName);
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                                                        .replace(R.id.content_frame, fragment);
        if(!viewName.equals(getString(R.string.login))){
            transaction.addToBackStack(viewName);
        }
        UtilityFunctions.removeSoftInputFromCurrentView(this);
        transaction.commit();
    }

    public void  selectView(String viewName){
        selectView(viewName, null);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void exitApp() {
        int count = getFragmentManager().getBackStackEntryCount();
        Boolean showLogout = false;
        if (count > 0) {
            showLogout = true;
//            FragmentManager.BackStackEntry backStackEntry = getFragmentManager().getBackStackEntryAt(0);
//            if (backStackEntry.getName() != null && backStackEntry.getName().equals(getString(R.string.login)))
//                showLogout = false;
        }
        //show confirmation dialog
        ExitConfirmationDialog dialog = ExitConfirmationDialog.getInstance(showLogout);
        dialog.show(getFragmentManager(), getString(R.string.exit_confirmation));
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Params.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
