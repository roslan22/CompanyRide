/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.matching;

import com.mongodb.DBObject;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mongodb.Mongo;
import properties.Parameter;

/**
 *
 * @author Vlada
 */
public class MatchingProcess implements Runnable{
    
    LinkedList<DBObject> oneTimeHitchersDocuments;
    LinkedList<DBObject> reoccuringHitchersDocuments;
    LinkedList<DBObject> unmatchedHitchersDocuments;
    MatchingExecutor executor = new MatchingExecutor();   
    boolean stopCommand = false;
    
    public void setStop()
    {
        System.out.println("set stop on matching");
        stopCommand = true;
    }
    
    
    @Override
    public void run() {   
       boolean matchFound;
       Mongo mongo = Mongo.getInstance();
        
       try
       {       
            while(!stopCommand)
            {
                Thread.sleep(10000);
                
                 oneTimeHitchersDocuments = executor.getOneTimeHitchersDocuments();
                 reoccuringHitchersDocuments = executor.getReoccuringHitchersDocuments();
                 unmatchedHitchersDocuments = executor.getUnmatchedHitchersDocuments();

                 for (DBObject doc : reoccuringHitchersDocuments) 
                 {    
                     mongo.incrementCounter(Parameter.TOTAL_NUM_OF_HITCHER_RIDE_REQUESTS);
                     matchFound = executor.matchHitcherRideRequestWithDriverRideRequests(doc);
                     if (!matchFound) executor.matchHitcherRideRequestWithRides(doc);
                 } 

                 System.out.println("----------------");

                 for (DBObject doc : unmatchedHitchersDocuments) 
                 {    
                      matchFound =  executor.matchHitcherRideRequestWithDriverRideRequests(doc);
                      if (!matchFound) executor.matchHitcherRideRequestWithRides(doc);
                 } 

                 System.out.println("----------------");

                 for (DBObject doc : oneTimeHitchersDocuments) 
                 {    
                     mongo.incrementCounter(Parameter.TOTAL_NUM_OF_HITCHER_RIDE_REQUESTS);
                     matchFound = executor.matchHitcherRideRequestWithDriverRideRequests(doc);
                     if (!matchFound) executor.matchHitcherRideRequestWithRides(doc);
                 } 

                 executor.emptyOneTimeHitchersDocuments();
                 executor.emptyReoccuringHitchersDocuments();
                 executor.emptyUnmatchedHitchersDocuments();
             }
       }
        catch (InterruptedException ex) {
            Logger.getLogger(MatchingProcess.class.getName()).log(Level.SEVERE, null, ex);
        }       finally
       {       
             System.out.println("Matching process gets out");
       }
    }    
}
