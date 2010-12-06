package com.sipgate.parser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sipgate.api.types.RegisteredMobileDevice;

public class RegisteredMobileDeviceParser extends DefaultHandler
{
	private Vector <RegisteredMobileDevice> registeredMobileDevices = null;
	private RegisteredMobileDevice registeredMobileDevice = null;

	private StringBuffer currentValue = null;
	
	public RegisteredMobileDeviceParser()
	{
		registeredMobileDevices = new Vector<RegisteredMobileDevice>();
		currentValue = new StringBuffer();
	}
	
	public void init()
	{
		registeredMobileDevices.clear();
		currentValue.setLength(0);
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if ("RegisteredMobileDevice".equalsIgnoreCase(localName))
		{
			registeredMobileDevice = new RegisteredMobileDevice();
		}
		
		currentValue.setLength(0);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if ("RegisteredMobileDevice".equalsIgnoreCase(localName))
		{
			registeredMobileDevices.add(registeredMobileDevice);
		}
		else if ("deviceNumberE164".equalsIgnoreCase(localName))
		{
			registeredMobileDevice.setDeviceNumberE164("+" + currentValue.toString());
		}
		else if ("deviceVendor".equalsIgnoreCase(localName))
		{
			registeredMobileDevice.setDeviceVendor(currentValue.toString());
		}
		else if ("deviceModel".equalsIgnoreCase(localName))
		{
			registeredMobileDevice.setDeviceModel(currentValue.toString());
		}
		else if ("deviceFirmware".equalsIgnoreCase(localName))
		{
			registeredMobileDevice.setDeviceFirmware(currentValue.toString());
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		currentValue.append(ch, start, length);
	}
	
	public Vector<RegisteredMobileDevice> getRegisteredMobileDevices()
	{
		return registeredMobileDevices;
	}
}
