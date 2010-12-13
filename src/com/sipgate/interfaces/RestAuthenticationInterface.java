package com.sipgate.interfaces;

import java.io.InputStream;

import com.sipgate.exceptions.AccessProtectedResourceException;
import com.sipgate.exceptions.NetworkProblemException;

public interface RestAuthenticationInterface {
	
	public InputStream getBillingBalance() throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream getProvisioningData() throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream getContacts() throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream getCalls(long periodStart, long periodEnd) throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream getVoiceMails(long periodStart, long periodEnd) throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream getVoicemail(String voicemail) throws AccessProtectedResourceException, NetworkProblemException;

	public void setVoicemailRead(String voicemail) throws AccessProtectedResourceException, NetworkProblemException;
	
	public void setCallRead(String call) throws AccessProtectedResourceException, NetworkProblemException;

	public InputStream getMobileExtensions() throws AccessProtectedResourceException, NetworkProblemException;

	public InputStream getBaseProductType() throws AccessProtectedResourceException, NetworkProblemException;
	
	public InputStream setupMobileExtension(String phoneNumber, String model, String vendor, String firmware) throws AccessProtectedResourceException, NetworkProblemException;

	public InputStream getRegisteredMobileDevices() throws AccessProtectedResourceException, NetworkProblemException;
}
