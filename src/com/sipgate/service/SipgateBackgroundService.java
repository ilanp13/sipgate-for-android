package com.sipgate.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

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
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;
import com.sipgate.util.Constants;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.NotificationClient.NotificationType;
import com.sipgate.util.SettingsClient;

/**
 * 
 * @author Marcus Hunger
 * @author Karsten Knuth
 * @version 1.1
 *
 */
public class SipgateBackgroundService extends Service implements EventService {
	
	public static final String ACTION_NEWEVENTS = "action_newEvents";
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.SipgateBackgroundService";
	public static final int REQUEST_NEWEVENTS = 0;

	private static final long EVENTREFRESH_INTERVAL = 60000;
		
	private static final String TAG = "SipgateBackgroundService";
	private static final String PREF_YOUNGESTVOICEMAILDATE = "youngestVoicemailDate";
	private static final String PREF_FIRSTLAUNCHDATE = "firstLaunchDate";
	
	private boolean serviceEnabled = false;
	
	private Timer voiceMailRefreshTimer = null;
	private Timer callListRefreshTimer = null;
	
	private Set<PendingIntent> onNewVoiceMailsTriggers = new HashSet<PendingIntent>();
	private Set<PendingIntent> onNewCallsTriggers = new HashSet<PendingIntent>();

	private Date youngestVoicemail = new Date(0); 
	private Date firstLaunch = null; // never access directly. use getFirstLaunchDate()

	private NotificationClient notifyClient = null;
	private ApiServiceProvider apiClient = null;
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	private CallDataDBObject oldCallDataDBObject = null;
	private VoiceMailDataDBObject oldVoiceMailDataDBObject = null;
	
	private	int unreadCounter = 0;
	
	/**
	 * @since 1.0
	 */
	public void onCreate() 
	{
		super.onCreate();
	
		notifyClient = new NotificationClient(this); 
		apiClient = ApiServiceProvider.getInstance(getApplicationContext());
		
		startService();
	}

	/**
	 * @since 1.0
	 */
	private void startService() 
	{
		if (serviceEnabled) {
			return;
		}
		
		serviceEnabled = true;
		
		voiceMailRefreshTimer = new Timer();  
		callListRefreshTimer = new Timer();  
		
		if (hasVmListFeature()) {
			voiceMailRefreshTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					Log.v(TAG, "voicemail timertask started");
					if(serviceEnabled) {
						Log.d(TAG, "get vms");
						refreshVoicemailEvents();
					}
				}
			}, 0, EVENTREFRESH_INTERVAL);
			
		}
		
		callListRefreshTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.v(TAG, "calllist timertask started");
				if(serviceEnabled) {
					refreshCallEvents();
				}
			}

		}, 0, EVENTREFRESH_INTERVAL);
	}

	/**
	 * @since 1.0
	 */
	public void stopService() 
	{
		Log.d(TAG,"stopservice");
		
		if (voiceMailRefreshTimer != null){
			Log.d(TAG,"voiceMailRefreshTimer.cancel");
			voiceMailRefreshTimer.cancel();
		}		
		
		if (callListRefreshTimer != null){
			Log.d(TAG,"callListRefreshTimer.cancel");
			callListRefreshTimer.cancel();
		}	
		
		Log.d(TAG,"cancel notifications");
		
		notifyClient.deleteNotification(NotificationType.VOICEMAIL);
		notifyClient.deleteNotification(NotificationType.CALL);
	}
	
	/**
	 * @since 1.0
	 */
	public void onDestroy() 
	{
		Log.d(TAG,"onDestroy");
		
		stopService();
	}

	/**
	 * @since 1.0
	 */
	private void refreshVoicemailEvents() 
	{
		Log.v(TAG, "refreshVoicemailEvents() -> start");
		
		try {
			notifyIfUnreadsVoiceMails(apiClient.getVoiceMails());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshVoicemailEvents() -> finish");
	}
	
	/**
	 * @author graef
	 * @since 1.1
	 */
	private void refreshCallEvents() 
	{
		Log.v(TAG, "refreshCallEvents() -> start");
		
		try {
			notifyIfUnreadsCalls(apiClient.getCalls());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshCallEvents() -> finish");
	}

	/**
	 * @author graef
	 * @param Vector with voiceMailDataDBObjects
	 * @since 1.1
	 */
	
	private void notifyIfUnreadsVoiceMails(Vector<VoiceMailDataDBObject> voiceMailDataDBObjects) 
	{
		Log.d(TAG, "notifyIfUnreadVoiceMails");
		
		sipgateDBAdapter = new SipgateDBAdapter(this);
		
		oldVoiceMailDataDBObject = null;
		
		unreadCounter = 0;
				
		for (VoiceMailDataDBObject currentVoiceMailDataDBObject : voiceMailDataDBObjects) {
			oldVoiceMailDataDBObject = sipgateDBAdapter.getVoiceMailDataDBObjectById(currentVoiceMailDataDBObject.getId());

			if (oldVoiceMailDataDBObject != null)
			{
				currentVoiceMailDataDBObject.setLocalFileUrl(oldVoiceMailDataDBObject.getLocalFileUrl());
				currentVoiceMailDataDBObject.setSeen(oldVoiceMailDataDBObject.getSeen());
			}
			
			if (!currentVoiceMailDataDBObject.isRead() && !currentVoiceMailDataDBObject.isSeen())
			{
				unreadCounter++;
			}
			
			oldVoiceMailDataDBObject = null;
		}
		
		sipgateDBAdapter.deleteAllVoiceMailDBObjects();

		sipgateDBAdapter.insertAllVoiceMailDBObjects(voiceMailDataDBObjects);
		
		sipgateDBAdapter.close();
		
		if (unreadCounter > 0) 
		{
			createNewVoiceMailNotification(unreadCounter);
		
			Log.d(TAG, "new unread voicemails: " + unreadCounter);
		}
		else
		{
			removeNewVoiceMailNotification();
		}
		
		for (PendingIntent pendingIntent: onNewVoiceMailsTriggers){
			try {
				Log.d(TAG, "notifying refresh voice mails to activity");
				pendingIntent.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * @author graef
	 * @param Vector with callDataDBObjects
	 * @since 1.1
	 */
	
	private void notifyIfUnreadsCalls(Vector<CallDataDBObject> callDataDBObjects) 
	{
		Log.d(TAG, "notifyIfUnreadCalls");
		
		sipgateDBAdapter = new SipgateDBAdapter(this);
		
		oldCallDataDBObject = null;
		
		unreadCounter = 0;
				
		for (CallDataDBObject currentDataDBObject : callDataDBObjects) {
			oldCallDataDBObject = sipgateDBAdapter.getCallDataDBObjectById(currentDataDBObject.getId());
			
			if (currentDataDBObject.getRead() == -1)
			{
				if (oldCallDataDBObject == null)
				{
					currentDataDBObject.setRead(false);
				}
				else
				{
					currentDataDBObject.setRead(oldCallDataDBObject.isRead());
				}
			}
			
			if (oldCallDataDBObject == null && !currentDataDBObject.isRead() && currentDataDBObject.getDirection() == CallDataDBObject.INCOMING)
			{
				unreadCounter++;
			}
			
			oldCallDataDBObject = null;
		}
		
		sipgateDBAdapter.deleteAllCallDBObjects();

		sipgateDBAdapter.insertAllCallDBObjects(callDataDBObjects);
		
		sipgateDBAdapter.close();
		
		if (unreadCounter > 0) 
		{
			createNewCallNotification(unreadCounter);
			
			Log.d(TAG, "new unread calls: " + unreadCounter);
		}
		else
		{
			removeNewCallNotification();
		}
		
		for (PendingIntent pendingIntent: onNewCallsTriggers){
			try {
				Log.d(TAG, "notifying refresh calls to activity");
				pendingIntent.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}			
		}
	}
		
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.0
	 */
	private void createNewVoiceMailNotification(int unreadCounter) 
	{
		notifyClient.setNotification(NotificationClient.NotificationType.VOICEMAIL, R.drawable.statusbar_voicemai_48, buildVoicemailNotificationString(unreadCounter));
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void createNewCallNotification(int unreadCounter) 
	{
		notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.statusbar_icon_calllist, buildCallNotificationString(unreadCounter));
	}
	
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void removeNewCallNotification() 
	{
		notifyClient.deleteNotification(NotificationClient.NotificationType.CALL);
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void removeNewVoiceMailNotification() 
	{
		notifyClient.deleteNotification(NotificationClient.NotificationType.VOICEMAIL);
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @return
	 * @since 1.0
	 */
	private String buildVoicemailNotificationString(int unreadCounter) 
	{
		if(unreadCounter == 1) {
			return String.format((String) getResources().getText(R.string.sipgate_a_new_voicemail), Integer.valueOf(unreadCounter));
		} else {
			return String.format((String) getResources().getText(R.string.sipgate_new_voicemails), Integer.valueOf(unreadCounter));
		}
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @return
	 * @since 1.1
	 */
	private String buildCallNotificationString(int unreadCounter) 
	{
		if(unreadCounter == 1 ) {
			return String.format((String) getResources().getText(R.string.sipgate_a_new_call), Integer.valueOf(unreadCounter));
		} else {
			return String.format((String) getResources().getText(R.string.sipgate_new_calls), Integer.valueOf(unreadCounter));
		}
	}
	
	/**
	 * 
	 * @since 1.0
	 */
	public IBinder asBinder() 
	{
		return null;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void registerOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "registering on voice events intent");
		onNewVoiceMailsTriggers.add(i);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void unregisterOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "unregistering on voice events intent");
		onNewVoiceMailsTriggers.remove(i);
	}
	
	/**
	 * 
	 * @since 1.1
	 */
	public void registerOnCallsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "registering on call events intent");
		onNewCallsTriggers.add(i);
	}

	/**
	 * 
	 * @since 1.1
	 */
	public void unregisterOnCallsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "unregistering on call events intent");
		onNewCallsTriggers.remove(i);
	}

	/**
	 * 
	 * @since 1.0
	 */
	@Override
	public IBinder onBind(Intent arg0) 
	{
		final EventService service = this;

		/**
		 * 
		 * @since 1.0
		 */
		return new Stub() 
		{

			/**
			 * 
			 * @since 1.0
			 */
			public void unregisterOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
			{
				service.unregisterOnVoiceMailsIntent(i);
			}

			/**
			 * 
			 * @since 1.0
			 */
			public void registerOnVoiceMailsIntent(PendingIntent i)	throws RemoteException 
			{
				service.registerOnVoiceMailsIntent(i);
			}
			
			/**
			 * 
			 * @since 1.1
			 */
			public void unregisterOnCallsIntent(PendingIntent i) throws RemoteException 
			{
				service.unregisterOnCallsIntent(i);
			}

			/**
			 * 
			 * @since 1.1
			 */
			public void registerOnCallsIntent(PendingIntent i) throws RemoteException 
			{
				service.registerOnCallsIntent(i);
			}
			
			/**
			 * 
			 * @since 1.0
			 */
			@Override
			public void refreshVoicemails() throws RemoteException 
			{
				service.refreshVoicemails();
			}
			
			/**
			 * 
			 * @since 1.0
			 */
			@Override
			public void refreshCalls() throws RemoteException 
			{
				service.refreshCalls();
			}
		};
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void refreshVoicemails() throws RemoteException
	{
		refreshVoicemailEvents();
	}
	
	/**
	 * 
	 * @since 1.1
	 */
	public void refreshCalls() throws RemoteException 
	{
		refreshCallEvents();
	}
	
	/**
	 * 
	 * @return
	 * @since 1.1
	 */
	private boolean hasVmListFeature() 
	{
		try {
			return ApiServiceProvider.getInstance(getApplicationContext()).featureAvailable(API_FEATURE.VM_LIST);
		} catch (Exception e) {
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.getLocalizedMessage());
		}
		
		return false;
	}
}
