package com.sipgate.models;

import java.io.Serializable;

public class SipgateProvisioningExtension implements Serializable
{
	private static final long serialVersionUID = -6362670901168025574L;
	
	private String alias = null;
	private String sipid = null;
	private String password = null;

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public String getSipid()
	{
		return sipid;
	}

	public void setSipid(String sipid)
	{
		this.sipid = sipid;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
