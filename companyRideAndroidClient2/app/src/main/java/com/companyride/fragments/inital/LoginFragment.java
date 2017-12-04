package com.companyride.fragments.inital;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.companyride.R;
import com.companyride.activities.InitialScreen;
import com.companyride.activities.MainScreen;
import com.companyride.http.GetExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.services.notifications.RegistrationIntentService;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;

import com.companyride.utils.UtilityFunctions;
import org.json.JSONObject;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private AppCompatActivity parentActivity;
    private View rootView;

    private Button btnLogin;
    private Button btnSignUp;
    private TextView tvError;
    private EditText etUserName;
    private EditText etPass;
    private String path = Params.serverIP + "login/";
    private GetExecutor getExecutor;
    private AppSharedData sharedData;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private boolean saveLogin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setTitle(getString(R.string.app_name));
        rootView = inflater.inflate(R.layout.page_login, container, false);

        btnLogin = (Button)rootView.findViewById(R.id.btnLogin);
        btnSignUp = (Button)rootView.findViewById(R.id.buttonSignUp);
        tvError = (TextView)rootView.findViewById(R.id.tvError);
        sharedData = AppSharedData.getInstance();
        etUserName = (EditText)rootView.findViewById(R.id.etLoginUser);
        etPass = (EditText)rootView.findViewById(R.id.etLoginPass);
        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        // bypass login if previously logged in
        loginPreferences = parentActivity.getSharedPreferences(Params.PREF_FILE, parentActivity.MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean(Params.SAVE_LOGIN, false);
        if (saveLogin == true) {
            etUserName.setText(loginPreferences.getString(Params.USERNAME, ""));
            etPass.setText(loginPreferences.getString(Params.PASSWORD, ""));
            btnLogin.performClick();
        }

        // DEBUG PURPOSES ONLY
        // TODO: remove this before submission
        Button lucyBtn = (Button) rootView.findViewById(R.id.buttonLucy);
        Button samBtn = (Button) rootView.findViewById(R.id.buttonSam);
        Button michaelBtn = (Button) rootView.findViewById(R.id.buttonMichael);
        Button emmaBtn = (Button) rootView.findViewById(R.id.buttonEmma);
        lucyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserName.setText("lucy.miller@google.com");
                etPass.setText("123456");
            }
        });
        samBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserName.setText("sam.williams@yahoo.com");
                etPass.setText("123456");
            }
        });
        samBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserName.setText("sam.williams@yahoo.com");
                etPass.setText("123456");
            }
        });
        michaelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserName.setText("michael.taylor@amazon.com");
                etPass.setText("123456");
            }
        });
        emmaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etUserName.setText("emma.anderson@amexpress.com");
                etPass.setText("123456");
            }
        });

        setHasOptionsMenu(true);
        ActionBar actionBar = parentActivity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.login_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.to_exit:
                ((InitialScreen)parentActivity).exitApp();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
            {
                if (etUserName.getText().toString().isEmpty())
                    UtilityFunctions.showMessageInToast(parentActivity, "Please fill username.");
                else if (etPass.getText().toString().isEmpty())
                    UtilityFunctions.showMessageInToast(parentActivity, "Please fill password.");
                else
                    verifyUser();
                    loginPrefsEditor.putString(Params.USERNAME, etUserName.getText().toString());
                    loginPrefsEditor.putString(Params.PASSWORD, etPass.getText().toString());
                    loginPrefsEditor.putBoolean(Params.SAVE_LOGIN, true);
//                    loginPrefsEditor.putString(Params.USER_ID, AppSharedData.getInstance().getUserId());
                    loginPrefsEditor.commit();
                break;
            }
            case R.id.buttonSignUp: {
                ((SelectViewInterface)parentActivity).selectView(getString(R.string.sign_up));
                break;
            }
        }
    }

    private void verifyUser()
    {
        StringBuilder newPathSb = new StringBuilder();

        newPathSb.append(path);
        newPathSb.append(etUserName.getText().toString());
        newPathSb.append('/');
        newPathSb.append(etPass.getText().toString());

        try {
            getExecutor = new GetExecutor();
            JSONObject res =  getExecutor.execute(newPathSb.toString()).get();

            if (res == null)
            {
                showMessageInLabel("Failed to send request to server. Try again later");
            }
            else
            {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    routeLoggedInUser(res.getJSONObject("data"));
                }
                else
                {
                    String message = res.getString("message");
                    showMessageInLabel(message);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Failed to retrieve data");
            e.printStackTrace();
        }
    }


    private void showMessageInLabel(String message)
    {
    tvError.setText(message);
    }

    private void routeLoggedInUser(JSONObject res)
    {
        try
        {
            String userId = res.getString("_id");
            sharedData.setUserId(userId);
            // start topics registration service
            registerToUsersTopics();
            if (res.has("userProfileId"))
            {
                String userProfileId = res.getString("userProfileId");
                AppSharedData.getInstance().setUserProfileId(userProfileId);
                sharedData.setUserProfileId(userProfileId);
                goToUserProfileScreen(); // have profile and userID
            }
            else
            {
                sharedData.setUserProfileId(null);
                goToUserInfoScreen(); //don't have profile yet
            }

            Toast.makeText(parentActivity, "Loading..." , Toast.LENGTH_LONG).show();
        }
        catch (Exception ex)
        {
            System.out.println("Failed to retrieve json data");
            ex.printStackTrace();
        }
    }

    private void registerToUsersTopics() {
        Intent intent = new Intent(parentActivity, RegistrationIntentService.class);
        parentActivity.startService(intent);
    }

    private void goToUserInfoScreen()
    {
        ((SelectViewInterface)parentActivity).selectView(getString(R.string.account));
    }

    private void goToUserProfileScreen()
    {
        try {
            Intent intent = new Intent(parentActivity, MainScreen.class);
            startActivity(intent);
            //finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
