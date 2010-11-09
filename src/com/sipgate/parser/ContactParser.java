package com.sipgate.parser;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.ContactNumberDBObject;

public class ContactParser extends DefaultHandler
{
	private Vector <ContactDataDBObject> contactDataDBObjects = null;
	
	private ContactDataDBObject contactDataDBObject = null;
	private ContactNumberDBObject contactNumberDBObject = null;

	private StringBuffer currentValue = null;
	private String parent = null;

	public ContactParser()
	{
		contactDataDBObjects = new Vector<ContactDataDBObject>();
		currentValue = new StringBuffer();
	}
	
	public void init()
	{
		contactDataDBObjects.clear();
		currentValue.setLength(0);
		parent = null;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if ("item".equalsIgnoreCase(localName))
		{
			contactDataDBObject = new ContactDataDBObject();
			contactNumberDBObject = new ContactNumberDBObject();
		}
						
		currentValue.setLength(0);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if ("item".equalsIgnoreCase(localName))
		{
			contactDataDBObjects.add(contactDataDBObject);
		}
		else if ("entryName".equalsIgnoreCase(localName))
		{
			parent = currentValue.toString();
		}
		else if ("uuid".equalsIgnoreCase(localName))
		{
			contactDataDBObject.setUuid(currentValue.toString());
			
			for (ContactNumberDBObject contactNumberDBObject : contactDataDBObject.getContactNumberDBObjects())
			{
				contactNumberDBObject.setUuid(contactDataDBObject.getUuid());
			}
		}
		else if ("family".equalsIgnoreCase(localName))
		{
			contactDataDBObject.setLastName(currentValue.toString());
		}
		else if ("given".equalsIgnoreCase(localName))
		{
			contactDataDBObject.setFirstName(currentValue.toString());
		}
		else if ("value".equalsIgnoreCase(localName))
		{
			if ("tel".equalsIgnoreCase(parent))
			{
				contactNumberDBObject.setNumberE164(currentValue.toString());
			}
			else if ("fn".equalsIgnoreCase(parent))
			{
				contactDataDBObject.setDisplayName(currentValue.toString());
			}
		}
		else if ("type".equalsIgnoreCase(localName))
		{ 
			if ("tel".equalsIgnoreCase(parent))
			{
				contactNumberDBObject.setType(currentValue.toString());
			}
		}
		else if ("x-local-value".equalsIgnoreCase(localName))
		{
			contactNumberDBObject.setNumberPretty(currentValue.toString());
		}
		else if ("entry".equalsIgnoreCase(localName))
		{
			if ("tel".equalsIgnoreCase(parent))
			{
				contactDataDBObject.addContactNumberDBObject(contactNumberDBObject);
				contactNumberDBObject = new ContactNumberDBObject();
			}
			
			parent = null;
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		currentValue.append(ch, start, length);
	}

	public Vector<ContactDataDBObject> getContactDataDBObjects()
	{
		return contactDataDBObjects;
	}
}
