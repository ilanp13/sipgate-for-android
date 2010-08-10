package com.sipgate.ui;

import com.sipgate.service.EventServiceImpl;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.sipua.ui.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sipgate.R;

// @SuppressWarnings("deprecation")
public class optionsMenu {
	
	
	/* Following the menu item constants which will be used for menu creation */
	public static final int FIRST_MENU_ID = Menu.FIRST;
	public static final int CONFIGURE_MENU_ITEM = FIRST_MENU_ID + 1;
	public static final int DIALPAD_MENU_ITEM = FIRST_MENU_ID + 2;
	public static final int ABOUT_MENU_ITEM = FIRST_MENU_ID + 3;
	public static final int EXIT_MENU_ITEM = FIRST_MENU_ID + 4;
	public static final int CONTACTS_MENU_ITEM = FIRST_MENU_ID + 5;
	public static final int EVENTLIST_MENU_ITEM = FIRST_MENU_ID + 6;
	public static final int REFRESH_VOICEMAIL_LIST = FIRST_MENU_ID + 7;
	
	private static AlertDialog m_AlertDlg;

	private static final String TAG = "optionsMenu";
	
	public void createMenu (Menu menu, String caller){ 


		// About
		MenuItem m = menu.add(0, ABOUT_MENU_ITEM, 0, R.string.menu_about);
		m.setIcon(android.R.drawable.ic_menu_info_details);
		
		// Exit
		m = menu.add(0, EXIT_MENU_ITEM, 0, R.string.menu_exit);
		m.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		// settings
		if(caller != "Login"){
			m = menu.add(0, CONFIGURE_MENU_ITEM, 0, R.string.menu_settings);
			m.setIcon(R.drawable.menu_icon_settings_48);
		}
		// refresh for voicemail tab only
		if(caller == "EventList") {
			m = menu.add(0, REFRESH_VOICEMAIL_LIST, 0, R.string.menu_refresh);
			m.setIcon(R.drawable.ic_menu_refresh);
		}
		
		// Eventlist
//		m = menu.add(0, EVENTLIST_MENU_ITEM, 0, R.string.menu_event_list);
//		m.setIcon(R.drawable.menu_icon_voicemail_48);

		// Dialpad
//		m = menu.add(0, DIALPAD_MENU_ITEM, 0, R.string.menu_dial_pad);
//		m.setIcon(R.drawable.menu_icon_dialpad_48);
		
		// Contacts
//		m = menu.add(0, CONTACTS_MENU_ITEM, 0, R.string.menu_contacts);
//		m.setIcon(R.drawable.menu_icon_contacts_48);
	}
	
	
	public void selectItem (MenuItem item, Context context, Activity activity){
		Intent intent = null;

		switch (item.getItemId()) {
		case ABOUT_MENU_ITEM:
			if (m_AlertDlg != null) 
			{
				m_AlertDlg.cancel();
			}
			m_AlertDlg = new AlertDialog.Builder(activity)
			.setMessage(context.getString(R.string.about).replace("\\n","\n").replace("${VERSION}", getVersion(context)))
			.setTitle(context.getString(R.string.menu_about))
			.setIcon(R.drawable.icon22)
			.setCancelable(true)
			.show();
			break;
			
		case EXIT_MENU_ITEM: 
			Receiver.reRegister(0);
			Receiver.engine(context).unregister();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
			on(context,false);
			Receiver.pos(true);
			Receiver.engine(context).halt();
			Receiver.mSipdroidEngine = null;
			context.stopService(new Intent(context,RegisterService.class));
			Log.d(TAG, "stopping voicemail service");
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        notificationManager.cancelAll();
			context.stopService(new Intent(context,EventServiceImpl.class));
			activity.finish();
			break;

		case CONFIGURE_MENU_ITEM: {
			try {
				intent = new Intent(activity, SimpleSettingsActivity.class);
				activity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			break;
		}
		case REFRESH_VOICEMAIL_LIST: {
			if(activity.getClass() == EventListActivity.class){
				EventListActivity tmp = (EventListActivity) activity;
				tmp.getEvents();
			}
			break;
		}
		case EVENTLIST_MENU_ITEM: {
			try {	
				intent = new Intent(activity, EventListActivity.class);
				activity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			break;
		}
		case DIALPAD_MENU_ITEM: {
			try {	
				intent = new Intent(activity, Sipgate.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			break;
		}
		case CONTACTS_MENU_ITEM: {
			try {	
				intent = new Intent(Intent.ACTION_VIEW, People.CONTENT_URI);
				activity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			break;
		}
			
		}
	}
	
	public static String getVersion() {
		return getVersion(Receiver.mContext);
	}
	
	public static String getVersion(Context context) {
		final String unknown = "Unknown";
		
		if (context == null) {
			return unknown;
		}
		
		try {
			return context.getPackageManager()
				   .getPackageInfo(context.getPackageName(), 0)
				   .versionName;
		} catch(NameNotFoundException ex) {}
		
		return unknown;		
	}
	
	public static boolean on(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_ON, Settings.DEFAULT_ON);
	}

	public static void on(Context context,boolean on) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
        if (on) Receiver.engine(context).isRegistered();
	}
	
}
