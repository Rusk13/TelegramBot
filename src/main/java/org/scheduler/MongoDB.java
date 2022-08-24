package org.scheduler;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.json.JSONObject;

public class MongoDB {

    MongoClientURI connectionString;
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;

    public String check(int user_id) {
        setNewConnection();
        long found = collection.countDocuments(Document.parse("{id : " + Integer.toString(user_id) + "}"));
        if (found == 0) {
//            Document doc = new Document("first_name", first_name)
//                    .append("last_name", last_name)
//                    .append("id", user_id)
//                    .append("username", username);
//            collection.insertOne(doc);
            mongoClient.close();
            System.out.println("User not exists in database.");
            return "no_exists";
        } else {
            System.out.println("User exists in database.");
            mongoClient.close();
            return "exists";
        }
    }

    public void addToDatabase(int user_id, String parentName, String childName, String username){
        setNewConnection();
        Document doc = new Document("parentName", parentName)
                .append("childName", childName)
                .append("id", user_id)
                .append("username", username);
        collection.insertOne(doc);
        mongoClient.close();
        System.out.println("Written.");
    }

    public void setNewConnection(){
        connectionString = new MongoClientURI("mongodb://localhost:27017");
        mongoClient = new MongoClient(connectionString);
        database = mongoClient.getDatabase("ScheduleDB");
        collection = database.getCollection("users");
    }

    public String getLastState(int user_id){
        setNewConnection();
        long found = collection.countDocuments(Document.parse("{id : " + Integer.toString(user_id) + "}"));
        if(found==0){
            return "null";
        }

        Document doc = collection.find(new BasicDBObject("id", user_id))
                .projection(Projections.fields(Projections.include("lastUserStat"), Projections.excludeId())).first();
        mongoClient.close();

        return doc.getString("lastUserStat");
    }

    public String getParentName(int user_id){
        setNewConnection();
        long found = collection.countDocuments(Document.parse("{id : " + Integer.toString(user_id) + "}"));
        if(found==0){
            return "null";
        }

        Document doc = collection.find(new BasicDBObject("id", user_id))
                .projection(Projections.fields(Projections.include("parentName"), Projections.excludeId())).first();
        mongoClient.close();

        return doc.getString("parentName");
    }

    public String getChildName(int user_id){
        setNewConnection();
        long found = collection.countDocuments(Document.parse("{id : " + Integer.toString(user_id) + "}"));
        if(found==0){
            return "null";
        }

        Document doc = collection.find(new BasicDBObject("id", user_id))
                .projection(Projections.fields(Projections.include("childName"), Projections.excludeId())).first();
        mongoClient.close();

        return doc.getString("childName");
    }

    public void setLastState(int user_id, String state){
        setNewConnection();
        long found = collection.countDocuments(Document.parse("{id : " + Integer.toString(user_id) + "}"));
        if(found==0){
            return;
        }
        Document doc = collection.find(new BasicDBObject("id", user_id))
                .projection(Projections.fields(Projections.include("lastUserStat"), Projections.excludeId())).first();
        collection.updateOne(new BasicDBObject("id", user_id), new BasicDBObject("$set", new BasicDBObject("lastUserStat", state)));
        mongoClient.close();
    }
}
