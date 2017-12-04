/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.rating;

import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Vlada
 */
public class RatingProcess implements Runnable{
    private final static String LOG_TAG = "Rating Process: ";
    boolean stopCommand = false;
    RatingExecutor executor = new RatingExecutor();
        
    public void setStop()
    {
        System.out.println(LOG_TAG + "set stop on rating");
        stopCommand = true;
    }
    
   @Override
   public void run()
   {
       try
        {
            while(!stopCommand)
            {
                if(executor.hasPeriodChanged())
                {
                    System.out.println(LOG_TAG + "Period changed! ");
                    executor.getNewBatchOfPastPeriodRides();
                    executor.sendRatingToPastRides();
                    executor.updatePeriodStartStop();
                }    
                
                Thread.sleep(10000);
            }    
        }
        catch (Exception ex) {
            Logger.getLogger(RatingProcess.class.getName()).log(Level.SEVERE, null, ex);
        }        
       finally
        {
              System.out.println(LOG_TAG + "Stopped rating");
        }
   } 
}
