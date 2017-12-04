/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongodb;

import com.mongodb.DBCollection;

/**
 *
 * @author Vlada
 */
public enum Collections {
    RIDEREQUESTS("rideRequests"),
    RIDES("rides"), USERS("users"),
    USERPROFILES("userProfiles"),
    STATISTICS("statistics");

    private String name;
    private DBCollection collection;

    private Collections(String name)
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public DBCollection getCollection() {
        return collection;
    }
    
    public void setCollection(DBCollection collection) {
        this.collection = collection;
    }
}
