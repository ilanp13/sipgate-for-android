package com.sipgate.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
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
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.sipua.ui.Settings;
import com.sipgate.ui.SipgateFramesMessage;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

public class EventServiceImpl extends Service implements EventService {

	private static final String TAG = "background service";
	private static final long EVENTREFRESH_INTERVAL = 30000;
	public static final String ACTION_NEWEVENTS = "action_newEvents";
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.EventServiceImpl";
	public static final int REQUEST_NEWEVENTS = 0;
	private static final String PREF_YOUNGESTVOICEMAILDATE = "youngestVoicemailDate";
	private static final String PREF_FIRSTLAUNCHDATE = "firstLaunchDate";
	private boolean serviceEnabled = false;
	private Timer timer;
	private List<Event> events = new ArrayList<Event>();
	private Set<PendingIntent> onNewEventsTriggers = new HashSet<PendingIntent>();

	private Date youngestVoicemail = new Date(0); 
	private Date firstLaunch = null; // never access directly. use getFirstLaunchDate()

	public void onCreate() {
		super.onCreate();
		fetchYoungestVoicemaildate();
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
					refresh();
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
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	public void onDestroy() {
		Log.d(TAG,"onDestroy");
		if (timer != null){
			Log.d(TAG,"timer.cancel");
			timer.cancel();
		}		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	private void refresh() {
		
		try {

			HashMap<String, String> params = new HashMap<String, String>();

			@SuppressWarnings({ "unused", "rawtypes" })
			Collection<? extends Entry> paramCollection;
			paramCollection = params.entrySet();
			ApiServiceProvider apiClient = ApiServiceProvider.getInstance(getApplicationContext());

			List<Event> currentEvents = apiClient.getEvents();
			List<Event> oldEvents = events;
			events = currentEvents;
			notifyIfUnreads(oldEvents, currentEvents);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeatureNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void notifyIfUnreads(List<Event> oldList, List<Event> newList) {

		Log.d(TAG, "notifyIfUnreads");
		
		if (oldList == null || !oldList.equals(newList)) {
			Log.d(TAG, "notifyIfUnreads, oldlist equals newlist not: " + onNewEventsTriggers.size());
			for (PendingIntent pInt: onNewEventsTriggers){
				
				try {
					Log.d(TAG, "notifying unreads to activity");
					pInt.send();
				} catch (CanceledException e) {
					e.printStackTrace();
				}			
			}
		} else {
			Log.d(TAG, "notifyIfUnreads, oldlist equals newlist");
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
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		} else {
			if (hasUnreadEvents) {
				createNewMessagesNotification(unreadCounter);
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

	private void createNewMessagesNotification(int unreadCounter) {

		NotificationManager notificationManager =
			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.statusbar_voicemai_48, buildNotificationString(unreadCounter), 0 );
		notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
		//          notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent = new Intent(this, SipgateFramesMessage.class);

		Log.d("createNewMessagesNotification","Executed");
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, getResources().getText(R.string.sipgate), buildNotificationString(unreadCounter), contentIntent);
		notificationManager.notify(0, notification);
		Log.d(TAG,"send notification");
		
		
	}
	
	private String buildNotificationString(int unreadCounter) {
		return String.format((String) getResources().getText(R.string.sipgate_a_new_voicemail), Integer.valueOf(unreadCounter));
	}
	

	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Event> getEvents() throws RemoteException {
		return events;
	}

	public void registerOnEventsIntent(PendingIntent i)
	throws RemoteException {
		Log.d(TAG, "registering on events intent");
		onNewEventsTriggers.add(i);
	}

	public void unregisterOnEventsIntent(PendingIntent i)
	throws RemoteException {
		onNewEventsTriggers.remove(i);
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

			public List<Event> getEvents() throws RemoteException {
				return service.getEvents();
			}

			@Override
			public void refreshEvents() throws RemoteException {
				service.refreshEvents();
			}
		};
	}


	@Override
	public void refreshEvents() throws RemoteException {
		refresh();
	}

}
