/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.populating;

import mongodb.Mongo;

/**
 *
 * @author Vlada
 */

public class PopulatingProcess implements Runnable{
    
    PopulationExecutor populationExecutor = new PopulationExecutor();
    boolean stopCommand = false;
    
    public void setStop()
    {
        // System.out.println("set stop on populating");
        stopCommand = true;
    }
    
    @Override
    public void run()
    {
        try
        {
            while(!stopCommand)
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch(Exception ex)
                {
                    System.out.println("Unable to set populating thread to sleep ");
                }
                populationExecutor.populateRides(200);
                System.out.println("Added 200 ride requests");
            }    
        }
        finally
        {
              System.out.println("Stoped populating");
        }
    }
}
