package com.companyride.fragments;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.companyride.R;
import com.companyride.http.PutExecutor;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ruslan on 05-Dec-15.
 */
public class FeedbackFragment extends Fragment {
    private Activity parentActivity;
    private View rootView;
    private AppSharedData sharedData = AppSharedData.getInstance();
    private Spinner feedbackSpinner;
    private LinearLayout customZone;
    private EditText feedbackText;
    private Button submitFeedButton;
    private CheckBox shareEmailCheckBox;
    private TextView tvAlert;
    private String feedbackSpinnerStr = "bug";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.page_feedback, container, false);

        initVariables();
        registerClickListenerOnButtons();

        setHasOptionsMenu(true);

        return rootView;
    }

    private void registerClickListenerOnButtons()
    {
        submitFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendFeedback();
                    tvAlert.setText("");
                } catch (NetworkErrorException e) {
                    e.printStackTrace();
                    tvAlert.setText("Network error, please try again later");
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvAlert.setText("Format error, please try again later");
                } catch (Exception e) {
                    e.printStackTrace();
                    tvAlert.setText(e.getMessage());
                }
            }
        });
    }

    private void sendFeedback() throws Exception {
        String feedbackStr = feedbackText.getText().toString();
        if(feedbackStr.isEmpty())
        {
            throw new Exception("No feedback was written");
        }
        else
        {
            trySendToServer(feedbackStr);
        }
    }

    private void trySendToServer(String feedback) throws NetworkErrorException, JSONException
    {
        String path = Params.serverIP + "feedback/" + feedbackSpinnerStr + "/" + "id/" + sharedData.getUserId();
        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("feedback", feedback);
            if(isUserAllowsEmail())
                reqJson.put("allowsEmail","YES");
            else
                reqJson.put("allowsEmail","NO");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException("Format json error");
        }

        JSONObject res = null;
        try
        {
            res = new PutExecutor().execute(path, reqJson.toString()).get();

            if (res == null)
            {
               throw new NetworkErrorException("no response from server");
            } else {
                if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                {
                    String message = res.getString("message");
                    UtilityFunctions.showMessageInToast(parentActivity.getBaseContext(), "Feedback was sent, Thank you");
                    goToMainScreen();
                }
                else
                {
                    String message = res.getString("message");
                    System.out.println("Failed to send following feedback to server " + feedback +
                            "" + "received following message: " + message);
                    throw new NetworkErrorException("received following message: " + message);
                }
            }
        } catch (NetworkErrorException e)
        {
            e.printStackTrace();
            tvAlert.setText("Network error, please try again later");
        }
        catch (Exception e) {
        e.printStackTrace();
        }
    }

    private void goToMainScreen() {
        ((SelectViewInterface)parentActivity).selectView(getString(R.string.user_profile));
    }

    private boolean isUserAllowsEmail() {
        if(shareEmailCheckBox.isChecked())
            return true;
        else
            return false;
    }

    private void initVariables()
    {
        feedbackSpinner = (Spinner) rootView.findViewById(R.id.feedback_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentActivity,
                R.array.feedback_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedbackSpinner.setAdapter(adapter);
        feedbackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //diffKeys.add("maxNumOfHitchers");
                feedbackSpinnerStr = adapterView.getAdapter().getItem(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        feedbackText = (EditText) rootView.findViewById(R.id.feedbackText);
        feedbackText.clearFocus();
        shareEmailCheckBox = (CheckBox)rootView.findViewById(R.id.shareEmailCheckBox);
        submitFeedButton = (Button)rootView.findViewById(R.id.submitFeedButton);
        tvAlert = (TextView)rootView.findViewById(R.id.tvErrorAlert);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.calendar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.goto_user_profile:
                ((SelectViewInterface)parentActivity).selectView(getString(R.string.user_profile));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
