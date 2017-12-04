/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.populating;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import mongodb.Collections;
import mongodb.Mongo;
import org.bson.types.ObjectId;
import utils.DateAndTime;


/**
 *
 * @author Vlada
 */
public class PopulationExecutor {
     
    private static final String MIN_DAY_IN_RANGE_STR = "2015-02-08 00:00:00";
    private static final String MAX_DAY_IN_RANGE_STR = "2015-12-08 00:00:00";
    private static final String MIN_CREATION_DATE_STR = "2013-01-08 00:00:00";
    private static final int SECONDS_IN_DAY = 86400;
    private static final double NUM_OF_RIDES_CREATED_IN_DAY = 50;
    
    private Mongo mongo;
    private long numOfAddedRides;
    
     Area BeerSheva = new Area("Beer Sheva", 3460.0, 3120.0, 3490.0, 3130.0 ); // lat , lon
     Area Ashdod = new Area("Ashsdod",3463.2459,3177.8331,3466.6092,3179.8893 ); // lat , lon
     Area Holon = new Area("Holon",3476.4295,3200.4388,3479.3121,3202.4172); 
     Area TelAviv = new Area("Tel Aviv",3477.305,3205.3578,3479.9301,3209.5749);
     Area PetahTiqwa = new Area("Petah Tikva",3486.712,3207.1763,3490.0925,3209.8076);
     ArrayList<Area> areas = new ArrayList<>(Arrays.asList(BeerSheva,Ashdod,Holon,TelAviv ,PetahTiqwa));
     
    public PopulationExecutor() 
    {
        mongo = Mongo.getInstance();
        numOfAddedRides = 0;
    }
           
    private void insertRandomRide()
    {
        String eventType = getRandomEventType();
        String rideType = getRandomRideType();
        
        Area fromArea = getRandomArea(-1);
        Coordinate from = fromArea.getRandomPointInTheArea();
        BasicDBList coordinatesFrom = new BasicDBList();
        coordinatesFrom.put(1, from.getLatitude());
        coordinatesFrom.put(0, from.getLongitude());        
        
        Area toArea = getRandomArea(areas.indexOf(fromArea));
        Coordinate to = toArea.getRandomPointInTheArea();
        BasicDBList coordinatesTo = new BasicDBList();
        coordinatesTo.put(0, to.getLatitude());
        coordinatesTo.put(1, to.getLongitude());
        
        int numOfPassengers = getRandomNumOfPassengers();
        double minHourToStartARide = getRandomHour();
        double maxHourToStartARide = (minHourToStartARide + 2.5) % 24;
                
        Date startDate = getRandomDate();
        Date stopDate = getRandomDate();
        Date creationDate = getCreationDate();
        if (startDate.after(stopDate))
        {
           Date temp = stopDate;
           stopDate = startDate;
           startDate = temp;
        }
        
        if (eventType == "one-time")
        {
            stopDate = startDate;
        }
        
        int weekday = DateAndTime.retrieveDayOfTheWeek(startDate);
        
        ObjectId userProfileId = new ObjectId();
        BasicDBList inconvenient = new BasicDBList();
        inconvenient.add(userProfileId);
        BasicDBList blockedUsers = new BasicDBList();
        blockedUsers.add(userProfileId);
        
        if(creationDate.before(startDate))
        {
            BasicDBObject rideRequestDoc = new BasicDBObject
                                             ("rideType",rideType)
                                            .append("userProfileId",userProfileId)
                                            .append("eventType", eventType)
                                            .append("inconvenientUsers",inconvenient)
                                            .append("blockedUsers", blockedUsers)
                                            .append("status", "new")
                                            .append("creationDate", creationDate) 
                                            .append("startDate", startDate)
                                            .append("weekday", weekday) 
                                            .append("stopDate", stopDate) 
                                            .append("preferredRideTime", new BasicDBObject("fromHour",minHourToStartARide)
                                                                                   .append("toHour",maxHourToStartARide))
                                            .append("from", new BasicDBObject("type","Point")
                                                                       .append("address",fromArea.name)
                                                                       .append("coordinates",new BasicDBObject("long", from.getLongitude())
                                                                                                          .append("lat", from.getLatitude())))
                                            .append("to", new BasicDBObject("type","Point")
                                                                        .append("address",toArea.name)
                                                                        .append("coordinates",new BasicDBObject("long", to.getLongitude())
                                                                                                          .append("lat", to.getLatitude())));

            if (rideType.equals("driver"))
            {
                rideRequestDoc.append("maxNumOfHitchers",numOfPassengers);
            }
            else if (rideType.equals("hitcher"))
            {
                rideRequestDoc.append("radius",getRandomRadius());
            }
            else throw new RuntimeException("Not supposed to be here. Invalid rideType - " + rideType);

            addStaticUserProfileData(userProfileId);
            
            mongo.insertDocument(Collections.RIDEREQUESTS, rideRequestDoc);        
            numOfAddedRides++;
        }
        else
        {
            System.out.println("ooops");
        }
    } 
    
    private void addStaticUserProfileData(ObjectId userProfileId)
    {
        int a = randInt(1,6);
        String name = "";
               
        BasicDBObject userProfileDoc = new BasicDBObject
                                             ("fullName",name + " Minion")
                                            .append("_id",userProfileId)
                                            .append("occupationTitle", "Henchmen at Gru Villian Corporation")
                .append("numOfRidesAsHitcher", 23)
                .append("numOfRidesAsDriver", 18)
                .append("rating", 580)
                .append("specialRequests",new BasicDBList().add(1))
                .append("messages", new BasicDBList())
                .append("blockedUsers", new BasicDBList());
        
        
        mongo.insertDocument(Collections.USERPROFILES, userProfileDoc);   
        
        
         BasicDBObject userDoc = new BasicDBObject
                                             ("firstName",name)
                                            .append("lastName","Minion")
                                            .append("userProfileId",userProfileId)
                                            .append("companyName", " Gru Villian Corporation")
                                            .append("occupation", "Henchmen")
                .append("status", "active")
                .append("lastLoginDate", new Date())
                .append("validationDate", new Date())
                .append("professionalEmail", name + numOfAddedRides +"minion@gruvillian.com")
                .append("password","123")
                .append("blockedUsers", new BasicDBList());
        
              mongo.insertDocument(Collections.USERS, userDoc);                                  
    }
    
    private double getRandomHour()
    {
        double a = randInt(600,900);
        double b = randInt(1500,1800);
        
        int aOrB = randInt(1,2);
        if (aOrB == 1) return (a/(double)100);
        else return (b/(double)100);         
    }
    
    private String getRandomRideType()
    {
        int a = randInt(1,5);
        
        if (a == 1) return "hitcher";
        if (a == 2) return "driver";
        if (a == 3) return "driver";
        if (a == 4) return "hitcher";
        if (a == 5) return "driver";
        
        
        return "driver";
    }
    
    private int getRandomNumOfPassengers()
    {
       return randInt(1,3);
    }
    
    private int getRandomRadius()
    {
       return randInt(5,3500);
    }
    
     private String getRandomEventType()
    {
        int a = randInt(1,4);
       
        if (a == 1) return "one-time";
        if (a == 2) return "weekly";
        if (a == 3) return "weekly";
        if (a == 4) return "weekly";
        
        return "weekly";
    }
     
     //exceptAreaNum - index of area that shouldn't be returned
     // negative number if no such area needed
     private Area getRandomArea(int exceptAreaNum)
     {
         int a = exceptAreaNum; 
         
         while (a == exceptAreaNum)
         {
            a = randInt(0,areas.size()-1);
         }
         
         return areas.get(a);
     }
     
     
     private Date getRandomDate()
     {
         long randTimeStamp ,rangebegin, rangeend, diff ;
                 
         do
         {
            rangebegin = Timestamp.valueOf(MIN_DAY_IN_RANGE_STR).getTime();
            rangeend = Timestamp.valueOf(MAX_DAY_IN_RANGE_STR).getTime();
            diff = rangeend - rangebegin + 1;
            randTimeStamp = rangebegin + (long)(Math.random() * diff);
         }
        while((DateAndTime.retrieveDayOfTheWeek(new Date(randTimeStamp)) == 7) || (DateAndTime.retrieveDayOfTheWeek(new Date(randTimeStamp)) == 6));
        
        return new Date(randTimeStamp);
     }
     
     private Date getCreationDate()
     {
        long startCreationDate = Timestamp.valueOf(MIN_CREATION_DATE_STR).getTime(); //milliseconds
        double numOfMilliSecondsToAdd = (numOfAddedRides / NUM_OF_RIDES_CREATED_IN_DAY) * SECONDS_IN_DAY *1000;
        startCreationDate += numOfMilliSecondsToAdd;        
        return new Date(startCreationDate);
     }
     
     //inclusive in top value
     private int randInt(int min, int max) 
     {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
    
    public void populateRides(int num) {
        for (int i = 0; i < num; i++)
        {
            insertRandomRide();        
        }
    }
     
}

class Area
{
     String name;
     Double latMax;
     Double latMin;
     Double longMax;
     Double longMin;

     
     
     //latitude = north ~ 30-33 in israel
     //longitude = east ~ 34-35 in israel
     //order is longMin, latMin, longMax ,latMax
     //like 34.632459,31.778331,34.666092,31.798893
     
    public Area(String name, Double longMin, Double latMin,Double longMax ,Double latMax)
    {
        this.name = name;
        this.latMax = latMax;
        this.latMin = latMin;
        this.longMax = longMax;
        this.longMin = longMin;
    }
     
    Coordinate getRandomPointInTheArea()
    {
        Coordinate loc = new Coordinate();
                    
         Random rLang = new Random();
        loc.setLongitude((longMin + (longMax - longMin) * rLang.nextDouble())/100);
         
         Random rLat = new Random();
         loc.setLatitude((latMin + (latMax - latMin) * rLat.nextDouble())/100);
                      
         return loc;         
    }
}

