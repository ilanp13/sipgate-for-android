package com.sipgate.api.types;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class MobileExtension {
	
	private static final String TAG = "MobileExtension";
	private String extensionId;
	private String type;
	private String alias;
	private String resource;
	private String password;
	
	
	public MobileExtension(String extensionId, String type, String alias, String resource, String password) {
		this.extensionId = extensionId;
		
		this.type = type;
		this.alias = alias;
		this.resource = resource;
		this.password = password;
	}


	public String getType() {
		return type;
	}

	public String getPassword() {
		return password;
	}


	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}


	public String getResource() {
		return resource;
	}


	public String getExtensionId() {
		return extensionId;
	}

	
	public static MobileExtension fromXMLNode(Node node) {
				
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) node;
			String extensionId = getElement(fstElmnt, "extensionId");
			String type =  getElement(fstElmnt, "type");
			String alias = getElement(fstElmnt, "alias");
			String password = getElement(fstElmnt, "sipPassword");
			String resource = getResourceElement(fstElmnt);
			
			Log.d(TAG, "mobile extension from xml :)");
			
			return new MobileExtension(extensionId, type, alias, resource, password);
		} else {
			return null;
		}
	}
	
	protected static String getElement(Element fstElmnt, String name) {
		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(name);
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		if (fstNmElmnt == null) {
			Log.w(TAG,"mobile extension without " + name + " element");
			return null;
		}
		NodeList fstNm = fstNmElmnt.getChildNodes();

		return ((Node) fstNm.item(0)).getNodeValue();
	}
	
	private static String getResourceElement(Element fstElmnt) {
		try {
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("resource");
			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			if (fstNmElmnt == null) {
				Log.w(TAG,"mobile extension without resource");
				return null;
			}
			
			fstNmElmntLst= fstNmElmnt.getElementsByTagName("get");
			fstNmElmnt = (Element) fstNmElmntLst.item(0);
			if (fstNmElmnt == null) {
				Log.w(TAG,"mobile extension resource without get");
				return null;
			}
			
			NodeList fstNm = fstNmElmnt.getChildNodes();
			return ((Node) fstNm.item(0)).getNodeValue();
		} catch (DOMException e) {
			Log.e(TAG,"error, getting voicemail content");
			e.printStackTrace();
		}
		return null;
	}

}
