package gcmmessaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import properties.Parameter;

/**
 * Created by Valeriy on 8/5/2015.
 */
public class GCMNotificationHandler {
    public static void SendNotificationMessage(String message,
                                               String userProfileID,
                                               String rideID,
                                               String startDate,
                                               String stopDate){
        try {
            System.out.println("Message: " +  message );
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject jGcmData = new JSONObject();
//            JSONObject jNotif = new JSONObject();
            JSONObject jData = new JSONObject();
            jData.put("message", message.trim());
            jData.put("msgType", Parameter.MSG_TYPE_NOTIFICATION);
            if(rideID != null)
                jData.put("rideId", rideID);
            if(startDate != null)
                jData.put("startDate", startDate);
            if(stopDate != null)
                jData.put("stopDate", stopDate);
//            jNotif.put("body", message.trim());
//            jNotif.put("title", "CompanyRide Message");
//            jNotif.put("icon", "ic_notification_fullsize");
//            jNotif.put("sound", "default");
//            jNotif.put("clickAction", "android.intent.action.MAIN");
//            jNotif.put("tickerText", "CompanyRide New Message!");
            // build final message
            jGcmData.put("to", "/topics/" + userProfileID);
//            jGcmData.put("notification", jNotif);
            jGcmData.put("data", jData);

            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Parameter.API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
            System.out.println("Check your device/emulator for notification or logcat for " +
                    "confirmation of the receipt of the GCM message.");
        } catch (Exception e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        }
    }

    public static void SendNotificationMessage(String notificationType, String profileId) {
        GCMNotificationHandler.SendNotificationMessage(notificationType, profileId, null, null, null);
    }
}
