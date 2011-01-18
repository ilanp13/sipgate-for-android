package com.sipgate.models;

import java.io.Serializable;
import java.util.ArrayList;

public class SipgateProvisioningData implements Serializable
{
	private static final long serialVersionUID = -3834322191871961655L;
	
	private String registrar = null;
	private String outboundProxy = null;
	private ArrayList<SipgateProvisioningExtension> extensions = null;

	public ArrayList<SipgateProvisioningExtension> getExtensions()
	{
		return this.extensions;
	}

	public String getRegistrar()
	{
		return registrar;
	}

	public void setRegistrar(String registrar)
	{
		this.registrar = registrar;
	}

	public String getOutboundProxy()
	{
		return outboundProxy;
	}

	public void setOutboundProxy(String outboundProxy)
	{
		this.outboundProxy = outboundProxy;
	}

	public void addExtension(SipgateProvisioningExtension extension)
	{
		if (this.extensions == null)
		{
			this.extensions = new ArrayList<SipgateProvisioningExtension>();
		}
		extensions.add(extension);
	}

	public int getExtensionCount()
	{
		if (this.extensions != null)
		{
			return this.extensions.size();
		}
		return 0;
	}
}
