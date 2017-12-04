/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils.schemas;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import mongodb.Collections;
import mongodb.Mongo;
import org.bson.types.ObjectId;

/**
 *
 * @author Vlada
 */
public class UserProfile {
        ObjectId _id;
    String fullName;
    String occupationTitle;
//    int numOfRidesAsHitcher;
//    int numOfRidesAsDriver;
//    int rating;
    
    
    public String getFullName()
    {
        return fullName;
    }
    public String getOccupationTitle()
    {
        return occupationTitle;
    }
    
    public UserProfile(String fullName, String occupationTitle)
    {
        this.fullName = fullName;
        this.occupationTitle = occupationTitle;
    }       
    
    public static UserProfile getUserProfileById(String id)
    {
        ObjectId _id = new ObjectId(id);
        BasicDBObject queryForHitcherProfile = new BasicDBObject("_id",_id);        
        DBCursor cursor = Mongo.getInstance().findDocuments(Collections.USERPROFILES, queryForHitcherProfile);      
               
        if (cursor.count() != 1)
        {
            System.out.println("");
           return null;
        }
        else
        {
            DBObject hitcherProfileDoc = cursor.next();
            
            String fullName = (String)hitcherProfileDoc.get("fullName");
            String occupationTitle = (String)hitcherProfileDoc.get("occupationTitle");
            return new UserProfile(fullName, occupationTitle);
        }
    
    
    }
}
