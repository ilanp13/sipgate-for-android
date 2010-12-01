package com.sipgate.ui;

import com.sipgate.R;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * 
 * 
 * @author niepel
 * @author Karsten Knuth
 * @version 1.0
 */
public class SettingsRefreshActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static String TAG = "SettingsRefreshActivity";
	
	private static SharedPreferences settings = null;
	private Intent intent = null;
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;

	private final String sharedPrefsFile = "com.sipgate_preferences";

	public static final String PREF_REFRESH_EVENTS = "refresh_events";
	public static final String PREF_REFRESH_CONTACTS = "refresh_contacts";

	public static final String DEFAULT_REFRESH_EVENTS = "5";
	public static final String DEFAULT_REFRESH_CONTACTS = "1440";
	
	/**
	 * onCreate-Method for SettingsRefreshActivity
	 * 
	 * @since 1.0
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSettingsTitle();
		addPreferencesFromResource(R.xml.sipgate_preferences_refresh);
		settings = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
		updateSummaries(settings.getString(PREF_REFRESH_EVENTS, DEFAULT_REFRESH_EVENTS),settings.getString(PREF_REFRESH_CONTACTS, DEFAULT_REFRESH_CONTACTS));
	}
	
	/**
	 * 
	 */
	public void onResume(){
		super.onResume();
		settings.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Sets the settings title
	 * 
	 * @since 1.0
	 */
	private void setSettingsTitle() {
		setTitle(R.string.simple_settings_refresh_timers);
	}

	/**
	 * Set a human-readable Text as summary
	 * 
	 * @param element
	 * @param time
	 * @since 1.0
	 */
	private void setHumanTimeAsSummary(Preference element, String elementName, String time) {
		Integer intTime = Integer.valueOf(time);
		Log.d(TAG, element.toString() + " (" + elementName + ") Set to " + intTime.toString() + " Minutes");
		int i = 0;
		switch(intTime) {
			case 1: i = 0; break; 
			case 5: i = 1; break; 
			case 15: i = 2; break; 
			case 30: i = 3; break; 
			case 60: i = 4; break; 
			case 180: i = 5; break; 
			case 360: i = 6; break; 
			case 1440: i = 7; break; 
		}
		element.setSummary(getResources().getStringArray(R.array.simple_settings_refresh_values)[i]);
	}
	
	/**
	 * Sets the summaries so that you can see what you've chosen
	 * 
	 * @since 1.0
	 */
	private void updateSummaries(String time_events, String time_contacts) {
		Log.d(TAG, "Events: " + time_events + " / Contacts: " + time_contacts);
		setHumanTimeAsSummary(getPreferenceScreen().findPreference(PREF_REFRESH_EVENTS),PREF_REFRESH_EVENTS,time_events);
		setHumanTimeAsSummary(getPreferenceScreen().findPreference(PREF_REFRESH_CONTACTS),PREF_REFRESH_CONTACTS,time_contacts);
	}
	
	/**
	 * 
	 */
	public void onPause() {
		super.onPause();
		settings.unregisterOnSharedPreferenceChangeListener(this);
		
		intent = new Intent(this, SipgateBackgroundService.class);
		Context appContext = getApplicationContext();
		appContext.startService(intent);

		if (serviceConnection == null) {
			serviceConnection = new ServiceConnection() {

				public void onServiceConnected(ComponentName name, IBinder binder)
				{
					Log.v(TAG, "service " + name + " connected -> bind");
					try {
						serviceBinding = (EventService) binder;
						try {
							serviceBinding.initContactRefreshTimer();
							serviceBinding.initCallRefreshTimer();
							serviceBinding.initVoicemailRefreshTimer();
						}
						catch (RemoteException e) {
							e.printStackTrace();
						}
					}
					catch (ClassCastException e) {
						e.printStackTrace();
					}
					
				}

				public void onServiceDisconnected(ComponentName name)
				{
					serviceBinding = null;
				}
				
			};
		}
		
		appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * Listener for Changes to the Preferences
	 * 
	 * @since 1.0
	 */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Log.d(TAG, "onSharedPreferenceChanged");
    	Log.d(TAG, key);
    	updateSummaries(sharedPreferences.getString(PREF_REFRESH_EVENTS, DEFAULT_REFRESH_EVENTS),sharedPreferences.getString(PREF_REFRESH_CONTACTS, DEFAULT_REFRESH_CONTACTS));
    }
}
