package com.sipgate.parser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sipgate.api.types.RegisteredMobileDevice;

/**
 * This is a SaxParser DefaulHandler implementation for RegisteredMobileDevices
 * A Document is handled by this Class and it trys to parse the content into RegisteredMobileDevices
 * <br/>
 * <b> Remeber to reuse the object instead of creating a new one for memory saving </b>
 * 
 * @author graef
 *
 */
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
	
	/**
	 * This method you should call to (re)initialise youre instance of CallParser to reuse instead of creating 
	 * a new instance of CallParser for memory saving
	 */
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
	
	/**
	 * This method returns a Vector of parsed RegisteredMobileDevices
	 * @return a Vector of all RegisteredMobileDevices parsed by this handler since the last call of init()
	 */
	public Vector<RegisteredMobileDevice> getRegisteredMobileDevices()
	{
		return registeredMobileDevices;
	}
}
