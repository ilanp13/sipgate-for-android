package com.sipgate.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sipgate.R;
import com.sipgate.adapters.ContactListAdapter;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.SipgateApplication;

/**
 * This class represents the contact list activity and implements all
 * it's functions.
 * 
 * @author graef
 * @author Karsten Knuth
 * @version 1.2
 */
public class ContactListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "ContactListActivity";
	
	private ContactListAdapter contactListAdapter = null;
	
	private ImageView refreshSpinner = null;
	private LinearLayout refreshView = null;
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private AnimationDrawable frameAnimation = null;
	private Thread animationThread = null;
	private boolean isAnimationRunning = false;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	
	private PendingIntent onNewContactsPendingIntent = null;
	private PendingIntent onNoContactsPendingIntent = null;
	private PendingIntent onGetContactsPendingIntent = null;
	private PendingIntent onErrorPendingIntent = null;
	
	private SipgateApplication application = null;
	private SipgateApplication.RefreshState refreshState = SipgateApplication.RefreshState.NONE;
	
	private Context context = null;
	
	/**
	 * This function is called right after the class is started by an intent.
	 * 
	 * @param bundle The bundle which caused the activity to be started.
	 * @since 1.0
	 */
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_contacts_list);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateContactsListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateContactsListRefreshView);
		elementList = (ListView) findViewById(R.id.ContactsListView);
		emptyList = (TextView) findViewById(R.id.EmptyContactListTextView);

		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		animationThread = new Thread()
		{
			public void run()
			{
				frameAnimation.start();
			}
		};
		
		context = getApplicationContext();
		
		contactListAdapter = new ContactListAdapter(this);
        
        elementList.setAdapter(contactListAdapter);
        elementList.setOnItemClickListener(this);
        
        application = (SipgateApplication) getApplication();
    }
	
	/**
	 * This function is called every time the activity comes back to the foreground.
	 * 
	 * @since 1.0
	 */
	public void onResume() 
	{
		super.onResume();
		
		registerForBackgroundIntents();
		
		refreshState = application.getRefreshState();
		application.setRefreshState(SipgateApplication.RefreshState.NONE);
		
		switch (refreshState) {
			case NEW_EVENTS: 
				refreshView.setVisibility(View.GONE);
				contactListAdapter.notifyDataSetChanged();
				showNewEntriesToast();
				break;
			case NO_EVENTS: 
				refreshView.setVisibility(View.GONE);
				contactListAdapter.notifyDataSetChanged();
				showNoEntriesToast();
				break;
			case GET_EVENTS: 
				refreshView.setVisibility(View.VISIBLE);
				break;
			case ERROR: 
				refreshView.setVisibility(View.GONE);
				showErrorToast();
				break;
			default:
				refreshView.setVisibility(View.GONE);
				break;
		}
		
		if (contactListAdapter.isEmpty()) 
		{
			elementList.setVisibility(View.GONE);
			emptyList.setVisibility(View.VISIBLE);
		} 
		else 
		{
			elementList.setVisibility(View.VISIBLE);
			emptyList.setVisibility(View.GONE);
		}	
		
		if(!isAnimationRunning) {
			animationThread.start();
			isAnimationRunning = true;
		}
	}
	
	/**
	 * This function is called every time the activity goes in to background.
	 * 
	 * @since 1.0
	 */
	public void onPause()
	{
		super.onPause();
	
		unregisterFromBackgroungIntents();
	}
		
	/**
	 * This function is called right before the activity is killed.
	 * 
	 * @since 1.0
	 */
	public void onDestroy()
	{
		super.onDestroy();
		
		unregisterFromBackgroungIntents();
	}
	
	/**
	 * This function is called every time the menu button is pressed.
	 * 
	 * @param menu The menu object to be used to create the menu.
	 * @since 1.0
	 */
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"ContactList");
		
		return result;
	}
	
	/**
	 * This function is called when an item was chosen from the menu.
	 * 
	 * @param item The item from the menu that was selected.
	 * @since 1.0
	 */
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, getApplicationContext(), this);

		return result;
	}
	
	/**
	 * This function is called when an item in the contact list was clicked.
	 * 
	 * @param parent The View containing the clicked item.
	 * @param view ?
	 * @param position The position of the clicked item in the list.
	 * @param id The id of the clicked item.
	 * @since 1.0
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		ContactDataDBObject contactDataDBObject = (ContactDataDBObject) parent.getItemAtPosition(position);
		
		Intent intent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
		intent.putExtra("uuid", contactDataDBObject.getUuid());
		startActivity(intent);
	}
	
	/**
	 * This function provides the background sevice with callback
	 * intent for several steps in the refresh cycle
	 * 
	 * @since 1.2
	 */
	private void registerForBackgroundIntents()
	{
		Intent intent = new Intent(this, SipgateBackgroundService.class);
		context.startService(intent);

		if (serviceConnection == null) {
			Log.d(TAG, "service connection is null -> create new");
			
			serviceConnection = new ServiceConnection() 
			{
				public void onServiceDisconnected(ComponentName name) 
				{
					Log.d(TAG, "service " + name + " disconnected -> clear binding");
					serviceBinding = null;
				}

				public void onServiceConnected(ComponentName name, IBinder binder) 
				{
					Log.v(TAG, "service " + name + " connected -> bind");
					
					try {
						serviceBinding = (EventService) binder;
						
						try {
							Log.d(TAG, "service binding -> registerOnContactsIntent");
							serviceBinding.registerOnContactsIntent(TAG, getContactsIntent(), newContactsIntent(), noContactsIntent(), errorIntent());
						} 
						catch (RemoteException e) {
							e.printStackTrace();
						}
					} 
					catch (ClassCastException e) {
						e.printStackTrace();
					}
				}
			};
			
			boolean bindret = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		} 
		else {
			Log.d(TAG, "service connection is not null -> already running");
		}
	}
	
	/**
	 * This function deletes the callback intents from the background
	 * service so the UI doesn't get any status updates anymore.
	 * 
	 * @since 1.2
	 */
	private void unregisterFromBackgroungIntents()
	{
		if (serviceConnection != null) {
			try {
				if (serviceBinding != null) {
					Log.d(TAG, "service unbinding -> unregisterOnContactsIntent");
					serviceBinding.unregisterOnContactsIntent(TAG);
				}
			} 
			catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.v(TAG, "unbind service");
			context.unbindService(serviceConnection);
			serviceConnection = null;
		}
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that the download of new contacts just has started.
	 * 
	 * @return The callback intent for starting to download contacts.
	 * @since 1.2
	 */
	private PendingIntent getContactsIntent() {
		if (onGetContactsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_GETEVENTS);
			onGetContactsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onGetContactsPendingIntent;
	}

	/**
	 * This functions returns a callback intent for the callback
	 * that new contacts have been downloaded.
	 * 
	 * @return The callback intent for new contacts.
	 * @since 1.2
	 */
	private PendingIntent newContactsIntent() 
	{
		if (onNewContactsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewContactsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		
		return onNewContactsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that no new contacts have been downloaded.
	 * 
	 * @return The callback intent for no new contacts.
	 * @since 1.2
	 */
	private PendingIntent noContactsIntent() {
		if (onNoContactsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NOEVENTS);
			onNoContactsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNoContactsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that an error occurred during the download of new contacts.
	 * 
	 * @return The callback intent for errors during the download.
	 * @since 1.2
	 */
	private PendingIntent errorIntent() {
		if (onErrorPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_ERROR);
			onErrorPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onErrorPendingIntent;
	}
	
	/**
	 * This functions starts a new thread that shows a toast with the
	 * "new entries" message.
	 * 
	 * @since 1.2
	 */
	private void showNewEntriesToast() {
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_new_entries), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}
	
	/**
	 * This functions starts a new thread that shows a toast with the
	 * "no new entries" message.
	 * 
	 * @since 1.2
	 */
	private void showNoEntriesToast() {
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_no_entries), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}
	
	/**
	 * This functions starts a new thread that shows a toast with the
	 * "error" message.
	 * 
	 * @since 1.2
	 */
	private void showErrorToast() {
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_api_error), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}
}