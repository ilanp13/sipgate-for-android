package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;
import org.zoolu.sip.address.SipURL;

import android.util.Log;

import com.sipgate.api.types.MobileExtension;
import com.sipgate.db.CallDataDBObject;
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
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

/*
 * 
 * XML-RPC client for 1.0 API access
 * 
 * For 2.0 API access use the RestClient class instead!
 * 
 */

public class XmlrpcClient implements ApiClientInterface {
	private static final String TAG = XmlrpcClient.class.getSimpleName();
	private static final String VERSION = "0.1";
	private static final String VENDOR = "sipgate GmbH";
	private static final String NAME = "Sipdroid4sipgate";
	private XMLRPCClient client = null;
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	private static final PhoneNumberFormatter formatter = new PhoneNumberFormatter();
	private static final Locale locale = Locale.getDefault();
	
	public XmlrpcClient(String ApiUser, String ApiPassword) throws ApiException {
		super();

		try {
			init(Constants.XMLRPC_API_10_SERVER_URL, ApiUser, ApiPassword);
		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPCExceptin in XmlrpcClient(): " + e.getLocalizedMessage());
			throw new ApiException();
		}
	}

	private void init(String ApiUrl, String ApiUser, String ApiPassword) throws XMLRPCException {
		Log.d(TAG, "init()");
		this.client = new XMLRPCClient(ApiUrl);

		this.client.setBasicAuthentication(ApiUser, ApiPassword);
	}

	private Date getDate(String createOn) {
		try {
			if (createOn == null) {
				return new Date(0);
			}
			SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			Log.d(TAG, "starting date parsing");
			Date ret = dateformatterIso.parse(createOn, new ParsePosition(0));
			Log.d(TAG, "finished date parsing");
			return ret;
		} catch (IllegalArgumentException e) {
			Log.e(TAG,"badly formated date");
			
		}
		return new Date(0);
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> doXmlrpcCall(String method, Object param) throws XMLRPCException, NetworkProblemException {
		HashMap<String, Object> res = null;
		try {
			res = (HashMap<String, Object>) this.client.call(method, param);
		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPCExceptin in clientIdentify(): " + e.getLocalizedMessage());
			Throwable cause = e.getCause();
			if (cause.getClass().equals(UnknownHostException.class)) {
				throw new NetworkProblemException();
			} else {
				throw e;
			}
		}
		return res;
	}

	private void clientIdentify() throws XMLRPCException, NetworkProblemException {
		Hashtable<String, String> ident = new Hashtable<String, String>();
		ident.put("ClientName", NAME);
		ident.put("ClientVersion", VERSION);
		ident.put("ClientVendor", VENDOR);
		HashMap<String, Object> apiResponse = this.doXmlrpcCall("samurai.ClientIdentify", ident);
		Log.d(TAG, apiResponse.toString());
	}

	private SipgateServerData serverDataGet() throws XMLRPCException, NetworkProblemException {
		SipgateServerData res = new SipgateServerData();

		Hashtable<String, Object> params = new Hashtable<String, Object>();
		HashMap<String, Object> apiResponse = (HashMap<String, Object>) this.doXmlrpcCall("samurai.ServerdataGet", params);

		res.setSipRegistrar((String) apiResponse.get("SipRegistrar"));
		res.setSipOutboundProxy((String) apiResponse.get("SipOutboundProxy"));

		return res;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<SipgateUserdataSip> userdataSipGet(ArrayList<String> requestedUris) throws XMLRPCException, NetworkProblemException {
		ArrayList<SipgateUserdataSip> res = new ArrayList<SipgateUserdataSip>();

		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.put("LocalUriList", requestedUris);

		HashMap<String, Object> apiResponse = (HashMap<String, Object>) this.doXmlrpcCall("samurai.UserdataSipGet", params);
		Object[] sipDataListObject = (Object[]) apiResponse.get("SipDataList");

		for (Object sipDataSetObject : sipDataListObject) {
			HashMap<String, Object> sipDataSet = (HashMap<String, Object>) sipDataSetObject;
			SipgateUserdataSip userData = new SipgateUserdataSip();

			userData.setLocalUri((String) sipDataSet.get("LocalUri"));
			userData.setSipUserID((String) sipDataSet.get("SipUserID"));
			userData.setSipPassword((String) sipDataSet.get("SipPassword"));

			
			res.add(userData);
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<SipgateOwnUri> ownUriListGet() throws XMLRPCException, NetworkProblemException {
		ArrayList<SipgateOwnUri> res = new ArrayList<SipgateOwnUri>();

		Hashtable<String, Object> params = new Hashtable<String, Object>();
		HashMap<String, Object> apiResponse = (HashMap<String, Object>) this.doXmlrpcCall("samurai.OwnUriListGet", params);

		Object[] ownUriList = (Object[]) apiResponse.get("OwnUriList");

		for (Object ownUriSetObject : ownUriList) {
			HashMap<String, Object> ownUriSet = (HashMap<String, Object>) ownUriSetObject;
			SipgateOwnUri ownUriDataTmp = new SipgateOwnUri();

			ownUriDataTmp.setSipUri((String) ownUriSet.get("SipUri"));
			ownUriDataTmp.setE164Out((String) ownUriSet.get("E164Out"));

			Object[] inListObject = (Object[]) ownUriSet.get("E164In");
			ArrayList<String> inList = new ArrayList<String>();
			for (Object inObject : inListObject) {
				inList.add((String) inObject);
			}
			ownUriDataTmp.setE164In(inList);

			Object[] tosListObject = (Object[]) ownUriSet.get("TOS");
			ArrayList<String> tosList = new ArrayList<String>();
			for (Object tosObject : tosListObject) {
				tosList.add((String) tosObject);
			}
			ownUriDataTmp.setTos(tosList);

			ownUriDataTmp.setDefaultUri((Boolean) ownUriSet.get("DefaultUri"));
			ownUriDataTmp.setUriAlias((String) ownUriSet.get("UriAlias"));

			res.add(ownUriDataTmp);
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public SipgateBalanceData getBillingBalance() throws ApiException, FeatureNotAvailableException, NetworkProblemException {
		SipgateBalanceData balance = null;

		Hashtable<String, Object> params = new Hashtable<String, Object>();
		try {
			HashMap<String, Object> apiResponse = (HashMap<String, Object>) this.doXmlrpcCall("samurai.BalanceGet", params);
			balance = new SipgateBalanceData();

			HashMap<String, Object> currentBalance = (HashMap<String, Object>) apiResponse.get("CurrentBalance");

			// set total
			Float totalIncludingVat = new Float((Double) currentBalance.get("TotalIncludingVat"));
			balance.setTotal(totalIncludingVat.toString());

			balance.setCurreny((String) currentBalance.get("Currency"));

			// set vat-percent
			if (currentBalance.containsKey("VatPercent")) {
				Float vatPercent = new Float((Double) currentBalance.get("VatPercent"));
				balance.setVatPercent(vatPercent.toString());
			}

		} catch (XMLRPCException e) {
			Log.e(TAG, "XMLRPC call to 'samurai.BalanceGet' failed with " + e.getLocalizedMessage());
			throw new ApiException();
		}

		return balance;
	}

	
	@SuppressWarnings("unchecked")
	public Vector<CallDataDBObject> getCalls() throws ApiException {
		
		Hashtable<String, String> params = new Hashtable<String, String>();

		HashMap<String, Object> apiResponse = null;
		
		try {
			apiResponse = (HashMap<String, Object>) this.doXmlrpcCall("samurai.HistoryGetByDate", params);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "XMLRPC call to 'samurai.HistoryGetByDate' failed with " + e.getLocalizedMessage());
			throw new ApiException();
		}
		
		if (apiResponse == null) {
			Log.e(TAG, "wtf, inputstream is null");
			return null;
		}
		
		Vector<CallDataDBObject> callDataDBObjects = new Vector<CallDataDBObject>();

		try {
			Object[] HistoryList = (Object[]) apiResponse.get("History");
			Integer counter = 0;
			for (Object HistoryObject : HistoryList) {
				if(counter++ == 50) break;
				CallDataDBObject callDataDBObject = new CallDataDBObject();
				HashMap<String, Object> HistorySet = (HashMap<String, Object>) HistoryObject;
				
				if(!HistorySet.get("TOS").equals("voice")) continue;
				
				callDataDBObject.setId(Long.parseLong((String)HistorySet.get("EntryID")));
				callDataDBObject.setTime(getCallTime((String) HistorySet.get("Timestamp")));

				String direction = (String) HistorySet.get("Status");
				
				String numberLocal = (String) HistorySet.get("LocalUri");
				String numberRemote = (String) HistorySet.get("RemoteUri");

				String sourceNumberPretty = formatter.formattedPhoneNumberFromStringWithCountry(numberLocal, locale.getCountry());
				String sourceNumberE164 = formatter.e164NumberWithPrefix("");

				String targetNumberPretty = formatter.formattedPhoneNumberFromStringWithCountry(numberRemote, locale.getCountry());
				String targetNumberE164 = formatter.e164NumberWithPrefix("");
								
				if(direction.equals("accepted")){
					callDataDBObject.setMissed(false);
					callDataDBObject.setDirection(CallDataDBObject.INCOMING);
					callDataDBObject.setLocalNumberE164(targetNumberE164);
					callDataDBObject.setLocalNumberPretty(targetNumberPretty);
					callDataDBObject.setLocalName("");
					callDataDBObject.setRemoteNumberE164(sourceNumberE164);
					callDataDBObject.setRemoteNumberPretty(sourceNumberPretty);
					callDataDBObject.setRemoteName("");
				}
				else if(direction.equals("missed")){
					callDataDBObject.setMissed(true);
					callDataDBObject.setDirection(CallDataDBObject.INCOMING);
					callDataDBObject.setLocalNumberE164(targetNumberE164);
					callDataDBObject.setLocalNumberPretty(targetNumberPretty);
					callDataDBObject.setLocalName("");
					callDataDBObject.setRemoteNumberE164(sourceNumberE164);
					callDataDBObject.setRemoteNumberPretty(sourceNumberPretty);
					callDataDBObject.setRemoteName("");
				}
				else if(direction.equals("outgoing")){
					callDataDBObject.setMissed(false);
					callDataDBObject.setDirection(CallDataDBObject.OUTGOING);
					callDataDBObject.setRemoteNumberE164(targetNumberE164);
					callDataDBObject.setRemoteNumberPretty(targetNumberPretty);
					callDataDBObject.setRemoteName("");
					callDataDBObject.setLocalNumberE164(sourceNumberE164);
					callDataDBObject.setLocalNumberPretty(sourceNumberPretty);
					callDataDBObject.setLocalName("");
				}
				
				callDataDBObject.setRead(-1);

				callDataDBObjects.add(callDataDBObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return callDataDBObjects;

	}

	@Override
	public Vector<VoiceMailDataDBObject> getVoiceMails() throws ApiException, FeatureNotAvailableException
	{
		throw new FeatureNotAvailableException();
	}

	public SipgateProvisioningData getProvisioningData() throws ApiException, FeatureNotAvailableException,
			AuthenticationErrorException, NetworkProblemException {
		SipgateServerData serverData = null;
		try {
			serverData = this.serverDataGet();

		} catch (XMLRPCException e) {

			if (XMLRPCFault.class.isInstance(e)) {
				XMLRPCFault f = (XMLRPCFault) e;
				if (f.getFaultCode() == 401) {
					throw new AuthenticationErrorException();
				}
			}

			Log.e(TAG, "serverDataGet() failed with " + e.getLocalizedMessage());
			throw new ApiException();
		}

		SipgateProvisioningData prov = new SipgateProvisioningData();

		// set SIP server info
		prov.setRegistrar(serverData.getSipRegistrar());
		prov.setOutboundProxy(serverData.getSipOutboundProxy());

		// add extensions
		ArrayList<SipgateOwnUri> ownUris = null;
		try {
			ownUris = this.ownUriListGet();
		} catch (XMLRPCException e) {
			Log.e(TAG, "ownUriListGet() failed with " + e.getLocalizedMessage());
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
			Log.e(TAG, "userdataSipGet() failed with " + e.getLocalizedMessage());
			throw new ApiException();
		}

		for (SipgateUserdataSip sipUserdata : sipUserdataList) {
			SipgateProvisioningExtension newExt = new SipgateProvisioningExtension();

			// we only need the user-part of the uri:
			SipURL extUrl = new SipURL(sipUserdata.getLocalUri());
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
		
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public void setVoiceMailRead(String voicemail) throws ApiException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}
	
	public void setCallRead(String voicemail) throws ApiException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}
	
	public boolean connectivityOk() throws ApiException {
		boolean ret = true;

		try {
			this.clientIdentify();
		} catch (Exception e) {
			ret = false;
		}

		return ret;
	}

	
	public boolean featureAvailable(API_FEATURE feature) {
		return false;
	}

	
	public List<MobileExtension> getMobileExtensions() throws IOException, URISyntaxException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public String getBaseProductType() throws IOException, URISyntaxException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware)
			throws FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}
}
