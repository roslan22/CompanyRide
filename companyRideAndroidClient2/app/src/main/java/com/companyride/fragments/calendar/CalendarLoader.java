package com.companyride.fragments.calendar;

import android.os.AsyncTask;
import android.util.Log;
import com.companyride.http.HTTPManager;
import com.companyride.parameters.AppSharedData;
import com.companyride.parameters.Params;
import com.companyride.utils.MyJSONArray;
import com.companyride.utils.UtilityFunctions;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Valeriy on 5/2/2015.
 */
public class CalendarLoader
{
    private final String NO_RIDES = "no-rides";
    private HashMap<String, ArrayList<Ride>> cachedRideRequests;
    private HashMap<String, ArrayList<Ride>> cachedRides;

    public HashMap<String, ArrayList<Ride>> inprocess_items; // container to store some random calendar items
    public HashMap<String, ArrayList<Ride>> matched_items; // container to store some random calendar items


    public CalendarLoader() {
        cachedRideRequests = new HashMap<String, ArrayList<Ride>>();
        cachedRides = new HashMap<String, ArrayList<Ride>>();
        inprocess_items = new HashMap<String, ArrayList<Ride>>();
        matched_items = new HashMap<String, ArrayList<Ride>>();
    }

    public void resetCache(){
        cachedRideRequests.clear();
        cachedRides.clear();
    }

    public void loadRidesAndRequestsData(Calendar currMonth)
    {
//        try {
//            new RidesLoader().execute(currMonth).get(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
        Log.d("Tag", "Loading ride requests...");
        loadRidesDataForCurrentMonth(currMonth, Params.rideRequestPath, cachedRideRequests);
        Log.d("Tag", "Loading rides (matched)...");
        loadRidesDataForCurrentMonth(currMonth, Params.ridePath, cachedRides);
    }

    public Ride findRideInCache(String rideId) {
        ArrayList<ArrayList<Ride>> combinedCache = new ArrayList<ArrayList<Ride>>(cachedRideRequests.values());
        combinedCache.addAll(cachedRides.values());
        for( ArrayList<Ride> rideArr : combinedCache){
            for ( Ride ride : rideArr) {
                if (ride.getRideId().equals(rideId))
                    return ride;
            }
        }
        return null;
    }

    class RidesLoader extends AsyncTask<Object ,Void, Void>
    {
        @Override
        protected Void doInBackground(Object... currMonth) {
            Log.d("Tag", "Loading ride requests...");
            loadRidesDataForCurrentMonth((Calendar) currMonth[0], Params.rideRequestPath, cachedRideRequests);
            Log.d("Tag", "Loading rides (matched)...");
            loadRidesDataForCurrentMonth((Calendar) currMonth[0], Params.ridePath, cachedRides);
            return null;
        }
    }


    public void resetItems(){
        inprocess_items.clear();
        matched_items.clear();
    }

//    private void generateRandomData() {
//        Log.d("Tag", "Generating random data...");
//        inprocess_items.clear();
//        matched_items.clear();
//        // format random values. You can implement a dedicated class to provide real values
//        for(int i=0;i<31;i++) {
//            Random r = new Random();
//
//            if(r.nextInt(10)>6)
//            {
//                inprocess_items.add(Integer.toString(i));
//            }
//            if(r.nextInt(10)>8){
//                matched_items.add(Integer.toString(i));
//            }
//        }
//    }

    public class Ride{
        // format to save exact time
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        private final String eventType;
        private final String rideStartDate;
        private final String rideStopDate;
        private final String rideType;
        private final String driverOrHitcher;
        private final Double rideTimeFrom;
        private final Double rideTimeTo;
        private final String rideId;
        private final long timeOffset;
        private final Integer weekday;

        public Ride(
                String rideId,
                String eventType,
                String rideStartDate,
                String rideStopDate ,
                String rideType     ,
                String driverOrHitcher,
                Double rideTimeFrom,
                Double rideTimeTo,
                Integer weekday,
                long timeOffset
        ){
            this.timeOffset = timeOffset;
            this.rideId = rideId;
            this.eventType = eventType;
            this.rideStartDate = rideStartDate;
            this.rideStopDate = rideStopDate;
            this.rideType = rideType;
            this.driverOrHitcher = driverOrHitcher;
            this.rideTimeFrom = rideTimeFrom;
            this.rideTimeTo = rideTimeTo;
            this.weekday = weekday;
        }

        private Calendar getCalendarFromStringDate(String dateStr){
            // parse Start date\time string
            Date date = UtilityFunctions.stringToDate(dateStr, Params.formatDate);
            // create a calendar entity from the datetime
            Calendar eventCal = Calendar.getInstance(TimeZone.getDefault());
            eventCal.setTime(date);
            return eventCal;
        }

        public Calendar getStartDateCal(){
            return getCalendarFromStringDate(rideStartDate);
        }

        public Calendar getStopDateCal(){
            return getCalendarFromStringDate(rideStopDate);
        }

        public boolean isWeeklyEvent(){
            return this.rideType.equals(Params.weeklyEvent);
        }

        public boolean isRideRequest(){
            return this.eventType.equals(Params.rideRequestPath);
        }

        public boolean isDriver(){
            return this.driverOrHitcher.equals(Params.extraRideTypeDriver);
        }

        public String getRideTimeFromStr() {
            return UtilityFunctions.convertNumberToTime(Double.valueOf(rideTimeFrom), timeOffset);
        }

        public String getRideTimeToStr() {
            return UtilityFunctions.convertNumberToTime(Double.valueOf(rideTimeTo), timeOffset);
        }

        public Date getRideTimeFrom(){
            Date timeFrom = null;
            try {
                timeFrom = timeFormat.parse(UtilityFunctions.convertNumberToTime(Double.valueOf(rideTimeFrom), timeOffset));
            } catch (ParseException e) {
                //todo: handle exception
                e.printStackTrace();
            }
            return timeFrom;
        }

        public Date getRideTimeTo(){
            Date timeTo = null;
            try {
                timeTo = timeFormat.parse(UtilityFunctions.convertNumberToTime(Double.valueOf(rideTimeTo), timeOffset));
            } catch (ParseException e) {
                //todo: handle exception
                e.printStackTrace();
            }
            return timeTo;
        }

        public String getRideId() {
                return rideId;
        }

        public Integer getWeekday(){return weekday;}
    }

    private Ride createRideFromJson(
            JSONObject jsonRide,
            String rideType
    ){
        Ride rideInstance = null;
        try {
            long timeOffset = UtilityFunctions.tryGetLongFromJson(jsonRide, "timeOffset");
            rideInstance = new Ride(
                    jsonRide.getString("_id"),
                    rideType,
                    UtilityFunctions.convertUTCFullStringToEDTFullString(jsonRide.getString("startDate"), timeOffset),
                    UtilityFunctions.convertUTCFullStringToEDTFullString(jsonRide.getString("stopDate"), timeOffset),
                    UtilityFunctions.tryGetStringFromJson(jsonRide, "eventType"),
                    UtilityFunctions.tryGetStringFromJson(jsonRide, "rideType"),
                    UtilityFunctions.tryGetDoubleFromJson(jsonRide, "rideTimeFrom"),
                    UtilityFunctions.tryGetDoubleFromJson(jsonRide, "rideTimeTo"),
                    jsonRide.getInt("weekday"),
                    timeOffset
                    );

            Log.d("Tag", String.format("Got event type: %s with start at: %s, end: %s", "driver or hitcher: %s",
                    jsonRide.getString("eventType"),
                    UtilityFunctions.convertUTCFullStringToEDTFullString(jsonRide.getString("startDate"), timeOffset),
                    UtilityFunctions.convertUTCFullStringToEDTFullString(jsonRide.getString("stopDate"), timeOffset),
                    jsonRide.getString("rideType")
                    ));
        } catch (JSONException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return rideInstance;
    }

    private void addSingleEventToList(String dayOfMonth, Ride rideInstance){
        // if that a ride request - add event to the inprocess_items
        if ( rideInstance.isRideRequest()) {
            addItemForDay(inprocess_items, dayOfMonth, rideInstance);
        }
        // else - this is a ride  - add event to the matched_items
        else{
            addItemForDay(matched_items, dayOfMonth, rideInstance);
        }
    }

    private void addRideOrRequestDays(Ride rideInstance,
                                      Calendar currMonth){
        // default interval is 1 - meaning one time event
        int eventInterval    = 1;
        // if it is a weekly event type update interval to be 7
        if (rideInstance.isWeeklyEvent()){
            eventInterval = 7;
        }
        Calendar startDate = rideInstance.getStartDateCal();
        Calendar stopDate  = rideInstance.getStopDateCal();
        // check that the current year is between start and end date
        if (    startDate.get(Calendar.YEAR) <= currMonth.get(Calendar.YEAR) &&
                stopDate.get(Calendar.YEAR)  >= currMonth.get(Calendar.YEAR) ){
            // day of month to add the events to
            String dayOfMonth = Integer.toString(startDate.get(Calendar.DAY_OF_MONTH));
            // one time event - add it if the month is equal
            if (startDate.get(Calendar.MONTH) == currMonth.get(Calendar.MONTH) && eventInterval == 1){
                addSingleEventToList(dayOfMonth, rideInstance);
            }
            else{
                int weekNum = 1;
//                int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);
                int dayOfWeek = rideInstance.getWeekday();
                // if the start month is this month
                if ( startDate.get(Calendar.MONTH) == currMonth.get(Calendar.MONTH) ) {
                    weekNum = startDate.get(Calendar.WEEK_OF_MONTH);
                }
                Calendar eventDate = (Calendar) currMonth.clone();
                // unless you over stop date or over the current month weeks
                // set the week day and add the event
                do{
                    eventDate.set(Calendar.YEAR, currMonth.get(Calendar.YEAR));
                    eventDate.set(Calendar.MONTH, currMonth.get(Calendar.MONTH));
                    eventDate.set(Calendar.WEEK_OF_MONTH, weekNum);
                    eventDate.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    Log.d("TAG", "week: " + weekNum +
                            "month:  " + eventDate.get(Calendar.MONTH) +
                            "year:  " + eventDate.get(Calendar.YEAR));

                    if ( (eventDate.get(Calendar.MONTH) >  currMonth.get(Calendar.MONTH)
                            && eventDate.get(Calendar.YEAR) ==  currMonth.get(Calendar.YEAR))
                            || eventDate.get(Calendar.YEAR) >  currMonth.get(Calendar.YEAR)
                            || eventDate.compareTo(stopDate) > 0   ) {
                        Log.d("TAG", "event month: " + eventDate.get(Calendar.MONTH) +
                                "curr month:  " + currMonth.get(Calendar.MONTH) +
                                "is event gt stop date: " + eventDate.compareTo(stopDate));
                        break;
                    }

                    if (eventDate.get(Calendar.MONTH) <  currMonth.get(Calendar.MONTH)
                            || eventDate.get(Calendar.YEAR) <  currMonth.get(Calendar.YEAR)){
                        weekNum++;
                        continue;
                    }

                    dayOfMonth = Integer.toString(eventDate.get(Calendar.DAY_OF_MONTH));
                    Log.d("TAG", "Month: " + Integer.toString(eventDate.get(Calendar.MONTH)) +
                                 "Day of month: " + dayOfMonth +
                                 "Actual start date: " + rideInstance.getRideTimeFromStr()
                    );
                    addSingleEventToList(dayOfMonth, rideInstance);
                    weekNum++;
                }
                while ( !(eventDate.compareTo(stopDate) > 0 ||
                        (eventDate.get(Calendar.MONTH) >  currMonth.get(Calendar.MONTH)
                        && eventDate.get(Calendar.YEAR) >=  currMonth.get(Calendar.YEAR))));
            }
        }
    }

    private void addItemForDay(HashMap<String, ArrayList<Ride>> items, String dayOfMonth, Ride rideInstance) {
        ArrayList<Ride> dayEvents = null;
        if( items.containsKey(dayOfMonth) ){
            dayEvents = items.get(dayOfMonth);
        }
        else{
            dayEvents = new ArrayList<Ride>();
            items.put(dayOfMonth, dayEvents);
        }
        dayEvents.add(rideInstance);
    }

    private void loadRidesDataForCurrentMonth(Calendar currMonth,
                                              String requestType,
                                              HashMap<String, ArrayList<Ride>> cachedData) {
        // Format for the title (month year)
        String monthStr = android.text.format.DateFormat.format("MMyyyy", currMonth).toString();
        MyJSONArray ridesData = new MyJSONArray();
        // TODO: change it to be taken from parameters
//        AppSharedData.getInstance().setUserProfileId("55329e02a2eee972bef7c3be");
        String userId = AppSharedData.getInstance().getUserProfileId();
        // Build the key identifier for user and month
        String key = userId + monthStr;
        ArrayList<Ride> cachedRides = null;
        // Try to get from cache first
        if (cachedData.containsKey(key)){
            Log.d("Tag", "Getting data from cache for: " + key);
            // Get cached data  for the user & month
            cachedRides = cachedData.get(key);
        }
        // if it is not in cache get from server
        else {
            cachedRides = new ArrayList<Ride>();
            // prepare format to send the request
            // Build server request path
            String path = Params.serverIP + requestType + "/";
            String executionPath = path + userId + "/" + monthStr;
            Log.d("Tag", "Execution path: " + executionPath);
            // send the request
            JSONObject res = UtilityFunctions.tryGetJsonFromServer(executionPath);
            // if we have a result
            if (res != null) {
                // go one by one over the ride requests and add to the in progress list
                ridesData = UtilityFunctions.tryGetMyJSONArray(res, "data");
            }
            // add ride requests
            for(int i=0; i < ridesData.length(); i++){
                JSONObject rideItem = null;
                try {
                    // get json object at index i
                    rideItem = ridesData.getJSONObject(i);
                } catch (JSONException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                //create ride from json
                Ride ride = createRideFromJson(rideItem, requestType);
                // add ride instance to the cache
                cachedRides.add(ride);
                cachedData.put(key, cachedRides);
            } // end of loop
        } // end of else
        if (cachedRides.size() > 0)
            for (Ride cachedRide : cachedRides) {
                // add the ride item for current month with the requestType (ride\ride request)
                addRideOrRequestDays(cachedRide, currMonth);
            }
    }
}
