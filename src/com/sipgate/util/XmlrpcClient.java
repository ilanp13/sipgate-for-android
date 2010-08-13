package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.oauth.OAuthException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;
import org.zoolu.sip.address.SipURL;

import android.util.Log;

import com.sipgate.R;
import com.sipgate.api.types.Event;
import com.sipgate.api.types.MobileExtension;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.AuthenticationErrorException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.exceptions.OAuthAccessProtectedResourceException;
import com.sipgate.exceptions.OAuthMissingContextException;
import com.sipgate.interfaces.ApiClientInterface;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.SipgateCallData;
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
			SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ssZZZZZ");
			return dateformatterIso.parse(createOn, new ParsePosition(0));
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

	
	public ArrayList<SipgateCallData> getCalls() throws ApiException {
		
		Hashtable<String, String> params = new Hashtable<String, String>();

		HashMap<String, Object> apiResponse = null;
		
		InputStream inputStream = null;
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
		
		ArrayList<SipgateCallData> calls = new ArrayList<SipgateCallData>();

		try {
			Object[] HistoryList = (Object[]) apiResponse.get("History");
			for (Object HistoryObject : HistoryList) {
				SipgateCallData call = new SipgateCallData();
				HashMap<String, Object> HistorySet = (HashMap<String, Object>) HistoryObject;
				
				if(!HistorySet.get("TOS").equals("voice")) continue;
				
				call.setCallId((String) HistorySet.get("EntryID"));
				call.setCallTime((Date) getDate((String) HistorySet.get("Timestamp")));

				String status = (String) HistorySet.get("Status");
				String direction = "";
				Boolean missed = false;
				
				Log.d("call Status: ", status);
				if(status.equals("accepted")) {
					direction = "incoming";
				}
				if(status.equals("missed")) {
					direction = "incoming";
					missed = true;
				}
				if(status.equals("outgoing")) {
					direction = "outgoing";
				}

				call.setCallDirection(direction);
				call.setCallMissed(missed);

				PhoneNumberFormatter formatter = new PhoneNumberFormatter();
				Locale locale = Locale.getDefault();
				
				String numberLocal = (String) HistorySet.get("LocalUri");
				String numberRemote = (String) HistorySet.get("RemoteUri");

				String src_number = "";
				String tgt_number = "";
				
				if(direction.equals("outgoing")) {
					src_number = numberLocal;
					tgt_number = numberRemote;
				}
				if(direction.equals("incoming")) {
					tgt_number = numberLocal;
					src_number = numberRemote;
				}

				String src_name = ""; // TODO: Match Phonebook Contacts - Here or somewhere else?
				String src_numberPretty = formatter.formattedPhoneNumberFromStringWithCountry(src_number, locale.getCountry());
				String src_numberE164 = formatter.e164NumberWithPrefix("");
				call.setCallSource(src_numberE164, src_numberPretty, src_name);

				String tgt_name = ""; // TODO: Match Phonebook Contacts - Here or somewhere else?
				String tgt_numberPretty = formatter.formattedPhoneNumberFromStringWithCountry(tgt_number, locale.getCountry());
				String tgt_numberE164 = formatter.e164NumberWithPrefix("");
				call.setCallTarget(tgt_numberE164, tgt_numberPretty, tgt_name);

				calls.add(call);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return calls;

	}


	public List<Event> getEvents() throws ApiException, FeatureNotAvailableException {
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

	
	public InputStream getVoicemail(String voicemail) throws ApiException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public void setVoicemailRead(String voicemail) throws ApiException, FeatureNotAvailableException {
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

	
	public List<MobileExtension> getMobileExtensions() throws IOException, OAuthException, URISyntaxException,
			OAuthAccessProtectedResourceException, OAuthMissingContextException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public String getBaseProductType() throws IOException, OAuthException, URISyntaxException,
			OAuthAccessProtectedResourceException, OAuthMissingContextException, FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}

	
	public MobileExtension setupMobileExtension(String phoneNumber, String model, String vendor, String firmware)
			throws FeatureNotAvailableException {
		throw new FeatureNotAvailableException();
	}
}
