package com.nextbus.util;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class MongoHandler {
	
	DB db = initializemongo();
	DBCollection agencycoll = db.getCollection("agency");
	DBCollection route_config_coll = db.getCollection("route_config");
	
	public DB initializemongo()
	{
		Mongo mongo=null;
		try{
			mongo = new Mongo("localhost",27017);
		}catch(UnknownHostException uhe){
			uhe.printStackTrace();
		}
		DB db = mongo.getDB("nextbus");
		
		return db;		
	}	
	//
	public  void insertAgencydb(JSONArray agencylist)

	{		 
		try
		{					
			db.createCollection("agency", null);
			
			//DBCollection agencycoll = db.getCollection("agency");
			for(int i=0;i<agencylist.length();i++)
			{
				DBObject dbobj = (DBObject) JSON.parse(agencylist.get(i).toString()) ;
				agencycoll.insert(dbobj);		
								
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		//put the list of agency as collections in mongodb
				
		
	}
	
	public void insertRouteToAgency(String agency,JSONArray jarr)
	{
		
		DBObject routelist = (DBObject) JSON.parse(jarr.toString());
		
		BasicDBObject bdb = new BasicDBObject();
		bdb.append("$set", new BasicDBObject("routes",routelist));		
		BasicDBObject searchquery = getQuery("tag", agency);
		
		
		agencycoll.update(searchquery, bdb);
	}
	
	public void insertRouteConfigToRoute(String agency,String routetag, JSONObject bodyobj)
	{
		try{
		
			DBObject routeconfigobj = (DBObject) JSON.parse(bodyobj.toString());
			
			bodyobj.put("agency", agency);
			bodyobj.put("route_tag", routetag);
			
			//System.out.println("bodyobj: "+bodyobj.toString(4));
			
			DBObject dbobj = (DBObject) JSON.parse(bodyobj.toString());					
			route_config_coll.insert(dbobj);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public BasicDBObject getQuery(String field,String value) {
	    BasicDBObject query = new BasicDBObject();
	    query.put(field, value);
	    return query;
	}
	
	public void addlocationfieldToRC()
	{
		try{
		
			DBCursor cursor = route_config_coll.find();
			
			while(cursor.hasNext())
			{
				DBObject dbobj = (DBObject)cursor.next();
				BSONObject jobj = (BSONObject)dbobj.get("route");
				
				String lonMin = jobj.get("lonMin").toString();
				System.out.println(lonMin);
//				String latMin = jobj.getString("latMin");
//				String lonMax = jobj.getString("lonMax");
//				String latMax = jobj.getString("latMax");
//				
//				BasicDBObject newobj = new BasicDBObject();
//				JSONArray locminarr ;				
//				newobj.append("locMin", "{"+lonMin+","+latMin+"}");
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args)
	{
		
		MongoHandler mh = new MongoHandler();
		mh.addlocationfieldToRC();
	}
	
}
