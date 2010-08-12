package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.oauth.OAuthException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

import com.sipgate.api.types.Event;
import com.sipgate.api.types.MobileExtension;
import com.sipgate.api.types.SMS;
import com.sipgate.api.types.Voicemail;
import com.sipgate.exceptions.AccessProtectedResourceException;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.exceptions.OAuthAccessProtectedResourceException;
import com.sipgate.exceptions.OAuthMissingContextException;
import com.sipgate.interfaces.ApiClientInterface;
import com.sipgate.interfaces.RestAuthenticationInterface;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateCallData;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.models.SipgateProvisioningExtension;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

public class RestClient implements ApiClientInterface {
	private static final String TAG = "RestClient";
	private static RestAuthenticationInterface authenticationInterface = null;

	public RestClient(String username, String password) {
		super();
		RestClient.authenticationInterface = new BasicAuthenticationClient(username, password);
	}

	public SipgateBalanceData getBillingBalance() throws ApiException {
		// request API
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.getBillingBalance();
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
			return null;
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		SipgateBalanceData balanceData = new SipgateBalanceData();
		
		// process stream from API
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("fullBalance");
			
			Node fstNode = nodeLst.item(0);
			Element fstElmnt = (Element) fstNode;
			balanceData.setCurreny(getElementById(fstElmnt, "currency"));
			balanceData.setTotal(getElementById(fstElmnt, "totalIncludingVat"));
			balanceData.setVatPercent(getElementById(fstElmnt, "vatPercent"));
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return balanceData;
	}
	
	public SipgateProvisioningData getProvisioningData() throws ApiException, AuthenticationErrorException, NetworkProblemException {
		// request API
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.getProvisioningData();
		} catch (AuthenticationErrorException e) {
			throw e;
		} catch (NetworkProblemException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		/*
		 * debug api result
		 * 
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				Log.e("FOOOOO", line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		*/
		
		SipgateProvisioningData provisioningData = new SipgateProvisioningData();
		// process stream from API
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("extension");
			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					if (getElementById(fstElmnt, "type").equals("register")){
						SipgateProvisioningExtension extension = new SipgateProvisioningExtension();
						extension.setAlias(getElementById(fstElmnt, "alias"));
						extension.setSipid(getElementById(fstElmnt, "extensionId"));
						extension.setPassword(getElementById(fstElmnt, "sipPassword"));
						provisioningData.addExtension(extension);
						if (provisioningData.getExtensionCount() == 1){
							provisioningData.setOutboundProxy(getElementById(fstElmnt, "outboundProxyUrl"));
							provisioningData.setRegistrar(getElementById(fstElmnt, "registerUrl"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return provisioningData;
	}
	
	private Date getDate(String createOn) {
		try {
			if (createOn == null) {
				return new Date(0);
			}
			SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			return dateformatterIso.parse(createOn, new ParsePosition(0));
		} catch (IllegalArgumentException e) {
			Log.e(TAG,"badly formated date");
			
		}
		return new Date(0);
	}
	
	public ArrayList<SipgateCallData> getCalls() throws ApiException {
		
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.getCalls();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		ArrayList<SipgateCallData> calls = new ArrayList<SipgateCallData>();
		// process stream from API
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("call");
			for (int s = 0; s < nodeLst.getLength(); s++) {
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					SipgateCallData call = new SipgateCallData();
					
					Element sources = getNodeById(fstElmnt, "sources");
					Element sourceEndpoint = getNodeById(sources, "endpoint");
					String sourceName = getElementById(sourceEndpoint, "contactFN");
					String sourceNumberPretty = getElementById(sourceEndpoint, "numberPretty");
					String sourceNumberE164 = getElementById(sourceEndpoint, "numberE164");
					
					Element targets = getNodeById(fstElmnt, "targets");
					Element targetEndpoint = getNodeById(targets, "endpoint");
					String targetName = getElementById(targetEndpoint, "contactFN");
					String targetNumberPretty = getElementById(targetEndpoint, "numberPretty");
					String targetNumberE164 = getElementById(targetEndpoint, "numberE164");
					
					call.setCallId(getElementById(fstElmnt, "id"));
					String direction = getElementById(fstElmnt, "direction");
					if(direction.equals("incoming")){
						call.setCallMissed(false);
						call.setCallDirection("incoming");
						call.setCallTarget(targetNumberE164, targetNumberPretty, targetName);
						call.setCallSource(sourceNumberE164, sourceNumberPretty, sourceName);
					}
					else if(direction.equals("missed_incoming")){
						call.setCallMissed(true);
						call.setCallDirection("incoming");
						call.setCallTarget(targetNumberE164, targetNumberPretty, targetName);
						call.setCallSource(sourceNumberE164, sourceNumberPretty, sourceName);
					}
					call.setCallTime(getDate(getElementById(fstElmnt, "created")));
					calls.add(call);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return calls;
	}
	
	public String getBaseProductType() throws IOException, OAuthException, URISyntaxException,
		OAuthAccessProtectedResourceException, OAuthMissingContextException  {
			InputStream inputStream = null;
			try {
				inputStream = RestClient.authenticationInterface.getBaseProductType();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			if (inputStream == null) {
				Log.e(TAG, "wtf, inputstream is null");
				return null;
			}
			
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(inputStream);
				doc.getDocumentElement().normalize();
				
				String ret = parseBaseProductType(doc);
			
				return ret;	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware) {
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.setupMobileExtension(phoneNumber, model, vendor, firmware);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			if (doc == null || doc.getDocumentElement() == null) {
				Log.e(TAG, "invalid document returned by api. could not setup mobile extension");
				return null;
			}
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getChildNodes();
			
			String sipid = null;
			String sippassword = null;

			for (int i = 0; i < nodeList.getLength(); i++) {			
				Node n = nodeList.item(i);
				
				Log.d(TAG, "setup mobile device node: " + n.getNodeName());
				
				if (n.getNodeName().equals("credentials")) {
					for (int j = 0; j < nodeList.getLength(); j++) {
						Node p = nodeList.item(j);
						if (p.getNodeName().equals("sipId")) {
							sipid = p.getNodeValue();
						} else if (p.getNodeName().equals("sipPassword")) {
							sippassword = p.getNodeValue();
						}
					}
				}
			}
			
			return new MobileExtension(sipid, null, null, null, sippassword);
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	
	}
	
	public List<MobileExtension> getMobileExtensions() throws IOException, OAuthException, URISyntaxException,
	OAuthAccessProtectedResourceException, OAuthMissingContextException  {
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.getMobileExtensions();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		//List<MobileExtension> ret = new ArrayList<MobileExtension>();
		
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			List<MobileExtension> extensions = parseMobileExtensions(doc);

			Iterator<MobileExtension> i = extensions.iterator();
			
			while (i.hasNext()) {
				MobileExtension e = i.next();
				Log.d(TAG, "mobile extension: " + e.getExtensionId());
			}
			
			return extensions;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String parseBaseProductType(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("BaseProductType");
		Node eventRecord = nodeList.item(0);
		nodeList = eventRecord.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {			
			Node n = nodeList.item(i);
			
			if (n.getNodeName().equals("baseproducttype")) {
				return n.getFirstChild().getNodeValue();
			}
		}
		return null;
	}
	
	private List<MobileExtension> parseMobileExtensions(Document doc) {
		List<MobileExtension> ret = new ArrayList<MobileExtension>();
		NodeList nodeList = doc.getElementsByTagName("extensions");

		Node eventRecord = nodeList.item(0);
		
		nodeList = eventRecord.getChildNodes();
		
		Log.v(TAG,"got extension list with " + nodeList.getLength()+ " nodes");
		for (int i = 0; i < nodeList.getLength(); i++) {			
			Node n = nodeList.item(i);
			MobileExtension e = null;
			
			if (n.getNodeName().equals("extension")) {
				e = MobileExtension.fromXMLNode(n);
			}
			if (e != null) {
				ret.add(e);
			} 
		}
		
		return ret;
	}

	public List<Event> getEvents() throws ApiException {
		
		InputStream inputStream = null;
		try {
			inputStream = RestClient.authenticationInterface.getEvents();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		List<Event> ret = new ArrayList<Event>();
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("events");
	
			Node eventRecord = nodeList.item(0);
			
			nodeList = eventRecord.getChildNodes();
			
			Log.v(TAG,"got voicemail list with " + nodeList.getLength()+ " nodes");
			for (int i = 0; i < nodeList.getLength(); i++) {			
				Node n = nodeList.item(i);
				Event e = null;
				
				if (n.getNodeName().equals("voicemail")) {
					e = Voicemail.fromXMLNode(n);
				} else if (n.getNodeName().equals("sms")) {
					e = SMS.fromXMLNode(n);
				} else if (n.getNodeName().equals("call")) {
					// handle call object
					// e = Call.fromXMLNode(n);
				} else {
					Log.d(TAG,"unknown nodename " + n.getNodeName());
				}
				if (e != null) {
					ret.add(e);
				} 
			}
		}
		catch (Exception e){
			Log.e(TAG, e.getLocalizedMessage());
		}
		
		return ret;
	}
	
	public InputStream getVoicemail(String voicemail) throws ApiException {
		try {
			return RestClient.authenticationInterface.getVoicemail(voicemail);
		} catch (Exception e) {
			throw new ApiException();
		}
	}
	
	public void setVoicemailRead(String voicemail) throws ApiException, NetworkProblemException {
		try {
			RestClient.authenticationInterface.setVoicemailRead(voicemail);
		} catch (AccessProtectedResourceException e) {
			e.printStackTrace();
			throw new ApiException();
		}
	}

	private String getElementById(Element fstElmnt, String id) {
		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(id);
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		if (fstNmElmnt != null && fstNmElmnt.hasChildNodes()){
			NodeList fstNm = fstNmElmnt.getChildNodes();
			return ((Node) fstNm.item(0)).getNodeValue();
		}

		return null;
	}
	
	private Element getNodeById(Element fstElmnt, String id) {
		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(id);
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);

		return (Element) fstNmElmnt;
	}


	public boolean connectivityOk() throws ApiException, NetworkProblemException {
		// TODO FIXME: We want some test that has less impact on the system!!!
		try {
			if (this.getProvisioningData() == null) {
				return false;
			}
		} catch (NetworkProblemException e) {
			throw e;
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	public boolean featureAvailable(API_FEATURE feature) {
		boolean ret = false;
		
		if (feature == API_FEATURE.VM_LIST) {
			ret = true;
		}
		
		return ret;
	}

}
