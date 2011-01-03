package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Vector;

import android.content.Context;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.api.types.RegisteredMobileDevice;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.util.ApiServiceProviderImpl.API_FEATURE;

public abstract class ApiServiceProvider {

	private static ApiServiceProvider singleton;

	/*
	 * check whether the application was registered to an account. This is
	 * transparent for OAuth and Basic-Auth use.
	 */
	public abstract boolean isRegistered();

	/*
	 * Register the application to an account. This is transparent for OAuth and
	 * Basic-Auth use.
	 */
	public abstract void register(String username, String password) throws ApiException,
			NetworkProblemException;

	/*
	 * Unregister the application from an account. This is transparent for OAuth
	 * and Basic-Auth use.
	 */
	public abstract void unRegister();

	/*
	 * get information for provisioning sip clients
	 */
	public abstract SipgateProvisioningData getProvisioningData() throws ApiException,
			FeatureNotAvailableException, AuthenticationErrorException,
			NetworkProblemException;

	/*
	 * get accounts current balance
	 */
	public abstract SipgateBalanceData getBillingBalance() throws ApiException,
			FeatureNotAvailableException, NetworkProblemException;

	/*
	 * voice mail list
	 */
	public abstract Vector<VoiceMailDataDBObject> getVoiceMails(long periodStart,
			long periodEnd) throws ApiException, FeatureNotAvailableException;

	/*
	 * voice mail list
	 */
	public abstract Vector<VoiceMailDataDBObject> getVoiceMails() throws ApiException,
			FeatureNotAvailableException;

	/*
	 * get voicemail content as stream
	 */
	public abstract InputStream getVoicemail(String voicemail) throws ApiException,
			FeatureNotAvailableException;

	/*
	 * mark specific vm as read
	 */
	public abstract void setVoiceMailRead(String voicemail) throws ApiException,
			FeatureNotAvailableException, NetworkProblemException;

	/*
	 * mark specific call as read
	 */
	public abstract void setCallRead(String call) throws ApiException,
			FeatureNotAvailableException, NetworkProblemException;

	/*
	 * contact list
	 */
	public abstract Vector<ContactDataDBObject> getContacts() throws ApiException,
			FeatureNotAvailableException;

	/*
	 * calls list
	 */
	public abstract Vector<CallDataDBObject> getCalls(long periodStart, long periodEnd)
			throws ApiException, FeatureNotAvailableException;

	/*
	 * calls list
	 */
	public abstract Vector<CallDataDBObject> getCalls() throws ApiException,
			FeatureNotAvailableException;

	public abstract boolean featureAvailable(API_FEATURE feature) throws ApiException;

	public abstract String getBaseProductType() throws IOException, URISyntaxException,
			FeatureNotAvailableException;

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
	public abstract MobileExtension setupMobileExtension(String phoneNumber,
			String model, String vendor, String firmware)
			throws FeatureNotAvailableException;

	/**
	 * This method calls the api and requests all registered mobile devices
	 * 
	 * @author graef
	 * 
	 * @return a Vector with all registered mobile devices
	 * @throws FeatureNotAvailableException
	 */
	public abstract Vector<RegisteredMobileDevice> getRegisteredMobileDevices()
			throws FeatureNotAvailableException, ApiException;

	
	/**
	 * get an instance of the singleton object.
	 * creates {@link ApiServiceProviderImpl} by default
	 * @param context used to initialize the instance
	 * @return a singleton instance
	 */
	synchronized public static ApiServiceProvider getInstance(Context context)
	{
		if (singleton == null)
		{
			singleton = new ApiServiceProviderImpl(context);
		}
		return singleton;
	}

	synchronized public static void destroy()
	{
		singleton = null;
	}

	/**
	 * defines the singleton instance returned by getInstance
	 * @param singleton
	 */
	synchronized public static void setInstance(ApiServiceProvider singleton) 
	{
		ApiServiceProvider.singleton = singleton;
	}
	
}