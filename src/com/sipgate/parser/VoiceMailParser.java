package com.sipgate.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.sipgate.db.VoiceMailDataDBObject;

public class VoiceMailParser extends DefaultHandler
{
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	
	private Vector <VoiceMailDataDBObject> voiceMailDataDBObjects = null;
	private VoiceMailDataDBObject voiceMailDataDBObject = null;

	private StringBuffer currentValue = null;
	private String parent = null;
	
	public VoiceMailParser()
	{
		voiceMailDataDBObjects = new Vector<VoiceMailDataDBObject>();
		currentValue = new StringBuffer();
	}
	
	public void init()
	{
		voiceMailDataDBObjects.clear();
		currentValue.setLength(0);
		parent = null;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if ("voicemail".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObject = new VoiceMailDataDBObject();
		}
		else if ("read".equalsIgnoreCase(localName) || "sources".equalsIgnoreCase(localName) || 
				 "targets".equalsIgnoreCase(localName) || "extension".equalsIgnoreCase(localName) || 
				 "content".equalsIgnoreCase(localName))		
		{
			parent = localName;
		}
	
		currentValue.setLength(0);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if ("voicemail".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObjects.add(voiceMailDataDBObject);
		}
		else if ("id".equalsIgnoreCase(localName))
		{
			if (!"extension".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setId(Long.parseLong(currentValue.toString()));
			}
		}
		else if ("created".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObject.setTime(getCallTime(currentValue.toString()));
		}
		else if ("value".equalsIgnoreCase(localName))
		{
			if ("read".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setRead(currentValue.toString());
			}
		}
		else if ("modify".equalsIgnoreCase(localName))
		{
			if ("read".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setReadModifyUrl(currentValue.toString());
			}
		}
		else if ("numberE164".equalsIgnoreCase(localName))
		{
			if ("targets".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setLocalNumberE164(currentValue.toString());
			}
			else if ("sources".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setRemoteNumberE164(currentValue.toString());
			}
		}
		else if ("numberPretty".equalsIgnoreCase(localName))
		{
			if ("targets".equalsIgnoreCase(parent))
			{				
				voiceMailDataDBObject.setLocalNumberPretty(currentValue.toString());
			}
			else if ("sources".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setRemoteNumberPretty(currentValue.toString());
			}
		}
		else if ("contactFN".equalsIgnoreCase(localName))
		{
			if ("targets".equalsIgnoreCase(parent))
			{				
				voiceMailDataDBObject.setLocalName(currentValue.toString());
			}
			else if ("sources".equalsIgnoreCase(parent))
			{
				voiceMailDataDBObject.setRemoteName(currentValue.toString());
			}
		}
		else if ("duration".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObject.setDuration(Long.parseLong(currentValue.toString()));
		}
		else if ("get".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObject.setContentUrl(currentValue.toString());
		}
		else if ("transcription".equalsIgnoreCase(localName))
		{
			voiceMailDataDBObject.setTranscription(currentValue.toString());
		}
		else if ("read".equalsIgnoreCase(localName) || "sources".equalsIgnoreCase(localName) || 
				 "targets".equalsIgnoreCase(localName) || "extension".equalsIgnoreCase(localName) || 
				 "content".equalsIgnoreCase(localName))
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
		
		try {
			if (dateString != null) {
				return dateformatterPretty.parse(dateString).getTime();
			}
		} 
		catch (ParseException e) {
			Log.e("VoiceMailParser", "getCallTime", e);
		}
		
		return callTime;
	}

	public Vector<VoiceMailDataDBObject> getVoiceMailDataDBObjects()
	{
		return voiceMailDataDBObjects;
	}
}
