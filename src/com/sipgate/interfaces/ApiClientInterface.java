package com.sipgate.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Vector;

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
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

public interface ApiClientInterface {
	public SipgateBalanceData getBillingBalance() throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public SipgateProvisioningData getProvisioningData() throws ApiException, FeatureNotAvailableException, AuthenticationErrorException, NetworkProblemException;
	public Vector<ContactDataDBObject> getContacts() throws ApiException, FeatureNotAvailableException;
	public Vector<CallDataDBObject> getCalls(long periodStart, long periodEnd) throws ApiException, FeatureNotAvailableException;
	public Vector<VoiceMailDataDBObject> getVoiceMails(long periodStart, long periodEnd) throws ApiException, FeatureNotAvailableException;
	public boolean connectivityOk() throws ApiException, NetworkProblemException;
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException;
	public void setVoiceMailRead(String voicemail) throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public void setCallRead(String call) throws ApiException, FeatureNotAvailableException, NetworkProblemException;
	public boolean featureAvailable(API_FEATURE feature);
	public String getBaseProductType() throws IOException, URISyntaxException, FeatureNotAvailableException;
	public MobileExtension setupMobileExtension(String phoneNumber,	String model, String vendor, String firmware) throws FeatureNotAvailableException;
	public Vector<RegisteredMobileDevice> getRegisteredMobileDevices() throws ApiException, FeatureNotAvailableException;
}
