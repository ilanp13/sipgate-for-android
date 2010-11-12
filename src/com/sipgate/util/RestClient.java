package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.AccessProtectedResourceException;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.interfaces.ApiClientInterface;
import com.sipgate.interfaces.RestAuthenticationInterface;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.models.SipgateProvisioningExtension;
import com.sipgate.parser.CallParser;
import com.sipgate.parser.ContactParser;
import com.sipgate.parser.VoiceMailParser;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

public class RestClient implements ApiClientInterface {
	
	private static final String TAG = "RestClient";
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	
	private RestAuthenticationInterface authenticationInterface = null;
	
	private InputStream inputStream = null;	
	
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Document doc = null;

	private NodeList nodeList = null;
	private Node node = null;
	private Element element = null;
	
	private NodeList helperNodeList = null;
	private Element helperElement = null;
	
	private int length = 0;
	private int subLength = 0;
	
	private SAXParser saxParser = null;
	private ContactParser contactParser = null;
	private CallParser callParser = null;
	private VoiceMailParser voiceMailParser = null;
	
	public RestClient(String username, String password) 
	{
		super();
		
		dbf = DocumentBuilderFactory.newInstance();
		authenticationInterface = new BasicAuthenticationClient(username, password);
		
		try 
		{
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			
			contactParser = new ContactParser();
			callParser = new CallParser();
			voiceMailParser = new VoiceMailParser();
		}
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		catch (SAXException e) 
		{
			e.printStackTrace();
		}
		catch (FactoryConfigurationError e) 
		{
			e.printStackTrace();
		}
	}

	public SipgateBalanceData getBillingBalance() throws ApiException 
	{
		try {
			inputStream = authenticationInterface.getBillingBalance();
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
			return null;
		}
		
		if (inputStream == null) {
			Log.e(TAG, "getBillingBalance() -> inputstream is null");
			return null;
		}
		
		SipgateBalanceData balanceData = new SipgateBalanceData();
		
		// process stream from API
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
		
			nodeList = doc.getElementsByTagName("fullBalance");
			
			node = nodeList.item(0);
			
			element = (Element) node;
			balanceData.setCurreny(getElementById(element, "currency"));
			balanceData.setTotal(getElementById(element, "totalIncludingVat"));
			balanceData.setVatPercent(getElementById(element, "vatPercent"));
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return balanceData;
	}
	
	public SipgateProvisioningData getProvisioningData() throws ApiException, AuthenticationErrorException, NetworkProblemException
	{
		try {
			inputStream = authenticationInterface.getProvisioningData();
		} catch (AuthenticationErrorException e) {
			throw e;
		} catch (NetworkProblemException e) {
			throw e;
		} catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
		
		if (inputStream == null) {
			Log.e(TAG, "getProvisioningData() -> inputstream is null");
			return null;
		}
		
		SipgateProvisioningData provisioningData = new SipgateProvisioningData();
		
		// process stream from API
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			
			nodeList = doc.getElementsByTagName("extension");
			
			SipgateProvisioningExtension extension = null;
			
			length = nodeList.getLength();
			
			for (int s = 0; s < length ; s++) {
				node = nodeList.item(s);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					
					element = (Element) node;
					
					if (getElementById(element, "type").equals("register")){
						extension = new SipgateProvisioningExtension();
						
						extension.setAlias(getElementById(element, "alias"));
						extension.setSipid(getElementById(element, "extensionId"));
						extension.setPassword(getElementById(element, "sipPassword"));
						provisioningData.addExtension(extension);
						
						if (provisioningData.getExtensionCount() == 1){
							provisioningData.setOutboundProxy(getElementById(element, "outboundProxyUrl"));
							provisioningData.setRegistrar(getElementById(element, "registerUrl"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return provisioningData;
	}
	

	public String getBaseProductType() throws IOException, URISyntaxException
	{
		try {
			inputStream = authenticationInterface.getBaseProductType();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if (inputStream == null) {
			Log.e(TAG, "getBaseProductType() -> inputstream is null");
			return null;
		}
		
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			
			nodeList = doc.getElementsByTagName("BaseProductType");
			
			if (nodeList == null) {
				return null;
			}
			
			node  = nodeList.item(0);
			if (node == null) {
				return null;
			}
			
			nodeList = node.getChildNodes();
			
			length = nodeList.getLength();
			
			Log.v(TAG, "parseBasePT " + length + " nodes");
			
			for (int i = 0; i < length; i++) {			
				node = nodeList.item(i);				
				
				if (node.getNodeName().equals("baseproducttype")) {
					Log.v(TAG, "parseBasePT found baseproducttype");
					return node.getFirstChild().getNodeValue();
				} else {
					Log.v(TAG, "parseBasePT nodename: " + node.getNodeName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware)
	{
		try {
			inputStream = authenticationInterface.setupMobileExtension(phoneNumber, model, vendor, firmware);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "setupMobileExtension() -> inputstream is null");
			return null;
		}
		
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(inputStream);
			
			if (doc == null || doc.getDocumentElement() == null) {
				Log.e(TAG, "invalid document returned by api. could not setup mobile extension");
				return null;
			}
			
			doc.getDocumentElement().normalize();
			nodeList = doc.getChildNodes();
			
			String sipid = null;
			String sippassword = null;
			
			length = nodeList.getLength();
			
			for (int i = 0; i < length; i++) {			
				node = nodeList.item(i);
				
				Log.d(TAG, "setup mobile device node: " + node.getNodeName());
				
				if (node.getNodeName().equals("credentials")) {
					
					subLength = nodeList.getLength();
					
					for (int j = 0; j < subLength; j++) {
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
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public List<MobileExtension> getMobileExtensions() throws IOException, URISyntaxException
	{
		try {
			inputStream = authenticationInterface.getMobileExtensions();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (inputStream == null) {
			Log.e(TAG, "getMobileExtensions() -> inputstream is null");
			return null;
		}
		
		try 
		{
			db = dbf.newDocumentBuilder();
			doc = db.parse(inputStream);
			doc.getDocumentElement().normalize();
			
			List<MobileExtension> extensions = new ArrayList<MobileExtension>();
			
			nodeList = doc.getElementsByTagName("extensions");

			node = nodeList.item(0);
			
			nodeList = node.getChildNodes();

			length = nodeList.getLength();
			
			Log.v(TAG,"got extension list with " + length + " nodes");
			
			MobileExtension e = null;
			
			for (int i = 0; i < length; i++) 
			{			
				node = nodeList.item(i);
				
				if (node.getNodeName().equals("extension")) {
					e = MobileExtension.fromXMLNode(node);
				
					if (e != null) {
						extensions.add(e);
					} 	
				}
			}
			
			return extensions;
		
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	public Vector<ContactDataDBObject> getContacts() throws ApiException
	{
		try 
		{
			inputStream = authenticationInterface.getContacts();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if (inputStream == null) 
		{
			Log.e(TAG, "getContacts() -> inputstream is null");
			return null;
		}
	
		if (contactParser != null && saxParser != null)
		{
			contactParser.init();
			
			try 
			{
				saxParser.parse(inputStream, contactParser);
				return contactParser.getContactDataDBObjects();
			}
			catch (SAXException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		Log.e(TAG, "getContacts() -> saxParser or contactParser is null");
		return null;
	}
	
	
	public Vector<CallDataDBObject> getCalls() throws ApiException
	{
		try 
		{
			inputStream = authenticationInterface.getCalls();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if (inputStream == null) 
		{
			Log.e(TAG, "getCalls() -> inputstream is null");
			return null;
		}
	
		if (callParser != null && saxParser != null)
		{
			callParser.init();
			
			try 
			{
				saxParser.parse(inputStream, callParser);
				return callParser.getCallDataDBObjects();
			}
			catch (SAXException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		Log.e(TAG, "getCalls() -> saxParser or callParser is null");
		return null;
	}
	
	public Vector<VoiceMailDataDBObject> getVoiceMails() throws ApiException
	{
		try 
		{
			inputStream = authenticationInterface.getVoiceMails();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if (inputStream == null) 
		{
			Log.e(TAG, "getVoiceMails() -> inputstream is null");
			return null;
		}
	
		if (voiceMailParser != null && saxParser != null)
		{
			voiceMailParser.init();
			
			try 
			{
				saxParser.parse(inputStream, voiceMailParser);
				return voiceMailParser.getVoiceMailDataDBObjects();
			}
			catch (SAXException e) 
			{
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		Log.e(TAG, "getVoiceMails() -> saxParser or voiceMails is null");
		return null;
	}
	
	public boolean connectivityOk() throws ApiException, NetworkProblemException 
	{
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
	
	public boolean featureAvailable(API_FEATURE feature) 
	{
		switch (feature) {
			case VM_LIST:
				return true;
			default:
				return false;
		}
	}
	
	public InputStream getVoicemail(String voicemail) throws ApiException 
	{
		try {
			return authenticationInterface.getVoicemail(voicemail);
		} catch (Exception e) {
			throw new ApiException();
		}
	}
	
	public void setVoiceMailRead(String voicemail) throws ApiException, NetworkProblemException
	{
		try {
			authenticationInterface.setVoicemailRead(voicemail);
		} catch (AccessProtectedResourceException e) {
			e.printStackTrace();
			throw new ApiException();
		}
	}
	
	public void setCallRead(String call) throws ApiException, NetworkProblemException 
	{
		try {
			authenticationInterface.setCallRead(call);
		} catch (AccessProtectedResourceException e) {
			e.printStackTrace();
			throw new ApiException();
		}
	}

	private String getElementById(Element element, String id) 
	{
		helperNodeList = element.getElementsByTagName(id);
		helperElement = (Element) helperNodeList.item(0);
		
		if (helperElement != null && helperElement.hasChildNodes()){
			helperNodeList = helperElement.getChildNodes();
			return helperNodeList.item(0).getNodeValue();
		}

		return null;
	}
	
	private Element getNodeById(Element element, String id) 
	{
		helperNodeList = element.getElementsByTagName(id);
		
		if (helperNodeList != null && helperNodeList.getLength() > 0){
			return (Element) helperNodeList.item(0);
		}
		
		return null;
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
			Log.e(TAG, "getCallTime", e);
		}
		
		return callTime;
	}
}
