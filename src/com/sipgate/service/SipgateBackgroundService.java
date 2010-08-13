package com.sipgate.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sipgate.R;
import com.sipgate.api.types.Event;
import com.sipgate.models.SipgateCallData;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.SettingsClient;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;
import com.sipgate.util.NotificationClient.NotificationType;

public class SipgateBackgroundService extends Service implements EventService {

	private static final String TAG = "SipgateBackgroundService";
	private static final long EVENTREFRESH_INTERVAL = 30000;
	public static final String ACTION_NEWEVENTS = "action_newEvents";
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.SipgateBackgroundService";
	public static final int REQUEST_NEWEVENTS = 0;
	private static final String PREF_YOUNGESTVOICEMAILDATE = "youngestVoicemailDate";
	private static final String PREF_YOUNGESTCALLDATE = "youngestCallDate";
	private static final String PREF_FIRSTLAUNCHDATE = "firstLaunchDate";
	private boolean serviceEnabled = false;
	private Timer timer;
	private List<Event> voicemails = new ArrayList<Event>();
	private List<SipgateCallData> calls = new ArrayList<SipgateCallData>();
	private Set<PendingIntent> onNewVoicemailsTriggers = new HashSet<PendingIntent>();
	private Set<PendingIntent> onNewCallsTriggers = new HashSet<PendingIntent>();

	private Date youngestCall = new Date(0); 
	
	private Date youngestVoicemail = new Date(0); 
	private Date firstLaunch = null; // never access directly. use getFirstLaunchDate()

	public void onCreate() {
		super.onCreate();
		fetchYoungestVoicemaildate();
		fetchYoungestCalldate();
		startservice();
	}


	private void startservice() {
		if (serviceEnabled) {
			return;
		}
		serviceEnabled = true;
		timer = new Timer();  
		timer.scheduleAtFixedRate( new TimerTask() {

			public void run() {
				Log.v(TAG, "timer tick tack");
				if(serviceEnabled) {
					if (hasVmListFeature()) {
						Log.d(TAG, "get vms");
						refreshVoicemailEvents();
					}
					Log.d(TAG, "get calls");
					refreshCallEvents();
				}
			}

		}, 0, EVENTREFRESH_INTERVAL);
	}
	

	public void stopservice() {
		Log.d(TAG,"stopservice");
		if(timer != null) {
			Log.d(TAG,"Timer!=null");
			timer.cancel();
			Log.d(TAG, "timer canceled");
		}
		Log.d(TAG,"cancel notifications");
		NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
		notifyClient.deleteNotification(NotificationType.VOICEMAIL);
		notifyClient.deleteNotification(NotificationType.CALL);
	}

	public void onDestroy() {
		Log.d(TAG,"onDestroy");
		if (timer != null){
			Log.d(TAG,"timer.cancel");
			timer.cancel();
		}		
		NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
		notifyClient.deleteNotification(NotificationType.VOICEMAIL);
		notifyClient.deleteNotification(NotificationType.CALL);
	}

	private void refreshVoicemailEvents() {
		
		try {

			ApiServiceProvider apiClient = ApiServiceProvider.getInstance(getApplicationContext());

			List<Event> currentEvents = apiClient.getEvents();
			List<Event> oldEvents = voicemails;
			voicemails = currentEvents;
			notifyIfUnreadsVoicemails(oldEvents, currentEvents);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void refreshCallEvents() {
		
		try {

			ApiServiceProvider apiClient = ApiServiceProvider.getInstance(getApplicationContext());

			List<SipgateCallData> currentCalls = apiClient.getCalls();
			List<SipgateCallData> oldCalls = calls;
			calls = currentCalls;
			notifyIfUnreadsCalls(oldCalls, currentCalls);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void notifyIfUnreadsVoicemails(List<Event> oldList, List<Event> newList) {

		Log.d(TAG, "notifyIfUnreadVoicemails");
		
		if (oldList == null || !oldList.equals(newList)) {
			Log.d(TAG, "notifyIfUnreadVoicemails, oldlist equals newlist not: " + onNewCallsTriggers.size());
			for (PendingIntent pInt: onNewCallsTriggers){
				
				try {
					Log.d(TAG, "notifying unread voicemails to activity");
					pInt.send();
				} catch (CanceledException e) {
					e.printStackTrace();
				}			
			}
		} else {
			Log.d(TAG, "notifyIfUnreadVoicemails, oldlist equals newlist");
		}

		Boolean hasUnreadEvents = false;

		int unreadCounter = 0;
		if (newList != null) {
			for (Event e: newList){
				if (! e.isRead()) {
					if (e.getCreateOnAsDate().after(youngestVoicemail)){
						updateYoungestVoicemaildate(e.getCreateOnAsDate());
						hasUnreadEvents = true;
					}
					if (e.getCreateOnAsDate().after(getFirstLaunchDate())) {
						unreadCounter++;
					}

				}
			}
		}
		if (unreadCounter <= 0) {
			NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
			notifyClient.deleteNotification(NotificationClient.NotificationType.VOICEMAIL);
		} else {
			if (hasUnreadEvents) {
				createNewVoicemailNotification(unreadCounter);
			}
		}
	}
	
	private void notifyIfUnreadsCalls(List<SipgateCallData> oldList, List<SipgateCallData> newList) {

		Log.d(TAG, "notifyIfUnreadCalls");
		
		if (oldList == null || !oldList.equals(newList)) {
			Log.d(TAG, "notifyIfUnreadCalls, oldlist equals newlist not: " + onNewVoicemailsTriggers.size());
			for (PendingIntent pInt: onNewVoicemailsTriggers){
				
				try {
					Log.d(TAG, "notifying unread calls to activity");
					pInt.send();
				} catch (CanceledException e) {
					e.printStackTrace();
				}			
			}
		} else {
			Log.d(TAG, "notifyIfUnreadCalls, oldlist equals newlist");
		}

		Boolean hasUnreadEvents = false;

		int unreadCounter = 0;
		if (newList != null) {
			for (SipgateCallData call: newList){
				if (! call.getCallRead()) {
					if (call.getCallTime().after(youngestCall) && call.getCallMissed() == true){
						updateYoungestCalldate(call.getCallTime());
						hasUnreadEvents = true;
						unreadCounter++;
					}
				}
			}
		}
		if (unreadCounter <= 0) {
			NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
			notifyClient.deleteNotification(NotificationClient.NotificationType.CALL);
		} else {
			if (hasUnreadEvents) {
				createNewCallNotification(unreadCounter);
			}
		}
	}

	private void updateYoungestVoicemaildate(Date d) {
		youngestVoicemail = d;
		
		SharedPreferences pref = getSharedPreferences(SettingsClient.sharedPrefsFile, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putLong(PREF_YOUNGESTVOICEMAILDATE, youngestVoicemail.getTime());
		editor.commit();
	}
	
	private void updateYoungestCalldate(Date d) {
		youngestCall = d;
		
		SharedPreferences pref = getSharedPreferences(SettingsClient.sharedPrefsFile, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putLong(PREF_YOUNGESTCALLDATE, youngestCall.getTime());
		editor.commit();
	}
	

	private Date getFirstLaunchDate() {
		if (firstLaunch != null) {
			return firstLaunch;
		}
		
		SharedPreferences pref = getSharedPreferences(SettingsClient.sharedPrefsFile, Context.MODE_PRIVATE);
		
		firstLaunch = new Date(pref.getLong(PREF_FIRSTLAUNCHDATE, 0));
		
		if (firstLaunch.equals(new Date(0))) {
			firstLaunch = new Date(new Date().getTime() - 24 * 60 * 60 * 1000); // yesterday
			
			Editor editor = pref.edit();
			editor.putLong(PREF_FIRSTLAUNCHDATE, firstLaunch.getTime());
		}
		return firstLaunch;
	}
	
	private void fetchYoungestVoicemaildate() {
		SharedPreferences pref = getSharedPreferences(SettingsClient.sharedPrefsFile, Context.MODE_PRIVATE);
		youngestVoicemail = new Date(pref.getLong(PREF_YOUNGESTVOICEMAILDATE, 0));
	}
	
	private void fetchYoungestCalldate() {
		SharedPreferences pref = getSharedPreferences(SettingsClient.sharedPrefsFile, Context.MODE_PRIVATE);
		youngestCall = new Date(pref.getLong(PREF_YOUNGESTCALLDATE, 0));
	}

	private void createNewVoicemailNotification(int unreadCounter) {
		NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
		notifyClient.setNotification(NotificationClient.NotificationType.VOICEMAIL, R.drawable.statusbar_voicemai_48, buildVoicemailNotificationString(unreadCounter));
	}
	
	private void createNewCallNotification(int unreadCounter) {
		NotificationClient notifyClient = NotificationClient.getInstance(getApplicationContext());
		notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.statusbar_voicemai_48, buildCallNotificationString(unreadCounter));
	}
	
	private String buildVoicemailNotificationString(int unreadCounter) {
		return String.format((String) getResources().getText(R.string.sipgate_a_new_voicemail), Integer.valueOf(unreadCounter));
	}
	
	private String buildCallNotificationString(int unreadCounter) {
		return String.format((String) getResources().getText(R.string.sipgate_a_new_call), Integer.valueOf(unreadCounter));
	}
	
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Event> getVoicemails() throws RemoteException {
		return voicemails;
	}
	
	public List<SipgateCallData> getCalls() throws RemoteException {
		return calls;
	}

	public void registerOnEventsIntent(PendingIntent i)
	throws RemoteException {
		Log.d(TAG, "registering on events intent");
		onNewVoicemailsTriggers.add(i);
	}

	public void unregisterOnEventsIntent(PendingIntent i)
	throws RemoteException {
		onNewVoicemailsTriggers.remove(i);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		final EventService service = this;

		return new Stub() {

			public void unregisterOnEventsIntent(PendingIntent i)
			throws RemoteException {
				service.unregisterOnEventsIntent(i);
			}

			public void registerOnEventsIntent(PendingIntent i)
			throws RemoteException {
				service.registerOnEventsIntent(i);
			}

			public List<Event> getVoicemails() throws RemoteException {
				return service.getVoicemails();
			}
			
			public List<SipgateCallData> getCalls() throws RemoteException {
				return service.getCalls();
			}

			@Override
			public void refreshVoicemails() throws RemoteException {
				service.refreshVoicemails();
			}
			
			@Override
			public void refreshCalls() throws RemoteException {
				service.refreshCalls();
			}
		};
	}


	public void refreshVoicemails() throws RemoteException {
		refreshVoicemailEvents();
	}
	
	public void refreshCalls() throws RemoteException {
		refreshCallEvents();
	}
	
	private boolean hasVmListFeature() {
		boolean hasVmListFeature = false;
		try {
			hasVmListFeature = ApiServiceProvider.getInstance(getApplicationContext()).featureAvailable(API_FEATURE.VM_LIST);
		} catch (Exception e) {
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.getLocalizedMessage());
		}
		
		return hasVmListFeature;
	}

}
