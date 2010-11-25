package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sipgate.R;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.sipua.ui.Settings;

/**
 * This class functions as an options menu and is used in every activity
 * that need an menu.
 * 
 * @author Karsten Knuth
 * @author Moritz Winterberg
 * @author Michael Rotmanov
 * @author Achim Marikar
 * @version 1.2
 */
public class OptionsMenu {
	
	
	/* Following the menu item constants which will be used for menu creation */
	public static final int FIRST_MENU_ID = Menu.FIRST;
	public static final int CONFIGURE_MENU_ITEM = FIRST_MENU_ID + 1;
	public static final int ABOUT_MENU_ITEM = FIRST_MENU_ID + 2;
	public static final int EXIT_MENU_ITEM = FIRST_MENU_ID + 3;
	public static final int REFRESH_VOICEMAIL_LIST = FIRST_MENU_ID + 4;
	public static final int REFRESH_CALL_LIST = FIRST_MENU_ID + 5;
	public static final int REFRESH_CONTACT_LIST = FIRST_MENU_ID + 6;
	
	private static AlertDialog m_AlertDlg;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	
	private static final String TAG = "optionsMenu";
	
	/**
	 * This function assembles the menu according to
	 * the caller activity.
	 * 
	 * @param menu The menu instance given to the activity.
	 * @param caller A string uniquely identifyin the caller.
	 * @since 1.0
	 */
	public void createMenu (Menu menu, String caller)
	{ 

		// About
		MenuItem m = menu.add(0, ABOUT_MENU_ITEM, 0, R.string.menu_about);
		m.setIcon(android.R.drawable.ic_menu_info_details);
		
		// Exit
		m = menu.add(0, EXIT_MENU_ITEM, 0, R.string.menu_exit);
		m.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		// settings
		if(!caller.equals("Login")){
			m = menu.add(0, CONFIGURE_MENU_ITEM, 0, R.string.menu_settings);
			m.setIcon(R.drawable.menu_icon_settings_48);
		}
		// refresh for voicemail tab only
		if(caller.equals("VoiceMailList")) {
			m = menu.add(0, REFRESH_VOICEMAIL_LIST, 0, R.string.menu_refresh);
			m.setIcon(R.drawable.ic_menu_refresh);
		}
		
		// refresh for calllist tab only
		if(caller.equals("CallList")) {
			m = menu.add(0, REFRESH_CALL_LIST, 0, R.string.menu_refresh);
			m.setIcon(R.drawable.ic_menu_refresh);
		}
		
		// refresh for contacts tab only
		if(caller.equals("ContactList")) {
			m = menu.add(0, REFRESH_CONTACT_LIST, 0, R.string.menu_refresh);
			m.setIcon(R.drawable.ic_menu_refresh);
		}	
	}
	
	/**
	 * This function is called whenever a menu item is clicked. It
	 * will decide what activity to execute.
	 * 
	 * @param item The menu item that has been clicked on
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.0
	 */
	public void selectItem (MenuItem item, Context context, Activity activity)
	{
		Intent intent = null;
		
		switch (item.getItemId()) {
			case ABOUT_MENU_ITEM:
				showAboutScreen(context, activity);
				break;
				
			case EXIT_MENU_ITEM: 
				shutDownProgram(context, activity);
				break;
	
			case CONFIGURE_MENU_ITEM:
				openSettings(intent, activity);
				break;

			case REFRESH_VOICEMAIL_LIST:
				refreshVoiceMailList(intent, context, activity);
				break;

			case REFRESH_CALL_LIST:
				refreshCallList(intent, context, activity);
				break;

			case REFRESH_CONTACT_LIST:
				refreshContactList(intent, context, activity);
				break;		
		}
	}
	
	/**
	 * This function reads the current build version to display it
	 * in the about box.
	 * 
	 * @param context The application context.
	 * @return The current build version as string.
	 * @since 1.0
	 */
	private static String getVersion(Context context)
	{
		final String unknown = "Unknown";
		
		if (context == null) {
			return unknown;
		}
		
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		}
		catch(NameNotFoundException ex) {
			ex.printStackTrace();
		}
		
		return unknown;		
	}
	
	/**
	 * Switches the state of the "on" setting.
	 * 
	 * @param context The application context.
	 * @param on A boolean value to be set in the settings.
	 * @since 1.0
	 */
	private static void on(Context context,boolean on)
	{
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
        if (on) Receiver.engine(context).isRegistered();
	}
	
	/**
	 * Shows the about screen on top of the current activity.
	 * 
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void showAboutScreen(Context context, Activity activity)
	{
		if (m_AlertDlg != null) {
			m_AlertDlg.cancel();
		}
		m_AlertDlg = new AlertDialog.Builder(activity)
		.setMessage(context.getString(R.string.about).replace("\\n","\n").replace("${VERSION}", getVersion(context)))
		.setTitle(context.getString(R.string.menu_about))
		.setIcon(R.drawable.icon22)
		.setCancelable(true)
		.show();
	}
	
	/**
	 * Shuts down the program.
	 * 
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void shutDownProgram(Context context, Activity activity)
	{
		Receiver.reRegister(0);
		Receiver.engine(context).unregister();
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		on(context,false);
		Receiver.pos(true);
		Receiver.engine(context).halt();
		Receiver.mSipdroidEngine = null;
		context.stopService(new Intent(context,RegisterService.class));
		Log.d(TAG, "stopping voicemail service");
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
		context.stopService(new Intent(context,SipgateBackgroundService.class));
		activity.finish();
	}
	
	/**
	 * Opens the settings screen.
	 * 
	 * @param intent The intent that is going to be used to start the settings activity.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void openSettings(Intent intent, Activity activity)
	{
		try {
			intent = new Intent(activity, SimpleSettingsActivity.class);
			activity.startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	/**
	 * This function triggers the manual reload of the contact list.
	 * 
	 * @param intent The intent that is going to be used to start refresh.
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void refreshContactList(Intent intent, Context context, Activity activity)
	{
		try {
			activity.findViewById(R.id.sipgateContactsListRefreshView).setVisibility(View.VISIBLE);
			activity.findViewById(R.id.ContactsListCountView).setVisibility(View.GONE);
			
			intent = new Intent(activity, SipgateBackgroundService.class);
			Context appContext = context.getApplicationContext();
			appContext.startService(intent);

			if (serviceConnection == null) {
				Log.d(TAG, "service connection is null -> create new");
				serviceConnection = new ServiceConnection() {

					public void onServiceConnected(ComponentName name, IBinder binder)
					{
						Log.v(TAG, "service " + name + " connected -> bind");
						try {
							serviceBinding = (EventService) binder;
							try {
								Log.d(TAG, "service binding -> registerOnContactsIntent");
								serviceBinding.initContactRefreshTimer();
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
						Log.d(TAG, "service " + name + " disconnected -> clear binding");
						serviceBinding = null;
					}
					
				};
			}
			
			boolean bindret = appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		}
		catch (ActivityNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	/**
	 * This function triggers the manual reload of the call list.
	 * 
	 * @param intent The intent that is going to be used to start refresh.
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void refreshCallList(Intent intent, Context context, Activity activity)
	{
		try {
			activity.findViewById(R.id.sipgateCallListRefreshView).setVisibility(View.VISIBLE);
			
			intent = new Intent(activity, SipgateBackgroundService.class);
			Context appContext = context.getApplicationContext();
			appContext.startService(intent);

			if (serviceConnection == null) {
				Log.d(TAG, "service connection is null -> create new");
				serviceConnection = new ServiceConnection() {

					public void onServiceConnected(ComponentName name, IBinder binder)
					{
						Log.v(TAG, "service " + name + " connected -> bind");
						try {
							serviceBinding = (EventService) binder;
							try {
								Log.d(TAG, "service binding -> registerOnCallsIntent");
								serviceBinding.initCallRefreshTimer();
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
						Log.d(TAG, "service " + name + " disconnected -> clear binding");
						serviceBinding = null;
					}
					
				};
			}
			
			boolean bindret = appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		}
		catch (ActivityNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
	
	/**
	 * This function triggers the manual reload of the voice mail list.
	 * 
	 * @param intent The intent that is going to be used to start refresh.
	 * @param context The application context.
	 * @param activity The calling activity.
	 * @since 1.2
	 */
	private void refreshVoiceMailList(Intent intent, Context context, Activity activity)
	{
		try {
			activity.findViewById(R.id.sipgateVoiceMailListRefreshView).setVisibility(View.VISIBLE);
			
			intent = new Intent(activity, SipgateBackgroundService.class);
			Context appContext = context.getApplicationContext();
			appContext.startService(intent);

			if (serviceConnection == null) {
				Log.d(TAG, "service connection is null -> create new");
				serviceConnection = new ServiceConnection() {

					public void onServiceConnected(ComponentName name, IBinder binder)
					{
						Log.v(TAG, "service " + name + " connected -> bind");
						try {
							serviceBinding = (EventService) binder;
							try {
								Log.d(TAG, "service binding -> registerOnVoicemailIntent");
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
						Log.d(TAG, "service " + name + " disconnected -> clear binding");
						serviceBinding = null;
					}
					
				};
			}
			
			boolean bindret = appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		}
		catch (ActivityNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}	
}
