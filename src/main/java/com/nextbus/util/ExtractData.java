package com.nextbus.util;

import org.apache.log4j.jmx.Agent;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONArray;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ExtractData {
	
	WebResource resource; 
	Client client =  Client.create();
	ClientResponse clientresponse;
	JSONObject agencyobj = new JSONObject();
	
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
		
		//System.out.println("Agency obj: "+agencyobj.toString(4));
		//System.out.println("agency list: "+agencylist.toString(4));
		//System.out.println("Json obj: "+obj.toString(4));			
		
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
			
			for(int i=0;i<1;i++)
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
				routelist = (JSONArray)bodyobj.get("route");
				
				tobj.put("routeList", routelist);
				
				//agencyobj.put("routeList", routelist);
				System.out.println("Agency obj with route list : "+agencyobj.toString(4));
				//System.out.println("route list obj: "+routelist.toString(4));
				//System.out.println("route list "+output);		
				
				getRouteConfig(agency, routelist);
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
			for(int i=0;i<1;i++)
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
				
				//System.out.println("route Config obj"+obj.toString(4));
				
//				routelist = (JSONArray)bodyobj.get("route");
//				
//				System.out.println("obj: "+routelist.toString(4));
				//System.out.println("route config data "+output);				
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
