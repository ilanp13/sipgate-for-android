package com.sipgate.service;

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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sipgate.R;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.NotificationClient.NotificationType;

/**
 * 
 * @author Marcus Hunger
 * @author Karsten Knuth
 * @author graef
 * @version 1.1
 *
 */
public class SipgateBackgroundService extends Service implements EventService 
{
	public static final String ACTION_NEWEVENTS = "action_newEvents";
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.SipgateBackgroundService";
	public static final int REQUEST_NEWEVENTS = 0;

	private static final long CONTACT_REFRESH_INTERVAL = 600000; // every 10mins
	private static final long CALL_REFRESH_INTERVAL = 60000; // every min
	private static final long VOICEMAIL_REFRESH_INTERVAL = 60000; // every min
		
	private static final String TAG = "SipgateBackgroundService";
	
	private boolean serviceEnabled = false;
	
	private Timer contactRefreshTimer = null;
	private Timer callRefreshTimer = null;
	private Timer voiceMailRefreshTimer = null;
	
	private Set<PendingIntent> contactListener = new HashSet<PendingIntent>();
	private Set<PendingIntent> callListener = new HashSet<PendingIntent>();
	private Set<PendingIntent> voiceMailListener = new HashSet<PendingIntent>();
	
	private NotificationClient notifyClient = null;
	private ApiServiceProvider apiClient = null;
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private VoiceMailDataDBObject oldVoiceMailDataDBObject = null;
	private CallDataDBObject oldCallDataDBObject = null;
		
	private	int unreadCounter = 0;
	
	private int deleted = 0;
	private int inserted = 0;
	private int updated = 0;
	
	/**
	 * @since 1.0
	 */
	public void onCreate() 
	{
		super.onCreate();
		
		notifyClient = new NotificationClient(this); 
		
		apiClient = ApiServiceProvider.getInstance(this);
			
		startService();
	}

	/**
	 * @since 1.0
	 */
	private void startService() 
	{
		if (serviceEnabled) 
		{
			return;
		}
		
		serviceEnabled = true;
	
		initContactRefreshTimer();
		
		initCallRefreshTimer();
	
		if (hasVmListFeature()) 
		{
			initVoicemailRefreshTimer();
		}
	}

	/**
	 * @since 1.0
	 */
	public void stopService() 
	{
		Log.d(TAG,"stopservice");
		
		if (contactRefreshTimer != null){
			Log.d(TAG,"contactRefreshTimer.cancel");
			contactRefreshTimer.cancel();
		}	
		
		if (callRefreshTimer != null){
			Log.d(TAG,"callRefreshTimer");
			callRefreshTimer.cancel();
		}	
		
		if (voiceMailRefreshTimer != null){
			Log.d(TAG,"voiceMailRefreshTimer.cancel");
			voiceMailRefreshTimer.cancel();
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
	 * @author graef
	 * @since 1.1
	 */
	private void refreshContactEvents() 
	{
		Log.v(TAG, "refreshContactEvents() -> start");
		
		try 
		{
			notifyIfNewContacts(apiClient.getContacts(), this);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshContactEvents() -> finish");
	}

	/**
	 * @author graef
	 * @since 1.1
	 */
	private void refreshCallEvents() 
	{
		Log.v(TAG, "refreshCallEvents() -> start");
		
		try 
		{
			notifyIfNewCalls(apiClient.getCalls(), this);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshCallEvents() -> finish");
	}


	/**
	 * @since 1.0
	 */
	private void refreshVoicemailEvents() 
	{
		Log.v(TAG, "refreshVoicemailEvents() -> start");
		
		try 
		{
			notifyIfNewVoiceMails(apiClient.getVoiceMails(), this);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshVoicemailEvents() -> finish");
	}
	
	/**
	 * @author graef
	 * @param Vector with voiceMailDataDBObjects
	 * @since 1.1
	 */
	
	public void notifyIfNewVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Context context) 
	{
		Log.d(TAG, "notifyIfUnreadVoiceMails");
		
		if (newVoiceMailDataDBObjects == null)
		{
			Log.i(TAG, "notifyIfUnreadVoiceMails() -> voiceMailDataDBObjects is null");
			return;
		}
		
		deleted = 0;
		inserted = 0;
		updated = 0;
		
		unreadCounter = 0;
		
		try
		{
			if (sipgateDBAdapter == null)
			{
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects = sipgateDBAdapter.getAllVoiceMailData();
			
			sipgateDBAdapter.startTransaction();
			
			for (VoiceMailDataDBObject oldVoiceMailDataDBObject : oldVoiceMailDataDBObjects) 
			{
				if (!newVoiceMailDataDBObjects.contains(oldVoiceMailDataDBObject))
				{
					sipgateDBAdapter.delete(oldVoiceMailDataDBObject);
					deleted++;
				}
			}
					
			for (VoiceMailDataDBObject newVoiceMailDataDBObject : newVoiceMailDataDBObjects) 
			{
				if (oldVoiceMailDataDBObjects.contains(newVoiceMailDataDBObject))
				{	
					oldVoiceMailDataDBObject = oldVoiceMailDataDBObjects.elementAt(oldVoiceMailDataDBObjects.indexOf(newVoiceMailDataDBObject));
										
					newVoiceMailDataDBObject.setSeen(oldVoiceMailDataDBObject.getSeen());
					
					sipgateDBAdapter.update(newVoiceMailDataDBObject);

					if (!newVoiceMailDataDBObject.isRead() && !newVoiceMailDataDBObject.isSeen())
					{
						unreadCounter++;
					}
					
					updated++;
				}
				else
				{
					sipgateDBAdapter.insert(newVoiceMailDataDBObject);
					
					if (!newVoiceMailDataDBObject.isRead())
					{
						unreadCounter++;
					}
					
					inserted++;
				}
			}
			
			Log.d(TAG, "VoiceMailDataDBObject deleted: " + deleted);
			Log.d(TAG, "VoiceMailDataDBObject inserted: " + inserted);
			Log.d(TAG, "VoiceMailDataDBObject updated: " + updated);
											
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e)
		{
			Log.e(TAG, "notifyIfUnreadsVoiceMails()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction())
			{
				sipgateDBAdapter.rollbackTransaction();
			}
		}
		
		if (unreadCounter > 0) 
		{
			createNewVoiceMailNotification(unreadCounter);
		
			Log.d(TAG, "new unseen voicemails: " + unreadCounter);
		}
		else
		{
			removeNewVoiceMailNotification();
		}
		
		for (PendingIntent pendingIntent: voiceMailListener)
		{
			try 
			{
				Log.d(TAG, "notifying refresh voice mails to activity");
				pendingIntent.send();
			} 
			catch (CanceledException e) 
			{
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * @author graef
	 * @param Vector with contactDataDBObjects
	 * @since 1.1
	 */
	public void notifyIfNewContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Context context) 
	{
		Log.d(TAG, "notifyIfNewContacts");
		
		if (newContactDataDBObjects == null)
		{
			Log.i(TAG, "notifyIfNewContacts() -> callDataDBObjects is null");
			return;
		}
		
		try
		{
			if (sipgateDBAdapter == null)
			{
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<ContactDataDBObject> oldContactDataDBObjects = sipgateDBAdapter.getAllContactData();
			
			sipgateDBAdapter.startTransaction();
			
			deleted = 0;
			inserted = 0;
			updated = 0;
		
			for (ContactDataDBObject oldContactDataDBObject : oldContactDataDBObjects) 
			{
				if (!newContactDataDBObjects.contains(oldContactDataDBObject))
				{
					sipgateDBAdapter.delete(oldContactDataDBObject);
					deleted++;
				}
			}
						
			for (ContactDataDBObject newContactDataDBObject : newContactDataDBObjects) 
			{
				if (oldContactDataDBObjects.contains(newContactDataDBObject))
				{	
					sipgateDBAdapter.update(newContactDataDBObject);
					
					sipgateDBAdapter.deleteAllContactNumberDBObjectsByUuid(newContactDataDBObject.getUuid());
					sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
					
					updated++;
				}
				else
				{
					sipgateDBAdapter.insert(newContactDataDBObject);
					sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
					
					inserted++;
				}
			}
			
			Log.d(TAG, "ContactDataDBObject deleted: " + deleted);
			Log.d(TAG, "ContactDataDBObject inserted: " + inserted);
			Log.d(TAG, "ContactDataDBObject updated: " + updated);
									
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e)
		{
			Log.e(TAG, "notifyIfNewContacts()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction())
			{
				sipgateDBAdapter.rollbackTransaction();
			}
		}
		
		for (PendingIntent pendingIntent: contactListener)
		{
			try 
			{
				Log.d(TAG, "notifying refresh contacts to activity");
				pendingIntent.send();
			} 
			catch (CanceledException e) 
			{
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * @author graef
	 * @param Vector with callDataDBObjects
	 * @since 1.1
	 */
	
	public void notifyIfNewCalls(Vector<CallDataDBObject> newCallDataDBObjects, Context context) 
	{
		Log.d(TAG, "notifyIfUnreadsCalls");
		
		if (newCallDataDBObjects == null)
		{
			Log.i(TAG, "notifyIfUnreadsCalls() -> callDataDBObjects is null");
			return;
		}
		
		deleted = 0;
		inserted = 0;
		updated = 0;

		unreadCounter = 0;
		
		try
		{
			if (sipgateDBAdapter == null)
			{
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<CallDataDBObject> oldCallDataDBObjects = sipgateDBAdapter.getAllCallData();
			
			sipgateDBAdapter.startTransaction();
			
			for (CallDataDBObject oldCallDataDBObject : oldCallDataDBObjects) 
			{
				if (!newCallDataDBObjects.contains(oldCallDataDBObject))
				{
					sipgateDBAdapter.delete(oldCallDataDBObject);
					deleted++;
				}
			}
					
			for (CallDataDBObject newCallDataDBObject : newCallDataDBObjects) 
			{
				if (oldCallDataDBObjects.contains(newCallDataDBObject))
				{	
					oldCallDataDBObject = oldCallDataDBObjects.elementAt(oldCallDataDBObjects.indexOf(newCallDataDBObject));
										
					if (newCallDataDBObject.getRead() == -1)
					{
						newCallDataDBObject.setRead(oldCallDataDBObject.getRead());
					}
					
					sipgateDBAdapter.update(newCallDataDBObject);
					
					if (!newCallDataDBObject.isRead() && newCallDataDBObject.isMissed() && newCallDataDBObject.getDirection() == CallDataDBObject.INCOMING)
					{
						unreadCounter++;
					}
					
					updated++;
				}
				else
				{
					sipgateDBAdapter.insert(newCallDataDBObject);
					
					if (!newCallDataDBObject.isRead() && newCallDataDBObject.isMissed() && newCallDataDBObject.getDirection() == CallDataDBObject.INCOMING)
					{
						unreadCounter++;
					}
					
					inserted++;
				}
			}

			Log.d(TAG, "CallDataDBObject deleted: " + deleted);
			Log.d(TAG, "CallDataDBObject inserted: " + inserted);
			Log.d(TAG, "CallDataDBObject updated: " + updated);
			
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e)
		{
			Log.e(TAG, "notifyIfUnreadsCalls()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction())
			{
				sipgateDBAdapter.rollbackTransaction();
			}
		}
	
		if (unreadCounter > 0) 
		{
			createNewCallNotification(unreadCounter);
			
			Log.d(TAG, "new unread calls: " + unreadCounter);
		}
		else
		{
			removeNewCallNotification();
		}
		
		for (PendingIntent pendingIntent: callListener)
		{
			try 
			{
				Log.d(TAG, "notifying refresh calls to activity");
				pendingIntent.send();
			} 
			catch (CanceledException e) 
			{
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
		if (notifyClient != null)
		{
			notifyClient.setNotification(NotificationClient.NotificationType.VOICEMAIL, R.drawable.statusbar_voicemai_48, buildVoicemailNotificationString(unreadCounter));
		}
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void createNewCallNotification(int unreadCounter) 
	{
		if (notifyClient != null)
		{
			notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.statusbar_icon_calllist, buildCallNotificationString(unreadCounter));
		}
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void removeNewCallNotification() 
	{
		if (notifyClient != null)
		{
			notifyClient.deleteNotification(NotificationClient.NotificationType.CALL);
		}
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @since 1.1
	 */
	private void removeNewVoiceMailNotification() 
	{
		if (notifyClient != null)
		{
			notifyClient.deleteNotification(NotificationClient.NotificationType.VOICEMAIL);
		}
	}
	
	/**
	 * 
	 * @param unreadCounter
	 * @return
	 * @since 1.0
	 */
	private String buildVoicemailNotificationString(int unreadCounter) 
	{
		if(unreadCounter == 1) 
		{
			return String.format((String) getResources().getText(R.string.sipgate_a_new_voicemail), Integer.valueOf(unreadCounter));
		} 
		else 
		{
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
		if(unreadCounter == 1 ) 
		{
			return String.format((String) getResources().getText(R.string.sipgate_a_new_call), Integer.valueOf(unreadCounter));
		}
		else 
		{
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

	public void registerOnContactsIntent(PendingIntent i) throws RemoteException
	{
		Log.d(TAG, "registering on contact events intent");
		contactListener.add(i);
	}

	public void unregisterOnContactsIntent(PendingIntent i) throws RemoteException
	{
		Log.d(TAG, "unregistering on contact events intent");
		contactListener.remove(i);
	}
	
	/**
	 * 
	 * @since 1.1
	 */
	public void registerOnCallsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "registering on call events intent");
		callListener.add(i);
	}

	/**
	 * 
	 * @since 1.1
	 */
	public void unregisterOnCallsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "unregistering on call events intent");
		callListener.remove(i);
	}
	
	/**
	 * 
	 * @since 1.0
	 */
	public void registerOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "registering on voice events intent");
		voiceMailListener.add(i);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void unregisterOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "unregistering on voice events intent");
		voiceMailListener.remove(i);
	}
	
	public void initCallRefreshTimer()
	{
		if(callRefreshTimer != null) 
		{
			callRefreshTimer.cancel();
			callRefreshTimer.purge();
		} 

		callRefreshTimer = new Timer();
		
		callRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "call timertask started");
			
				if(serviceEnabled) 
				{
					refreshCallEvents();
				}
			}

		}, 1000, CALL_REFRESH_INTERVAL);
	}
	
	public void initVoicemailRefreshTimer()
	{
		if(voiceMailRefreshTimer != null) 
		{
			voiceMailRefreshTimer.cancel();
			voiceMailRefreshTimer.purge();
		} 

		voiceMailRefreshTimer = new Timer();
		
		voiceMailRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "voicemail timertask started");
				
				if(serviceEnabled)
				{
					refreshVoicemailEvents();
				}
			}
		}, 1000, VOICEMAIL_REFRESH_INTERVAL);
	}
	
	public void initContactRefreshTimer()
	{
		if(contactRefreshTimer != null) 
		{
			contactRefreshTimer.cancel();
			contactRefreshTimer.purge();
		}

		contactRefreshTimer = new Timer();  

		contactRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "contact timertask started");
			
				if(serviceEnabled) 
				{
					refreshContactEvents();
				}
			}

		}, 1000, CONTACT_REFRESH_INTERVAL);
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

			@Override
			public void registerOnContactsIntent(PendingIntent i) throws RemoteException
			{
				service.registerOnContactsIntent(i);				
			}

			@Override
			public void unregisterOnContactsIntent(PendingIntent i) throws RemoteException
			{
				service.unregisterOnContactsIntent(i);
			}

			@Override
			public void initCallRefreshTimer() throws RemoteException
			{
				service.initCallRefreshTimer();				
			}

			@Override
			public void initContactRefreshTimer() throws RemoteException
			{
				service.initContactRefreshTimer();				
			}

			@Override
			public void initVoicemailRefreshTimer() throws RemoteException
			{
				service.initVoicemailRefreshTimer();				
			}
		};
	}

	/**
	 * 
	 * @return
	 * @since 1.1
	 */
	private boolean hasVmListFeature() 
	{
		try 
		{
			return ApiServiceProvider.getInstance(getApplicationContext()).featureAvailable(API_FEATURE.VM_LIST);
		} 
		catch (Exception e) 
		{
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.getLocalizedMessage());
		}
		
		return false;
	}
}
