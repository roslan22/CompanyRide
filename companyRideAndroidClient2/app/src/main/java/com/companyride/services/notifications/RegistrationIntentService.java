package com.companyride.services.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.companyride.R;
import com.companyride.http.PostExecutor;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static ArrayList<String> TOPICS = new ArrayList<>();

    public RegistrationIntentService() {
        super(TAG);
    }

    private ArrayList<String> getNewTopics(){
        ArrayList<String> newTopics = new ArrayList<>();
        newTopics.add(Params.topicName);
        String userId = AppSharedData.getInstance().getUserProfileId();
        if(userId != null) {
            newTopics.add(userId);
        }
        return newTopics;
    }

    private ArrayList<String> getTopicsDiff(ArrayList<String> from, ArrayList<String> to) {
        ArrayList<String> diffTopics = new ArrayList<>();
        for(String topic : from){
            if(!to.contains(topic))
                diffTopics.add(topic);
        }
        return diffTopics;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<String> allTopics = getNewTopics();
        ArrayList<String> removeTopics = getTopicsDiff(TOPICS, allTopics);
        ArrayList<String> addTopics = getTopicsDiff(allTopics, TOPICS);
        TOPICS = allTopics;
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);
                // Subscribe to topic channels
                subscribeTopics(token, addTopics);
                unSubscribeTopics(token, removeTopics);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
//                sharedPreferences.edit().putBoolean(Params.SENT_TOKEN_TO_SERVER, true).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
//            sharedPreferences.edit().putBoolean(Params.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Params.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }



    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        String userID = AppSharedData.getInstance().getUserId();
        if (userID == null)
            return;
        try {
            String path = Params.serverIP + Params.userTokenPath + "/" + userID + "/" + token;
            JSONObject res = null;
            res = new PostExecutor().execute(path, "{}").get();

            if (res == null) {
                System.out.println("-E- Failed to send request to server. Try again later");
                return;
            }

            if (res.getInt("code") == 200)
            {
                System.out.println("-I- Device token was sent to server");
            }
        }
        catch (Exception ex)
        {
            System.out.println("Unexpected exception: ");
            ex.printStackTrace();
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token, ArrayList<String> topics) throws IOException {
        String userProfileID = AppSharedData.getInstance().getUserProfileId();
        for (String topic : topics) {
            if (userProfileID != null && userProfileID.equals(topic)) {
                sendRegistrationToServer(token);
            }
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    private void unSubscribeTopics(String token, ArrayList<String> topics) throws IOException {
//        String userProfileID = AppSharedData.getInstance().getUserProfileId();
        for (String topic : topics) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.unsubscribe(token, "/topics/" + topic);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Registration Listener service was started");
    }
}
