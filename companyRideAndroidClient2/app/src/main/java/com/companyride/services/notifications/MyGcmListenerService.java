package com.companyride.services.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.companyride.R;
import com.companyride.activities.InitialScreen;
import com.companyride.activities.RideMainScreen;
import com.companyride.parameters.Params;


/**
 * Created by Valeriy on 8/4/2015.
 */
public class MyGcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;
    private static final String TAG = "NotificationReceiver";

    // Notification Text Elements
    private final CharSequence tickerText = "New message from CompanyRide!";
    private final CharSequence contentTitle = "New Message";
    private final CharSequence contentText = "Click to see the message";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "On Message Received!");
        String message = data.getString("message");
        String messageType = data.getString("msgType");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Message Type: " + messageType);
        Intent intent = null;
        if ( messageType==null || messageType.equals(Params.MSG_TYPE_NOTIFICATION))
            intent = new Intent(this, InitialScreen.class);
        else if ( messageType.equals(Params.MSG_TYPE_MESSAGE)) {
            intent = new Intent(this, RideMainScreen.class);
        }
        // get the ride ID anyway if exists
        String rideID = data.getString("rideId");
        if(rideID != null)
            intent.putExtra(Params.extraID, rideID);
        //TODO: if application is opened - update staff
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        if (intent == null)
            Log.d(TAG, "-E- Not supported notification type: " + messageType);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, intent);
    }

    private void sendNotification(String message, Intent intent) {
        Log.i(TAG, "Notification event FIRED! Message: " + message);
        // The PendingIntent that wraps the underlying Intent
        PendingIntent mContentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Build the Notification
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setTicker(tickerText)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_fullsize))
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(message)
                .setContentIntent(mContentIntent)
                .setSound(defaultSoundUri);

        // Get the NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Pass the Notification to the NotificationManager:
        mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "GCM Listener service was started");
    }
}
