package com.companyride.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.companyride.R;
import com.companyride.http.PostExecutor;
import com.companyride.http.PutExecutor;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import org.json.JSONObject;


/**
 * Created by Ruslan on 13-Jul-15.
 */
public class MessagesDialog extends DialogFragment {
    public String[] strMessages;
    private String path;
    private static boolean s_isDriver;

    public static MessagesDialog getInstance(String profileId, String rideId, boolean isDriver, String[] messages, String driverId) {
        MessagesDialog fragment = new MessagesDialog();
        Bundle bundle = new Bundle(2);
        bundle.putString("profileId", profileId);
        bundle.putString("rideId", rideId);
        bundle.putString("driverId", driverId);
        fragment.setArguments(bundle);
        s_isDriver = isDriver;
        fragment.strMessages = messages;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        //path = new String(Params.serverIP + "/ride/" + args.getString("rideId")
               // + "/hitcher/" + args.getString("profileId") + "/messageFromDriver");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.choose_message));

        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        populateAdapter(arrayAdapter);
        builder.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = arrayAdapter.getItem(which);
                        sendMessageToServer(args.getString("rideId"),args.getString("profileId"),message, args.getString("driverId"));
                    }
                });

        return builder.create();
    }

    private void populateAdapter(ArrayAdapter<String> arrayAdapter)
    {
        for(int i=0;i<strMessages.length;i++)
        {
            arrayAdapter.add(strMessages[i]);
        }
    }

    private void sendMessageToServer(String rideId, String profileId, String message, String driverId)
    {
        try {
            String path;
            if(s_isDriver) {
                path = Params.serverIP + "ride/" + rideId + "/hitcher/" + profileId + "/messageFromDriver/" + driverId;
            }
            else
            {
                path = Params.serverIP + "ride/" + rideId + "/hitcher/" + profileId + "/messageForDriver";
            }
            JSONObject msg = new JSONObject();
            msg.put("message", message);

            if (message != null)
            {
                JSONObject res = new PostExecutor().execute(path, msg.toString()).get();

                if (res == null) {
                    UtilityFunctions.showMessageInToast(getActivity(), "Failed to send request to server. Try again later");
                } else {
                    if (res.getInt("code") == 200 && res.getString("status").equals("success")) // logged in successfully
                    {
                        UtilityFunctions.showMessageInToast(getActivity(), "Message was successfully sent!");
                    } else {
                        String mes = res.getString("message");
                        UtilityFunctions.showMessageInToast(getActivity(), mes);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UtilityFunctions.showMessageInToast(getActivity(), "Error in sending message to server");
        }
    }
}
