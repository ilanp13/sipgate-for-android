package com.sipgate.util;


public abstract class Constants 
{
	public static final String API_VERSION_SUFFIX = "version=2.19";
		
	//public static final String API_10_HOSTNAME = "telegraf.netzquadrat.de:3333";
	//public static final String API_10_BASEURL =  "http://" + API_10_HOSTNAME;

	//public static final String API_20_HOSTNAME = "telegraf.netzquadrat.de:8082";
	//public static final String API_20_BASEURL =  "http://" + API_20_HOSTNAME;

	public static final String CALLBACK_URL  = "icecondor-android-app:///";
	
	public static final String MP3_DOWNLOAD_DIR = "/sdcard/sipgateVoicemail";

	public static String REQUEST_TOKEN = "";
	public static String OAUTH_VERIFIER = "";
	public static String ACCESS_TOKEN = "";
	public static String OAUTH_TOKEN_SECRET = "";

	public static final String API_BASEURL =  "https://api.sipgate.net";
	public static final String API_10_HOSTNAME = "samurai.sipgate.net";
	public static final String API_10_BASEURL =  "https://" + API_10_HOSTNAME;

	public static final String API_20_HOSTNAME = "api.staging.sipgate.net";
	public static final String API_20_BASEURL =  "https://" + API_20_HOSTNAME;
	
	public static final String OAUTH_REQUEST_URL = API_20_BASEURL + "/oauth/request_token";
	public static final String OAUTH_ACCESS_URL = API_20_BASEURL + "/oauth/access_token";

	public static final String XML_BASEURL = API_20_BASEURL + "/my";
	public static final String XML_URL = XML_BASEURL + "/events/voicemails/?complexity=full";
	public static final String VOICEMAIL_URL = XML_BASEURL + "/events/voicemails/?complexity=full";
	public static final String EVENT_URL = XML_BASEURL + "/events/?complexity=full";
	public static final String VOICEMAILREAD_URL = XML_BASEURL + "/events/voicemails/%s/read";
	public static final String EXTENSION_URL = XML_BASEURL + "/settings/extensions/";

	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COM = "https://secure.live.sipgate.com/oauth/authorize/";
	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_DE = "https://secure.live.sipgate.de/oauth/authorize/";
	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COUK = "https://secure.live.sipgate.co.uk/oauth/authorize/";

	public static final String REST_API_20_SERVER_URL = API_20_BASEURL;
	public static final String XMLRPC_API_10_SERVER_URL = API_10_BASEURL + "/RPC2";

	public static final long ONE_DAY_IN_MS = 86400000;
}
