/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.rating;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import gcmmessaging.GCMNotificationHandler;
import mongodb.Collections;
import mongodb.Mongo;
import org.bson.types.ObjectId;
import properties.Parameter;
import utils.DateAndTime;
 
/**
 *
 * @author Vlada
 */
public class RatingExecutor 
{
    private final static String LOG_TAG = "Rating Executor: ";
    private Mongo mongo;
    //private Date ratingDayStart = new Date(946_677_600_000L); //    01.01.2000
    //private Date ratingDayStop = new Date(946_677_600_000L); //    01.01.2000
    private Integer currentWeekDay;
    private LinkedList<DBObject> pastHourRides = new LinkedList<>();
    public final static String SEND_RATING_REQUEST =  "sendRatingRequest";
    public final static Integer HOURS_IN_DAY =  24;
    public final static Integer RATING_DELAY = 1;
    public final static String RIDE_STATUS_ACTIVE_FULL = "activeFull";
    public final static String RIDE_STATUS_ACTIVE_NOT_FULL = "activeNotFull";
    public final static String MAX_PICK_HOUR = "maxPickUpHour";
    public final static Double resolutionOfCheck = (1.0/60); // one minute
    double periodStart;
    double periodStop;
    double ratingSendTime;
    
    public RatingExecutor()
    {
         this.mongo = Mongo.getInstance();
         currentWeekDay = DateAndTime.getWeekdayInUTC();
         periodStart = DateAndTime.getNowTimeInUTC();
         periodStop = (periodStart + resolutionOfCheck) % HOURS_IN_DAY; 
         ratingSendTime = (periodStop + resolutionOfCheck) % HOURS_IN_DAY; 
    }

  
    public void updatePeriodStartStop()
    {
         periodStart = periodStop;
         periodStop = ratingSendTime;

         int weekday = DateAndTime.getWeekdayInUTC();
         if ( weekday != currentWeekDay )
            periodStop = HOURS_IN_DAY;
         ratingSendTime = (periodStop + resolutionOfCheck) % HOURS_IN_DAY;
         currentWeekDay = weekday;
    }
    
    
    public boolean hasPeriodChanged()
    {
        double nowTimeUTC = DateAndTime.getNowTimeInUTC();
        int weekday = DateAndTime.getWeekdayInUTC();
        System.out.println(LOG_TAG + "Is changed :" + nowTimeUTC + " > " + ratingSendTime
                + " ? Start period: " + periodStart + " End Period: " + periodStop);
        return (nowTimeUTC > ratingSendTime || weekday != currentWeekDay);
    }
    
        
    //should be runned ones in a day
    public void getNewBatchOfPastPeriodRides() 
    {
        pastHourRides.clear();

        DBObject currentDoc;
        
        BasicDBList statuses = new BasicDBList();
        statuses.put(0, RIDE_STATUS_ACTIVE_FULL);
        statuses.put(1, RIDE_STATUS_ACTIVE_NOT_FULL);
        
        BasicDBObject query =  new BasicDBObject("stopDate",new BasicDBObject("$gte", new Date()))
                .append("startDate", new BasicDBObject("$lte", new Date()))
                .append("status", new BasicDBObject("$in", statuses))
                .append("maxPickUpHour", new BasicDBObject("$gte", periodStart).append("$lte", periodStop))
                .append("$where", "this.weekday == new Date(new ISODate().getTime() + this.timeOffset).getDay() + 1");

        
        System.out.println(LOG_TAG + "The query is " + query.toString());
        System.out.println(LOG_TAG + "Weekday is " + query.toString());
        
        DBCursor cursor = mongo.findDocuments(Collections.RIDES, query);
        
        System.out.println(LOG_TAG + "Found " + cursor.size() +  " rides in range " + periodStart + "-" + (periodStop));
        
        if (cursor.hasNext()) 
        {
            while(cursor.hasNext()) 
            {
                currentDoc = cursor.next();
                pastHourRides.add(currentDoc);
            }
        }
       
        cursor.close();  
    }
      
    public void sendRatingToPastRides()
    {
        for (int i=0; i<pastHourRides.size(); i++)
        {
            updateSystemAfterRide(i);
            
        }  
        
    }
 
    private void updateSystemAfterRide(int index) 
    {
        DBObject rideToRate = pastHourRides.get(index);
        ObjectId rideId = (ObjectId)rideToRate.get("_id");
        String driverName = rideToRate.get("driverFullName").toString();
        System.out.println(LOG_TAG + "Inside the rating for: " + rideId.toString());
        ObjectId driverProfileId = (ObjectId)rideToRate.get("driverProfileId");
        BasicDBList hitchers = (BasicDBList)rideToRate.get("hitchers");
        
        updateDriverExperience(driverProfileId);
        sendRatingRequestMessageToHitchers(hitchers.toArray(), rideId, driverName);
        removeMessagesFromRide(rideId,hitchers.toArray());
        checkIfNeedToSendBadge(driverProfileId, true);
    }
          
    private void removeMessagesFromRide(ObjectId rideId, Object[] hitchersArr)
    {
        ObjectId userProfileId = null;
                
        for(Object hitcher : hitchersArr)
        {
            BasicDBObject currHitcher = (BasicDBObject)hitcher;
            userProfileId = (ObjectId)currHitcher.get("userProfileId");
                          
              BasicDBObject searchQuery = new BasicDBObject("_id",rideId)
                                            .append("hitchers", new BasicDBObject("$elemMatch",
                                                    new BasicDBObject("userProfileId", userProfileId)));
              
              BasicDBObject updateQuery = new BasicDBObject("$set", new BasicDBObject("hitchers.$.messages.sent",new BasicDBList())
                      .append("hitchers.$.messages.received",new BasicDBList()));
                                                   
              mongo.update(Collections.RIDES, searchQuery, updateQuery);
        }    
    }
    
    private void updateDriverExperience( ObjectId driverProfileId)
    {
        BasicDBObject searchQuery = new BasicDBObject("_id",driverProfileId);
        BasicDBObject updateQuery = new BasicDBObject("$inc",new BasicDBObject("numOfRidesAsDriver", 1));
              
        mongo.update(Collections.USERPROFILES, searchQuery, updateQuery);
    }
    
    private void sendRatingRequestMessageToHitchers( Object[] hitchersArr, ObjectId rideId, String driverName)
    {
        System.out.println(LOG_TAG + "Will send rating request for the hitchers of ride: " + rideId.toString() + " for driver: " + driverName);
        ObjectId userProfileId = null;
                
        for(Object hitcher : hitchersArr)
        {
            BasicDBObject currHitcher = (BasicDBObject)hitcher;
            userProfileId = (ObjectId)currHitcher.get("userProfileId");
            String messageStr = "Thank driver " + driverName+ " for today's ride";
            System.out.println(LOG_TAG + "Will send rating request for the hitchers: " + userProfileId.toString());
            BasicDBObject message = new BasicDBObject("messages",
             new BasicDBObject("type", "thanks")
             .append("message", messageStr)
             .append("rideId",  rideId));


            BasicDBObject searchQuery = new BasicDBObject("_id",userProfileId);
            BasicDBObject updateQuery = new BasicDBObject("$push", message)
                  .append("$inc",new BasicDBObject("numOfRidesAsHitcher", 1));

            mongo.update(Collections.USERPROFILES, searchQuery, updateQuery);
            GCMNotificationHandler.SendNotificationMessage(
                    messageStr,
                    userProfileId.toString());
            checkIfNeedToSendBadge(userProfileId, false);

        }    
    }

    private void checkIfNeedToSendBadge(ObjectId profileId, boolean isDriver) 
    {
        BasicDBObject query =  new BasicDBObject("_id", profileId);
        DBObject currentDoc;
        int numOfRidesAsDriver;
        int numOfRidesAsHitcher;
        boolean isHitcher = !isDriver;
        
        DBCursor cursor = mongo.findDocuments(Collections.USERPROFILES, query);
        
        String badgeStartMessage = "Congratulations! You just revealed your new badge: ";
        
        if (cursor.hasNext()) 
        {
            while(cursor.hasNext()) 
            {
                currentDoc = cursor.next();
               
                numOfRidesAsDriver = (int)currentDoc.get("numOfRidesAsDriver");
                numOfRidesAsHitcher = (int)currentDoc.get("numOfRidesAsHitcher");
                
                if (isDriver && numOfRidesAsDriver == 1)
                {
                    giveBadgeSendMessage(2, profileId, badgeStartMessage + "First driver ride", 20);
                }
                if (isHitcher && numOfRidesAsHitcher == 1)
                {
                    giveBadgeSendMessage(1, profileId, badgeStartMessage + "First hitcher ride", 20);
                }
                if (numOfRidesAsHitcher + numOfRidesAsDriver  == 5)
                {
                    giveBadgeSendMessage(5, profileId, badgeStartMessage + "5 Rides", 30);
                }
            }
        }
       
        cursor.close();
    }
    
    
    private void giveBadgeSendMessage(int badgeNumber, ObjectId profileId, String messageStr, int ratingToAdd)
    {
        messageStr += " You got +" + ratingToAdd + " rating for this badge";
        
         BasicDBObject message = new BasicDBObject("messages", 
                 new BasicDBObject("type", "badge")
                 .append("message", messageStr));
        
         BasicDBObject searchQuery = new BasicDBObject("_id",profileId);
         BasicDBObject updateQuery = new BasicDBObject("$push", message)
                 .append("$inc",new BasicDBObject("rating", ratingToAdd));
                                              
        mongo.update(Collections.USERPROFILES, searchQuery, updateQuery);       
        
        updateQuery = new BasicDBObject("$push",new BasicDBObject("badges",badgeNumber));
        mongo.update(Collections.USERPROFILES, searchQuery, updateQuery);
}
    
    
}
