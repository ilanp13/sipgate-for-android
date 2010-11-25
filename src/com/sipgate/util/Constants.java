package com.sipgate.util;


public abstract class Constants 
{
	public static final String API_VERSION_SUFFIX = "version=2.19";
		
	//public static final String API_10_HOSTNAME = "telegraf.netzquadrat.de:3333";
	//public static final String API_10_BASEURL =  "http://" + API_10_HOSTNAME;

	//public static final String API_20_HOSTNAME = "telegraf.netzquadrat.de:8082";
	//public static final String API_20_BASEURL =  "http://" + API_20_HOSTNAME;

	public static final String API_10_HOSTNAME = "samurai.dev.sipgate.net";
	public static final String API_10_BASEURL =  "https://" + API_10_HOSTNAME;

	public static final String API_20_HOSTNAME = "api.dev.sipgate.net";
	public static final String API_20_BASEURL =  "http://" + API_20_HOSTNAME;
		
	public static final String REST_API_20_SERVER_URL = API_20_BASEURL;
	public static final String XMLRPC_API_10_SERVER_URL = API_10_BASEURL + "/RPC2";

	public static final long ONE_DAY_IN_MS = 86400000;
}
