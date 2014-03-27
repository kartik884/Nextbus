package com.nextbus.util;

import org.apache.log4j.jmx.Agent;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONArray;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ExtractData {
	
	WebResource resource; 
	Client client =  Client.create();
	ClientResponse clientresponse;
	JSONObject agencyobj = new JSONObject();
	MongoHandler mh = new MongoHandler();
	
	
	
	public JSONArray getagencyList()
	{
		resource = client.resource("http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList");
		
		clientresponse = resource.accept("application/xml").get(ClientResponse.class);
		
		JSONArray agencylist = null;
		if (clientresponse.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : "
				+ clientresponse.getStatus());//
			}
		
		String output = clientresponse.getEntity(String.class);
		try {
		JSONObject obj = XMLtoJSON(output);
		JSONObject bodyobj = obj.getJSONObject("body");
		agencylist = (JSONArray)bodyobj.get("agency");
		
		agencyobj.put("agencyList", agencylist);
						
		//put the list of agency as collections in mongodb
		//mh.insertAgencydb(agencylist);		
		//System.out.println("inserted agencies into collection... ");
		
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return agencylist;
	}
	
	public void getRouteList()
	{	
		String baseurl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=";
		JSONArray routelist;
		
		JSONArray agencylist;
		try{
			agencylist = agencyobj.getJSONArray("agencyList");
			
			for(int i=0;i<agencylist.length();i++)
			{
				JSONObject tobj = agencylist.getJSONObject(i);
				String agency = tobj.getString("tag");
				
				System.out.println("agency title: "+agency);
				String routeurl = baseurl + agency;
				resource = client.resource(routeurl);
				clientresponse = resource.accept("application/xml").get(ClientResponse.class);
				
				if (clientresponse.getStatus() != 200) {
					   throw new RuntimeException("Failed : HTTP error code : "
						+ clientresponse.getStatus());//
					}				
				String output = clientresponse.getEntity(String.class);
				
				JSONObject obj = XMLtoJSON(output);
				JSONObject bodyobj = obj.getJSONObject("body");
				if(bodyobj.has("route"))
				{
					if(bodyobj.get("route").getClass().equals(JSONArray.class))
					{
						routelist = (JSONArray)bodyobj.get("route");					
						tobj.put("routeList", routelist);					
						getRouteConfig(agency, tobj.getJSONArray("routeList"));
						//put the route into proper agency collection
						//mh.insertRouteToAgency(agency, routelist);					
						//System.out.println("Inserted agency :"+agency + " :into agency collection...");
					}
					else
					{
						System.out.println("");
						System.out.println("Error for agency: "+agency);
						System.out.println();
					}
					
				}
				
	
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void getRouteConfig(String agency,JSONArray routeList)
	{
		
		String baseurl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agency+"&r=";
		
		
		try{
			for(int i=0;i<routeList.length();i++)
			{
				JSONObject tobj = routeList.getJSONObject(i);
				String tag = tobj.getString("tag"); 
				
				System.out.println("route tag: "+tag);
				
				String routeconfigurl = baseurl + tag;
				resource = client.resource(routeconfigurl);
				clientresponse = resource.accept("application/xml").get(ClientResponse.class);
				
				if (clientresponse.getStatus() != 200) {
					   throw new RuntimeException("Failed : HTTP error code : "
						+ clientresponse.getStatus());//
					}				
				String output = clientresponse.getEntity(String.class);
				
				JSONObject obj = XMLtoJSON(output);
				JSONObject bodyobj = obj.getJSONObject("body");
				
				tobj.put("routeConfig", bodyobj);
				
				mh.insertRouteConfigToRoute(agency, tag, bodyobj);
								
				//add the agency obj to mongodb 				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	//public void 
		
	public JSONObject XMLtoJSON(String xml)
	{
		JSONObject jsonobj = null;
		try {
			jsonobj = XML.toJSONObject(xml);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonobj;
	}
	
	public static void main(String[] args)
	{
		ExtractData ed = new ExtractData();
		JSONArray arrlist = ed.getagencyList();		
		ed.getRouteList();
				
	}
}
