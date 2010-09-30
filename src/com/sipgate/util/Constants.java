package com.sipgate.util;


public abstract class Constants {
	public static final String PREFERENCES_NAME = "OAUTH_PREFERENCES";

	// com live ...
	public static final String CONSUMER_KEY = "b833354ca5e4d5ec07416846a3392b47";
	public static final String CONSUMER_SECRET = "aa2385003b1c3dd16a25a65133a87ccd";
	// de dev ...
	//public static final String CONSUMER_KEY = "d7ae074131cfaeb697547137b6a531c6";
	//public static final String CONSUMER_SECRET = "82abc623d8d25629c35ab17ca189f13b";	
	//public static final String CONSUMER_KEY = "8ef2efde62a9741f5668e367573bfaac"; // karsten
	//public static final String CONSUMER_SECRET = "f555f8b2216e5fdb475bff29f4c5372e"; // karsten
	
//	public static final String CONSUMER_KEY = "60db8ff6c79e3e1ea63490ae04e768bc"; // moritz
//	public static final String CONSUMER_SECRET = "06f20ac831a708d057c76c77262b297c"; // moritz
	
	public static final String CALLBACK_URL  = "icecondor-android-app:///";
	
	// live
	
	// dev
	//public static final String OAUTH_REQUEST_URL = "http://samurai01.dev.sipgate.net:8080/oauth/request_token";
	//public static final String OAUTH_ACCESS_URL = "http://samurai01.dev.sipgate.net:8080/oauth/access_token";
	//public static final String OAUTH_REQUEST_URL = "http://halvar.netzquadrat.de:8082/oauth/request_token";
	//public static final String OAUTH_ACCESS_URL = "http://halvar.netzquadrat.de:8082/oauth/access_token";

	
	public static final String MP3_DOWNLOAD_DIR = "/sdcard/sipgateVoicemail";

	public static String REQUEST_TOKEN = "";
	public static String OAUTH_VERIFIER = "";
	public static String ACCESS_TOKEN = "";
	public static String OAUTH_TOKEN_SECRET = "";

	public static final String API_VERSION_SUFFIX = "version=2.17";  // TODO FIXME

	public static final String API_BASEURL =  "https://api.sipgate.net";
	public static final String API_10_HOSTNAME = "samurai.sipgate.net";
	public static final String API_10_BASEURL =  "https://" + API_10_HOSTNAME;
	public static final String API_20_HOSTNAME = "api.dev.sipgate.net";
	public static final String API_20_BASEURL =  "http://" + API_20_HOSTNAME;
	
	public static final String OAUTH_REQUEST_URL = API_20_BASEURL + "/oauth/request_token";
	public static final String OAUTH_ACCESS_URL = API_20_BASEURL + "/oauth/access_token";

	public static final String XML_BASEURL = API_20_BASEURL + "/my";
	public static final String XML_URL = XML_BASEURL + "/events/voicemails/?complexity=full";
	public static final String VOICEMAIL_URL = XML_BASEURL + "/events/voicemails/?complexity=full";
	public static final String EVENT_URL = XML_BASEURL + "/events/?complexity=full";
	public static final String VOICEMAILREAD_URL = XML_BASEURL + "/events/voicemails/%s/read";
	public static final String EXTENSION_URL = XML_BASEURL + "/settings/extensions/";
	//public static final String XML_URL = "http://samurai01.dev.sipgate.net:8080/my/events/voicemails/?complexity=full";

//	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COM = "https://secure.dev.sipgate.com/oauth/authorize/";
//	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_DE = "https://secure.dev.sipgate.de/oauth/authorize/";
//	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COUK = "https://secure.dev.sipgate.co.uk/oauth/authorize/";

	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COM = "https://secure.live.sipgate.com/oauth/authorize/";
	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_DE = "https://secure.live.sipgate.de/oauth/authorize/";
	public static final String OAUTH_POSSIBLE_AUTHORIZATION_URL_COUK = "https://secure.live.sipgate.co.uk/oauth/authorize/";

//	public static final String REST_API_SERVER_URL = "http://api.sipgate.net";
//	public static final String XMLRPC_API_SERVER_URL = "http://api.sipgate.net";

	public static final String REST_API_20_SERVER_URL = API_20_BASEURL;
	public static final String XMLRPC_API_10_SERVER_URL = API_10_BASEURL + "/RPC2";

	public static final long ONE_DAY_IN_MS = 86400000;
}
