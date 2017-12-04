/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package properties;

/**
 *
 * @author Vlada
 */
public class Parameter {
    public final static String RIDE_STATUS_ACTIVE_NOT_FULL = "activeNotFull";
    public final static String RIDE_STATUS_ACTIVE_FULL = "activeFull";
    
    //----------------------counters----------------------------------
    public final static String COUNTER_DOC_NAME = "counters";
    public final static String NUM_OF_MATCHES_FIRST_HITCHER = "numOfMatchesFirstHitcher";
    public final static String NUM_OF_MATCHES_EXISTING_RIDE = "numOfMatchesExistingRide";
    public final static String TOTAL_NUM_OF_HITCHER_RIDE_REQUESTS  = "totalNumOfHitcherRideRequests";
    public final static String TOTAL_NUM_OF_DRIVER_RIDE_REQUESTS  = "totalNumOfDriverRideRequests";

    // GCM API key
    public static final String API_KEY = "AIzaSyDb5eMIrHOw42_MPqdObBvnHW6JyfhPRPY";

    // GCM Notification Messages
    public static final String NEW_RIDE_MESSAGE = "Your ride has been matched!";
    public static final String MSG_TYPE_NOTIFICATION = "event";
}
