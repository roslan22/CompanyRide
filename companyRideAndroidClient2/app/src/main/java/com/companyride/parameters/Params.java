package com.companyride.parameters;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Params
{
    public static final String serverIP="http://ec2-54-194-91-81.eu-west-1.compute.amazonaws.com:3000/"    ;
    //public static final String serverIP="http://192.168.2.1:3000/"
    //public static final String serverIP="http://192.168.2.101:3000/";
    //public static final String serverIP="http://10.0.0.12:3000/";

    public static final String topicName = "companyride";
    public static final String extraRideType = "type";
    public static final String extraRideTypeHitcher = "hitcher";
    public static final String extraRideTypeDriver = "driver";

    public static final String argsJSON = "JSON_ARG";
    public static final String argsMessage = "MESSAGE_ARG";

    public static final String rideRequestPath = "rideRequest";
    public static final String rideRequestOneTimePath = "rideRequest/onetime";
    public static final String potRideRequestPath = "rideRequest/potential/rides/hitcher";
    public static final String ridePath = "ride";
    public static final String userTokenPath = "user/token";
    public static final String onTimeEvent = "one-time";
    public static final String weeklyEvent = "weekly";
    public static final String requestType_NEW = "new";
    public static final String PACKAGE_PATH = "com.companyride";
    public static final int maxDistanceToWalk = 500;


    public static final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat formDate   = new SimpleDateFormat(getLocaleCustomizedDateFormat());
    public static SimpleDateFormat shortDate  = new SimpleDateFormat(getLocaleCustomizedShortDateFormat());

    public static String getLocaleCustomizedDateFormat() {
        if(Locale.getDefault().getLanguage().equals("en_US") || Locale.getDefault().getLanguage().equals("en"))
        {
            return "MM/dd/yyyy";
        }
        else return "dd/MM/yyyy";
    }

    public static String getLocaleCustomizedShortDateFormat() {
        if(Locale.getDefault().getLanguage().equals("en_US") || Locale.getDefault().getLanguage().equals("en"))
        {
            return "MMM dd";
        }
        else return "dd MMM";
    }

    public static final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat formDateJS   = new SimpleDateFormat("yyyy-MM-dd");

    public static final String extraCallingActivity = "extraCallingActivity";
    public static final String extraID = "ID_NUM";
    public static final String extraDate= "DATE";
    public static final String extraLocations = "rideLocations";
    public static final String extraUserProfileId = "extraUserProfileId";

    public static final String PREF_FILE = "companyRidePrefs";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SAVE_LOGIN = "saveLogin";
    public static final String USER_ID = "useID";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_PASSIVE = "passive";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public static final String MSG_TYPE_NOTIFICATION = "event";
    public static final String MSG_TYPE_MESSAGE = "message";

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static String potentialRides = "potentialRides";
}
