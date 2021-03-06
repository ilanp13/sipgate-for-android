package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;
import org.zoolu.sip.address.SipURL;

import android.util.Log;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.api.types.RegisteredMobileDevice;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.ContactNumberDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.interfaces.ApiClientInterface;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateOwnUri;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.models.SipgateProvisioningExtension;
import com.sipgate.models.SipgateUserdataSip;
import com.sipgate.models.api.xmlrpc.SipgateServerData;
import com.sipgate.util.ApiServiceProviderImpl.API_FEATURE;

/*
 * 
 * XML-RPC client for 1.0 API access
 * 
 * For 2.0 API access use the RestClient class instead!
 * 
 */

public class XmlrpcClient implements ApiClientInterface {
	private static final String TAG = "XmlrpcClient";
	
	private static final String VERSION = "0.1";
	private static final String VENDOR = "sipgate GmbH";
	private static final String NAME = "Sipdroid4sipgate";
	
	private XMLRPCClient client = null;
	
	private static final SimpleDateFormat periodFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static final PhoneNumberFormatter formatter = new PhoneNumberFormatter();
	private static final Locale locale = Locale.getDefault();
	
	private HashMap<String, Object> parameters = null;
	private HashMap<String, Object> apiResult = null;
	
	public XmlrpcClient(String ApiUser, String ApiPassword) throws ApiException {
		super();

		try {
			init(Constants.XMLRPC_API_10_SERVER_URL, ApiUser, ApiPassword);
		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPCExceptin in XmlrpcClient(): " + e.toString());
			throw new ApiException();
		}
	}

	private void init(String ApiUrl, String ApiUser, String ApiPassword) throws XMLRPCException {
		Log.d(TAG, "init()");

		client = new XMLRPCClient(ApiUrl);
		client.setBasicAuthentication(ApiUser, ApiPassword);
		
		parameters = new HashMap<String, Object>();
		apiResult = new HashMap<String, Object>();
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> doXmlrpcCall(String method, Object param) throws XMLRPCException, NetworkProblemException {
		apiResult.clear();
		try {
			apiResult = (HashMap<String, Object>) this.client.call(method, param);
		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPCExceptin in clientIdentify(): " + e.toString());
			Throwable cause = e.getCause();
			if (cause.getClass().equals(UnknownHostException.class)) {
				throw new NetworkProblemException();
			} 
			else if (cause.getClass().equals(SocketException.class)) {
				throw new NetworkProblemException();
			}
			else {
				throw e;
			}
		}
		return apiResult;
	}

	public boolean connectivityOk() throws ApiException, NetworkProblemException 
	{
		try
		{
			parameters.clear();
			
			parameters.put("ClientName", NAME);
			parameters.put("ClientVersion", VERSION);
			parameters.put("ClientVendor", VENDOR);
		
			apiResult = this.doXmlrpcCall("samurai.ClientIdentify", parameters);
		
			return ("200".equals(apiResult.get("StatusCode").toString()));
		}
		catch (XMLRPCException e)
		{
			return false;
		}
	}

	private SipgateServerData serverDataGet() throws XMLRPCException, NetworkProblemException {
		SipgateServerData sipgateServerData = new SipgateServerData();

		parameters.clear();
		
		apiResult = this.doXmlrpcCall("samurai.ServerdataGet", parameters);

		sipgateServerData.setSipRegistrar((String) apiResult.get("SipRegistrar"));
		sipgateServerData.setSipOutboundProxy((String) apiResult.get("SipOutboundProxy"));

		return sipgateServerData;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<SipgateUserdataSip> userdataSipGet(ArrayList<String> requestedUris) throws XMLRPCException, NetworkProblemException {
		ArrayList<SipgateUserdataSip> result = new ArrayList<SipgateUserdataSip>();

		parameters.clear();
		
		parameters.put("LocalUriList", requestedUris);

		apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.UserdataSipGet", parameters);
		
		Object[] sipDataListObject = (Object[]) apiResult.get("SipDataList");

		HashMap<String, Object> sipDataSet = null;
		SipgateUserdataSip userData = null;
		
		for (Object sipDataSetObject : sipDataListObject) {
			sipDataSet = (HashMap<String, Object>) sipDataSetObject;
			userData = new SipgateUserdataSip();

			userData.setLocalUri((String) sipDataSet.get("LocalUri"));
			userData.setSipUserID((String) sipDataSet.get("SipUserID"));
			userData.setSipPassword((String) sipDataSet.get("SipPassword"));
			
			result.add(userData);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<SipgateOwnUri> ownUriListGet() throws XMLRPCException, NetworkProblemException {
		ArrayList<SipgateOwnUri> result = new ArrayList<SipgateOwnUri>();

		parameters.clear();
		
		apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.OwnUriListGet", parameters);

		Object[] ownUriList = (Object[]) apiResult.get("OwnUriList");

		HashMap<String, Object> ownUriSet = null;
		SipgateOwnUri ownUriDataTmp = null;
		
		for (Object ownUriSetObject : ownUriList) 
		{
			ownUriSet = (HashMap<String, Object>) ownUriSetObject;
			ownUriDataTmp = new SipgateOwnUri();

			ownUriDataTmp.setSipUri((String) ownUriSet.get("SipUri"));
			ownUriDataTmp.setE164Out((String) ownUriSet.get("E164Out"));

			Object[] inListObject = (Object[]) ownUriSet.get("E164In");
			
			ArrayList<String> inList = new ArrayList<String>();
			
			for (Object inObject : inListObject) 
			{
				inList.add((String) inObject);
			}
			
			ownUriDataTmp.setE164In(inList);

			Object[] tosListObject = (Object[]) ownUriSet.get("TOS");
			
			ArrayList<String> tosList = new ArrayList<String>();
			
			for (Object tosObject : tosListObject) 
			{
				tosList.add((String) tosObject);
			}
			
			ownUriDataTmp.setTos(tosList);

			ownUriDataTmp.setDefaultUri((Boolean) ownUriSet.get("DefaultUri"));
			ownUriDataTmp.setUriAlias((String) ownUriSet.get("UriAlias"));

			result.add(ownUriDataTmp);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public SipgateBalanceData getBillingBalance() throws ApiException, FeatureNotAvailableException, NetworkProblemException {
		SipgateBalanceData balance = null;

		parameters.clear();
		
		try {
			apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.BalanceGet", parameters);
			balance = new SipgateBalanceData();

			HashMap<String, Object> currentBalance = (HashMap<String, Object>) apiResult.get("CurrentBalance");

			Float totalIncludingVat = new Float((Double) currentBalance.get("TotalIncludingVat"));
		
			balance.setTotal(totalIncludingVat.toString());
			balance.setCurreny((String) currentBalance.get("Currency"));

			if (currentBalance.containsKey("VatPercent")) {
				Float vatPercent = new Float((Double) currentBalance.get("VatPercent"));
				balance.setVatPercent(vatPercent.toString());
			}

		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPC call to 'samurai.BalanceGet' failed with " + e.toString());
			throw new ApiException();
		}

		return balance;
	}

	/**
	 * This method calls the xmlrpc-api and request call data.
	 * If one of the params is <= 0 a full list is requested, otherwise a list 
	 * with data in the given period. 
	 * 
	 * @param periodStart a periodStart value in unix timestamp
	 * @param periodEnd a periodEnd value in unix timestamp 
	 * @return a Vector filled with CallDataDBObjects or 
	 */
	@SuppressWarnings("unchecked")
	public Vector<CallDataDBObject> getCalls(long periodStart, long periodEnd) throws ApiException {
		
		Vector<CallDataDBObject> callDataDBObjects = new Vector<CallDataDBObject>();

		parameters.clear();

		if (periodStart > 0)
		{
			parameters.put("PeriodStart", periodFormatter.format(new Date(periodStart)));
		}
		
		if (periodStart > 0)
		{
			parameters.put("PeriodEnd", periodFormatter.format(new Date(periodEnd)));
		}
		
		try {
			
			apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.HistoryGetByDate", parameters);
			
			Object[] historyList = (Object[]) apiResult.get("History");
			
			CallDataDBObject callDataDBObject = null;
			HashMap<String, Object> historySet = null;
			
			String direction = null;
			
			String numberLocal = null;
			String numberRemote = null;
			
			String localNumberPretty = null;
			String localNumberE164 = null;
			
			String remoteNumberPretty = null;
			String remoteNumberE164 = null;
			
			String callID = null;
			
			Integer count = 0;
			
			for (Object historyObject : historyList) {
				
				callDataDBObject = new CallDataDBObject();
				
				historySet = (HashMap<String, Object>) historyObject;
				
				if (historySet.containsKey("TOS") && historySet.get("TOS") != null && !historySet.get("TOS").equals("voice")) {
					continue;
				}
				
				count++;
				if (count == 101) break;

				callID = (String)historySet.get("EntryID");
				
				if (callID != null && callID.length() > 0)
				{
					if (callID.substring(0,1).equals("C"))
					{
						callID = "0" + callID.substring(2, 17);
					}
					else if (callID.substring(0,1).equals("O"))
					{
						callID = "1" + callID.substring(2, 17);
					}
					else
					{
						callID = "2" + callID.substring(2, 17);
					}
					
					callDataDBObject.setId(Long.parseLong(callID, 16));
				}
				else
				{
					// Better use CurrentTimeMillis as id then nothing ;)
					callDataDBObject.setId(System.currentTimeMillis());
				}
								
				callDataDBObject.setTime(getCallTime((String) historySet.get("Timestamp")));

				direction = (String) historySet.get("Status");
				
				numberLocal = (String) historySet.get("LocalUri");
				numberRemote = (String) historySet.get("RemoteUri");
				
				numberLocal = formatter.formattedPhoneNumberFromStringWithCountry(numberLocal, locale.getCountry());
				numberRemote = formatter.formattedPhoneNumberFromStringWithCountry(numberRemote, locale.getCountry());
				
				formatter.initWithE164(numberLocal, locale.getCountry());
				
				localNumberPretty = formatter.formattedNumber();
				localNumberE164 = formatter.e164NumberWithPrefix("+");
				
				formatter.initWithE164(numberRemote, locale.getCountry());
								
				remoteNumberPretty = formatter.formattedNumber();
				remoteNumberE164 = formatter.e164NumberWithPrefix("+");
								
				if(direction.equals("accepted")){
					callDataDBObject.setMissed(false);
					callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				}
				else if(direction.equals("missed")){
					callDataDBObject.setMissed(true);
					callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				}
				else if(direction.equals("outgoing")){
					callDataDBObject.setMissed(false);
					callDataDBObject.setDirection(CallDataDBObject.OUTGOING);
				}
				
				callDataDBObject.setRemoteNumberE164(remoteNumberE164);
				callDataDBObject.setRemoteNumberPretty(remoteNumberPretty);
				callDataDBObject.setRemoteName("");
				callDataDBObject.setLocalNumberE164(localNumberE164);
				callDataDBObject.setLocalNumberPretty(localNumberPretty);
				callDataDBObject.setLocalName("");
		
				callDataDBObject.setRead(-1);
				callDataDBObject.setReadModifyUrl("");
				
				callDataDBObjects.add(callDataDBObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApiException();
		}
		
		return callDataDBObjects;
	}

	@Override
	public Vector<VoiceMailDataDBObject> getVoiceMails(long periodStart, long periodEnd) throws ApiException, FeatureNotAvailableException
	{
		throw new FeatureNotAvailableException();
	}
 
	public SipgateProvisioningData getProvisioningData() throws ApiException, FeatureNotAvailableException, AuthenticationErrorException, NetworkProblemException {
	
		SipgateServerData serverData = null;
		
		try {
			serverData = serverDataGet();

		} catch (XMLRPCException e) {

			if (XMLRPCFault.class.isInstance(e)) {
				XMLRPCFault f = (XMLRPCFault) e;
				if (f.getFaultCode() == 401) {
					throw new AuthenticationErrorException();
				}
			}

			Log.e(TAG, "serverDataGet() failed with " + e.toString());
			throw new ApiException();
		}

		SipgateProvisioningData prov = new SipgateProvisioningData();

		// set SIP server info
		prov.setRegistrar(serverData.getSipRegistrar());
		prov.setOutboundProxy(serverData.getSipOutboundProxy());

		// add extensions
		ArrayList<SipgateOwnUri> ownUris = null;
		try {
			ownUris = ownUriListGet();
		} catch (XMLRPCException e) {
			Log.e(TAG, "ownUriListGet() failed with " + e.toString());
			throw new ApiException();
		}

		// request credentials for all sip-uris
		ArrayList<String> requestedUris = new ArrayList<String>();
		HashMap<String, String> uriToAlias = new HashMap<String, String>();
		
		for (SipgateOwnUri sipgateOwnUri : ownUris) {
			if (sipgateOwnUri.getTos().contains("voice")) {
				requestedUris.add(sipgateOwnUri.getSipUri());

				// remember alias
				uriToAlias.put(sipgateOwnUri.getSipUri(), sipgateOwnUri.getUriAlias());
			}
		}
		
		ArrayList<SipgateUserdataSip> sipUserdataList = null;
		try {
			sipUserdataList = userdataSipGet(requestedUris);
		} catch (XMLRPCException e) {
			Log.e(TAG, "userdataSipGet() failed with " + e.toString());
			throw new ApiException();
		}

		SipURL extUrl = null;
		
		for (SipgateUserdataSip sipUserdata : sipUserdataList) {
			SipgateProvisioningExtension newExt = new SipgateProvisioningExtension();

			// we only need the user-part of the uri:
			extUrl = new SipURL(sipUserdata.getLocalUri());
			newExt.setSipid(extUrl.getUserName());

			newExt.setPassword(sipUserdata.getSipPassword());

			// get alias from temporary map:
			newExt.setAlias(uriToAlias.get(sipUserdata.getLocalUri()));

			prov.addExtension(newExt);
		}

		return prov;
	}
	
	private long getCallTime(String dateString) 
	{
		long callTime = 0;
		
		try {
			if (dateString != null)
			{
				return dateformatterPretty.parse(dateString).getTime();
			}
		} 
		catch (ParseException e) {
			Log.e(TAG, "getCallTime", e);
		}
		
		return callTime;
	}
		
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException
	{
		throw new FeatureNotAvailableException();
	}

	
	public void setVoiceMailRead(String voicemail) throws ApiException, FeatureNotAvailableException 
	{
		throw new FeatureNotAvailableException();
	}
	
	public void setCallRead(String voicemail) throws ApiException, FeatureNotAvailableException
	{
		throw new FeatureNotAvailableException();
	}
	
	public boolean featureAvailable(API_FEATURE feature) {
		return false;
	}
	
	public String getBaseProductType() throws IOException, URISyntaxException, FeatureNotAvailableException 
	{
		return "basic/plus";
	}
	
	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware) throws FeatureNotAvailableException 
	{
		throw new FeatureNotAvailableException();
	}

	/**
	 * This method fetchs all sipgate contacts from the account
	 * @return a Vector with ContactDataDBObjects
	 */
	@SuppressWarnings("unchecked")
	public Vector<ContactDataDBObject> getContacts() throws ApiException, FeatureNotAvailableException
	{
		Vector<ContactDataDBObject> contactDataDBObjects = new Vector<ContactDataDBObject>();

		try 
		{
			parameters.clear();
			apiResult = (HashMap<String, Object>) doXmlrpcCall("samurai.PhonebookListGet", parameters);
			
			Object[] phonebookList = (Object[]) apiResult.get("PhonebookList");
			
			HashMap<String, Object> objectHashMap = null;
			
			ArrayList<String> entryIds = new ArrayList<String>();
			
			for (Object phonebookObject : phonebookList) 
			{
				objectHashMap = (HashMap<String, Object>) phonebookObject;
				
				entryIds.add((String)objectHashMap.get("EntryID"));
			}
			
			parameters.clear();
			parameters.put("EntryIDList", entryIds);
			
			apiResult = (HashMap<String, Object>) doXmlrpcCall("samurai.PhonebookEntryGet", parameters);

			Object[] entryList = (Object[]) apiResult.get("EntryList");
			
			for (Object entryObject : entryList) 
			{
				objectHashMap = (HashMap<String, Object>) entryObject;
				
				contactDataDBObjects.add(getContactDataDBObjectFromVCard((String)objectHashMap.get("EntryID"), (String)objectHashMap.get("Entry")));
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new ApiException();
		}
		
		return contactDataDBObjects;
	}
	
	/**
	 * This function created a ContactDataDBObject with ContactNumberDBObjects from the given String in vCard-format
	 * @param entryId the unique entry uuid
	 * @param vCard a String filled with vCard-Data
	 * @return a ContactDataDBObject with all ContactNumberDBObjects of the vCard
	 */
	public ContactDataDBObject getContactDataDBObjectFromVCard(String entryId, String vCard)
	{
		ContactDataDBObject contactDataDBObject = new ContactDataDBObject();
		ContactNumberDBObject contactNumberDBObject = null;
		
		contactDataDBObject.setUuid(entryId);
		
		String[] vCardRows = vCard.split("\n");
		
		String value = "";
		
		for (String currentRow : vCardRows)
		{
			if (currentRow.contains("FN;"))
			{
				currentRow = currentRow.substring(currentRow.indexOf(":") + 1);
				
				if (contactDataDBObject.getDisplayName().length() == 0)
				{
					contactDataDBObject.setDisplayName(decodeQuotedPrintable(currentRow));
				}
			}
			else if (currentRow.contains("TEL;"))
			{
				contactNumberDBObject = new ContactNumberDBObject();

				contactNumberDBObject.setUuid(contactDataDBObject.getUuid());
				
				currentRow = currentRow.substring(currentRow.indexOf(";") + 1);
				
				if (currentRow.indexOf(";") > -1)
				{
					value = currentRow.substring(0, currentRow.indexOf(";")).toUpperCase();
					currentRow = currentRow.substring(currentRow.indexOf(";") + 1);
				}
				else
				{
					value = currentRow.substring(0, currentRow.indexOf(":")).toUpperCase();
				}
								
				if (value.equalsIgnoreCase("CELL"))
				{
					if (currentRow.indexOf(";") > -1)
					{
						value = currentRow.substring(0, currentRow.indexOf(";")).toUpperCase();
					}
					
					contactNumberDBObject.setType(value.toUpperCase());
				} 
				else if (value.equalsIgnoreCase("VOICE"))
				{
					if (currentRow.indexOf(";") > -1)
					{
						value = currentRow.substring(0, currentRow.indexOf(";")).toUpperCase();
					}
					else
					{
						value = "WORK";
					}
					
					contactNumberDBObject.setType(value.toUpperCase());
				}
				else
				{
					contactNumberDBObject.setType("OTHER");
				}
			
				currentRow = currentRow.substring(currentRow.indexOf(":") + 1);
				
				contactNumberDBObject.setNumberE164(currentRow);
				contactNumberDBObject.setNumberPretty(formatter.formattedPhoneNumberFromStringWithCountry(currentRow, locale.getCountry()));
			
				contactDataDBObject.addContactNumberDBObject(contactNumberDBObject);	
			}
		}
				
		return contactDataDBObject;
	}
	
	/**
	 * This methoded decodes a string from quoted-printable to plain text 
	 * @param inString the input String
	 * @returnthe decoded plain text string
	 */
	public String decodeQuotedPrintable(String inString)
	{
		if (inString.contains("=")) 
		{
	        StringBuffer builder = new StringBuffer();
	        
	        int pos;

	        while (inString.contains("=")) 
	        {
	        	pos = inString.indexOf('=');
	            
	        	builder.append(inString.substring(0, pos));

	            char chr = (char) (Character.digit(inString.charAt(pos + 1), 16) * 16 + Character.digit(inString.charAt(pos + 2), 16));
	          
	            builder.append(chr);

	            inString = inString.substring(pos + 2 + 1);
	        }
	        
	        builder.append(inString);
	        
	        return builder.toString();
	    } 
		else 
	    {
	        return inString;
	    }
	}

	public Vector<RegisteredMobileDevice> getRegisteredMobileDevices() throws FeatureNotAvailableException, ApiException
	{
		throw new FeatureNotAvailableException();
	}
}

