package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.adapters.CallListAdapter;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;

public class CallListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "CallListActivity";
	
	private CallListAdapter callListAdapter = null;
	private AlertDialog m_AlertDlg = null;
	
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	private PendingIntent onNewCallPendingIntent = null;
	
	private Context appContext = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_call_list);
		
		elementList = (ListView) findViewById(R.id.CalllistListView);
		emptyList = (TextView) findViewById(R.id.EmptyCallListTextView);

		appContext = getApplicationContext();
		
		callListAdapter = new CallListAdapter(this);
        
        elementList.setAdapter(callListAdapter);
        elementList.setOnItemClickListener(this);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		startScanActivity();		
		
		callListAdapter.notifyDataSetChanged();
		
		if (callListAdapter.isEmpty()) {
			elementList.setVisibility(View.GONE);
			emptyList.setVisibility(View.VISIBLE);
		} else {
			elementList.setVisibility(View.VISIBLE);
			emptyList.setVisibility(View.GONE);
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
		appContext.startService(intent);

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
							serviceBinding.registerOnCallsIntent(getNewMessagesIntent());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
				}
			};
			
			boolean bindret = appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
					serviceBinding.unregisterOnCallsIntent(getNewMessagesIntent());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.v(TAG, "unbind service");
			appContext.unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	private PendingIntent getNewMessagesIntent() {
		if (onNewCallPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFramesCalls.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewCallPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewCallPendingIntent;
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