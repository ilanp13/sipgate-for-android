package com.sipgate.api.types;

import org.w3c.dom.Node;

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
		}
	
		return sms;
	}
}
