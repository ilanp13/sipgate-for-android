package com.sipgate.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import android.util.Log;

import com.sipgate.exceptions.AccessProtectedResourceException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.exceptions.RestClientException;
import com.sipgate.interfaces.RestAuthenticationInterface;

public class BasicAuthenticationClient implements RestAuthenticationInterface
{
	@SuppressWarnings("unused")
	private DefaultRedirectHandler redirectHandler = new DefaultRedirectHandler();
	private final String TAG = "BasicAuthenticationClient";
	private String user = null;
	private String pass = null;
	private final static SimpleDateFormat periodFormatter  = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

	public BasicAuthenticationClient(String user, String pass)
	{
		super();
		this.user = user;
		this.pass = pass;
	}

	private InputStream accessProtectedResource(String url) throws AccessProtectedResourceException, AuthenticationErrorException, NetworkProblemException
	{
		return this.accessProtectedResource("GET", url);
	}

	private InputStream accessProtectedResource(String httpMethod, String url) throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(httpMethod, url, null);
	}

	@SuppressWarnings("unchecked")
	private String appendUrlParameters(String url, Collection<? extends Entry> params)
	{
		StringBuilder sb = new StringBuilder(url);
		if (params != null)
		{
			Iterator<? extends Entry> i = params.iterator();
			while (i.hasNext())
			{
				Entry entry = (Entry) i.next();
				sb.append("&" + entry.getKey());
				sb.append("=" + entry.getValue());
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private HttpPost createPostRequest(String url, Collection<? extends Entry> params)
	{
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
		Iterator<? extends Entry> i = params.iterator();
		while (i.hasNext())
		{
			Entry e = i.next();
			nameValuePairs.add(new BasicNameValuePair((String) e.getKey(), (String) e.getValue()));
		}
		try
		{
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		return httpPost;
	}

	@SuppressWarnings("unchecked")
	private InputStream accessProtectedResource(String httpMethod, String urlString, Collection<? extends Entry> params) throws AccessProtectedResourceException, NetworkProblemException
	{
		URL url;
		
		try
		{
			url = new URL(urlString);
		}
		catch (MalformedURLException e1)
		{
			throw new AccessProtectedResourceException();
		}
		
		String username = this.user;
		String password = this.pass;
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT), new UsernamePasswordCredentials(username, password));
	
		if (urlString.contains("?"))
		{
			urlString += "&" + Constants.API_VERSION_SUFFIX; // TODO FIXME
		}
		else
		{
			urlString += "?" + Constants.API_VERSION_SUFFIX; // TODO FIXME
		}
		
		HttpUriRequest request = null;
		
		if (httpMethod.equals("GET"))
		{
			Log.d(TAG, "getting " + urlString);
			urlString = appendUrlParameters(urlString, params);
			request = new HttpGet(urlString);
		}
		else if (httpMethod.equals("PUT"))
		{
			urlString = appendUrlParameters(urlString, params);
			request = new HttpPut(urlString);
		}
		else if (httpMethod.equals("POST"))
		{
			request = createPostRequest(urlString, params);
		}
		else
		{
			throw new AccessProtectedResourceException("unknown method");
		}
		
		request.addHeader("Accept-Encoding", "gzip");
		HttpResponse response = null;
		InputStream inputStream = null;
		HttpEntity entity = null;
		
		try
		{
			do
			{
				response = httpClient.execute(request);
				StatusLine statusLine = response.getStatusLine();
				Log.d(TAG, "is get: " + (request.getClass().equals(HttpGet.class)) + " " + request.getClass().getName());
			
				if (statusLine.getStatusCode() == 307 && request.getClass().equals(HttpGet.class))
				{
					HttpParams httpParams = response.getParams();
					request = new HttpGet((String) httpParams.getParameter("Location"));
					response = null;
				}
				else if (statusLine.getStatusCode() == 200 || statusLine.getStatusCode() == 204)
				{
					Log.v(TAG, "successful request to '" + urlString + "'");
				}
				else if (statusLine.getStatusCode() == 401)
				{
					Log.w(TAG, "cannot authenticate");
					throw new AuthenticationErrorException();
				}
				else
				{
					Log.w(TAG, "API returned " + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
					throw new RestClientException(statusLine);
				}
			}
			while (response == null);
			
			entity = response.getEntity();
			
			if (entity != null)
			{
				inputStream = entity.getContent();
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
				{
					inputStream = new GZIPInputStream(inputStream);
				}
			}
		}
		catch (UnknownHostException e)
		{
			Log.e(this.getClass().getSimpleName(), "accessProtectedResource(): " + e.getLocalizedMessage());
			
			throw new NetworkProblemException();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e(this.getClass().getSimpleName(), "accessProtectedResource(): " + e.getLocalizedMessage());
			
			throw new AccessProtectedResourceException();
		}
		return inputStream;
	}

	public InputStream getBillingBalance() throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/billing/balance/?complexity=full");
	}

	public InputStream getContacts() throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/contacts/?complexity=full&limit=0");
	}


	/**
	 * This method calls the Rest-api and request call data.
	 * If one of the params is <= 0 a full list is requested, otherwise a list 
	 * with data in the given period. 
	 * 
	 * @param periodStart a periodStart value in unix timestamp
	 * @param periodEnd a periodEnd value in unix timestamp 
	 * @return an InputStream containing a xml with call data
	 */
	public InputStream getCalls(long periodStart, long periodEnd) throws AccessProtectedResourceException, NetworkProblemException
	{
		if (periodStart > 0 && periodEnd > 0)
		{
			return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/calls/?complexity=full&period_start=" + getDateTimeString(periodStart) + "&period_end=" + getDateTimeString(periodEnd));
		}
		else
		{
			return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/calls/?complexity=full");
		}
	}

	
	/**
	 * This method calls the Rest-api and request voicemail data.
	 * If one of the params is <= 0 a full list is requested, otherwise a list 
	 * with data in the given period. 
	 * 
	 * @param periodStart a periodStart value in unix timestamp
	 * @param periodEnd a periodEnd value in unix timestamp
	 * @return an InputStream containing a xml with voicemail data
	 */
	public InputStream getVoiceMails(long periodStart, long periodEnd) throws AccessProtectedResourceException, NetworkProblemException
	{
		if (periodStart > 0 && periodEnd > 0)
		{
			return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/voicemails/?complexity=full&period_start=" + getDateTimeString(periodStart) + "&period_end=" + getDateTimeString(periodEnd));
		}
		else
		{
			return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/voicemails/?complexity=full");
		} 
	}

	public InputStream getProvisioningData() throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/extensions/?complexity=full");
	}

	public InputStream getVoicemail(String voicemail) throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(voicemail);
	}

	public void setVoicemailRead(String voicemail) throws AccessProtectedResourceException, NetworkProblemException
	{
		accessProtectedResource("PUT", voicemail + "?value=true");
	}

	public void setCallRead(String call) throws AccessProtectedResourceException, NetworkProblemException
	{
		accessProtectedResource("PUT", call + "?value=true");
	}

	public InputStream getMobileExtensions() throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/mobile/extensions/");
	}

	public InputStream getBaseProductType() throws AccessProtectedResourceException, NetworkProblemException
	{
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/baseproducttype/");
	}

	/**
	 * This method calls the rest-api to create a mobile extension
	 * 
	 * @param phoneNumber the phoneNumber of this device
	 * @param model the model of this device
	 * @param vendor the vendor of this device
	 * @param firmware the firmware version of this device
	 * @return an InputStream containing the created sip credentials
	 */
	public InputStream setupMobileExtension(String phoneNumber, String model, String vendor, String firmware) throws AccessProtectedResourceException, NetworkProblemException
	{
		HashMap<String, String> params = new HashMap<String, String>();
		
		params.put("phoneNumber", phoneNumber);
		params.put("model", model);
		params.put("vendor", vendor);
		params.put("firmware", firmware);
		
		return accessProtectedResource("POST", Constants.API_20_BASEURL + "/my/settings/mobile/extensions/", params.entrySet());
	}
	
	/**
	 * This method created a human readable String of a time in millis (unix timestamp)
	 * @param timeInMillis unix timestamp in millis to format
	 * @return a formatted date time string in (yyyy-MM-dd'T'hh:mm:ss'Z')
	 */
	private String getDateTimeString(long timeInMillis)
	{
		return periodFormatter.format(new Date(timeInMillis));
	}
}
