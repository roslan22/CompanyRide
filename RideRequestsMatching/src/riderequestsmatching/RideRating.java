package riderequestsmatching;

import java.net.UnknownHostException;
import java.util.Date;
import mongodb.Mongo;
import process.rating.RatingProcess;

/**
 *
 * @author Vlada
 */
public class RideRating {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException, InterruptedException
    {
        Thread ratingThread;
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

            //Date stopDate = new Date(new Date().getTime() + 604800000); // 10 minutes later = 600_000 ,
            ratingProcess = new RatingProcess();
            ratingThread = new Thread(ratingProcess);
            ratingThread.start();
            // run forever
            while(true)
            {

                Thread.sleep(1000);

                if (!ratingThread.isAlive())
                {
                    ratingThread.join();
                    System.out.println("Rating was not alive");
                    ratingProcess = new RatingProcess();
                    ratingThread = new Thread(ratingProcess);
                    ratingThread.start();
                }
            }

            //ratingProcess.setStop();
        }
    }
}

