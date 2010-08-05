package com.sipgate.api.types;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Parcel;

public class SMS extends Event {
	
	public SMS() {		
	}

	public SMS(Parcel in) {
		super(in);
	}

	public static SMS fromXMLNode(Node node) {
		SMS sms = (SMS) Event.fromXMLNode(node, SMS.class);
			
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) node;
		}
	
		return sms;
	}
}
