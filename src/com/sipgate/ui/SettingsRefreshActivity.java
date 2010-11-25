package com.sipgate.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.sipdroid.codecs.Codecs;

import com.sipgate.R;
import com.sipgate.sipua.ui.Checkin;
import com.sipgate.sipua.ui.InstantAutoCompleteTextView;
import com.sipgate.sipua.ui.Receiver;

import org.zoolu.sip.provider.SipStack;

import android.R.array;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsRefreshActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static String TAG = "SettingsRefreshActivity";
	
	// Current settings handler
	private static SharedPreferences settings;
	// Context definition
	private Context context = null;

	// Path where to store all profiles - !!!should be replaced by some system variable!!!
	private final static String profilePath = "/sdcard/Sipgate/";
	// Path where is stored the shared preference file - !!!should be replaced by some system variable!!!
	private final String sharedPrefsPath = "/data/data/com.sipgate/shared_prefs/";
	// Shared preference file name - !!!should be replaced by some system variable!!!
	private final String sharedPrefsFile = "com.sipgate_preferences";
	// List of profile files available on the SD card
	private String[] profileFiles = null;
	// Which profile file to delete
	private int profileToDelete;

	// Name of the keys in the Preferences XML file
	public static final String PREF_REFRESH_EVENTS = "refresh_events";
	public static final String PREF_REFRESH_CONTACTS = "refresh_contacts";

	// Default values of the preferences
	public static final String DEFAULT_REFRESH_EVENTS = "5";
	public static final String DEFAULT_REFRESH_CONTACTS = "1440";
	
	

	
	/**
	 * onCreate-Method for SettingsRefreshActivity
	 * 
	 * @author niepel	
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSettingsTitle();
		addPreferencesFromResource(R.xml.sipgate_preferences_refresh);
		settings = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
		settings.registerOnSharedPreferenceChangeListener(this);
		updateSummaries(settings.getString(PREF_REFRESH_EVENTS, DEFAULT_REFRESH_EVENTS),settings.getString(PREF_REFRESH_CONTACTS, DEFAULT_REFRESH_CONTACTS));
	}

	/**
	 * Sets the settings title
	 * 
	 * @author niepel	
	 */
	private void setSettingsTitle() {
		setTitle(R.string.simple_settings_refresh_timers);
	}

	/**
	 * Set a human-readable Text as summary
	 * 
	 * @param element
	 * @param time
	 * @author niepel
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
	 * @author niepel	
	 */
	private void updateSummaries(String time_events, String time_contacts) {
		Log.d(TAG, "Events: " + time_events + " / Contacts: " + time_contacts);
		setHumanTimeAsSummary(getPreferenceScreen().findPreference(PREF_REFRESH_EVENTS),PREF_REFRESH_EVENTS,time_events);
		setHumanTimeAsSummary(getPreferenceScreen().findPreference(PREF_REFRESH_CONTACTS),PREF_REFRESH_CONTACTS,time_contacts);
	}
	
	/**
	 * onDestroy-Method to unregister the changelistener
	 * 
	 * @author niepel	
	 */
	@Override
	public void onDestroy()	{
		super.onDestroy();

		settings.unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Listener for Changes to the Preferences
	 * 
	 * @author niepel	
	 */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Log.d(TAG, "onSharedPreferenceChanged");
    	Log.d(TAG, key);
    	updateSummaries(sharedPreferences.getString(PREF_REFRESH_EVENTS, DEFAULT_REFRESH_EVENTS),sharedPreferences.getString(PREF_REFRESH_CONTACTS, DEFAULT_REFRESH_CONTACTS));
    }
    
	/**
	 * onResume-Method for SettingsRefreshActivity
	 * 
	 * @author niepel	
	 */
	protected void onResume() {
		super.onResume();
	}
	
}
