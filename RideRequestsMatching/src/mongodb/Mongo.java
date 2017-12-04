/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import java.net.UnknownHostException;
import java.util.Arrays;
import static mongodb.Collections.*;

import org.bson.BSON;
import org.bson.types.ObjectId;
import properties.Parameter;

/**
 *
 * @author Vlada
 */
public class Mongo 
{
    private static Mongo instance = new Mongo();
    private DB db = null;    
    Boolean isConnected = false;
    
    public static Mongo getInstance()
    {
        return instance;
    }
    
    private Mongo(){}
    
    public void Connect() throws UnknownHostException
    {   
        //local connection
        //MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
         
        //server connection
        MongoCredential credential = MongoCredential.createCredential("mongo_user", "CompanyRide", "mongo123".toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress("localhost" , 27017), Arrays.asList(credential));

         db = mongoClient.getDB("CompanyRide" );
         
        defineCollections();
        validateStatisticalModule();
        
        isConnected = true;
        //RIDEREQUESTS.getCollection().createIndex(new BasicDBObject("from.coordinates","2dsphere"),"fromIndex");
        //RIDES.getCollection().createIndex(new BasicDBObject("from.coordinates","2dsphere"),"fromIndex");
    }

    
    private void validateStatisticalModule()
    {
        BasicDBObject searchQuery = new BasicDBObject("name", Parameter.COUNTER_DOC_NAME);
        DBCursor cursor = findDocuments(STATISTICS,searchQuery);
        if (cursor.size() == 0)
        {
            DBObject counterDocument = new BasicDBObject("name", Parameter.COUNTER_DOC_NAME)
                    .append(Parameter.NUM_OF_MATCHES_EXISTING_RIDE , 0)
                    .append(Parameter.NUM_OF_MATCHES_FIRST_HITCHER, 0)
                    .append(Parameter.TOTAL_NUM_OF_HITCHER_RIDE_REQUESTS, 0)
                    .append(Parameter.TOTAL_NUM_OF_DRIVER_RIDE_REQUESTS, 0);
            insertDocument(STATISTICS, counterDocument);
        }
        else if (cursor.size() != 1)
            throw new RuntimeException("Multiple counter documents in statistics collection");
    }
    
    private void defineCollections()
    {
                
        db.createCollection(RIDEREQUESTS.getName(), null);
        RIDEREQUESTS.setCollection(db.getCollection(RIDEREQUESTS.getName())); 
        
        db.createCollection(RIDES.getName(), null);
        RIDES.setCollection(db.getCollection(RIDES.getName())); 
        
        db.createCollection(USERS.getName(), null);
        USERS.setCollection(db.getCollection(USERS.getName())); 
        
        db.createCollection(USERPROFILES.getName(), null);
        USERPROFILES.setCollection(db.getCollection(USERPROFILES.getName()));
        
        db.createCollection(STATISTICS.getName(), null);
        STATISTICS.setCollection(db.getCollection(STATISTICS.getName()));        
    }
    
    public synchronized Boolean isMongoConnected()
    {
        return isConnected;
    }
    
    public synchronized DBCursor findDocuments(Collections collection, BasicDBObject query)
    {
        DBCursor cursor = null;
      
        switch(collection)
        {
            case RIDEREQUESTS:      {cursor = RIDEREQUESTS.getCollection().find(query); break;}
            case RIDES:             {cursor = RIDES.getCollection().find(query); break;}
            case USERPROFILES:      {cursor = USERPROFILES.getCollection().find(query); break;}
            case USERS:             {cursor = USERS.getCollection().find(query); break;}
            case STATISTICS:        {cursor = STATISTICS.getCollection().find(query); break;}
            default:                throw new RuntimeException("Unknown collection");
        } 
        
        return cursor;
    }

    public synchronized DBCursor findDocuments(Collections collection, DBObject query)
    {
        DBCursor cursor = null;

        switch(collection)
        {
            case RIDEREQUESTS:      {cursor = RIDEREQUESTS.getCollection().find(query); break;}
            case RIDES:             {cursor = RIDES.getCollection().find(query); break;}
            case USERPROFILES:      {cursor = USERPROFILES.getCollection().find(query); break;}
            case USERS:             {cursor = USERS.getCollection().find(query); break;}
            case STATISTICS:        {cursor = STATISTICS.getCollection().find(query); break;}
            default:                throw new RuntimeException("Unknown collection");
        }

        return cursor;
    }
    
    public synchronized void insertDocument(Collections collection, DBObject document)
    { 
        switch(collection)
        {
            case RIDEREQUESTS:      { RIDEREQUESTS.getCollection().insert(document); break;}
            case RIDES:             { RIDES.getCollection().insert(document); break;}
            case USERPROFILES:      { USERPROFILES.getCollection().insert(document); break;}
            case USERS:             { USERS.getCollection().insert(document); break;}
            case STATISTICS:        { STATISTICS.getCollection().insert(document); break;}
            default:                throw new RuntimeException("Unknown collection");
        }
    }
    
    public synchronized void updateFirstLevelSimpleField(Collections collection, String docId, String key, String value)
    {
        BasicDBObject doc = new BasicDBObject();
	doc.append("$set", new BasicDBObject().append(key, value));
 
	BasicDBObject searchQuery = new BasicDBObject().append("_id",new ObjectId(docId));
 
         switch(collection)
        {
            case RIDEREQUESTS:      { RIDEREQUESTS.getCollection().update(searchQuery, doc);  break;}
            case RIDES:             { RIDES.getCollection().update(searchQuery, doc);  break;}
            case USERPROFILES:      { USERPROFILES.getCollection().update(searchQuery, doc);  break;}
            case USERS:             { USERS.getCollection().update(searchQuery, doc);  break;}
            default:                throw new RuntimeException("Unknown collection");
        }   
    }
    
    
    //docToPush in form of {arrayFieldName: valueToPush}
    public synchronized void pushToArray(Collections collection, String docId, BasicDBObject docToPush)
    {
        BasicDBObject doc = new BasicDBObject("$push", docToPush);
        BasicDBObject searchQuery = new BasicDBObject().append("_id",new ObjectId(docId));
        update(collection, searchQuery, doc);
    }
    
    
    public synchronized void incrementCounter(String counterName)
    {
        DBObject incQuery = new BasicDBObject("$inc", new BasicDBObject(counterName, 1));        
        DBObject searchQuery = new BasicDBObject("name",Parameter.COUNTER_DOC_NAME );
        update(STATISTICS, searchQuery, incQuery);
    }
    
    public synchronized void update(Collections collection, DBObject searchQuery, DBObject updateQuery )
    {        
        switch(collection)
        {
            case RIDEREQUESTS:      { RIDEREQUESTS.getCollection().update(searchQuery, updateQuery);  break;}
            case RIDES:             { RIDES.getCollection().update(searchQuery, updateQuery);  break;}
            case USERPROFILES:      { USERPROFILES.getCollection().update(searchQuery, updateQuery);  break;}
            case USERS:             { USERS.getCollection().update(searchQuery, updateQuery);  break;}
            case STATISTICS:        { STATISTICS.getCollection().update(searchQuery, updateQuery);  break;}
            default:                throw new RuntimeException("Unknown collection");
        }   
    }
           
}


