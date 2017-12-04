/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package riderequestsmatching;

import java.net.UnknownHostException;
import java.util.Date;
import mongodb.Mongo;
import process.matching.MatchingProcess;
//import process.populating.PopulatingProcess;
import process.rating.RatingProcess;

/**
 *
 * @author Vlada
 */
public class RideRequestsMatching {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, InterruptedException 
    {
       // Thread populatingThread;
        Thread matchingThread;
        Thread ratingThread;
        //PopulatingProcess populationOfRideRequests;
        MatchingProcess matchingProcess;
        RatingProcess ratingProcess;
        Mongo mongo = Mongo.getInstance();
        mongo.Connect();
                       
        if (mongo.isMongoConnected())
        {
            System.out.println("Starting local background proccess:");
            System.out.println("with parameters:");
            System.out.println("10h run");
            System.out.println("1 min rating period");
            System.out.println("no populating");
            
           Date stopDate = new Date(new Date().getTime() + 604800000); // 10 minutes later = 600_000 , 
           //Date populatingStopDate = new Date(new Date().getTime() + 300_000);
            
//           populationOfRideRequests  = new PopulatingProcess();
//           populatingThread = new Thread(populationOfRideRequests);
//           populatingThread.start();
            
           matchingProcess = new MatchingProcess();
           matchingThread = new Thread(matchingProcess);
           matchingThread.start();
           
//           ratingProcess = new RatingProcess();
//           ratingThread = new Thread(ratingProcess);
//           ratingThread.start();
           
           while(true)
           {  
              //System.out.println("****************************************   Check number " + i++);
              Thread.sleep(1000);
//              
//              if(new Date().after(populatingStopDate))
//                populationOfRideRequests.setStop();
//              
//              if (!populatingThread.isAlive() && new Date().before(populatingStopDate))
//              { 
//                  populatingThread.join();
//                  System.out.println("Population was not alive");
//                   populationOfRideRequests  = new PopulatingProcess();
//                   populatingThread = new Thread(populationOfRideRequests);
//                   populatingThread.start();
//              }
              
              if (!matchingThread.isAlive())
              {
                   matchingThread.join();
                  System.out.println("Matching was not alive");
                   matchingProcess = new MatchingProcess();
                   matchingThread = new Thread(matchingProcess);
                   matchingThread.start();
              }
              
//              if (!ratingThread.isAlive())
//              {
//                   ratingThread.join();
//                   System.out.println("Rating was not alive");
//                   ratingProcess = new RatingProcess();
//                   ratingThread = new Thread(ratingProcess);
//                   ratingThread.start();
//              }
           }
          
           //matchingProcess.setStop();
           //ratingProcess.setStop();
        }
    }
}
