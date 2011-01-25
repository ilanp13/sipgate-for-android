package com.sipgate.service;

import java.util.HashMap;
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
import com.sipgate.db.SystemDataDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.StoreDataException;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.ApiServiceProviderImpl;
import com.sipgate.util.ApiServiceProviderImpl.API_FEATURE;
import com.sipgate.util.Constants;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.NotificationClient.NotificationType;
import com.sipgate.util.SettingsClient;

/**
 * The Background service is responsible for loading new data from the
 * sipgate-API and notify the GUI about it's current status.
 * 
 * @author Marcus Hunger
 * @author Karsten Knuth
 * @author graef
 * @author niepel
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

	private static final String TAG = "SipgateBackgroundService";
	
	private boolean serviceEnabled = false;
		
	private Timer contactRefreshTimer = null;
	private Timer callRefreshTimer = null;
	private Timer voiceMailRefreshTimer = null;
	
	private PendingIntent pendingIntent = null;
	private HashMap<String, PendingIntent> newIntents = null;
	private HashMap<String, HashMap<String, PendingIntent>> contactListener = new HashMap<String, HashMap<String, PendingIntent>>();
	private HashMap<String, HashMap<String, PendingIntent>> callListener = new HashMap<String, HashMap<String, PendingIntent>>();
	private HashMap<String, HashMap<String, PendingIntent>> voiceMailListener = new HashMap<String, HashMap<String, PendingIntent>>();
	
	private NotificationClient notifyClient = null;
	private ApiServiceProvider apiClient = null;
	private SettingsClient settingsClient = null;
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private VoiceMailDataDBObject oldVoiceMailDataDBObject = null;
	private CallDataDBObject oldCallDataDBObject = null;
		
	private	int tempMissedCalls = 0;
	private int missedTempCallsCounter = 0;
	private	int missedCallsCount = 0;
	
	private	int newVoiceMailsCounter = 0;
	
	private int deletedContacts = 0;
	private int updatedContacts = 0;
	private int insertedContacts = 0;
	
	private int deletedCalls = 0;
	private int updatedCalls = 0;
	private int insertedCalls = 0;
	
	private int deletedVoiceMails = 0;
	private int updatedVoiceMails = 0;
	private int insertedVoiceMails = 0;
	
	private long currentRefreshCalls = 0;
	private long lastRefreshCalls = 0;
	private long lastFullRefreshCalls = 0;
	
	private long currentRefreshVoiceMails = 0;
	private long lastRefreshVoiceMails = 0;
	private long lastFullRefreshVoiceMails = 0;
	
	private Vector<CallDataDBObject> calls = null;
	private Vector<VoiceMailDataDBObject> voiceMails = null;
			
	/**
	 * The onCreate function of the service, which is called asetRemoteNumberE164t every
	 * first start and is used to instantiate several other classes.
	 * 
	 * @since 1.0
	 */
	public void onCreate() 
	{
		super.onCreate();
		
		notifyClient = new NotificationClient(this); 
		
		apiClient = ApiServiceProvider.getInstance(this);
		
		settingsClient = SettingsClient.getInstance(this);
			
		sipgateDBAdapter = new SipgateDBAdapter(this);
		
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
	
		if (sipgateDBAdapter != null)
		{
			sipgateDBAdapter.close();
		}
	}
	
	/**
	 * This function allows you to register for information about the
	 * background services' status regarding the refreshing of
	 * contacts.
	 * 
	 * @param tag A string uniquely identifying the process that wants to be called back.
	 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
	 * @param newEventsIntent The intent used as callback when there was new data.
	 * @param noEventsIntent The intent used as callback when there was no new data.
	 * @param errorIntent The intent used as callback when an error occurred.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void registerOnContactsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent) throws RemoteException
	{
		Log.d(TAG, "registering on contact events intent");
		
		newIntents = new HashMap<String, PendingIntent>();
		newIntents.put(ACTION_GETEVENTS, getEventsIntent);
		newIntents.put(ACTION_NEWEVENTS, newEventsIntent);
		newIntents.put(ACTION_NOEVENTS, noEventsIntent);
		newIntents.put(ACTION_ERROR, errorIntent);
		
		contactListener.put(tag, newIntents);
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
	 * This function allows you to register for information about the
	 * background services' status regarding the refreshing of
	 * voice mails.
	 * 
	 * @param tag A string uniquely identifying the process that wants to be called back.
	 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
	 * @param newEventsIntent The intent used as callback when there was new data.
	 * @param noEventsIntent The intent used as callback when there was no new data.
	 * @param errorIntent The intent used as callback when an error occurred.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void registerOnVoiceMailsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent) throws RemoteException 
	{
		Log.d(TAG, "registering on voice events intent");
		
		newIntents = new HashMap<String, PendingIntent>();
		newIntents.put(ACTION_GETEVENTS, getEventsIntent);
		newIntents.put(ACTION_NEWEVENTS, newEventsIntent);
		newIntents.put(ACTION_NOEVENTS, noEventsIntent);
		newIntents.put(ACTION_ERROR, errorIntent);
		
		voiceMailListener.put(tag, newIntents);
	}

	/**
	 * This function unregisters from information regarding
	 * the refreshing of contacts.
	 * 
	 * @param tag A string uniquely identifying the process that wants to be no longer called back.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void unregisterOnContactsIntents(String tag) throws RemoteException
	{
		Log.d(TAG, "unregistering on contact events intent");
		contactListener.remove(tag);
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
	 * @param tag A string uniquely irefreshCallEventsdentifying the process that wants to be no longer called back.
	 * @throws RemoteException Thrown when the remote communication failed.
	 * @since 1.2
	 */
	public void unregisterOnVoiceMailsIntents(String tag) throws RemoteException 
	{
		Log.d(TAG, "unregistering on voice events intent");
		voiceMailListener.remove(tag);
	}
	
	/**
	 * This function resets the timer and triggers a refresh
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

		}, 1000, settingsClient.getContactsRefreshTime());
	}
	
	/**
	 * This function resets the timer and triggers a full refresh
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
		
		lastFullRefreshCalls = 0;
		lastRefreshCalls = 0;
				
		callRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "call timertask started");
			
				if(serviceEnabled) {
					refreshCallEvents();
				}
			}

		}, 1000, settingsClient.getEventsRefreshTime());
	}
		
	/**
	 * This function resets the timer and triggers a full refresh
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
	
		lastFullRefreshVoiceMails = 0;
		lastRefreshVoiceMails = 0;
		
		voiceMailRefreshTimer.scheduleAtFixedRate(new TimerTask() 
		{
			public void run() 
			{
				Log.v(TAG, "voicemail timertask started");
				
				if(serviceEnabled) {
					refreshVoicemailEvents();
				}
			}
		}, 1000, settingsClient.getEventsRefreshTime());
	}
	
	/**
	 * This function returns a stub class containing the interface
	 * this class provides for RPCs.
	 * tempCounter
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
			 * @param tag A string uniquely identifying the process that wants to be called back.
			 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
			 * @param newEventsIntent The intent used as callback when there was new data.
			 * @param noEventsIntent The intent used as callback when there was no new data.
			 * @param errorIntent The intent used as callback when an error occurred.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void registerOnContactsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent) throws RemoteException
			{
				service.registerOnContactsIntents(tag, getEventsIntent, newEventsIntent, noEventsIntent, errorIntent);				
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
			 * voice mail update status.
			 * 
			 * @param tag A string uniquely identifying the process that wants to be called back.
			 * @param getEventsIntent The intent used as callback when starting a refresh cycle.
			 * @param newEventsIntent The intent used as callback when there was new data.
			 * @param noEventsIntent The intent used as callback when there was no new data.
			 * @param errorIntent The intent used as callback when an error occurred.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void registerOnVoiceMailsIntents(String tag, PendingIntent getEventsIntent, PendingIntent newEventsIntent, PendingIntent noEventsIntent, PendingIntent errorIntent)	throws RemoteException 
			{
				service.registerOnVoiceMailsIntents(tag, getEventsIntent, newEventsIntent, noEventsIntent, errorIntent);
			}
			
			/**
			 * This is a wrapper function for unregistering on the
			 * contacts update status.
			 * 
			 * @param tag A string uniquely identifying the process that wants to no longer be called back.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void unregisterOnContactsIntents(String tag) throws RemoteException
			{
				service.unregisterOnContactsIntents(tag);
			}
			
			/**
			 * This is a wrapper function for unregistering on the
			 * calls update status.
			 * newTempCallsCounter
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
			 * @param tag A string uniquely identifying the process that wants to no longer be called back.
			 * @throws RemoteException Thrown when the remote communication failed.
			 * @since 1.2
			 */
			public void unregisterOnVoiceMailsIntents(String tag) throws RemoteException 
			{
				service.unregisterOnVoiceMailsIntents(tag);
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
	 * This function syncs the newly downloaded contacts into
	 * the database.
	 * 
	 * @param newCallDataDBObjects The Vector with the new contact data.
	 * @param context The application context.
	 * @throws StoreDataException This exception is thrown when the data can not be stored in the database.
	 * @return The number of newly added contacts.
	 * @since 1.1
	 */
	public int notifyIfNewContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Context context) throws StoreDataException 
	{
		Log.d(TAG, "notifyIfNewContacts");
		
		if (newContactDataDBObjects == null) 
		{
			Log.i(TAG, "notifyIfNewContacts() -> callDataDBObjects is null");
			return -1;
		}
		
		deletedContacts = 0;
		insertedContacts = 0;
		updatedContacts = 0;
		
		try
		{
			if (sipgateDBAdapter == null) 
			{
				sipgateDBAdapter = new SipgateDBAdapter(context);
			}
			
			Vector<ContactDataDBObject> oldContactDataDBObjects = sipgateDBAdapter.getAllContactData();
			
			sipgateDBAdapter.startTransaction();
			
			deleteOldContacts(newContactDataDBObjects, oldContactDataDBObjects, sipgateDBAdapter);
						
			updateContacts(newContactDataDBObjects, oldContactDataDBObjects, sipgateDBAdapter);		
			
			Log.d(TAG, "ContactDataDBObject deleted: " + deletedContacts);
			Log.d(TAG, "ContactDataDBObject inserted: " + insertedContacts);
			Log.d(TAG, "ContactDataDBObject updated: " + updatedContacts);
			
			sipgateDBAdapter.commitTransaction();
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyIfNewContacts()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) 
			{
				sipgateDBAdapter.rollbackTransaction();
			}
			
			throw new StoreDataException();
		}
		
		return insertedContacts;
	}
	
	/**
	 * This function syncs the newly downloaded call data into
	 * the database and creates a notification in the phones
	 * notification area if needed.
	 * 
	 * @param newCallDataDBObjects The Vector with the new call data.
	 * @param context The application context.
	 * @param fullRefresh Indicator if newCallDataDBObjects contains a full or delta refresh
	 * @throws StoreDataException This exception is thrown when the data can not be stored in the database.
	 * @return The number of newly added calls.
	 * @since 1.1
	 */
	public int notifyIfNewCalls(Vector<CallDataDBObject> newCallDataDBObjects, Context context, boolean fullRefresh) throws StoreDataException 
	{
		Log.d(TAG, "notifyIfUnreadsCalls");
		
		if (newCallDataDBObjects == null) 
		{
			Log.i(TAG, "notifyIfUnreadsCalls() -> callDataDBObjects is null");
			return -1;
		}
		
		deletedCalls = 0;
		insertedCalls = 0;
		updatedCalls = 0;

		missedCallsCount = 0;
		missedTempCallsCounter = 0;
		
		tempMissedCalls = 0;
		
		try 
		{
			if (sipgateDBAdapter == null) 
			{
				sipgateDBAdapter = new SipgateDBAdapter(context);
			}
			
			SystemDataDBObject notifyCallsCountDBObj = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_CALLS_COUNT);
			SystemDataDBObject notifyTempCallsCountDBObj = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_TEMP_CALLS_COUNT);
						
			Vector<CallDataDBObject> oldCallDataDBObjects = sipgateDBAdapter.getAllCallData();
			Vector<CallDataDBObject> tempCallDataDBObjects = sipgateDBAdapter.getAllTempCallDataDBObjects();
						
			sipgateDBAdapter.startTransaction();

			if (notifyCallsCountDBObj != null)
			{
				missedCallsCount = Integer.valueOf(notifyCallsCountDBObj.getValue());
			}
			
			if (notifyTempCallsCountDBObj != null)
			{
				missedTempCallsCounter = Integer.valueOf(notifyTempCallsCountDBObj.getValue());
			}
			
			if (fullRefresh) 
			{
				deleteOldCalls(newCallDataDBObjects, oldCallDataDBObjects, sipgateDBAdapter);
				
				missedCallsCount = 0;
				tempMissedCalls = 0;
				missedTempCallsCounter = 0;
				
				tempCallDataDBObjects.removeAllElements();
			}
			
			updateCalls(newCallDataDBObjects, oldCallDataDBObjects, sipgateDBAdapter);
			
			for(int i=0; i < tempCallDataDBObjects.size(); i++)
			{
				if(tempCallDataDBObjects.get(i).getDirection() == CallDataDBObject.INCOMING)
				{
					if (tempCallDataDBObjects.get(i).isMissed())
					{
						tempMissedCalls++;
					}
				}
				
				sipgateDBAdapter.delete(tempCallDataDBObjects.get(i));
			}
			
			if (notifyTempCallsCountDBObj != null)
			{
				notifyTempCallsCountDBObj.setValue(String.valueOf(0));
				sipgateDBAdapter.update(notifyTempCallsCountDBObj);
			}
			
			sipgateDBAdapter.commitTransaction();
				
			Log.d(TAG, "missedCallsCount: " + missedCallsCount);
			Log.d(TAG, "missedTempCallsCounter: " + missedTempCallsCounter);
			Log.d(TAG, "tempMissedCalls: " + tempMissedCalls);
			
			Log.d(TAG, "CallDataDBObject deleted: " + deletedCalls);
			Log.d(TAG, "CallDataDBObject inserted: " + insertedCalls);
			Log.d(TAG, "CallDataDBObject updated: " + updatedCalls);
			
			if (notifyCallsCountDBObj != null)
			{
				missedCallsCount = missedCallsCount - (tempMissedCalls - missedTempCallsCounter);
				
				if (missedCallsCount > 0)
				{
					notifyCallsCountDBObj.setValue(String.valueOf(missedCallsCount));
				}
				else
				{
					notifyCallsCountDBObj.setValue(String.valueOf(0));
				}
				
				sipgateDBAdapter.update(notifyCallsCountDBObj);
				
				if (missedCallsCount > 0) 
				{
					createNewCallNotification(missedCallsCount);
					Log.d(TAG, "new calls: " + missedCallsCount);
				}
				else 
				{
					removeNewCallNotification();
				}
			}
			else
			{
				notifyCallsCountDBObj = new SystemDataDBObject();
				
				notifyCallsCountDBObj.setKey(SystemDataDBObject.NOTIFY_CALLS_COUNT);
				notifyCallsCountDBObj.setValue(String.valueOf(0));
				
				sipgateDBAdapter.insert(notifyCallsCountDBObj);
				
				removeNewCallNotification();
			}
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyIfUnreadsCalls()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) 
			{
				sipgateDBAdapter.rollbackTransaction();
			}
			
			throw new StoreDataException();
		}
		
		return insertedCalls;
	}
	
	/**
	 * This function syncs the newly downloaded voice mail data into
	 * the database and creates a notification in the phones
	 * notification area if needed.
	 * 
	 * @param newVoiceMailDataDBObjects A Vector of the new voice mails.
	 * @param context The Application context.
	 * @param fullRefresh Indicator if newVoiceMailDataDBObjects contains a full or delta refresh
	 * @throws StoreDataException This exception is thrown when the data can not be stored in the database.
	 * @return The number of newly added voice mails.
	 * @since 1.0
	 */
	public int notifyIfNewVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Context context, boolean fullRefresh) throws StoreDataException 
	{
		Log.d(TAG, "notifyIfUnreadVoiceMails");
		
		if (newVoiceMailDataDBObjects == null)
		{
			Log.i(TAG, "notifyIfUnreadVoiceMails() -> voiceMailDataDBObjects is null");
			return -1;
		}
		
		deletedVoiceMails = 0;
		insertedVoiceMails = 0;
		updatedVoiceMails = 0;
		
		newVoiceMailsCounter = 0;
		
		try 
		{
			if (sipgateDBAdapter == null) 
			{
				sipgateDBAdapter = new SipgateDBAdapter(context);
			}
			
			SystemDataDBObject systemDataDBObject = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_VOICEMAILS_COUNT);
						
			Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects = sipgateDBAdapter.getAllVoiceMailData();
			
			sipgateDBAdapter.startTransaction();
			
			if (fullRefresh) 
			{
				deleteOldVoiceMails(newVoiceMailDataDBObjects, oldVoiceMailDataDBObjects, sipgateDBAdapter);
			}
			
			updateVoiceMails(newVoiceMailDataDBObjects, oldVoiceMailDataDBObjects, sipgateDBAdapter);
			
			Log.d(TAG, "VoiceMailDataDBObject deleted: " + deletedVoiceMails);
			Log.d(TAG, "VoiceMailDataDBObject inserted: " + insertedVoiceMails);
			Log.d(TAG, "VoiceMailDataDBObject updated: " + updatedVoiceMails);
											
			sipgateDBAdapter.commitTransaction();
			
			if (systemDataDBObject != null)
			{
				newVoiceMailsCounter = newVoiceMailsCounter + Integer.parseInt(systemDataDBObject.getValue());
				
				systemDataDBObject.setValue(String.valueOf(newVoiceMailsCounter));
				
				sipgateDBAdapter.update(systemDataDBObject);
				
				if (newVoiceMailsCounter > 0) 
				{
					createNewVoiceMailNotification(newVoiceMailsCounter);
					Log.d(TAG, "new voicemails: " + newVoiceMailsCounter);
				}
				else 
				{
					removeNewVoiceMailNotification();
				}
			}
			else
			{
				systemDataDBObject = new SystemDataDBObject();
				
				systemDataDBObject.setKey(SystemDataDBObject.NOTIFY_VOICEMAILS_COUNT);
				systemDataDBObject.setValue(String.valueOf(0));
				
				sipgateDBAdapter.insert(systemDataDBObject);
				
				removeNewVoiceMailNotification();
			}
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyIfUnreadsVoiceMails()", e);
			
			if (sipgateDBAdapter != null && sipgateDBAdapter.inTransaction()) 
			{
				sipgateDBAdapter.rollbackTransaction();
			}
			
			throw new StoreDataException();
		}
			
		return insertedVoiceMails;
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
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.toString());
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
		
		notifyFrontend(contactListener, ACTION_GETEVENTS);
		
		try {
			try {
				Thread.sleep(2000);
			}
			catch (Exception threadException) {
				threadException.printStackTrace();
			}
			finally {
				if (notifyIfNewContacts(apiClient.getContacts(), this) > 0 ) {
					notifyFrontend(contactListener, ACTION_NEWEVENTS);
				}
				else {
					notifyFrontend(contactListener, ACTION_NOEVENTS);
				}
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
				notifyFrontend(contactListener, ACTION_ERROR);
			}
		}
		
		Log.v(TAG, "refreshContactEvents() -> finish");
	}
	
	/**
	 * This function loads the last 100 (2.0, full) / 3 months worth of (1.0, full) or the last calls in a period (delta)
	 * from the api and syncs them into the database.
	 * 
	 * @since 1.1
	 */
	private void refreshCallEvents() 
	{		
		Log.v(TAG, "refreshCallEvents() -> start");
		
		currentRefreshCalls = System.currentTimeMillis();
		
		notifyFrontend(callListener, ACTION_GETEVENTS);
		
		try 
		{
			try 
			{
				Thread.sleep(2000);
			}
			catch (Exception threadException) 
			{
				threadException.printStackTrace();
			}
			finally 
			{
				if (lastFullRefreshCalls == 0 || lastFullRefreshCalls + Constants.ONE_DAY_IN_MS < currentRefreshCalls ||
					lastFullRefreshCalls + Constants.ONE_DAY_IN_MS < currentRefreshCalls + settingsClient.getEventsRefreshTime()) 
				{
					calls = apiClient.getCalls();
					
					if (calls.size() > 0)
					{
						lastFullRefreshCalls = currentRefreshCalls;
					}
				}
				else 
				{
					calls = apiClient.getCalls(lastRefreshCalls-Constants.ONE_MIN_IN_MS, currentRefreshCalls+Constants.ONE_DAY_IN_MS);
				}
				
				if (calls.size() > 0)
				{
					lastRefreshCalls = currentRefreshCalls;
				}
				
				if (notifyIfNewCalls(calls, this, (lastFullRefreshCalls == currentRefreshCalls)) > 0 ) 
				{
					notifyFrontend(callListener, ACTION_NEWEVENTS);
				}
				else 
				{
					notifyFrontend(callListener, ACTION_NOEVENTS);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			try 
			{
				Thread.sleep(2000);
			}
			catch (Exception threadException) 
			{
				threadException.printStackTrace();
			}
			finally 
			{
				notifyFrontend(callListener, ACTION_ERROR);
			}
		}
		
		Log.v(TAG, "refreshCallEvents() -> finish");
	}
	
	/**
	 * This function loads the last 100 voice mails (full) or the new voice mails (delta) in a period and
	 * syncs them into the database.
	 * 
	 * @since 1.1
	 */
	private void refreshVoicemailEvents() 
	{
		Log.v(TAG, "refreshVoicemailEvents() -> start");

		currentRefreshVoiceMails = System.currentTimeMillis();
		
		notifyFrontend(voiceMailListener, ACTION_GETEVENTS);
		
		try 
		{
			try 
			{
				Thread.sleep(2000);
			}
			catch (Exception threadException) 
			{
				threadException.printStackTrace();
			}
			finally 
			{
				if (lastFullRefreshVoiceMails == 0 || lastFullRefreshVoiceMails + Constants.ONE_DAY_IN_MS < currentRefreshVoiceMails ||
					lastFullRefreshVoiceMails + Constants.ONE_DAY_IN_MS < currentRefreshVoiceMails + settingsClient.getEventsRefreshTime()) 
				{	
					voiceMails = apiClient.getVoiceMails();
					
					if (voiceMails.size() > 0)
					{
						lastFullRefreshVoiceMails = currentRefreshVoiceMails; 
					}
				}
				else 
				{
					voiceMails = apiClient.getVoiceMails(lastRefreshVoiceMails-Constants.ONE_MIN_IN_MS, currentRefreshVoiceMails+Constants.ONE_DAY_IN_MS);
				}
				
				if (voiceMails.size() > 0)
				{
					lastRefreshVoiceMails = currentRefreshVoiceMails;
				}
								
				if (notifyIfNewVoiceMails(voiceMails, this, (lastFullRefreshVoiceMails == currentRefreshVoiceMails)) > 0 ) 
				{
					notifyFrontend(voiceMailListener, ACTION_NEWEVENTS);
				}
				else 
				{
					notifyFrontend(voiceMailListener, ACTION_NOEVENTS);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			try 
			{
				Thread.sleep(2000);
			}
			catch (Exception threadException) 
			{
				threadException.printStackTrace();
			}
			finally 
			{
				notifyFrontend(voiceMailListener, ACTION_ERROR);
			}
		}
						
		Log.v(TAG, "refreshVoicemailEvents() -> finish");
	}
	
	/**
	 * This function send a notification about the services status
	 * to the subscribed frontend processes.
	 * 
	 * @param listener The hash map containing the listener callback intents.
	 * @param action The action the frontend is supposed to show to the user.
	 * @since 1.2
	 */
	private void notifyFrontend(HashMap<String, HashMap<String, PendingIntent>> listener, String action)
	{
		try {
			for(String key : listener.keySet()) {
				pendingIntent = null;
				pendingIntent = listener.get(key).get(action);
				
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
	 * This function deletes all old contacts from the sipgate database and from android adressbook
	 * 
	 * @param newContactDataDBObjects The vector containing the new contacts.
	 * @param oldContactDataDBObjects The vector containing the old contacts.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void deleteOldContacts(Vector<ContactDataDBObject> newContactDataDBObjects, Vector<ContactDataDBObject> oldContactDataDBObjects, SipgateDBAdapter sipgateDBAdapter)
	{
		for (ContactDataDBObject oldContactDataDBObject : oldContactDataDBObjects) {
			if (!newContactDataDBObjects.contains(oldContactDataDBObject)) {
				sipgateDBAdapter.delete(oldContactDataDBObject);
				deletedContacts++;
			}
		}
	}
	
	/**
	 * This function deletes all old calls from the database.
	 * 
	 * @param newCallDataDBObjects The vector containing the new calls.
	 * @param oldCallDataDBObjects The vector containing the old calls.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void deleteOldCalls(Vector<CallDataDBObject> newCallDataDBObjects, Vector<CallDataDBObject> oldCallDataDBObjects, SipgateDBAdapter sipgateDBAdapter)
	{
		for (CallDataDBObject oldCallDataDBObject : oldCallDataDBObjects) {
			if (!newCallDataDBObjects.contains(oldCallDataDBObject)) {
				sipgateDBAdapter.delete(oldCallDataDBObject);
				deletedCalls++;
			}
		}
	}
	
	/**
	 * This function deletes all old voice mails from the database.
	 * 
	 * @param newVoiceMailDataDBObjects The vector containing the new voice mails.
	 * @param oldVoiceMailDataDBObjects The vector containing the old voice mails.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void deleteOldVoiceMails(Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects, Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects, SipgateDBAdapter sipgateDBAdapter)
	{
		for (VoiceMailDataDBObject oldVoiceMailDataDBObject : oldVoiceMailDataDBObjects) {
			if (!newVoiceMailDataDBObjects.contains(oldVoiceMailDataDBObject)) {
				sipgateDBAdapter.delete(oldVoiceMailDataDBObject);
				deletedVoiceMails++;
			}
		}
	}
	
	/**
	 * This functions inserts new and updates already existing contacts
	 * in the database.
	 * 
	 * @param newContactDataDBObjects The vector containing the new calls.
	 * @param oldContactDataDBObjects The vector containing the old calls.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void updateContacts(
			Vector<ContactDataDBObject> newContactDataDBObjects,
			Vector<ContactDataDBObject> oldContactDataDBObjects,
			SipgateDBAdapter sipgateDBAdapter)
	{
		for (ContactDataDBObject newContactDataDBObject : newContactDataDBObjects) {
			if (oldContactDataDBObjects.contains(newContactDataDBObject)) {	
				sipgateDBAdapter.update(newContactDataDBObject);
				
				sipgateDBAdapter.deleteAllContactNumberDBObjectsByUuid(newContactDataDBObject.getUuid());
				sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
				
				updatedContacts++;
			}
			else {
				sipgateDBAdapter.insert(newContactDataDBObject);
				sipgateDBAdapter.insertAllContactNumberDBObjects(newContactDataDBObject.getContactNumberDBObjects());
				
				insertedContacts++;
			}
		}
	}

	/**
	 * This functions inserts new and updates already existing calls
	 * in the database.
	 * 
	 * @param newCallDataDBObjects The vector containing the new calls.
	 * @param oldCallDataDBObjects The vector containing the old calls.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void updateCalls(
			Vector<CallDataDBObject> newCallDataDBObjects,
			Vector<CallDataDBObject> oldCallDataDBObjects,
			SipgateDBAdapter sipgateDBAdapter)
	{
		for (CallDataDBObject newCallDataDBObject : newCallDataDBObjects) {
			if (oldCallDataDBObjects.contains(newCallDataDBObject)) {	
				oldCallDataDBObject = oldCallDataDBObjects.elementAt(oldCallDataDBObjects.indexOf(newCallDataDBObject));
									
				if (newCallDataDBObject.getRead() == -1) {
					newCallDataDBObject.setRead(oldCallDataDBObject.getRead());
				}
				
				sipgateDBAdapter.update(newCallDataDBObject);
				
				updatedCalls++;
			}
			else {
				sipgateDBAdapter.insert(newCallDataDBObject);
				
				if (!newCallDataDBObject.isRead() && newCallDataDBObject.isMissed() && newCallDataDBObject.getDirection() == CallDataDBObject.INCOMING) {
					missedCallsCount++;
				}
				
				insertedCalls++;
			}
		}
	}
	
	/**
	 * This functions inserts new and updates already existing voice mails
	 * in the database.
	 * 
	 * @param newVoiceMailDataDBObjects The vector containing the new voice mails.
	 * @param oldVoiceMailDataDBObjects The vector containing the old voice mails.
	 * @param sipgateDBAdapter An instance of the sipgate database adapter.
	 * @since 1.2
	 */
	private void updateVoiceMails(
			Vector<VoiceMailDataDBObject> newVoiceMailDataDBObjects,
			Vector<VoiceMailDataDBObject> oldVoiceMailDataDBObjects,
			SipgateDBAdapter sipgateDBAdapter)
	{
		for (VoiceMailDataDBObject newVoiceMailDataDBObject : newVoiceMailDataDBObjects) {
			if (oldVoiceMailDataDBObjects.contains(newVoiceMailDataDBObject)) {	
				oldVoiceMailDataDBObject = oldVoiceMailDataDBObjects.elementAt(oldVoiceMailDataDBObjects.indexOf(newVoiceMailDataDBObject));
									
				newVoiceMailDataDBObject.setSeen(oldVoiceMailDataDBObject.getSeen());
				
				sipgateDBAdapter.update(newVoiceMailDataDBObject);

				updatedVoiceMails++;
			}
			else {
				sipgateDBAdapter.insert(newVoiceMailDataDBObject);
				
				if (!newVoiceMailDataDBObject.isRead()) {
					newVoiceMailsCounter++;
				}
				
				insertedVoiceMails++;
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
			notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.sipgate_call_list_icon_notification, buildCallNotificationString(unreadCounter));
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
			notifyClient.setNotification(NotificationClient.NotificationType.VOICEMAIL, R.drawable.sipgate_voice_mail_list_icon_notification, buildVoicemailNotificationString(unreadCounter));
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
