package com.sipgate.models;

import java.util.ArrayList;

public class SipgateOwnUri
{
	private String sipUri = null;
	private String e164Out = null;
	
	private ArrayList<String> e164In = null;
	private ArrayList<String> tos = null;
	
	private Boolean defaultUri = null;
	private String uriAlias = null;

	public String getSipUri()
	{
		return sipUri;
	}

	public void setSipUri(String sipUri)
	{
		this.sipUri = sipUri;
	}

	public String getE164Out()
	{
		return e164Out;
	}

	public void setE164Out(String e164Out)
	{
		this.e164Out = e164Out;
	}

	public ArrayList<String> getE164In()
	{
		return e164In;
	}

	public void setE164In(ArrayList<String> e164In)
	{
		this.e164In = e164In;
	}

	public ArrayList<String> getTos()
	{
		return tos;
	}

	public void setTos(ArrayList<String> tos)
	{
		this.tos = tos;
	}

	public Boolean getDefaultUri()
	{
		return defaultUri;
	}

	public void setDefaultUri(Boolean defaultUri)
	{
		this.defaultUri = defaultUri;
	}

	public String getUriAlias()
	{
		return uriAlias;
	}

	public void setUriAlias(String uriAlias)
	{
		this.uriAlias = uriAlias;
	}
}
