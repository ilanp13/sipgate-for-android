package com.sipgate.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.oauth.OAuthException;

import com.sipgate.api.types.Event;
import com.sipgate.api.types.MobileExtension;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.exceptions.OAuthAccessProtectedResourceException;
import com.sipgate.exceptions.OAuthMissingContextException;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateCallData;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

public interface ApiClientInterface {
	public SipgateBalanceData getBillingBalance() throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public SipgateProvisioningData getProvisioningData() throws ApiException, FeatureNotAvailableException, AuthenticationErrorException, NetworkProblemException;
	public List<Event> getEvents() throws ApiException, FeatureNotAvailableException;
	public ArrayList<SipgateCallData> getCalls() throws ApiException, FeatureNotAvailableException;
	public boolean connectivityOk() throws ApiException, NetworkProblemException;
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException;
	public void setVoicemailRead(String voicemail) throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public void setCallRead(String call) throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public boolean featureAvailable(API_FEATURE feature);
	public List<MobileExtension> getMobileExtensions() throws IOException, OAuthException, URISyntaxException, OAuthAccessProtectedResourceException, OAuthMissingContextException, FeatureNotAvailableException;
	public String getBaseProductType() throws IOException, OAuthException, URISyntaxException, OAuthAccessProtectedResourceException, OAuthMissingContextException, FeatureNotAvailableException;
	public MobileExtension setupMobileExtension(String phoneNumber,	String model, String vendor, String firmware) throws FeatureNotAvailableException;
}
