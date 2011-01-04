package com.sipgate.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.sipgate.db.CallDataDBObject;
import com.sipgate.util.PhoneNumberFormatter;

/**
 * This is a SaxParser DefaulHandler implementation for CallDataDBObjects
 * A Document is handled by this Class and it trys to parse the content into CallDataDBObjects
 * <br/>
 * <b> Remeber to reuse the object instead of creating a new one for memory saving </b>
 * 
 * @author graef
 *
 */
public class CallParser extends DefaultHandler
{
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	
	private Vector <CallDataDBObject> callDataDBObjects = null;
	private CallDataDBObject callDataDBObject = null;

	private StringBuffer currentValue = null;
	private String parent = null;
	private String location = null;
	
	private String numberPretty = null;
	private String numberE164 = null;
	
	private static final PhoneNumberFormatter formatter = new PhoneNumberFormatter();
	private static final Locale locale = Locale.getDefault();
	
	public CallParser()
	{
		callDataDBObjects = new Vector<CallDataDBObject>();
		currentValue = new StringBuffer();
	}
	
	/**
	 * This method you should call to (re)initialise youre instance of CallParser to reuse instead of creating 
	 * a new instance of CallParser for memory saving
	 */
	public void init()
	{
		callDataDBObjects.clear();
		currentValue.setLength(0);
		parent = null;
		location = null;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if ("call".equalsIgnoreCase(localName))
		{
			callDataDBObject = new CallDataDBObject();
		}
		else if ("read".equalsIgnoreCase(localName) || "sources".equalsIgnoreCase(localName) || 
				 "targets".equalsIgnoreCase(localName) || "extension".equalsIgnoreCase(localName) || 
				 "recordings".equalsIgnoreCase(localName))
		{
			parent = localName;
		}
	
		currentValue.setLength(0);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if ("call".equalsIgnoreCase(localName))
		{
			if (!location.equalsIgnoreCase("trash"))
			{
				callDataDBObjects.add(callDataDBObject);
			}
		}
		else if ("id".equalsIgnoreCase(localName))
		{
			if (!"extension".equalsIgnoreCase(parent))
			{
				callDataDBObject.setId(Long.parseLong(currentValue.toString()));
			}
		}
		else if ("created".equalsIgnoreCase(localName))
		{
			if (!"recordings".equalsIgnoreCase(parent))
			{
				callDataDBObject.setTime(getCallTime(currentValue.toString()));
			}
		}
		else if ("direction".equalsIgnoreCase(localName))
		{
			if (currentValue.toString().equalsIgnoreCase("incoming"))
			{
				callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				callDataDBObject.setMissed(false);
			}
			else if(currentValue.toString().equalsIgnoreCase("missed_incoming"))
			{
				callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				callDataDBObject.setMissed(true);
			}
			else if(currentValue.toString().equalsIgnoreCase("outgoing"))
			{
				callDataDBObject.setDirection(CallDataDBObject.OUTGOING);
				callDataDBObject.setMissed(false);
			}
			else if(currentValue.toString().equalsIgnoreCase("missed_outgoing"))
			{
				callDataDBObject.setDirection(CallDataDBObject.OUTGOING);
				callDataDBObject.setMissed(true);
			}
		}
		else if ("value".equalsIgnoreCase(localName))
		{
			if ("read".equalsIgnoreCase(parent))
			{
				callDataDBObject.setRead(currentValue.toString());
			}
		}
		else if ("modify".equalsIgnoreCase(localName))
		{
			if ("read".equalsIgnoreCase(parent))
			{
				callDataDBObject.setReadModifyUrl(currentValue.toString());
			}
		}
		else if ("location".equalsIgnoreCase(localName))
		{
			location = currentValue.toString();
		}
		else if ("numberE164".equalsIgnoreCase(localName))
		{
			formatter.initWithFreestyle(currentValue.toString().replace("+", ""), locale.getCountry());
			
			numberPretty = formatter.formattedNumber();
			numberE164 = formatter.e164NumberWithPrefix("+");
			
			if ("targets".equalsIgnoreCase(parent))
			{
				if (callDataDBObject.getDirection() == CallDataDBObject.INCOMING)
				{
					callDataDBObject.setLocalNumberE164(numberE164);
					callDataDBObject.setLocalNumberPretty(numberPretty);
				}
				else
				{
					callDataDBObject.setRemoteNumberE164(numberE164);
					callDataDBObject.setRemoteNumberPretty(numberPretty);
				}
			}
			else if ("sources".equalsIgnoreCase(parent))
			{
				if (callDataDBObject.getDirection() == CallDataDBObject.INCOMING)
				{
					callDataDBObject.setRemoteNumberE164(numberE164);
					callDataDBObject.setRemoteNumberPretty(numberPretty);
				}
				else
				{
					callDataDBObject.setLocalNumberE164(numberE164);
					callDataDBObject.setLocalNumberPretty(numberPretty);
				}
			}
		}
		else if ("contactFN".equalsIgnoreCase(localName))
		{
			if ("targets".equalsIgnoreCase(parent))
			{				
				if (callDataDBObject.getDirection() == CallDataDBObject.INCOMING)
				{
					callDataDBObject.setLocalName(currentValue.toString());
				}
				else
				{
					callDataDBObject.setRemoteName(currentValue.toString());
				}
			}
			else if ("sources".equalsIgnoreCase(parent))
			{
				if (callDataDBObject.getDirection() == CallDataDBObject.INCOMING)
				{
					callDataDBObject.setRemoteName(currentValue.toString());
				}
				else
				{
					callDataDBObject.setLocalName(currentValue.toString());
				}
			}
		}
		else if ("read".equalsIgnoreCase(localName) || "sources".equalsIgnoreCase(localName) || 
				 "targets".equalsIgnoreCase(localName) || "extension".equalsIgnoreCase(localName) || 
				 "recordings".equalsIgnoreCase(localName))
		{
			parent = null;
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		currentValue.append(ch, start, length);
	}
	
	private static long getCallTime(String dateString) 
	{
		long callTime = 0;
		
		try 
		{
			if (dateString != null) 
			{
				return dateformatterPretty.parse(dateString).getTime();
			}
		} 
		catch (ParseException e)
		{
			Log.e("CallParser", "getCallTime", e);
		}
		
		return callTime;
	}

	/**
	 * This method returns a Vector of parsed CallDataDBObjects
	 * @return a Vector of all CallDataDBObjects parsed by this handler since the last call of init()
	 */
	public Vector<CallDataDBObject> getCallDataDBObjects()
	{
		return callDataDBObjects;
	}
}
