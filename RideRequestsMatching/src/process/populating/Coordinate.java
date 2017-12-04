/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process.populating;

/**
 *
 * @author Vlada
 */

public class Coordinate
{
    private double longitude;
    private double latitude;

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }    

    Coordinate() {}
    
    public void setLatitude(double  latitude)
    {
     this.latitude = latitude;
    }
    
    public void setLongitude(double  longitude)
    {
     this.longitude = longitude;
    }
    
    public double getLatitude()
    {
        return latitude;
    }
   
        public double getLongitude()
    {
        return longitude;
    }
    
}
