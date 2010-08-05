package com.sipgate.util;


import com.sipgate.exceptions.SipgateSettingsProviderGeneralException;
import com.sipgate.sipua.ui.Settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Allows Access to the Sipdroid configurations
 * 
 * @author Karsten Knuth
 * @version 1.0
 *
 */
public class SettingsClient {
	public enum API_TYPE { REST, XMLRPC };
	
	private static SettingsClient instance = null;

	private static final String randomDefaultValue= "DEFAULTTHATWILLNEVEROCCUR";
	private static final String sharedPrefsFile = "com.sipgate_preferences";
	private static final String extensionAlias = "extension_alias";
	private SharedPreferences preferences = null;
	private SharedPreferences.Editor editor = null;
	private Context context = null;
	
	private SettingsClient() {
		super();
	}

	/**
	 * Returns an instance of the SettingsClient
	 * 
	 * @since 1.0
	 * @param context the application context
	 * @return the single instance of the SettingsClient
	 */
	synchronized public static SettingsClient getInstance(Context context) {
		if (SettingsClient.instance == null) {
			SettingsClient.instance = new SettingsClient();
			SettingsClient.instance.init(context);
		}

		return SettingsClient.instance;
	}
	
	/**
	 * Initialises the preferences and the editor
	 * 
	 * @since 1.0
	 * @param context the application context
	 */
	private void init(Context context) {
		this.context = context;
		this.preferences = this.context.getSharedPreferences(sharedPrefsFile, 0);
		this.editor = this.preferences.edit();
	}
	
	/**
	 * Saves the alias of the extension
	 * 
	 * @since 1.0
	 * @param extensionAlias the extension alias to be saved
	 */
	public void setExtensionAlias(String extensionAlias) {
		this.editor.putString(SettingsClient.extensionAlias, extensionAlias);
		this.editor.commit();
	}
	
	/**
	 * Returns the extension alias for the registered extension
	 * 
	 * @since 1.0
	 * @return the extension alias
	 */
	public String getExtensionAlias() {
		return this.preferences.getString(SettingsClient.extensionAlias, "");
	}
	
	/**
	 * Saves the registration server
	 * 
	 * @since 1.0
	 * @param server the registration server
	 */
	public void setServer(String server){
		this.editor.putString(Settings.PREF_SERVER, server);
		this.editor.commit();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getServer() {
		return this.preferences.getString(Settings.PREF_SERVER, "");
	}
	
	/**
	 * Saves the registration domain
	 * 
	 * @since 1.0
	 * @param domain the registration domain
	 */
	public void setDomain(String domain){
		this.editor.putString(Settings.PREF_DOMAIN, domain);
		this.editor.commit();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDomain() {
		return this.preferences.getString(Settings.PREF_DOMAIN, "");
	}
	
	/**
	 * Saves the sipID for the registered extension
	 * 
	 * @since 1.0
	 * @param sipID the sipID
	 */
	public void setSipID(String sipID){
		this.editor.putString(Settings.PREF_USERNAME, sipID);
		this.editor.commit();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSipID() {
		return this.preferences.getString(Settings.PREF_USERNAME, "");
	}
	
	public boolean isSipIdSet() {
		String sipId = this.preferences.getString(Settings.PREF_USERNAME, randomDefaultValue);
		if (sipId.equals(randomDefaultValue)){
			return false;
		}
		return true;
	}
	
	/**
	 * Saves the password for the registered extension
	 * 
	 * @since 1.0
	 * @param password the sip password
	 */
	public void setPassword(String password){
		this.editor.putString(Settings.PREF_PASSWORD, password);
		this.editor.commit();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPassword() {
		return this.preferences.getString(Settings.PREF_PASSWORD, "");
	}

	/**
	 * Saves the protocol used for connection to the servetr
	 * 
	 * @since 1.0
	 * @param protocol the sip protocol ("udp"/"tcp")
	 */
	public void setProtocol(String protocol){
		this.editor.putString(Settings.PREF_PROTOCOL, protocol);
		this.editor.commit();
	}
	
	/**
	 * Sets the usage of a wireless network off or on
	 * 
	 * @since 1.0
	 * @param useWireless defines whether a wireless network should be used
	 */
	public void setUseWireless(Boolean useWireless){
		this.editor.putBoolean(Settings.PREF_WLAN, useWireless);
		this.editor.commit();
	}
	
	/**
	 * Returns whether a wireless network is used
	 * 
	 * @return whether a wireless network is used
	 */
	public Boolean getUseWireless(){
		return this.preferences.getBoolean(Settings.PREF_WLAN, false);
	}
	
	/**
	 * Sets the usage of the UMTS/HSDPA network off or on
	 * 
	 * @since 1.0
	 * @param use3G defines whether the UMTS/HSDPA network should be used
	 */
	public void setUse3G(Boolean use3G){
		this.editor.putBoolean(Settings.PREF_3G, use3G);
		this.editor.commit();
	}
	
	/**
	 * Returns whether the UMTS/HSDPA network is used
	 * 
	 * @return whether the UMTS/HSDPA network is used
	 */
	public Boolean getUse3G(){
		return this.preferences.getBoolean(Settings.PREF_3G, false);
	}
	
	/**
	 * Sets the usage of a STUN server off or on
	 * 
	 * @since 1.0
	 * @param useStunServer defines whether a STUN server should be used
	 */
	public void setUseStunServer(Boolean useStunServer){
		this.editor.putBoolean(Settings.PREF_STUN, useStunServer);
		this.editor.commit();
	}
	
	/**
	 * Changes the STUN server
	 * 
	 * @since 1.0
	 * @param stunServer the new STUN server that should be used
	 */
	public void setStunServer(String stunServer){
		this.editor.putString(Settings.PREF_STUN_SERVER, stunServer);
		this.editor.commit();
	}
	
	/**
	 * Changes the STUN server port
	 * 
	 * @since 1.0
	 * @param stunPort the new STUN server port that should be used
	 */
	public void setStunPort(String stunPort){
		this.editor.putString(Settings.PREF_STUN_SERVER_PORT, stunPort);
		this.editor.commit();
	}
	
	/**
	 * Auto provisions the application using the provided configuration data
	 * 
	 * @since 1.0
	 * @param sipID the sipID
	 * @param password the password
	 * @param alias the Alias of the extension
	 * @param server the registration server
	 * @param domain the registration domain
	 */
	public void registerExtension(String sipID, String password, String alias, String server, String domain){
		setSipID(sipID);
		setPassword(password);
		setExtensionAlias(alias);
		setServer(server);
		setDomain(domain);
		setProtocol("udp");
		setUseWireless(true);
		setUse3G(false);
		setUseStunServer(true);
		setStunServer("stun.sipgate.net");
		setStunPort("10000");
	}
	
	/**
	 * Auto provisions the application using the provided configuration data without changing wireless and 3G settings
	 * 
	 * @since 1.0
	 * @param sipID the sipID
	 * @param password the password
	 * @param alias the Alias of the extension
	 * @param server the registration server
	 * @param domain the registration domain
	 */
	public void reregisterExtension(String sipID, String password, String alias, String server, String domain){
		setSipID(sipID);
		setPassword(password);
		setExtensionAlias(alias);
		setServer(server);
		setDomain(domain);
		setProtocol("udp");
		setUseStunServer(true);
		setStunServer("stun.sipgate.net");
		setStunPort("10000");
	}
	
	/**
	 * 
	 */
	public void unRegisterExtension(){
		setSipID("");
		setPassword("");
		setExtensionAlias("");
		setServer("");
		setDomain("");
		setProtocol("udp");
		setUseStunServer(true);
		setStunServer("stun.sipgate.net");
		setStunPort("10000");
	}
	
	public Boolean isProvisioned(){
		if(!getSipID().equals("") && !getPassword().equals("") && !getServer().equals("") && !getDomain().equals(""))
			return true;
		return false;
	}

	public void setWebusername(String webuser){
		this.editor.putString("sipgate_webuser", webuser);
		this.editor.commit();
	}
	
	public String getWebusername(){
		return this.preferences.getString("sipgate_webuser", "");
	}

	public void purgeWebuserCredentials() {
		this.editor.remove("sipgate_webuser");
		this.editor.remove("sipgate_webpass");
		this.editor.remove("sipgate_api-type");
		this.editor.commit();
	}
	
	public void setWebuserpass(String webpass){
		this.editor.putString("sipgate_webpass", webpass);
		this.editor.commit();
	}
	
	public String getWebpassword() {
		return this.preferences.getString("sipgate_webpass", "");
	}
	
	public boolean isFirstRegistration() {
		return !isSipIdSet();
	}
	
	public void setApiType(API_TYPE type) {
		if (type == null) {
			this.editor.remove("sipgate_api-type");
		} else {
			this.editor.putString("sipgate_api-type", type.toString());
		}
		this.editor.commit();
	}
	
	public API_TYPE getApiType() throws SipgateSettingsProviderGeneralException {
		String typeString = this.preferences.getString("sipgate_api-type","");
		if (typeString.equals(API_TYPE.XMLRPC.toString())) {
			return API_TYPE.XMLRPC;
		} else if(typeString.equals(API_TYPE.REST.toString())) {
			return API_TYPE.REST;
		}
		
		throw new SipgateSettingsProviderGeneralException("not setting found for 'sipgate_api-type'");
	}
}
