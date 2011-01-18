package com.sipgate.models.api.xmlrpc;

public class SipgateServerData
{
	private String sipRegistrar = null;
	private String sipOutboundProxy = null;
	private String stunServer = null;
	private String ntpServer = null;
	private String httpServer = null;
	private String samuraiServer = null;
	private String simpleServer = null;

	public String getSipRegistrar()
	{
		return sipRegistrar;
	}

	public void setSipRegistrar(String sipRegistrar)
	{
		this.sipRegistrar = sipRegistrar;
	}

	public String getSipOutboundProxy()
	{
		return sipOutboundProxy;
	}

	public void setSipOutboundProxy(String sipOutboundProxy)
	{
		this.sipOutboundProxy = sipOutboundProxy;
	}

	public String getStunServer()
	{
		return stunServer;
	}

	public void setStunServer(String stunServer)
	{
		this.stunServer = stunServer;
	}

	public String getNtpServer()
	{
		return ntpServer;
	}

	public void setNtpServer(String ntpServer)
	{
		this.ntpServer = ntpServer;
	}

	public String getHttpServer()
	{
		return httpServer;
	}

	public void setHttpServer(String httpServer)
	{
		this.httpServer = httpServer;
	}

	public String getSamuraiServer()
	{
		return samuraiServer;
	}

	public void setSamuraiServer(String samuraiServer)
	{
		this.samuraiServer = samuraiServer;
	}

	public String getSimpleServer()
	{
		return simpleServer;
	}

	public void setSimpleServer(String simpleServer)
	{
		this.simpleServer = simpleServer;
	}
}
