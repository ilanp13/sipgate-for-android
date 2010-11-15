package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
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
import com.sipgate.adapters.CallListAdapter;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.util.SipgateApplication;

public class CallListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "CallListActivity";
	
	private CallListAdapter callListAdapter = null;
	private AlertDialog m_AlertDlg = null;
	
	private ImageView refreshSpinner = null;
	private LinearLayout refreshView = null;
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private AnimationDrawable frameAnimation = null;
	private Thread animationThread = null;
	private boolean isAnimationRunning = false;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	
	private PendingIntent onNewCallsPendingIntent = null;
	private PendingIntent onNoCallsPendingIntent = null;
	private PendingIntent onGetCallsPendingIntent = null;
	private PendingIntent onErrorPendingIntent = null;
	
	private Context context = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_call_list);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateCallListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateCallListRefreshView);
		elementList = (ListView) findViewById(R.id.CalllistListView);
		emptyList = (TextView) findViewById(R.id.EmptyCallListTextView);
		
		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		animationThread = new Thread(new Runnable() {
			public void run() {
				frameAnimation.start();
			}
		});
		
		context = getApplicationContext();
		
		callListAdapter = new CallListAdapter(this);
        
        elementList.setAdapter(callListAdapter);
        elementList.setOnItemClickListener(this);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SipgateApplication.RefreshState refreshState = ((SipgateApplication) getApplication()).getRefreshState();
		
		switch (refreshState) {
			case NEW_EVENTS: 
				refreshView.setVisibility(View.GONE);
				showNewEntriesToast();
				break;
			case NO_EVENTS: 
				refreshView.setVisibility(View.GONE);
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
		
		registerForBackgroundIntents();		
		
		callListAdapter.notifyDataSetChanged();
		
		if (callListAdapter.isEmpty()) {
			elementList.setVisibility(View.GONE);
			emptyList.setVisibility(View.VISIBLE);
		} else {
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
	
	private void registerForBackgroundIntents()
	{
		Intent intent = new Intent(this, SipgateBackgroundService.class);
		context.startService(intent);

		if (serviceConnection == null) {
			Log.d(TAG, "service connection is null -> create new");
			
			serviceConnection = new ServiceConnection() {

				public void onServiceDisconnected(ComponentName name) {
					Log.d(TAG, "service " + name + " disconnected -> clear binding");
					serviceBinding = null;
				}

				public void onServiceConnected(ComponentName name, IBinder binder) {
					Log.v(TAG, "service " + name + " connected -> bind");
					try {
						serviceBinding = (EventService) binder;
						try {
							Log.d(TAG, "service binding -> registerOnCallsIntent");
							serviceBinding.registerOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_NEW, newCallsIntent());
							serviceBinding.registerOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_NO, noCallsIntent());
							serviceBinding.registerOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_GET, getCallsIntent());
							serviceBinding.registerOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_ERROR, errorIntent());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
				}
			};
			
			boolean bindret = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.v(TAG, "bind service -> " + bindret);
		} else {
			Log.d(TAG, "service connection is not null -> already running");
		}
	}
	
	public void stopScanActivity()
	{		
		if (serviceConnection != null) {
			try {
				if (serviceBinding != null) {
					Log.d(TAG, "service unbinding -> unregisterOnCallsIntent");
					serviceBinding.unregisterOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_NEW);
					serviceBinding.unregisterOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_NO);
					serviceBinding.unregisterOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_GET);
					serviceBinding.unregisterOnCallsIntents(SipgateBackgroundService.ACTION_CALLS_ERROR);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.v(TAG, "unbind service");
			context.unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	private PendingIntent newCallsIntent() {
		if (onNewCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_CALLS_NEW);
			onNewCallsPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewCallsPendingIntent;
	}
	
	private PendingIntent noCallsIntent() {
		if (onNoCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_CALLS_NO);
			onNoCallsPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNoCallsPendingIntent;
	}
	
	private PendingIntent getCallsIntent() {
		if (onGetCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_CALLS_GET);
			onGetCallsPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onGetCallsPendingIntent;
	}
	
	private PendingIntent errorIntent() {
		if (onErrorPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_CALLS_ERROR);
			onErrorPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onErrorPendingIntent;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"CallList");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
	
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
	
	private void call_menu(final String target)
	{
		if (m_AlertDlg != null) 
		{
			m_AlertDlg.cancel();
		}
		
		if (target.length() == 0)
		{
			m_AlertDlg = new AlertDialog.Builder(this)
		
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		}
		else if (!Receiver.engine(this).call(target))
		{
			m_AlertDlg = new AlertDialog.Builder(this)
			.setMessage(R.string.notfast)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon22)
			.setCancelable(false)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
	        {
	           public void onClick(DialogInterface dialog, int id) 
	           {
	        		Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", Uri.decode(target), null));
		   		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   		    startActivity(intent);
	           }
	        })
	        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() 
	        {
	           public void onClick(DialogInterface dialog, int id) 
	           {
	                dialog.cancel();
	           }
	        })
			.show();		
		}
	}	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) 
	{
		CallDataDBObject callDataDBObject = (CallDataDBObject) parent.getItemAtPosition(position);
		call_menu(callDataDBObject.getRemoteNumberE164().replaceAll("tel:", "").replaceAll("dd:", ""));
	}
}