package com.sipgate.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
	private static final String TAG = "XmlrpcClient";
	
	private static final String VERSION = "0.1";
	private static final String VENDOR = "sipgate GmbH";
	private static final String NAME = "Sipdroid4sipgate";
	
	private XMLRPCClient client = null;
	
	private static final SimpleDateFormat dateformatterPretty = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
	private static final PhoneNumberFormatter formatter = new PhoneNumberFormatter();
	private static final Locale locale = Locale.getDefault();
	
	private HashMap<String, Object> parameters = null;
	private HashMap<String, Object> apiResult = null;
	
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
			Log.e(TAG, "XMLRPCExceptin in clientIdentify(): " + e.getLocalizedMessage());
			Throwable cause = e.getCause();
			if (cause.getClass().equals(UnknownHostException.class)) {
				throw new NetworkProblemException();
			} else {
				throw e;
			}
		}
		return apiResult;
	}

	private void clientIdentify() throws XMLRPCException, NetworkProblemException {
		
		parameters.clear();
		
		parameters.put("ClientName", NAME);
		parameters.put("ClientVersion", VERSION);
		parameters.put("ClientVendor", VENDOR);
		
		apiResult = this.doXmlrpcCall("samurai.ClientIdentify", parameters);
		Log.d(TAG, apiResult.toString());
	}

	private SipgateServerData serverDataGet() throws XMLRPCException, NetworkProblemException {
		SipgateServerData sipgateServerData = new SipgateServerData();

		parameters.clear();
		
		apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.ServerdataGet", parameters);

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
		
		ArrayList<String> inList = new ArrayList<String>();
		ArrayList<String> tosList = new ArrayList<String>();
		
		for (Object ownUriSetObject : ownUriList) 
		{
			ownUriSet = (HashMap<String, Object>) ownUriSetObject;
			ownUriDataTmp = new SipgateOwnUri();

			ownUriDataTmp.setSipUri((String) ownUriSet.get("SipUri"));
			ownUriDataTmp.setE164Out((String) ownUriSet.get("E164Out"));

			Object[] inListObject = (Object[]) ownUriSet.get("E164In");
			
			inList.clear();
			
			for (Object inObject : inListObject) {
				inList.add((String) inObject);
			}
			
			ownUriDataTmp.setE164In(inList);

			Object[] tosListObject = (Object[]) ownUriSet.get("TOS");
			
			tosList.clear();
			
			for (Object tosObject : tosListObject) {
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
			Log.e(TAG, "XMLRPC call to 'samurai.BalanceGet' failed with " + e.getLocalizedMessage());
			throw new ApiException();
		}

		return balance;
	}

	
	@SuppressWarnings("unchecked")
	public Vector<CallDataDBObject> getCalls() throws ApiException {
		
		Vector<CallDataDBObject> callDataDBObjects = new Vector<CallDataDBObject>();

		parameters.clear();

		try {
			
			apiResult = (HashMap<String, Object>) this.doXmlrpcCall("samurai.HistoryGetByDate", parameters);
			
			Object[] HistoryList = (Object[]) apiResult.get("History");
			
			int counter = 0;
			
			CallDataDBObject callDataDBObject = null;
			HashMap<String, Object> historySet = null;
			
			String direction = null;
			
			String numberLocal = null;
			String numberRemote = null;
			
			String localNumberPretty = null;
			String localNumberE164 = null;
			
			String remoteNumberPretty = null;
			String rempoteNumberE164 = null;
			
			String callID = null;
			
			for (Object HistoryObject : HistoryList) {
				
				if(counter == 50) break;
					
				callDataDBObject = new CallDataDBObject();
				
				historySet = (HashMap<String, Object>) HistoryObject;
				
				if (historySet.containsKey("TOS") && historySet.get("TOS") != null && !historySet.get("TOS").equals("voice")) {
					continue;
				}
				
				counter++;
				
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

				localNumberPretty = formatter.formattedPhoneNumberFromStringWithCountry(numberLocal, locale.getCountry());
				localNumberE164 = formatter.e164NumberWithPrefix("");

				remoteNumberPretty = formatter.formattedPhoneNumberFromStringWithCountry(numberRemote, locale.getCountry());
				rempoteNumberE164 = formatter.e164NumberWithPrefix("");
								
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
				
				callDataDBObject.setRemoteNumberE164(rempoteNumberE164);
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
		}
		
		return callDataDBObjects;
	}

	@Override
	public Vector<VoiceMailDataDBObject> getVoiceMails() throws ApiException, FeatureNotAvailableException
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
			ownUris = ownUriListGet();
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
		try {
			this.clientIdentify();
		} catch (Exception e) {
			return false;
		}

		return true;
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
