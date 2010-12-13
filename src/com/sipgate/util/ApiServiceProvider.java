package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.api.types.RegisteredMobileDevice;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.exceptions.SipgateSettingsProviderGeneralException;
import com.sipgate.interfaces.ApiClientInterface;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.util.SettingsClient.API_TYPE;

public class ApiServiceProvider
{
	public enum API_FEATURE
	{
		VM_LIST
	};

	private static final String TAG = "ApiServiceProvider";
	// singleton foo:
	private static ApiServiceProvider singleton = null;
	// attributes:
	private ApiClientInterface apiClient = null;
	private Context ctx = null;
	private SettingsClient settings = null;

	private ApiServiceProvider(Context context)
	{
		super();
		this.ctx = context;
		this.settings = SettingsClient.getInstance(this.ctx);
		// do we have credentials an api-type in the settings?
		String username = this.settings.getWebusername();
		String password = this.settings.getWebpassword();
		API_TYPE apiType;
		try
		{
			apiType = this.settings.getApiType();
		}
		catch (SipgateSettingsProviderGeneralException e)
		{
			apiType = null;
		}
		if (username.length() > 0 && password.length() > 0 && apiType != null)
		{
			try
			{
				this.apiClient = this.initClient(apiType, username, password);
				if (!apiClient.connectivityOk())
				{
					unRegister();
				}
			}
			catch (ApiException e)
			{
				Log.e(TAG, "ApiServiceProvider() unregistering due to error getting api-client using settings -> " + e.toString());
				unRegister();
			}
			catch (NetworkProblemException e)
			{
				Log.e(TAG, "ApiServiceProvider() unregistering due to network error checking api credentials -> " + e.toString());
				unRegister();
			}
		}
	}

	/*
	 * get an instance of the singleton object
	 */
	synchronized public static ApiServiceProvider getInstance(Context context)
	{
		if (singleton == null)
		{
			singleton = new ApiServiceProvider(context);
		}
		return singleton;
	}

	synchronized public static void destroy()
	{
		singleton = null;
	}

	/*
	 * check whether the application was registered to an account. This is
	 * transparent for OAuth and Basic-Auth use.
	 */
	public boolean isRegistered()
	{
		return (apiClient != null);
	}

	private ApiClientInterface initClient(API_TYPE apiType, String username, String password) throws ApiException
	{
		ApiClientInterface tmpClient = null;
		if (apiType.equals(API_TYPE.REST))
		{
			tmpClient = new RestClient(username, password);
		}
		else
		{
			tmpClient = new XmlrpcClient(username, password);
		}
		return tmpClient;
	}

	/*
	 * Register the application to an account. This is transparent for OAuth and
	 * Basic-Auth use.
	 */
	public void register(String username, String password) throws ApiException, NetworkProblemException
	{
		// unregister first if needed!
		if (this.isRegistered())
		{
			this.unRegister();
		}
		// now we try to find out the right api:
		ArrayList<API_TYPE> apiClientList = new ArrayList<API_TYPE>();
		if (username.contains("@"))
		{
			apiClientList.add(API_TYPE.REST);
			apiClientList.add(API_TYPE.XMLRPC);
		}
		else
		{
			apiClientList.add(API_TYPE.XMLRPC);
			apiClientList.add(API_TYPE.REST);
		}
		for (API_TYPE clientType : apiClientList)
		{
			ApiClientInterface tmpClient = null;
			try
			{
				tmpClient = this.initClient(clientType, username, password);
				if (tmpClient.connectivityOk())
				{
					this.apiClient = tmpClient;
					this.settings.setApiType(clientType);
					this.settings.setWebusername(username);
					this.settings.setWebuserpass(password);
					break;
				}
			}
			catch (NetworkProblemException e)
			{
				Log.e(TAG, "register() try with '" + clientType.toString() + "' failed -> " + e.toString());
				throw e;
			}
			catch (Exception e)
			{
				Log.i(TAG, "register() try with '" + clientType.toString() + "' failed -> " + e.toString());
			}
		}
		if (this.apiClient == null)
		{
			Log.e(TAG, "register() failed: none of the API-backends was accessible!");
			throw new ApiException();
		}
		Log.d(TAG, "register() done without problems");
	}

	/*
	 * Unregister the application from an account. This is transparent for OAuth
	 * and Basic-Auth use.
	 */
	public void unRegister()
	{
		apiClient = null;
	}

	/*
	 * get information for provisioning sip clients
	 */
	public SipgateProvisioningData getProvisioningData() throws ApiException, FeatureNotAvailableException, AuthenticationErrorException, NetworkProblemException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getProvisioningData();
		}
	}

	/*
	 * get accounts current balance
	 */
	public SipgateBalanceData getBillingBalance() throws ApiException, FeatureNotAvailableException, NetworkProblemException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getBillingBalance();
		}
	}

	/*
	 * voice mail list
	 */
	public Vector<VoiceMailDataDBObject> getVoiceMails(long periodStart, long periodEnd) throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getVoiceMails(periodStart, periodEnd);
		}
	}

	/*
	 * voice mail list
	 */
	public Vector<VoiceMailDataDBObject> getVoiceMails() throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getVoiceMails(-1, -1);
		}
	}

	/*
	 * get voicemail content as stream
	 */
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getVoicemail(voicemail);
		}
	}

	/*
	 * mark specific vm as read
	 */
	public void setVoiceMailRead(String voicemail) throws ApiException, FeatureNotAvailableException, NetworkProblemException
	{
		synchronized (this.apiClient)
		{
			apiClient.setVoiceMailRead(voicemail);
		}
	}

	/*
	 * mark specific call as read
	 */
	public void setCallRead(String call) throws ApiException, FeatureNotAvailableException, NetworkProblemException
	{
		synchronized (this.apiClient)
		{
			apiClient.setCallRead(call);
		}
	}

	/*
	 * contact list
	 */
	public Vector<ContactDataDBObject> getContacts() throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getContacts();
		}
	}

	/*
	 * calls list
	 */
	public Vector<CallDataDBObject> getCalls(long periodStart, long periodEnd) throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getCalls(periodStart, periodEnd);
		}
	}

	/*
	 * calls list
	 */
	public Vector<CallDataDBObject> getCalls() throws ApiException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getCalls(-1, -1);
		}
	}

	public boolean featureAvailable(API_FEATURE feature) throws ApiException
	{
		if (apiClient != null)
		{
			synchronized (this.apiClient)
			{
				return apiClient.featureAvailable(feature);
			}
		}
		return false;
	}

	public String getBaseProductType() throws IOException, URISyntaxException, FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getBaseProductType();
		}
	}

	/**
	 * This methods calls the api to create a mobile extension and returns it
	 * 
	 * @author graef
	 * 
	 * @param phoneNumber a given phonenumber
	 * @param model the model of the device
	 * @param vendor the vendor of the device
	 * @param firmware the firmware of the device
	 * @return a fresh MobileExtension with credentials
	 * @throws FeatureNotAvailableException
	 */
	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware) throws FeatureNotAvailableException
	{
		synchronized (this.apiClient)
		{
			return apiClient.setupMobileExtension(phoneNumber, model, vendor, firmware);
		}
	}

	/**
	 * This method calls the api and requests all registered mobile devices
	 * 
	 * @author graef
	 * 
	 * @return a Vector with all registered mobile devices
	 * @throws FeatureNotAvailableException
	 */
	public Vector<RegisteredMobileDevice> getRegisteredMobileDevices() throws FeatureNotAvailableException, ApiException
	{
		synchronized (this.apiClient)
		{
			return apiClient.getRegisteredMobileDevices();
		}
	}
}
