package com.sipgate.models;

public class SipgateUserdataSip
{
	private String localUri = null;
	private String sipUserID = null;
	private String sipPassword = null;

	public String getLocalUri()
	{
		return localUri;
	}

	public void setLocalUri(String localUri)
	{
		this.localUri = localUri;
	}

	public String getSipUserID()
	{
		return sipUserID;
	}

	public void setSipUserID(String sipUserID)
	{
		this.sipUserID = sipUserID;
	}

	public String getSipPassword()
	{
		return sipPassword;
	}

	public void setSipPassword(String sipPassword)
	{
		this.sipPassword = sipPassword;
	}
}
