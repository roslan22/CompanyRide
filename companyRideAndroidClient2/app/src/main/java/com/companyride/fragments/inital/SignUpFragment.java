package com.companyride.fragments.inital;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.companyride.R;
import com.companyride.http.PostExecutor;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Created by Ruslan on 28-Mar-15.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener{
    private AppCompatActivity parentActivity;
    private View rootView;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etCompanyName;
    private EditText etUrlPic;
    private EditText etPosition;
    private EditText etCorpEmail;
    private EditText etPass;
    private EditText etPass2;
    private Button btnSignUp;
    private TextView tvMessageSignUp;
    private Boolean calledWithUser;
    private JSONObject profileJSON;
    private HashSet<String> diffKeys;
    private boolean changePass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setTitle(getString(R.string.sign_up));
        rootView = inflater.inflate(R.layout.page_singup, container, false);

        btnSignUp = (Button)rootView.findViewById(R.id.btSignUp);
        btnSignUp.setOnClickListener(this);
        Bundle args = getArguments();

        loadDataIntoVariables();
        calledWithUser = false;
        if(args != null) try {
            calledWithUser = true;
            parentActivity.setTitle(getString(R.string.edit_account));
            profileJSON = new JSONObject(args.getString(Params.argsJSON));
            fillFieldsFromJSON();
        } catch (JSONException e) {
            //todo: handle exception
            e.printStackTrace();
        }

        setHasOptionsMenu(true);
        // Customize the action bar
        ActionBar actionBar = parentActivity.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        addListeners();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void addListeners() {
        diffKeys = new HashSet<>();
        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("firstName");
            }
        });
        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("lastName");
            }
        });
        etCompanyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("companyName");
            }
        });
        etCorpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("professionalEmail");
            }
        });
        etPosition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                diffKeys.add("occupation");
            }
        });
        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                changePass = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                ((SelectViewInterface)parentActivity).goBackView();
                //((SelectViewInterface)parentActivity).selectView(getString(R.string.account));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btSignUp:
            {
                if(verifyInputData()) sendInputData();
                 break;
            }
        }
    }

    private void loadDataIntoVariables()
    {
        etFirstName = (EditText)    rootView.findViewById(R.id.etName);
        etLastName = (EditText)     rootView.findViewById(R.id.etLastName);
        etCompanyName = (EditText)  rootView.findViewById(R.id.etCompanyName);
        etPosition = (EditText)     rootView.findViewById(R.id.etPosition);
        etCorpEmail = (EditText)    rootView.findViewById(R.id.etEmail);
        etPass = (EditText)         rootView.findViewById(R.id.etPassword);
        etPass2 = (EditText)        rootView.findViewById(R.id.etPass2);
        tvMessageSignUp = (TextView)rootView.findViewById(R.id.tvMessageSignUp);
    }

    private boolean verifyInputData()
    {
       if (etFirstName.getText().toString().trim().equals(""))
       {
           UtilityFunctions.showMessageInToast(parentActivity, "Please fill First name.");
           return false;
       }
       else if(etLastName.getText().toString().trim().equals(""))
       {
           UtilityFunctions.showMessageInToast(parentActivity, "Please fill Last name.");
           return false;
       }
//       else if(etCompanyName.getText().toString().equals(""))
//       {
//           UtilityFunctions.showMessageInToast(parentActivity, "Please fill name of company you work at.");
//           return false;
//       }
//       else if(etPosition.getText().toString().trim().equals(""))
//       {
//           UtilityFunctions.showMessageInToast(parentActivity, "Please fill your position at the company.");
//           return false;
//       }
       else if(etCorpEmail.getText().toString().trim().equals(""))
       {
            UtilityFunctions.showMessageInToast( parentActivity, "Please fill corporate email.");
            return false;
       }
       else if(etPass.getText().toString().trim().equals(""))
       {
           if (calledWithUser) return true;
           UtilityFunctions.showMessageInToast(parentActivity, "Please fill password.");
           return false;
       }
       else if(etPass2.getText().toString().trim().equals(""))
       {
           if (calledWithUser) return true;
           UtilityFunctions.showMessageInToast(parentActivity, "Please fill the password.");
           return false;
       }
       else if(!etPass.getText().toString().equals(etPass2.getText().toString()))
       {
           showMessageInLabel("Passwords don't match!");
           return false;
       }
       else
           return true;
    }

    private void showMessageInLabel(String message)
    {
        tvMessageSignUp.setText(message);
    }

    private void sendInputData()
    {
        JSONObject json = getJSONFromFields();
        JSONObject res;
        if (json != null)
        {
            try
            {
                String path = Params.serverIP + "user/";
                if (calledWithUser) {
                    path = path + profileJSON.getString("_id");
                    res = new PutExecutor().execute(path, json.toString()).get();
                }
                else{
                    res = new PostExecutor().execute(path, json.toString()).get();
                }

                if (res == null)
                {
                    showMessageInLabel("Failed to send request to server. Try again later");
                }
                else
                {
                    if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                    {
                        if (calledWithUser)
                            routeUserToUserPage(profileJSON.getString("_id"), "User data was successfully updated!");
                        else
                            routeUserToUserPage(res.getJSONObject("data").getString("userId"), "Verification email was sent to user email!");
                    }
                    else
                    {
                        String message = res.getString("message");
                        showMessageInLabel(message);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                UtilityFunctions.showMessageInToast(parentActivity,"Error in sending request to server");
            }
        }
    }



    private void routeUserToUserPage(String userId, String message)
    {
        Bundle args = new Bundle();
        args.putString(Params.argsMessage, message);
        AppSharedData.getInstance().setUserId(userId);

        //parentActivity.getFragmentManager().popBackStack();
        ((SelectViewInterface)parentActivity).selectView(getString(R.string.account), args);
    }

    private JSONObject getJSONFromFields()
    {
        JSONObject res = new JSONObject();
        JSONObject diffJson = null;
        try
        {
            if (diffKeys.size() == 0 && !changePass){
                showMessageInLabel( "Nothing was changed!");
                return null;
            }
            if(diffKeys.size() > 0 && !changePass){
                showMessageInLabel("Password required to set\\update account!");
                return null;
            }
            else if(diffKeys.size() > 0 && changePass){
                res.put("password", etPass.getText().toString());
            }

            res.put("firstName", etFirstName.getText().toString());
            res.put("lastName", etLastName.getText().toString());
            res.put("professionalEmail", etCorpEmail.getText().toString());
            res.put("companyName", etCompanyName.getText().toString());
            res.put("occupation", etPosition.getText().toString());

            diffJson = new JSONObject();

            for( String key : diffKeys){
                diffJson.put(key, res.get(key));
            }
            // password
            if(changePass){
                if (verifyPasswordMatch()) diffJson.put("password", etPass.getText());
                else {
                    showMessageInLabel("Password should match in both fields!");
                    return null;
                }
            }
        }
        catch (Exception ex)
        {
            //todo: handle exception
            UtilityFunctions.showMessageInToast(parentActivity, "Error while fetching account values");
            ex.printStackTrace();
            return null;
        }

        return diffJson;
    }

    private void fillFieldsFromJSON()
    {
        try
        {
            etFirstName.setText(profileJSON.getString("firstName"));
            etLastName.setText(profileJSON.getString("lastName"));
            etCorpEmail.setText(profileJSON.getString("professionalEmail"));
            if(calledWithUser)
                etCompanyName.setVisibility(View.VISIBLE);
                etCompanyName.setText(UtilityFunctions.tryGetStringFromJson(profileJSON,"companyName"));
                etPosition.setVisibility(View.VISIBLE);
                etPosition.setText(UtilityFunctions.tryGetStringFromJson(profileJSON,"occupation"));
        }
        catch (Exception ex)
        {
            //TODO: handle exception
            ex.printStackTrace();
        }

    }

    private boolean verifyPasswordMatch(){
        if(etPass.getText().toString().equals(etPass2.getText().toString())) return true;
        return false;
    }

}
