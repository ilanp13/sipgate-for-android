package com.sipgate.service;

import java.util.HashMap;
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
import com.sipgate.exceptions.StoreDataException;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.NotificationClient.NotificationType;

/**
 * The Background service is responsible for loading new data from the
 * sipgate-API and notify the GUI about it's current status.
 * 
 * @author Marcus Hunger
 * @author Karsten Knuth
 * @author graef
 * @version 1.2
 */
public class SipgateBackgroundService extends Service implements EventService 
{
	public enum NotificationReason {STARTED, FINISHED_NEW_DATA, FINISHED_NO_DATA, FAILED};
	
	public static final String ACTION_GETEVENTS = "action_get";
	public static final String ACTION_NEWEVENTS = "action_new";
	public static final String ACTION_NOEVENTS = "action_no";
	public static final String ACTION_ERROR = "action_error";
	
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.SipgateBackgroundService";
	public static final int REQUEST_NEWEVENTS = 0;

	private static final long CONTACT_REFRESH_INTERVAL = 600000; // every 10mins
	private static final long CALL_REFRESH_INTERVAL = 30000; // every min
	private static final long VOICEMAIL_REFRESH_INTERVAL = 60000; // every min
		
	private static final String TAG = "SipgateBackgroundService";
	
	private boolean serviceEnabled = false;
	
	private Timer contactRefreshTimer = null;
	private Timer callRefreshTimer = null;
	private Timer voiceMailRefreshTimer = null;
	
	private PendingIntent pendingIntent = null;
	private HashMap<String, PendingIntent> newIntents = null;
	private Set<PendingIntent> contactListener = new HashSet<PendingIntent>();
	private HashMap<String, HashMap<String, PendingIntent>> callListener = new HashMap<String, HashMap<String, PendingIntent>>();
	private Set<PendingIntent> voiceMailListener = new HashSet<PendingIntent>();
	
	private NotificationClient notifyClient = null;
	private ApiServiceProvider apiClient = null;
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private VoiceMailDataDBObject oldVoiceMailDataDBObject = null;
	private CallDataDBObject oldCallDataDBObject = null;
		
	private	int unreadCounter = 0;
	
	private int deleted = 0;
	private int updated = 0;
	private int inserted = 0;
	private int insertedCalls = 0;
	
	/**
	 * The onCreate function of the service, which is called at every
	 * first start and is used to instanciate several other classes.
	 * 
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
	 * The onDestroy function is called right before the class is killed.
	 * 
	 * @since 1.0
	 */
	public void onDestroy() 
	{
		Log.d(TAG,"onDestroy");
		
		stopService();
	}
	
	/**
	 * This fucntion allows you to register for information about the
	 * background services' status regarding the refreshing of
	 * contacts.
	 * 
	 * @param i The intent that will function as call back.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.1
	 */
	public void registerOnContactsIntent(PendingIntent i) throws RemoteException
	{
		Log.d(TAG, "registering on contact events intent");
		contactListener.add(i);
	}
	
	/**
	 * This function allows you to register for information about the
	 * background services' status regarding the refreshing of calls.
	 * 
	 * @param tag A string uniquely identifying the process that wants to be called back.
	 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
	 * @param newEventsIntent The intent used as callback when there was new data.
	 * @param noEventsIntent The intent used as callback when there was no new data.
	 * @param errorIntent The intent used as callback when an error occurred.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void registerOnCallsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent) throws RemoteException 
	{
		Log.d(TAG, "registering on call events intent");
		newIntents = new HashMap<String, PendingIntent>();
		newIntents.put(ACTION_GETEVENTS, getEventsIntent);
		newIntents.put(ACTION_NEWEVENTS, newEventsIntent);
		newIntents.put(ACTION_NOEVENTS, noEventsIntent);
		newIntents.put(ACTION_ERROR, errorIntent);
		
		callListener.put(tag, newIntents);
	}
	
	/**
	 * This fucntion allows you to register for information about the
	 * background services' status regarding the refreshing of
	 * voice mails.
	 * 
	 * @param i The intent that will function as call back.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.0
	 */
	public void registerOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "registering on voice events intent");
		voiceMailListener.add(i);
	}

	/**
	 * This function unregisters from information regarding
	 * the refreshing of contacts.
	 * 
	 * @param i The intent to be removed.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.1
	 */
	public void unregisterOnContactsIntent(PendingIntent i) throws RemoteException
	{
		Log.d(TAG, "unregistering on contact events intent");
		contactListener.remove(i);
	}
	
	/**
	 * This function unregisters from information regarding
	 * the refreshing of calls.
	 * 
	 * @param tag A string uniquely identifying the process that wants to be no longer called back.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void unregisterOnCallsIntents(String tag) throws RemoteException 
	{
		Log.d(TAG, "unregistering on call events intent");
		callListener.remove(tag);
	}
	
	/**
	 * This function unregisters from information regarding
	 * the refreshing of voice mails.
	 * 
	 * @param i The intent to be removed.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.0
	 */
	public void unregisterOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
	{
		Log.d(TAG, "unregistering on voice events intent");
		voiceMailListener.remove(i);
	}
	
	/**
	 * This function resets the timer that triggers the refreshing
	 * of contacts.
	 * 
	 * @since 1.1
	 */
	public void initContactRefreshTimer()
	{
		if(contactRefreshTimer != null) {
			contactRefreshTimer.cancel();
			contactRefreshTimer.purge();
		}

		contactRefreshTimer = new Timer();  

		contactRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "contact timertask started");
			
				if(serviceEnabled) {
					refreshContacts();
				}
			}

		}, 1000, CONTACT_REFRESH_INTERVAL);
	}
	
	/**
	 * This function resets the timer that triggers the refreshing
	 * of calls.
	 * 
	 * @since 1.1
	 */
	public void initCallRefreshTimer()
	{
		if(callRefreshTimer != null) {
			callRefreshTimer.cancel();
			callRefreshTimer.purge();
		} 

		callRefreshTimer = new Timer();
		
		callRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "call timertask started");
			
				if(serviceEnabled) {
					refreshCallEvents();
				}
			}

		}, 1000, CALL_REFRESH_INTERVAL);
	}
	
	/**
	 * This function resets the timer that triggers the refreshing
	 * of voice mails.
	 * 
	 * @since 1.1
	 */
	public void initVoicemailRefreshTimer()
	{
		if(voiceMailRefreshTimer != null) {
			voiceMailRefreshTimer.cancel();
			voiceMailRefreshTimer.purge();
		} 

		voiceMailRefreshTimer = new Timer();
		
		voiceMailRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "voicemail timertask started");
				
				if(serviceEnabled) {
					refreshVoicemailEvents();
				}
			}
		}, 1000, VOICEMAIL_REFRESH_INTERVAL);
	}
	
	/**
	 * This function returns a stub class containing the interface
	 * this class provides for RPCs.
	 * 
	 * @param intent The intent that binded on the service.
	 * @return A stub containing the interface to access this class. 
	 * @since 1.0
	 */
	@Override
	public IBinder onBind(Intent intent) 
	{
		final EventService service = this;

		return new Stub() 
		{
 
			/**
			 * This is a wrapper function for registering on the
			 * contacts update status.
			 * 
			 * @param i The intent that will function as the callback.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.1
			 */
			public void registerOnContactsIntent(PendingIntent i) throws RemoteException
			{
				service.registerOnContactsIntent(i);				
			}
			
			/**
			 * This is a wrapper function for registering on the
			 * calls update status.
			 * 
			 * @param tag A string uniquely identifying the process that wants to be called back.
			 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
			 * @param newEventsIntent The intent used as callback when there was new data.
			 * @param noEventsIntent The intent used as callback when there was no new data.
			 * @param errorIntent The intent used as callback when an error occurred.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void registerOnCallsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent) throws RemoteException 
			{
				service.registerOnCallsIntents(tag, getEventsIntent, newEventsIntent, noEventsIntent, errorIntent);
			}
			
			/**
			 * This is a wrapper function for registering on the
			 * vioce mail update status.
			 * 
			 * @param i The intent that will function as the callback.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.0
			 */
			public void registerOnVoiceMailsIntent(PendingIntent i)	throws RemoteException 
			{
				service.registerOnVoiceMailsIntent(i);
			}
			
			/**
			 * This is a wrapper function for unregistering on the
			 * contacts update status.
			 * 
			 * @param i The intent to be unregistered.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.0
			 */
			public void unregisterOnContactsIntent(PendingIntent i) throws RemoteException
			{
				service.unregisterOnContactsIntent(i);
			}
			
			/**
			 * This is a wrapper function for unregistering on the
			 * calls update status.
			 * 
			 * @param tag A string uniquely identifying the process that wants to no longer be called back.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void unregisterOnCallsIntents(String tag) throws RemoteException 
			{
				service.unregisterOnCallsIntents(tag);
			}
			
			/**
			 * This is a wrapper function for unregistering on the
			 * voice mail update status.
			 * 
			 * @param i The intent to be unregistered.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.0
			 */
			public void unregisterOnVoiceMailsIntent(PendingIntent i) throws RemoteException 
			{
				service.unregisterOnVoiceMailsIntent(i);
			}

			/**
			 * This is a wrapper function for reseting the contacts
			 * refresh timer.
			 * 
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.1
			 */
			public void initContactRefreshTimer() throws RemoteException
			{
				service.initContactRefreshTimer();				
			}
			
			/**
			 * This is a wrapper function for reseting the calls
			 * refresh timer.
			 * 
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.1
			 */
			public void initCallRefreshTimer() throws RemoteException
			{
				service.initCallRefreshTimer();				
			}

			/**
			 * This is a wrapper function for reseting the voice mail
			 * refresh timer.
			 * 
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.1
			 */
			public void initVoicemailRefreshTimer() throws RemoteException
			{
				service.initVoicemailRefreshTimer();				
			}
		};
	}
	
	/**
	 * Not implemented
	 * 
	 * @since 1.0
	 */
	public IBinder asBinder() 
	{
		return null;
	}
	
	/**
	 * This function starts all the background task that will retrieve
	 * data from the api.
	 * 
	 * @since 1.0
	 */
	private void startService() 
	{
		if (serviceEnabled) {
			return;
		}
		
		serviceEnabled = true;
	
		initContactRefreshTimer();
		initCallRefreshTimer();
	
		if (hasVmListFeature()) {
			initVoicemailRefreshTimer();
		}
	}
	
	/**
	 * This function stops all the tasks and deletes any notifications
	 * that might still be in the phones notification area.
	 * 
	 * @since 1.0
	 */
	private void stopService() 
	{
		Log.d(TAG,"stopservice");
		
		if (contactRefreshTimer != null) {
			Log.d(TAG,"contactRefreshTimer.cancel");
			contactRefreshTimer.cancel();
		}	
		
		if (callRefreshTimer != null) {
			Log.d(TAG,"callRefreshTimer");
			callRefreshTimer.cancel();
		}	
		
		if (voiceMailRefreshTimer != null) {
			Log.d(TAG,"voiceMailRefreshTimer.cancel");
			voiceMailRefreshTimer.cancel();
		}			
		
		Log.d(TAG,"cancel notifications");
		
		notifyClient.deleteNotification(NotificationType.VOICEMAIL);
		notifyClient.deleteNotification(NotificationType.CALL);
	}
	
	/**
	 * This function checks whether we have the voicemail list feature.
	 * 
	 * @return A boolean returning if the feature is available.
	 * @since 1.1
	 */
	private boolean hasVmListFeature() 
	{
		try {
			return ApiServiceProvider.getInstance(getApplicationContext()).featureAvailable(API_FEATURE.VM_LIST);
		} 
		catch (Exception e) {
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.getLocalizedMessage());
		}
		
		return false;
	}
	
	/**
	 * This function loads all contacts from the api and syncs them
	 * into the database.
	 * 
	 * @since 1.1
	 */
	private void refreshContacts() 
	{
		Log.v(TAG, "refreshContactEvents() -> start");
		
		try {
			notifyIfNewContacts(apiClient.getContacts(), this);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshContactEvents() -> finish");
	}
	
	/**
	 * This function loads the last 100 (2.0) / 3 months worth of (1.0)
	 * calls from the api and syncs them into the database.
	 * 
	 * @since 1.1
	 */
	private void refreshCallEvents() 
	{
		Log.v(TAG, "refreshCallEvents() -> start");
		
		notifyFrontend(ACTION_GETEVENTS);
		
		try {
			insertedCalls = notifyIfNewCalls(apiClient.getCalls(), this);
			
			if (insertedCalls > 0 ) {
				notifyFrontend(ACTION_NEWEVENTS);
			} else {
				notifyFrontend(ACTION_NOEVENTS);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			}
			catch (Exception threadException) {
				threadException.printStackTrace();
			}
			finally {
				notifyFrontend(ACTION_ERROR);
			}
		}
		
		Log.v(TAG, "refreshCallEvents() -> finish");
	}
	
	/**
	 * This function loads the last 100 voice mails from the api and
	 * syncs them into the database.
	 * 
	 * @since 1.1
	 */
	private void refreshVoicemailEvents() 
	{
		Log.v(TAG, "refreshVoicemailEvents() -> start");
		
		try {
			notifyIfNewVoiceMails(apiClient.getVoiceMails(), this);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.v(TAG, "refreshVoicemailEvents() -> finish");
	}
	
	/**
	 * This function send a notification about the services status
	 * to the subscribed frontend processes.
	 * 
	 * @param action The action the frontend is supposed to show to the user.
	 */
	private void notifyFrontend(String action) {
		try {
			for(String key : callListener.keySet()) {
				pendingIntent = null;
				pendingIntent = callListener.get(key).get(action);
				
				if (pendingIntent != null) {
					pendingIntent.send();
				}
			}
		} 
		catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function syncs the newly downloaded contacts into
	 * the database.
	 * 
	 * @param newContactDataDBObjects The Vector with the new contacts data
	 * @param context The application context
	 * @since 1.1
	 */
	private void notifyIfNewContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Context context) 
	{
		Log.d(TAG, "notifyIfNewContacts");
		
		if (newContactDataDBObjects == null) {
			Log.i(TAG, "notifyIfNewContacts() -> callDataDBObjects is null");
			return;
		}
		
		try {
			if (sipgateDBAdapter == null) {
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<ContactDataDBObject> oldContactDataDBObjects = sipgateDBAdapter.getAllContactData();
			
			sipgateDBAdapter.startTransaction();
			
			deleted = 0;
			inserted = 0;
			updated = 0;
		
			deleteOldContacts(newContactDataDBObjects, oldContactDataDBObjects);
			updateContacts(newContactDataDBObjects, oldContactDataDBObjects);		

			Log.d(TAG, "ContactDataDBObject deleted: " + deleted);
			Log.d(TAG, "ContactDataDBObject inserted: " + inserted);
			Log.d(TAG, "ContactDataDBObject updated: " + updated);
									
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e) {
			Log.e(TAG, "notifyIfNewContacts()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) {
				sipgateDBAdapter.rollbackTransaction();
			}
		}
		
		for (PendingIntent pendingIntent: contactListener) {
			try  {
				Log.d(TAG, "notifying refresh contacts to activity");
				pendingIntent.send(NotificationReason.FINISHED_NEW_DATA.ordinal());
			} 
			catch (CanceledException e)  {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * This function syncs the newly downloaded voice mail data into
	 * the database and creates a notification in the phones
	 * notification area if needed.
	 * 
	 * @param newCallDataDBObjects The Vector with the now call data.
	 * @param context The application context.
	 * @throws StoreDataException This exception is thrown when the data can not be stored in the database.
	 * @return The number of newly added calls.
	 * @since 1.1
	 */
	private int notifyIfNewCalls(Vector<CallDataDBObject> newCallDataDBObjects, Context context) throws StoreDataException 
	{
		Log.d(TAG, "notifyIfUnreadsCalls");
		
		if (newCallDataDBObjects == null) {
			Log.i(TAG, "notifyIfUnreadsCalls() -> callDataDBObjects is null");
			return -1;
		}
		
		deleted = 0;
		inserted = 0;
		updated = 0;

		unreadCounter = 0;
		
		try {
			if (sipgateDBAdapter == null) {
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<CallDataDBObject> oldCallDataDBObjects = sipgateDBAdapter.getAllCallData();
			
			sipgateDBAdapter.startTransaction();
			
			deleteOldCalls(newCallDataDBObjects, oldCallDataDBObjects);
			updateCalls(newCallDataDBObjects, oldCallDataDBObjects);

			Log.d(TAG, "CallDataDBObject deleted: " + deleted);
			Log.d(TAG, "CallDataDBObject inserted: " + inserted);
			Log.d(TAG, "CallDataDBObject updated: " + updated);
			
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e) {
			Log.e(TAG, "notifyIfUnreadsCalls()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) {
				sipgateDBAdapter.rollbackTransaction();
			}
			
			throw new StoreDataException();
		}
	
		if (unreadCounter > 0) {
			createNewCallNotification(unreadCounter);
			
			Log.d(TAG, "new unread calls: " + unreadCounter);
		}
		else {
			removeNewCallNotification();
		}

		return inserted;
	}
	
	/**
	 * This function syncs the newly downloaded voice mail data into
	 * the database and creates a notification in the phones
	 * notification area if needed.
	 * 
	 * @param newVoiceMailDataDBObjects A Vector of the new voice mails.
	 * @param context The Application context.
	 * @since 1.0
	 */
	private void notifyIfNewVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Context context) 
	{
		Log.d(TAG, "notifyIfUnreadVoiceMails");
		
		if (newVoiceMailDataDBObjects == null) {
			Log.i(TAG, "notifyIfUnreadVoiceMails() -> voiceMailDataDBObjects is null");
			return;
		}
		
		deleted = 0;
		inserted = 0;
		updated = 0;
		
		unreadCounter = 0;
		
		try {
			if (sipgateDBAdapter == null) {
				sipgateDBAdapter = SipgateDBAdapter.getInstance(context);
			}
			
			Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects = sipgateDBAdapter.getAllVoiceMailData();
			
			sipgateDBAdapter.startTransaction();
			
			deleteOldVoiceMails(newVoiceMailDataDBObjects, oldVoiceMailDataDBObjects);
			updateVoiceMails(newVoiceMailDataDBObjects, oldVoiceMailDataDBObjects);
			
			Log.d(TAG, "VoiceMailDataDBObject deleted: " + deleted);
			Log.d(TAG, "VoiceMailDataDBObject inserted: " + inserted);
			Log.d(TAG, "VoiceMailDataDBObject updated: " + updated);
											
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e) {
			Log.e(TAG, "notifyIfUnreadsVoiceMails()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) {
				sipgateDBAdapter.rollbackTransaction();
			}
		}
		
		if (unreadCounter > 0) {
			createNewVoiceMailNotification(unreadCounter);
		
			Log.d(TAG, "new unseen voicemails: " + unreadCounter);
		}
		else {
			removeNewVoiceMailNotification();
		}
		
		for (PendingIntent pendingIntent: voiceMailListener) {
			try  {
				Log.d(TAG, "notifying refresh voice mails to activity");
				pendingIntent.send(NotificationReason.FINISHED_NEW_DATA.ordinal());
			} 
			catch (CanceledException e) {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * This function deletes all old contacts from the database.
	 * 
	 * @param newContactDataDBObjects The vector containing the new contacts.
	 * @param oldContactDataDBObjects The vector containing the old contacts.
	 * @since 1.2
	 */
	private void deleteOldContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Vector<ContactDataDBObject> oldContactDataDBObjects)
	{
		for (ContactDataDBObject oldContactDataDBObject : oldContactDataDBObjects) {
			if (!newContactDataDBObjects.contains(oldContactDataDBObject)) {
				sipgateDBAdapter.delete(oldContactDataDBObject);
				deleted++;
			}
		}
	}
	
	/**
	 * This function deletes all old calls from the database.
	 * 
	 * @param newCallDataDBObjects The vector containing the new calls.
	 * @param oldCallDataDBObjects The vector containing the old calls.
	 * @since 1.2
	 */
	private void deleteOldCalls(Vector<CallDataDBObject> newCallDataDBObjects, Vector<CallDataDBObject> oldCallDataDBObjects)
	{
		for (CallDataDBObject oldCallDataDBObject : oldCallDataDBObjects) {
			if (!newCallDataDBObjects.contains(oldCallDataDBObject)) {
				sipgateDBAdapter.delete(oldCallDataDBObject);
				deleted++;
			}
		}
	}
	
	/**
	 * This function deletes all old voice mails from the database.
	 * 
	 * @param newVoiceMailDataDBObjects The vector containing the new voice mails.
	 * @param oldVoiceMailDataDBObjects The vector containing the old voice mails.
	 * @since 1.2
	 */
	private void deleteOldVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects)
	{
		for (VoiceMailDataDBObject oldVoiceMailDataDBObject : oldVoiceMailDataDBObjects) {
			if (!newVoiceMailDataDBObjects.contains(oldVoiceMailDataDBObject)) {
				sipgateDBAdapter.delete(oldVoiceMailDataDBObject);
				deleted++;
			}
		}
	}
	
	/**
	 * This functions inserts new and updates already existing contacts
	 * in the database.
	 * 
	 * @param newContactDataDBObjects The vector containing the new calls.
	 * @param oldContactDataDBObjects The vector containing the old calls.
	 * @since 1.2
	 */
	private void updateContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Vector<ContactDataDBObject> oldContactDataDBObjects)
	{
		for (ContactDataDBObject newContactDataDBObject : newContactDataDBObjects) {
			if (oldContactDataDBObjects.contains(newContactDataDBObject)) {	
				sipgateDBAdapter.update(newContactDataDBObject);
				
				sipgateDBAdapter.deleteAllContactNumberDBObjectsByUuid(newContactDataDBObject.getUuid());
				sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
				
				updated++;
			}
			else {
				sipgateDBAdapter.insert(newContactDataDBObject);
				sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
				
				inserted++;
			}
		}
	}
	
	/**
	 * This functions inserts new and updates already existing calls
	 * in the database.
	 * 
	 * @param newCallDataDBObjects The vector containing the new calls.
	 * @param oldCallDataDBObjects The vector containing the old calls.
	 * @since 1.2
	 */
	private void updateCalls(Vector<CallDataDBObject> newCallDataDBObjects, Vector<CallDataDBObject> oldCallDataDBObjects)
	{
		for (CallDataDBObject newCallDataDBObject : newCallDataDBObjects) {
			if (oldCallDataDBObjects.contains(newCallDataDBObject)) {	
				oldCallDataDBObject = oldCallDataDBObjects.elementAt(oldCallDataDBObjects.indexOf(newCallDataDBObject));
									
				if (newCallDataDBObject.getRead() == -1) {
					newCallDataDBObject.setRead(oldCallDataDBObject.getRead());
				}
				
				sipgateDBAdapter.update(newCallDataDBObject);
				
				if (!newCallDataDBObject.isRead() && newCallDataDBObject.isMissed() && newCallDataDBObject.getDirection() == CallDataDBObject.INCOMING) {
					unreadCounter++;
				}
				
				updated++;
			}
			else {
				sipgateDBAdapter.insert(newCallDataDBObject);
				
				if (!newCallDataDBObject.isRead() && newCallDataDBObject.isMissed() && newCallDataDBObject.getDirection() == CallDataDBObject.INCOMING) {
					unreadCounter++;
				}
				
				inserted++;
			}
		}
	}
	
	/**
	 * This functions inserts new and updates already existing voice mails
	 * in the database.
	 * 
	 * @param newVoiceMailDataDBObjects The vector containing the new voice mails.
	 * @param oldVoiceMailDataDBObjects The vector containing the old voice mails.
	 * @since 1.2
	 */
	private void updateVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects)
	{
		for (VoiceMailDataDBObject newVoiceMailDataDBObject : newVoiceMailDataDBObjects) {
			if (oldVoiceMailDataDBObjects.contains(newVoiceMailDataDBObject)) {	
				oldVoiceMailDataDBObject = oldVoiceMailDataDBObjects.elementAt(oldVoiceMailDataDBObjects.indexOf(newVoiceMailDataDBObject));
									
				newVoiceMailDataDBObject.setSeen(oldVoiceMailDataDBObject.getSeen());
				
				sipgateDBAdapter.update(newVoiceMailDataDBObject);

				if (!newVoiceMailDataDBObject.isRead() && !newVoiceMailDataDBObject.isSeen()) {
					unreadCounter++;
				}
				
				updated++;
			}
			else {
				sipgateDBAdapter.insert(newVoiceMailDataDBObject);
				
				if (!newVoiceMailDataDBObject.isRead()) {
					unreadCounter++;
				}
				
				inserted++;
			}
		}
	}
	
	/**
	 * This function adds a notification containing the number of
	 * unread calls to the phones notification area.
	 * 
	 * @param unreadCounter The number of unread calls that are to be shown in the notification.
	 * @since 1.1
	 */
	private void createNewCallNotification(int unreadCounter) 
	{
		if (notifyClient != null) {
			notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.statusbar_icon_calllist, buildCallNotificationString(unreadCounter));
		}
	}
	
	/**
	 * This function adds a notification containing the number of
	 * unread voice mails to the phones notification area.
	 * 
	 * @param unreadCounter The number of unread voice mails that are to be shown in the notification.
	 * @since 1.0
	 */
	private void createNewVoiceMailNotification(int unreadCounter) 
	{
		if (notifyClient != null) {
			notifyClient.setNotification(NotificationClient.NotificationType.VOICEMAIL, R.drawable.statusbar_voicemai_48, buildVoicemailNotificationString(unreadCounter));
		}
	}
	
	/**
	 * This function removes the notification for new calls form the
	 * phones notification area.
	 * 
	 * @since 1.1
	 */
	private void removeNewCallNotification() 
	{
		if (notifyClient != null) {
			notifyClient.deleteNotification(NotificationClient.NotificationType.CALL);
		}
	}
	
	/**
	 * This function removes the notification for new voice mails
	 * form the phones notification area.
	 * 
	 * @since 1.1
	 */
	private void removeNewVoiceMailNotification() 
	{
		if (notifyClient != null) {
			notifyClient.deleteNotification(NotificationClient.NotificationType.VOICEMAIL);
		}
	}
	
	/**
	 * This function creates the string for the "new calls"
	 * notification.
	 * 
	 * @param unreadCounter The number of unread calls.
	 * @return A string to be used in the notificaion.
	 * @since 1.0
	 */
	private String buildCallNotificationString(int unreadCounter) 
	{
		if(unreadCounter == 1 ) {
			return String.format((String) getResources().getText(R.string.sipgate_a_new_call), Integer.valueOf(unreadCounter));
		}
		else {
			return String.format((String) getResources().getText(R.string.sipgate_new_calls), Integer.valueOf(unreadCounter));
		}
	}
	
	/**
	 * This function creates the string for the "new voice mails"
	 * notification.
	 * 
	 * @param unreadCounter The number of unread voicemails.
	 * @return A string to be used in the notificaion.
	 * @since 1.0
	 */
	private String buildVoicemailNotificationString(int unreadCounter) 
	{
		if(unreadCounter == 1) {
			return String.format((String) getResources().getText(R.string.sipgate_a_new_voicemail), Integer.valueOf(unreadCounter));
		} 
		else  {
			return String.format((String) getResources().getText(R.string.sipgate_new_voicemails), Integer.valueOf(unreadCounter));
		}
	}
}
