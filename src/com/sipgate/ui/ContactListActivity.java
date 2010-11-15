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

import com.sipgate.R;
import com.sipgate.adapters.ContactListAdapter;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;

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
	private PendingIntent onContactPendingIntent = null;
	
	private Context context = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_contacts_list);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateContactsListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateContactsListRefreshView);
		elementList = (ListView) findViewById(R.id.ContactsListView);
		emptyList = (TextView) findViewById(R.id.EmptyContactListTextView);

		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		animationThread = new Thread() {
			public void run() {
				frameAnimation.start();
			}
		};
		
		context = getApplicationContext();
		
		contactListAdapter = new ContactListAdapter(this);
        
        elementList.setAdapter(contactListAdapter);
        elementList.setOnItemClickListener(this);
    }
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		startScanActivity();		
		
		contactListAdapter.notifyDataSetChanged();
		
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
	
	@Override
	protected void onPause()
	{
		super.onPause();
	
		stopScanActivity();
	}
		
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		stopScanActivity();
	}
	
	private void startScanActivity()
	{
		Intent intent = new Intent(this, SipgateBackgroundService.class);
		context.startService(intent);

		if (serviceConnection == null) 
		{
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
					
					try 
					{
						serviceBinding = (EventService) binder;
						
						try 
						{
							Log.d(TAG, "service binding -> registerOnContactsIntent");
							serviceBinding.registerOnContactsIntent(getNewMessagesIntent());
						} 
						catch (RemoteException e) 
						{
							e.printStackTrace();
						}
					} 
					catch (ClassCastException e) 
					{
						e.printStackTrace();
					}
				}
			};
			
			boolean bindret = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		} 
		else 
		{
			Log.d(TAG, "service connection is not null -> already running");
		}
	}
	
	public void stopScanActivity()
	{
		if (serviceConnection != null) 
		{
			try 
			{
				if (serviceBinding != null) 
				{
					Log.d(TAG, "service unbinding -> unregisterOnContactsIntent");
					serviceBinding.unregisterOnContactsIntent(getNewMessagesIntent());
				}
			} 
			catch (RemoteException e) 
			{
				e.printStackTrace();
			}

			Log.v(TAG, "unbind service");
			context.unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	private PendingIntent getNewMessagesIntent() 
	{
		if (onContactPendingIntent == null) 
		{
			Intent onChangedIntent = new Intent(this, SipgateFramesContacts.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_CALLS_NEW);
			onContactPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		
		return onContactPendingIntent;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"ContactList");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, getApplicationContext(), this);

		return result;
	}
		
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) 
	{
		ContactDataDBObject contactDataDBObject = (ContactDataDBObject) parent.getItemAtPosition(position);
		
		Intent intent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
		intent.putExtra("uuid", contactDataDBObject.getUuid());
		startActivity(intent);
	}
}