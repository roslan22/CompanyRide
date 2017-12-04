/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.matching;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.Date;
import java.util.LinkedList;

import gcmmessaging.GCMNotificationHandler;
import mongodb.Collections;
import mongodb.Mongo;
import org.bson.types.ObjectId;
import process.populating.Coordinate;
import properties.Parameter;
import utils.DateAndTime;
import utils.geoUtils;
import utils.schemas.UserProfile;

/**
 *
 * @author Vlada
 */
public class MatchingExecutor
{
    LinkedList<DBObject> oneTimeHitchersDocuments = new LinkedList<>();
    LinkedList<DBObject> reoccuringHitchersDocuments = new LinkedList<>();
    LinkedList<DBObject> unmatchedHitchersDocuments = new LinkedList<>();
    Date lastQueringDateForOneTimeRideRequests;
    Date lastQueringDateForReocuringRideRequests;
    Mongo mongo;
    
    public final static String ONE_TIME = "one-time";
    public final static String WEEKLY = "weekly";
    public final static int DESTINATION_DISTANCE_MULTIPLIER = 4;
    public final static int MAX_NUM_OF_DRIVERS_TO_RETRIEVE = 20;
        
            
    public MatchingExecutor()
    {
        this.mongo = Mongo.getInstance();
        lastQueringDateForOneTimeRideRequests = new Date(946_677_600_000L); //    01.01.2000
        lastQueringDateForReocuringRideRequests = new Date(946_677_600_000L); //    01.01.2000
    }
    
    public LinkedList<DBObject> getOneTimeHitchersDocuments()
    {
        System.out.println("=========================================================");
        System.out.println("Retrieving one time hitchers new documents");        
        retrieveNewOneTimeRideRequests();
        System.out.println("=========================================================");
        return oneTimeHitchersDocuments;
    }
    
    public LinkedList<DBObject> getReoccuringHitchersDocuments()
    {
        System.out.println("=========================================================");
        System.out.println("Retrieving weekly hitchers new documents");     
        retrieveNewReocuringRideRequests();
        System.out.println("=========================================================");
        return reoccuringHitchersDocuments;
    }
    
    public LinkedList<DBObject> getUnmatchedHitchersDocuments()
    {
        System.out.println("=========================================================");
        System.out.println("Retrieving unmatched hitchers documents");     
        retrieveUnmatchedRideRequests();
        System.out.println("=========================================================");
        return unmatchedHitchersDocuments;
    }
     
    public void emptyOneTimeHitchersDocuments()
    {
       oneTimeHitchersDocuments.clear();
    }
    
    public void emptyUnmatchedHitchersDocuments()
    {
       unmatchedHitchersDocuments.clear();
    }
        
    public void emptyReoccuringHitchersDocuments()
    {
        reoccuringHitchersDocuments.clear();
    }    
    
    public void retrieveNewReocuringRideRequests()
    {
        System.out.println("Last quering date is " + lastQueringDateForReocuringRideRequests);
        BasicDBObject query =  new BasicDBObject("creationDate", new BasicDBObject("$gte", lastQueringDateForReocuringRideRequests))
                                         .append("rideType","hitcher")
                                         .append("eventType",WEEKLY)
                                         .append("status","new");
        DBObject currentDoc;
        
        DBCursor cursor = mongo.findDocuments(Collections.RIDEREQUESTS, query);
        
         System.out.println("Found " + cursor.size() +  " new weekly hitchers documents");
        
        if (cursor.hasNext()) 
        {
            while(cursor.hasNext()) 
            {
                currentDoc = cursor.next();
                reoccuringHitchersDocuments.add(currentDoc);
            }
            lastQueringDateForReocuringRideRequests = (Date)reoccuringHitchersDocuments.getLast().get("creationDate");
        }
       
        cursor.close();
    }    
    
      private void retrieveNewOneTimeRideRequests()
    {
        System.out.println("Last quering date is " + lastQueringDateForOneTimeRideRequests);
        BasicDBObject query =  new BasicDBObject("creationDate", new BasicDBObject("$gte", lastQueringDateForOneTimeRideRequests))
                                         .append("rideType","hitcher")
                                         .append("eventType",ONE_TIME)
                                         .append("status","new");
        DBObject currentDoc;
        
        DBCursor cursor = mongo.findDocuments(Collections.RIDEREQUESTS, query);

        System.out.println("Found " + cursor.size() +  " new one-time hitchers documents");
        
        if (cursor.hasNext()) 
        {
            while(cursor.hasNext()) 
            {
                currentDoc = cursor.next();
                oneTimeHitchersDocuments.add(currentDoc);
            }
            lastQueringDateForOneTimeRideRequests = (Date)oneTimeHitchersDocuments.getLast().get("creationDate");
        }
           
        cursor.close();
    }           
        
    private void retrieveUnmatchedRideRequests()
    {
        BasicDBObject query =  new BasicDBObject("status","unmatched").append("rideType","hitcher");
        
        DBObject currentDoc;
        
        DBCursor cursor = mongo.findDocuments(Collections.RIDEREQUESTS, query);
        
        System.out.println("Found " + cursor.size() +  " unmatched hitchers documents");

        while(cursor.hasNext()) 
        {
            currentDoc = cursor.next();
            unmatchedHitchersDocuments.add(currentDoc);
        }
        
        cursor.close(); 
    }
    
    public void createNewRide(DBObject hitcherRideRequest,DBObject driverRideRequest)
    {
        BasicDBObject fromDriverObject= (BasicDBObject)driverRideRequest.get("from");
        BasicDBObject toDriverObject= (BasicDBObject)driverRideRequest.get("to");
        BasicDBObject fromHitcherObject= (BasicDBObject)hitcherRideRequest.get("from");
        BasicDBObject toHitcherObject= (BasicDBObject)hitcherRideRequest.get("to");
        int maxNumOfHitchers = (int)driverRideRequest.get("maxNumOfHitchers");
        String eventTypeDriver = (String)driverRideRequest.get("eventType");
        String eventTypeHitcher = (String)hitcherRideRequest.get("eventType");
        ObjectId driverRideRequestId = (ObjectId)driverRideRequest.get("_id");
        ObjectId driverProfileId = (ObjectId)driverRideRequest.get("userProfileId");
        ObjectId hitcherProfileId = (ObjectId)hitcherRideRequest.get("userProfileId");
        int driverTimeOffset     =  (int)driverRideRequest.get("timeOffset");
        int hitcherTimeOffset     =  (int)hitcherRideRequest.get("timeOffset");
        int weekday = (int) hitcherRideRequest.get("weekday");
        
        UserProfile hitcherUserProfile = UserProfile.getUserProfileById(hitcherProfileId.toString());
        UserProfile driverUserProfile = UserProfile.getUserProfileById(driverProfileId.toString());
                  
        Date startDriverDate = (Date)driverRideRequest.get("startDate");
        Date stopDriverDate = (Date)driverRideRequest.get("stopDate");        
        Date startHitcherDate = (Date)hitcherRideRequest.get("startDate");
        Date stopHitcherDate = (Date)hitcherRideRequest.get("stopDate");
        
        Date rideStartDate = getMax(startDriverDate,startHitcherDate);
        Date rideStopDate = getMin(stopDriverDate,stopHitcherDate);
        
        BasicDBObject prefferedTimeDriver = (BasicDBObject)driverRideRequest.get("preferredRideTime");
        BasicDBObject prefferedTimeHitcher = (BasicDBObject)driverRideRequest.get("preferredRideTime");
               
        double fromHour1  = prefferedTimeDriver.getDouble("fromHour");
        double toHour1  = prefferedTimeDriver.getDouble("toHour");
        
        double fromHour2  = prefferedTimeHitcher.getDouble("fromHour");
        double toHour2  = prefferedTimeHitcher.getDouble("toHour");
        
        double maxPickUpHour = min(toHour2, toHour1);
        double minPickUpHour = max(fromHour2, fromHour1);
        
        BasicDBObject hitcherPickUpDetails = getPickUpDetailsOfHitcher(fromHitcherObject, minPickUpHour);
        BasicDBObject hitcherDropDetails = getDropDetailsOfHitcher(toHitcherObject);
             
        String eventTypeRide = (eventTypeDriver.equals(ONE_TIME) || eventTypeHitcher.equals(ONE_TIME) ) ? ONE_TIME : WEEKLY;
        
        ObjectId rideObjectId = new ObjectId();   
        
        BasicDBList hitchers = new BasicDBList();
        hitchers.put(0, new BasicDBObject("userProfileId",hitcherProfileId) 
                                                        .append("hitcherRideReqId", hitcherRideRequest.get("_id"))
                                                        .append("status","waitingForDriverApprovement")
                                                        .append("fullName", hitcherUserProfile.getFullName())
                                                        .append("occupationTitle",hitcherUserProfile.getOccupationTitle())
                                                        .append("pickUp", hitcherPickUpDetails)
                                                        .append("drop", hitcherDropDetails)
							.append("timeOffset", hitcherTimeOffset));
        DBObject ride = new BasicDBObject("_id",rideObjectId)
				  .append("timeOffset", driverTimeOffset)
                                  .append("from", fromDriverObject)
                                  .append("to", toDriverObject)
                                  .append("status", "inDriverApprove")
                                  .append("maxNumOfHitchers", maxNumOfHitchers)
                                  .append("startDate", rideStartDate)
                                  .append("weekday", weekday)
                                  .append("stopDate", rideStopDate)
                                  .append("creationDay", new Date())
                                  .append("eventType", eventTypeRide)
                                  .append("driverRideReqId", driverRideRequestId)
                                  .append("driverProfileId", driverProfileId)
                                  .append("driverOccupationTitle", driverUserProfile.getOccupationTitle())
                                  .append("driverFullName", driverUserProfile.getFullName())
                                  .append("maxPickUpHour", maxPickUpHour)
                                  .append("minPickUpHour", minPickUpHour)
                                  .append("hitchers", hitchers);
                                                                                                            
                                   
        mongo.insertDocument(Collections.RIDES, ride);
        
        addMessageToDriverProfile(driverProfileId.toString(), rideObjectId.toString());
        GCMNotificationHandler.SendNotificationMessage(
                Parameter.NEW_RIDE_MESSAGE,
                driverProfileId.toString(),
                rideObjectId.toString(),
                rideStartDate.toString(),
                rideStopDate.toString());
    }
    
    private BasicDBObject getPickUpDetailsOfHitcher(BasicDBObject hitcherFrom, double timeToPickUp)
    {
        String address = (String)hitcherFrom.getString("address");
        BasicDBObject coordinates = (BasicDBObject)hitcherFrom.get("coordinates");
        BasicDBObject hitcherPickUpDetails = new BasicDBObject("coordinates",coordinates)
                .append("address",address)
                .append("time", timeToPickUp);
        
        return hitcherPickUpDetails;
    }
    
        private BasicDBObject getDropDetailsOfHitcher(BasicDBObject hitcherTo)
    {
        String address = (String)hitcherTo.getString("address");
        BasicDBObject coordinates = (BasicDBObject)hitcherTo.get("coordinates");
        BasicDBObject hitcherDropDetails = new BasicDBObject("coordinates",coordinates)
                .append("address",address);
        
        return hitcherDropDetails;
    }
    
    private DBObject retrieveUserProfileById(ObjectId id)
    {
        BasicDBObject queryForHitcherProfile = new BasicDBObject("_id",id);        
        DBCursor cursor = Mongo.getInstance().findDocuments(Collections.USERPROFILES, queryForHitcherProfile);      
               
        if (cursor.count() != 1)
        {
            System.out.println("");
           return null;
        }
        else
        {
            return cursor.next();
        }
    }            
    
     public void addMessageToDriverProfile(String userProfileId, String rideId)
     {
         BasicDBObject message =new BasicDBObject("messages", 
                 new BasicDBObject("type", "newRide")
                 .append("message", "You have a new ride match. Please set exact time and location of a pick up and drop.")
                 .append("rideId", new ObjectId(rideId)));
         mongo.pushToArray(Collections.USERPROFILES, userProfileId ,message);
     }
        
     
    private void addHitcherToRide(String hitcherProfileId,
            String hitcherRideReqId,
            String fullName,
            String occupationTitle,   
            String rideId,
            BasicDBObject pickUp, 
            BasicDBObject drop, int hitcherTimeOffset)
     {
        
         BasicDBObject hitcher =new BasicDBObject("hitchers", 
                 new BasicDBObject("userProfileId",  new ObjectId(hitcherProfileId))
                .append("hitcherRideReqId", new ObjectId(hitcherRideReqId))
                .append("fullName", fullName)
                .append("occupationTitle", occupationTitle)
                .append("status","waitingForDriverApprovement")
                .append("pickUp",pickUp)
                .append("drop",drop)
                .append("timeOffset", hitcherTimeOffset));            
                  
         mongo.pushToArray(Collections.RIDES, rideId ,hitcher);         
     }
     
    private Date getMin (Date date1, Date date2)
    {
        if (date1.before(date2)) return date1;
        else return date2;
    }
    
    private Date getMax (Date date1, Date date2)
    {
        if (date1.before(date2)) return date2;
        else return date1;
    } 

    public boolean matchHitcherRideRequestWithDriverRideRequests(DBObject rideRequestToMatch)
    {
        if (isDateInPast((Date)rideRequestToMatch.get("stopDate")))
        {
            System.out.println("ride request " + rideRequestToMatch.get("_id").toString() + " is in past");
            return false;
        }
        else {
            DBObject coord = (DBObject) ((DBObject) rideRequestToMatch.get("from")).get("coordinates");
            Double longitude = Double.parseDouble(coord.get("long").toString());
            Double latitude = Double.parseDouble(coord.get("lat").toString());
            int radius = (int) rideRequestToMatch.get("radius");
            Boolean matchFound = false;

            DBObject currentDoc;

            BasicDBList myLocation = new BasicDBList();
            myLocation.put(0, longitude);
            myLocation.put(1, latitude);

            BasicDBList statuses = new BasicDBList();
            statuses.put(0, "new");
            statuses.put(1, "unmatched");

            DBCursor cursor = mongo.findDocuments(Collections.RIDEREQUESTS,
                    new BasicDBObject("from.coordinates",
                            new BasicDBObject("$near",
                                    new BasicDBObject("$geometry",
                                            new BasicDBObject("type", "Point")
                                                    .append("coordinates", myLocation))
                                            .append("$maxDistance", radius)
                            )
                    ).append("rideType", "driver").append("status", new BasicDBObject("$in", statuses))
            ).limit(MAX_NUM_OF_DRIVERS_TO_RETRIEVE);


            if (cursor.hasNext()) {
                while (cursor.hasNext()) {
                    currentDoc = cursor.next();
                    if(currentDoc != null && rideRequestToMatch != null) {
                        if (areMatch(rideRequestToMatch, currentDoc, radius)) {
                            createNewRide(rideRequestToMatch, currentDoc);
                            setStatusToMatched(rideRequestToMatch.get("_id").toString());
                            setStatusToMatched(currentDoc.get("_id").toString());
                            matchFound = true;
                            mongo.incrementCounter(Parameter.NUM_OF_MATCHES_FIRST_HITCHER);

                            System.out.println("found match for hitcher document " + rideRequestToMatch.get("_id").toString() + " --> " + currentDoc.get("_id").toString());
                            break;
                        }
                    }
                }
            } else {
                System.out.println("hitcher with ride request " + rideRequestToMatch.get("_id").toString() + " don't have near new drivers");
            }

            if (!matchFound)
                setStatusToUnMatched(rideRequestToMatch.get("_id").toString());

            return matchFound;
        }
    }
    
    public boolean matchHitcherRideRequestWithRides(DBObject rideRequestToMatch)
    {
        if (isDateInPast((Date)rideRequestToMatch.get("stopDate")))
        {
            System.out.println("ride request " + rideRequestToMatch.get("_id").toString() + " is in past");
            return false;
        }
        else
        {
            DBObject coord = (DBObject) ((DBObject) rideRequestToMatch.get("from")).get("coordinates");
            Double longitude = Double.parseDouble(coord.get("long").toString());
            Double latitude = Double.parseDouble(coord.get("lat").toString());
            int radius = (int) rideRequestToMatch.get("radius");
            Boolean matchFound = false;

            DBObject currentDoc;

            BasicDBList myLocation = new BasicDBList();
            myLocation.put(0, longitude);
            myLocation.put(1, latitude);


            DBCursor cursor = mongo.findDocuments(Collections.RIDES,
                    new BasicDBObject("from.coordinates",
                            new BasicDBObject("$near",
                                    new BasicDBObject("$geometry",
                                            new BasicDBObject("type", "Point")
                                                    .append("coordinates", myLocation))
                                            .append("$maxDistance", radius)
                            )
                    ).append("status", "activeNotFull")
            ).limit(MAX_NUM_OF_DRIVERS_TO_RETRIEVE);


            if (cursor.hasNext()) {
                while (cursor.hasNext()) {
                    currentDoc = cursor.next();

                    if (isMatchWithRide(rideRequestToMatch, currentDoc, radius)) {
                        String hitcherProfileId = rideRequestToMatch.get("userProfileId").toString();
                        String hitcherRideRequestId = rideRequestToMatch.get("_id").toString();
                        String rideId = currentDoc.get("_id").toString();
                        String driverProfileId = currentDoc.get("driverProfileId").toString();
                        UserProfile hitcherUserProfile = UserProfile.getUserProfileById(hitcherProfileId);

                        BasicDBObject fromHitcherObject = (BasicDBObject) rideRequestToMatch.get("from");
                        BasicDBObject toHitcherObject = (BasicDBObject) rideRequestToMatch.get("to");
                        double minPickUpHour = Double.parseDouble(currentDoc.get("minPickUpHour").toString());

                        BasicDBObject pickUp = getPickUpDetailsOfHitcher(fromHitcherObject, minPickUpHour);
                        BasicDBObject drop = getDropDetailsOfHitcher(toHitcherObject);
                        int hitcherTimeOffset = (int) rideRequestToMatch.get("timeOffset");

                        addHitcherToRide(hitcherProfileId,
                                hitcherRideRequestId,
                                hitcherUserProfile.getFullName(),
                                hitcherUserProfile.getOccupationTitle(),
                                rideId, pickUp, drop, hitcherTimeOffset);

                        addMessageToDriverProfile(driverProfileId, rideId);

                        setStatusToMatched(rideRequestToMatch.get("_id").toString());
                        matchFound = true;
                        mongo.incrementCounter(Parameter.NUM_OF_MATCHES_EXISTING_RIDE);
                        System.out.println("found match for hitcher document " + rideRequestToMatch.get("_id").toString() + " to ride " + currentDoc.get("_id").toString());
                        break;
                    }
                }
            } else {
                System.out.println("hitcher with ride request " + rideRequestToMatch.get("_id").toString() + " don't have near rides");
            }

            if (!matchFound)
                setStatusToUnMatched(rideRequestToMatch.get("_id").toString());

            return matchFound;
        }
    }
    
    
    private boolean areMatch(DBObject rideRequest1,DBObject rideRequest2, int radius)
    {
        boolean match = checkBlockedAndInconvenientUsers(rideRequest1, rideRequest2);
                
        //check if destination match
        if (match) 
        {
            double distanceBetweenDestinationsInKm =  geoUtils.measureDistance(retrieveCoordinateFromDocument("to", rideRequest1), retrieveCoordinateFromDocument("to", rideRequest2));
            System.out.println("distance between " +  rideRequest1.get("_id").toString() + " to " +  rideRequest2.get("_id").toString() +  " is " + distanceBetweenDestinationsInKm);
            match =  (distanceBetweenDestinationsInKm * 1000 <= radius * DESTINATION_DISTANCE_MULTIPLIER);   // comparison is in meters
            if (!match)   System.out.println("Ride request "+ rideRequest1.get("_id").toString() + " and ride request "+ rideRequest2.get("_id").toString() +  " don't match by distance");
        }
        
        if (match) 
        {
            //check if time match
            match = checkIfTimeMatch(rideRequest1,rideRequest2);
        }
      
        return match;
    }
    
    private boolean checkBlockedAndInconvenientUsers(DBObject rideRequest1,DBObject rideRequest2)
    {
        if (rideRequest1 == null)
            System.out.println("First ride request is null!");
        if (rideRequest2 == null)
            System.out.println("Second ride request is null!");
        String userProfile1Id = rideRequest1.get("userProfileId").toString();
        String userProfile2Id = rideRequest2.get("userProfileId").toString();
        BasicDBList inconvenient1 = (BasicDBList)rideRequest1.get("inconvenientUsers");
        BasicDBList inconvenient2 = (BasicDBList)rideRequest2.get("inconvenientUsers");
        BasicDBList blocked1 = (BasicDBList)rideRequest1.get("blockedUsers");
        BasicDBList blocked2 = (BasicDBList)rideRequest2.get("blockedUsers");
        
        boolean convenient = !checkIfIdIsInList(userProfile1Id,inconvenient2);
        if (convenient) convenient = !checkIfIdIsInList(userProfile1Id,blocked2);
        if (convenient) convenient = !checkIfIdIsInList(userProfile2Id,inconvenient1);
        if (convenient) convenient = !checkIfIdIsInList(userProfile2Id, blocked1);
        
        if (!convenient)  System.out.println("ride requests "+ rideRequest1.get("_id").toString() + 
                " and ride request " + rideRequest2.get("_id").toString()  +  " don't match because they blocked or inconvenitnt" );
        return convenient;
    }
    
    
     private boolean checkBlockedAndInconvenientUsersWithRide(DBObject rideRequest, String driverId)
    {        
        String userProfileId = rideRequest.get("userProfileId").toString();
        BasicDBList inconvenient = (BasicDBList)rideRequest.get("inconvenientUsers");
        BasicDBList blocked = (BasicDBList)rideRequest.get("blockedUsers");
        
        boolean convenient = !checkIfIdIsInList(driverId,inconvenient);
        if (convenient) convenient = !checkIfIdIsInList(driverId,blocked);
        
        if (!convenient)  System.out.println("ride requests "+ rideRequest.get("_id").toString() + 
                " has driver " + driverId + " in his blocked or inconvenitnt arrays");
        return convenient;
    }
    
    
    
    private boolean checkIfIdIsInList(String id, BasicDBList list)
    {
        int size;
        boolean match = false;

        if ((list != null) && (list.size() > 0))
        {
            size = list.size();
            for (int i = 0 ;  i < size ;  i++)
            {
                match = list.get(i).toString().contains(id);
            }
        }
        
        return match;
    }
    
     private boolean isMatchWithRide(DBObject rideRequest,DBObject ride, int radius)
    {
        boolean match = true; //supose
        
        match = checkBlockedAndInconvenientUsersWithRide(rideRequest, ride.get("driverProfileId").toString());       
                
        //check if destination match
        if (match) 
        {
            double distanceBetweenDestinationsInKm =  geoUtils.measureDistance(retrieveCoordinateFromDocument("to", rideRequest), retrieveCoordinateFromDocument("to", ride));
            System.out.println("distance between " +  rideRequest.get("_id").toString() + " to " +  ride.get("_id").toString() +  " is " + distanceBetweenDestinationsInKm);
            match =  (distanceBetweenDestinationsInKm * 1000 <= radius * DESTINATION_DISTANCE_MULTIPLIER);   // comparison is in meters
            if (!match)  System.out.println("ride request "+  rideRequest.get("_id").toString() + 
                " and ride  " + ride.get("_id").toString()  +  " don't match because they they are too far from each other" );
        }
        
        if (match) 
        {
            //check if time match
            match = checkIfTimeMatchWithRide(rideRequest,ride);
        }
      
        return match;
    }
    
    public boolean checkIfTimeMatch(DBObject rideRequest1,DBObject rideRequest2)
    {
        BasicDBObject preferredRideTime1 = (BasicDBObject)rideRequest1.get("preferredRideTime");
        double fromHour1  = preferredRideTime1.getDouble("fromHour");
        double toHour1  = preferredRideTime1.getDouble("toHour");
        
        BasicDBObject preferredRideTime2 = (BasicDBObject)rideRequest2.get("preferredRideTime");
        double fromHour2  = preferredRideTime2.getDouble("fromHour");
        double toHour2  = preferredRideTime2.getDouble("toHour");
        
        //fast check
        if(DateAndTime.isHoursMatch(fromHour1, toHour1, fromHour2, toHour2))
        {
            Date startDate1 = (Date)rideRequest1.get("startDate");
            Date startDate2 = (Date)rideRequest2.get("startDate");
            String eventType1 = (String)rideRequest1.get("eventType");
            String eventType2 = (String)rideRequest2.get("eventType");
            
            int dayOfTheWeek1 = (int)rideRequest1.get("weekday");
            int dayOfTheWeek2 =(int)rideRequest2.get("weekday");
            
            if ((eventType1.equals(ONE_TIME)) && (eventType2.equals(ONE_TIME)))
            {
                if (startDate1.equals(startDate2)) 
                    return true;
                else  System.out.println("ride requests "+  rideRequest1.get("_id").toString() + 
                " and ride request  " + rideRequest2.get("_id").toString()  +  " don't match in day of ride" );
                    
            }
            else if (((eventType1.equals(ONE_TIME)) && (eventType2.equals(WEEKLY))) || 
                    ((eventType1.equals(WEEKLY)) && (eventType2.equals(ONE_TIME))) ||
                    ((eventType1.equals(WEEKLY)) && (eventType2.equals(WEEKLY))))
            {
                if (dayOfTheWeek1 == dayOfTheWeek2)  
                {
                     Date stopDate1 = (Date)rideRequest1.get("stopDate");
                     Date stopDate2 = (Date)rideRequest2.get("stopDate");
                    
                     if (isDateRangeMatch(stopDate1,  stopDate2, startDate1, startDate2))
                              return true; 
                     else
                         System.out.println("ride requests "+  rideRequest1.get("_id").toString() + 
                " and ride request  " + rideRequest2.get("_id").toString()  +  " don't match in date range" );
                }
                else
                {
                    System.out.println("ride requests "+  rideRequest1.get("_id").toString() + 
                " and ride request  " + rideRequest2.get("_id").toString()  +  " don't match in weekday" );
                }
            }
        }
        else
        {
            System.out.println("ride requests "+  rideRequest1.get("_id").toString() + 
                " and ride request  " + rideRequest2.get("_id").toString()  +  " don't match in hours" );
        }
        
       return false;
    }
    
    
     public boolean checkIfTimeMatchWithRide(DBObject hitcherRideRequest,DBObject ride)
    {
        BasicDBObject preferredRideTime = (BasicDBObject)hitcherRideRequest.get("preferredRideTime");
        double hitcherFromHour  = preferredRideTime.getDouble("fromHour");
        double hitcherToHour  = preferredRideTime.getDouble("toHour");
        
        double maxPickUpHour = Double.parseDouble(ride.get("maxPickUpHour").toString());
        double minPickUpHour = Double.parseDouble(ride.get("minPickUpHour").toString());
        
        
        //fast check
        if(DateAndTime.isHoursMatch(hitcherFromHour, hitcherToHour, minPickUpHour - 1, maxPickUpHour + 1))
        {
            Date startDateHitcher = (Date)hitcherRideRequest.get("startDate");
            Date startDateRide = (Date)ride.get("startDate");
            String eventTypeHitcher = (String)hitcherRideRequest.get("eventType");
            String eventTypeRide = (String)ride.get("eventType");
            
            int dayOfTheWeekHitcher = (int)hitcherRideRequest.get("weekday");
            int dayOfTheWeekRide = (int)ride.get("weekday");           
            
            
            if ((eventTypeHitcher.equals(ONE_TIME)) && (eventTypeRide.equals(ONE_TIME)))
            {
                if (startDateHitcher.equals(startDateRide)) 
                    return true;
                else
                     System.out.println("ride requests "+  hitcherRideRequest.get("_id").toString() + 
                " and ride " + ride.get("_id").toString()  +  " don't match in date" );
            }
            else if (((eventTypeHitcher.equals(ONE_TIME)) && (eventTypeRide.equals(WEEKLY))) || 
                    ((eventTypeHitcher.equals(WEEKLY)) && (eventTypeRide.equals(ONE_TIME))) ||
                    ((eventTypeHitcher.equals(WEEKLY)) && (eventTypeRide.equals(WEEKLY))))
            {
                if (dayOfTheWeekHitcher == dayOfTheWeekRide)  
                {
                     Date stopDateHitcherRideReq = (Date)hitcherRideRequest.get("stopDate");
                     Date stopDateRide = (Date)ride.get("stopDate");
                    
                     if (isDateRangeMatch(stopDateHitcherRideReq,  stopDateRide, startDateHitcher, startDateRide))
                              return true; 
                     else
                          System.out.println("ride requests "+  hitcherRideRequest.get("_id").toString() + 
                " and ride " + ride.get("_id").toString()  +  " don't match in date range" );
                }
                else
                     System.out.println("ride requests "+  hitcherRideRequest.get("_id").toString() + 
                " and ride " + ride.get("_id").toString()  +  " don't match in weekday" );
            }
        }
        else
            System.out.println("ride requests "+  hitcherRideRequest.get("_id").toString() + 
                " and ride " + ride.get("_id").toString()  +  " don't match in hours" );
        
       return false;
    }
    
    private boolean isDateRangeMatch(Date stopDate1, Date stopDate2, Date startDate1, Date startDate2)
    {
        if((stopDate1.before(startDate2)) || (stopDate2.before(startDate1)))return false;
        else
        {
             return true;
        }
    }

    private boolean isDateInPast(Date date)
    {
        return (date.before(new Date()));
    }
    
    private void setStatusToMatched(String rideRequestId)
    {
        mongo.updateFirstLevelSimpleField(Collections.RIDEREQUESTS, rideRequestId, "status", "matched");
    }
    
    private void setStatusToUnMatched(String rideRequestId)
    {
        mongo.updateFirstLevelSimpleField(Collections.RIDEREQUESTS,rideRequestId, "status", "unmatched");
    }               
    
    private Coordinate retrieveCoordinateFromDocument(String key, DBObject document)
    {
        BasicDBObject geoJSONObject;
        BasicDBObject coordinatesObject;
        if (key.equals("from"))
        {
              geoJSONObject = (BasicDBObject)document.get("from");
        }
        else if (key.equals("to"))
        {
              geoJSONObject = (BasicDBObject)document.get("to");
        }
        else
        {
            throw new RuntimeException("Not supposed to get here. Invalid key " + key);
        }            
        
        coordinatesObject = (BasicDBObject)geoJSONObject.get("coordinates");
        double latitude = coordinatesObject.getDouble("lat");
        double longitude = coordinatesObject.getDouble("long");
       
        return new Coordinate(longitude,latitude);
    }
}
