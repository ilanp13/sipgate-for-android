package com.sipgate.api.types;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.os.Parcel;

public class Call extends Event {

	public Call() {		
	}

	public Call(Parcel in) {
		super(in);
	}

	public static Call fromXMLNode(Node node) {
		Call ret = (Call) Event.fromXMLNode(node, Call.class);
			
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) node;
		}
	
		return ret;
	}

}
