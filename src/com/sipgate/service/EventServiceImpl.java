package com.sipgate.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sipgate.R;
import com.sipgate.api.types.Event;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.ui.SipgateFramesMessage;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.Oauth;

public class EventServiceImpl extends Service implements EventService {
	
	private static final String TAG = "background service";
	private static final long EVENTREFRESH_INTERVAL = 30000;
	public static final String ACTION_NEWEVENTS = "action_newEvents";
	public static final String ACTION_START_ON_BOOT = "com.sipgate.service.EventServiceImpl";
	public static final int REQUEST_NEWEVENTS = 0;
	private boolean serviceEnabled = false;
	private Timer timer;
	private List<Event> events = new ArrayList<Event>();
	private Set<PendingIntent> onNewEventsTriggers = new HashSet<PendingIntent>();
	
	public void onCreate() {
		super.onCreate();
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
				refresh();
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
	
	@SuppressWarnings("unchecked")
	private void refresh() {
		if(!serviceEnabled) {
			Log.d(TAG,"serviceEnabled == false");
			return;
		}
		try {

			HashMap<String, String> params = new HashMap<String, String>();

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
		
		if (!oldList.equals(newList)) {
			for (PendingIntent pInt: onNewEventsTriggers){
				try {
					pInt.send();
				} catch (CanceledException e) {
					e.printStackTrace();
				}			
			}
		}
		
		Boolean hasUnreadEvents = false;
		
		for (Event e: newList){
			if (! e.isRead()){
				hasUnreadEvents = true;
				createNewMessagesNotification();
				break;
			}
		}
		if (!hasUnreadEvents) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	        notificationManager.cancelAll();
		}
	}
	

	  private void createNewMessagesNotification() {

          NotificationManager notificationManager =
              (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
          Notification notification = new Notification(R.drawable.statusbar_voicemai_48, getResources().getText(R.string.sipgate_a_new_voicemail), 0 );
          notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
//          notification.flags |= Notification.FLAG_AUTO_CANCEL;

          Intent notificationIntent = new Intent(this, SipgateFramesMessage.class);
          
          Log.d("createNewMessagesNotification","Executed");
          PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
          
          notification.setLatestEventInfo(this, getResources().getText(R.string.sipgate), getResources().getText(R.string.sipgate_a_new_voicemail), contentIntent);
          notificationManager.notify(0, notification);
          Log.d(TAG,"send notification");
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
		};
	}

}
