package com.sipgate.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.sipgate.exceptions.OAuthAccessProtectedResourceException;
import com.sipgate.exceptions.OAuthAuthorizeException;
import com.sipgate.exceptions.OAuthMissingContextException;
import com.sipgate.exceptions.OAuthObtainAccessTokenException;
import com.sipgate.exceptions.OAuthObtainRequestTokenException;
import com.sipgate.exceptions.OAuthRegisterException;
import com.sipgate.exceptions.OAuthUnregisterException;
import com.sipgate.util.Constants;

public class Oauth {
	private static final String OAUTH_ACCESSTOKEN_NAME = "token";
	private static final String OAUTH_ACCESSSECRET_NAME = "secret";
	private static final String OAUTH_TEMP_REQUESTTOKEN_NAME = "request_token";
	private static final String OAUTH_TEMP_REQUESTSECRET_NAME = "request_secret";

	private SharedPreferences preferences = null;

	private static OAuthAccessor accessor = null; // Note: Don't access this directly but through getAccessor() !!

	private static Context context = null;
	
	private static Oauth singleton;

	private Oauth(Context context) {
		super();

		this.preferences = context.getSharedPreferences(Constants.PREFERENCES_NAME, 0);

		Oauth.context = context;
	}

	synchronized public static Oauth getInstance(Context context) {
		if (Oauth.singleton == null) {
			Oauth.singleton = new Oauth(context.getApplicationContext());
		}

		return Oauth.singleton;
	}
	
	synchronized public static Oauth getInstance() throws OAuthMissingContextException {
		if (Oauth.context == null) {
			throw new OAuthMissingContextException();
		}

		return Oauth.getInstance(Oauth.context);
	}
	
	public void authorize() throws OAuthAuthorizeException {
		// get request token
		OAuthTokenPair requestPair;
		try {
			Log.d(this.getClass().getSimpleName(), "authorize() fetching request token ...");
			requestPair = this.obtainRequestToken();
			Log.d(this.getClass().getSimpleName(), "authorize() got token "+requestPair.toString());
		} catch (OAuthObtainRequestTokenException e) {
			throw new OAuthAuthorizeException();
		}
		// store request token for later access after resume (note the
		// callback!)
		SharedPreferences.Editor editor = this.preferences.edit();
		editor.putString(OAUTH_TEMP_REQUESTTOKEN_NAME, requestPair.getToken());
		editor.putString(OAUTH_TEMP_REQUESTSECRET_NAME, requestPair.getSecret());
		if (!editor.commit()) {
			throw new OAuthAuthorizeException();
		}

		// authorize request token (opens the browser)
		authorizeRequestToken(requestPair);
	}

	public boolean registrationInProgress() {
		String token = this.preferences.getString(OAUTH_TEMP_REQUESTTOKEN_NAME, "");
		String secret = this.preferences.getString(OAUTH_TEMP_REQUESTSECRET_NAME, "");

		if (token.length() > 0 && secret.length() > 0) {
			return true;
		}

		return false;
	}

	public void register(Intent callbackIntent) throws OAuthRegisterException {
		OAuthTokenPair accessPair;
		try {
			OAuthTokenPair requestPair = new OAuthTokenPair(this.preferences
					.getString(OAUTH_TEMP_REQUESTTOKEN_NAME, ""), this.preferences.getString(
					OAUTH_TEMP_REQUESTSECRET_NAME, ""));
			accessPair = this.obtainAccessToken(requestPair, callbackIntent);
		} catch (Exception e) {
			throw new OAuthRegisterException();
		}

		// store token
		SharedPreferences.Editor editor = this.preferences.edit();
		editor.clear(); // remove any temp. or old data
		editor.putString(OAUTH_ACCESSTOKEN_NAME, accessPair.getToken());
		editor.putString(OAUTH_ACCESSSECRET_NAME, accessPair.getSecret());
		if (!editor.commit()) {
			throw new OAuthRegisterException();
		}
	}

	public void unRegister() throws OAuthUnregisterException {
		SharedPreferences.Editor editor = this.preferences.edit();

		// clear all (!) preferences
		editor.clear();
		if (!editor.commit()) {
			throw new OAuthUnregisterException();
		}
		
		Oauth.accessor = null;
	}

	public boolean isRegistered() {
		String token = this.preferences.getString(OAUTH_ACCESSTOKEN_NAME, "");
		String secret = this.preferences.getString(OAUTH_ACCESSSECRET_NAME, "");

		if (token.length() > 0 && secret.length() > 0) {
			return true;
		}

		return false;
	}

	public OAuthMessage accessProtectedResource(String url, Collection<? extends Entry> parameters) throws IOException,
			OAuthException, URISyntaxException, OAuthAccessProtectedResourceException {
		return this.accessProtectedResource("GET", url, parameters);
	}

	public OAuthMessage accessProtectedResource(String httpMethod, String url, Collection<? extends Entry> parameters)
			throws IOException, OAuthException, URISyntaxException, OAuthAccessProtectedResourceException {
		if (parameters == null) {
			throw new OAuthAccessProtectedResourceException("parameters must not be null");
		}
		
		if (url.contains("?")) {
			url += "&"+Constants.API_VERSION_SUFFIX; // TODO FIXME
		} else {
			url += "?"+Constants.API_VERSION_SUFFIX; // TODO FIXME
		}
		
		Log.d("OAuth", "will do a '"+httpMethod+"' on '"+url+"'");
		
		OAuthMessage response = null;

		OAuthClient client = new OAuthClient(new HttpClient4());

		try {
			do {
				try {
					response = client.invoke(this.getAccessor(), httpMethod, url, parameters);
				} catch (net.oauth.OAuthProblemException e) {
					// handle redirects
					if (e.getHttpStatusCode() == 307) {
						url = (String) e.getParameters().get("Location");
					} else {
						throw e;
					}
				}
			} while (response == null);
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "accessProtectedResource(): "+e.getLocalizedMessage());
			throw new OAuthAccessProtectedResourceException(e.getLocalizedMessage());
		}

		return response;
	}

	private OAuthTokenPair obtainRequestToken() throws OAuthObtainRequestTokenException {
		OAuthTokenPair requestPair = null;
		OAuthClient client = new OAuthClient(new HttpClient4());
		try {
			client.getRequestToken(this.getAccessor());
			requestPair = new OAuthTokenPair(this.getAccessor().requestToken, this.getAccessor().tokenSecret);
		} catch (Exception e) {
			throw new OAuthObtainRequestTokenException();
		}

		return requestPair;
	}

	private void authorizeRequestToken(OAuthTokenPair requestPair) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(this.getAccessor().consumer.serviceProvider.userAuthorizationURL + "?oauth_token="
				+ this.getAccessor().requestToken + "&oauth_callback=" + this.getAccessor().consumer.callbackURL));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Oauth.context.startActivity(i);
	}

	public boolean isOauthIntent(Intent callbackIntent) {
		boolean res = false;
		Uri uri = callbackIntent.getData();
		if (uri != null) {
			if (uri.getQueryParameter("oauth_verifier").length() > 0) {
				res = true;
			}
		}
		
		return res;
	}
	
	private OAuthTokenPair obtainAccessToken(OAuthTokenPair requestPair, Intent callbackIntent)
			throws OAuthObtainAccessTokenException {
		OAuthTokenPair accessTokenPair = null;

		// extract the OAUTH verifier if it exists
		String verifier = null;
		Uri uri = callbackIntent.getData();
		if (uri != null) {
			verifier = uri.getQueryParameter("oauth_verifier");
		} else {
			throw new OAuthObtainAccessTokenException();
		}

		// get access token
		ArrayList<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
		params.add(new OAuth.Parameter("oauth_verifier", verifier));
		OAuthClient client = new OAuthClient(new HttpClient4());
		OAuthMessage accessTokenResponse;
		try {
			accessTokenResponse = client.getAccessToken(this.getAccessor(), "GET", params);

			accessTokenPair = new OAuthTokenPair(accessTokenResponse.getParameter("oauth_token"), accessTokenResponse
					.getParameter("oauth_token_secret"));
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "obtainAccessToken(): "+e.getLocalizedMessage());
			throw new OAuthObtainAccessTokenException();
		}

		Log.d(this.getClass().getSimpleName(), "obtainAccessToken(): got access token "+accessTokenPair.toString());
		
		return accessTokenPair;
	}
	
	private OAuthAccessor getAccessor() {
		if (Oauth.accessor == null) {
			//String selectedDomain = this.preferences.getString("SELECTED_DOMAIN", "sipgate.com");
			String request_url = Constants.OAUTH_REQUEST_URL;
			String authorization_url = null;
			authorization_url = Constants.OAUTH_POSSIBLE_AUTHORIZATION_URL_DE;
			String access_url = Constants.OAUTH_ACCESS_URL;

			// initialize accessor
			OAuthServiceProvider provider = new OAuthServiceProvider(request_url, authorization_url, access_url);
			OAuthConsumer consumer = new OAuthConsumer(Constants.CALLBACK_URL, Constants.CONSUMER_KEY,
					Constants.CONSUMER_SECRET, provider);
			Oauth.accessor = new OAuthAccessor(consumer);


			// initialize oauth client with access token and secret (if available!)
			String token = this.preferences.getString(OAUTH_ACCESSTOKEN_NAME, "");
			String secret = this.preferences.getString(OAUTH_ACCESSSECRET_NAME, "");
			if (token.length() > 0 && secret.length() > 0) {
				Oauth.accessor.accessToken = token;
				Oauth.accessor.tokenSecret = secret;
			}
		}

		return Oauth.accessor;
	}
}
