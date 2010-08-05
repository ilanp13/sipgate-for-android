package com.sipgate.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.oauth.OAuthMessage;
import android.content.Context;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.exceptions.AccessProtectedResourceException;
import com.sipgate.exceptions.MissingContextException;
import com.sipgate.interfaces.RestAuthenticationInterface;

public class RestAuthenticationWrapper implements RestAuthenticationInterface {

	private static RestAuthenticationWrapper singleton;
	private static Context context = null;
	private static Oauth oauth = null;
	
	private RestAuthenticationWrapper(Context context) {
		super();

		RestAuthenticationWrapper.context = context;
	}

	synchronized public static RestAuthenticationWrapper getInstance(Context context) {
		if (RestAuthenticationWrapper.singleton == null) {
			RestAuthenticationWrapper.singleton = new RestAuthenticationWrapper(context.getApplicationContext());
		}
		if (RestAuthenticationWrapper.oauth == null) {
			RestAuthenticationWrapper.oauth = Oauth.getInstance(RestAuthenticationWrapper.context);
		}
		return RestAuthenticationWrapper.singleton;
	}
	
	synchronized public static RestAuthenticationWrapper getInstance() throws MissingContextException {
		if (RestAuthenticationWrapper.context == null) {
			throw new MissingContextException();
		}

		return RestAuthenticationWrapper.getInstance(RestAuthenticationWrapper.context);
	}
	
	private InputStream accessProtectedResource(String url) throws AccessProtectedResourceException {
		return accessProtectedResource("GET", url, null);
	}

	private InputStream accessProtectedResource(String httpMethod, String url) throws AccessProtectedResourceException {
		return accessProtectedResource(httpMethod, url, null);
	}
	
	private InputStream accessProtectedResource(String httpMethod, String url, Collection<? extends Entry> params) throws AccessProtectedResourceException {
		OAuthMessage message = null;
		if (params == null) {
			params = new ArrayList<Map.Entry<String, String>>();
		}
		try {
			message = RestAuthenticationWrapper.oauth.accessProtectedResource(httpMethod, url, params);
		} catch (Exception e) {
			throw new AccessProtectedResourceException();
		}
		InputStream stream = null;
		try {
			stream = message.getBodyAsStream();
		} catch (Exception e) {
			throw new AccessProtectedResourceException();
		}
		return stream;
	}

	@Override
	public InputStream getBillingBalance() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "hun/my/billing/balance/?complexity=full");
	}

	@Override
	public InputStream getCalls() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/calls/?complexity=full");
	}

	@Override
	public InputStream getProvisioningData() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/extensions/?complexity=full");
	}

	@Override
	public InputStream getEvents() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/events/?complexity=full");
	}
	
	public InputStream getVoicemail(String voicemail) throws AccessProtectedResourceException {
		return accessProtectedResource(voicemail);
	}
	
	public void setVoicemailRead(String voicemail) throws AccessProtectedResourceException {
		accessProtectedResource(voicemail);
	}

	@Override
	public InputStream getMobileExtensions() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/mobile/extensions/");
	}
	
	@Override
	public InputStream getBaseProductType() throws AccessProtectedResourceException {
		return accessProtectedResource(Constants.API_20_BASEURL + "/my/settings/baseproducttype/");
	}

	@Override
	public InputStream setupMobileExtension(String phoneNumber, String model, String vendor, String firmware)
			throws AccessProtectedResourceException {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("phoneNumber", phoneNumber);
		params.put("model", model);
		params.put("vendor", vendor);
		params.put("firmware", firmware);
		
		return accessProtectedResource("POST", Constants.API_20_BASEURL + "/my/settings/mobile/extensions/", params.entrySet());
	}
}
