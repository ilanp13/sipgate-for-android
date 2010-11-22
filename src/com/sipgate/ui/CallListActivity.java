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

/**
 * This class represents the call list activity and implements all
 * it's functions.
 * 
 * @author Karsten Knuth
 * @version 1.2
 */
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
		
		setContentView(R.layout.sipgate_call_list);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateCallListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateCallListRefreshView);
		elementList = (ListView) findViewById(R.id.CalllistListView);
		emptyList = (TextView) findViewById(R.id.EmptyCallListTextView);
		
		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		animationThread = new Thread(new Runnable()
		{
			public void run()
			{
				frameAnimation.start();
			}
		});
		
		context = getApplicationContext();
		
		callListAdapter = new CallListAdapter(this);
        
        elementList.setAdapter(callListAdapter);
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
				callListAdapter.notifyDataSetChanged();
				showNewEntriesToast();
				break;
			case NO_EVENTS: 
				refreshView.setVisibility(View.GONE);
				callListAdapter.notifyDataSetChanged();
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
		m.createMenu(menu,"CallList");
		
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
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
	
	/**
	 * This function is called when an item in the call list was clicked.
	 * 
	 * @param parent The View containing the clicked item.
	 * @param view ?
	 * @param position The position of the clicked item in the list.
	 * @param id The id of the clicked item.
	 * @since 1.0
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		CallDataDBObject callDataDBObject = (CallDataDBObject) parent.getItemAtPosition(position);
		callTarget(callDataDBObject.getRemoteNumberE164().replaceAll("tel:", "").replaceAll("dd:", ""));
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
							Log.d(TAG, "service binding -> registerOnCallsIntent");
							serviceBinding.registerOnCallsIntents(TAG, getCallsIntent(), newCallsIntent(), noCallsIntent(), errorIntent());
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
					Log.d(TAG, "service unbinding -> unregisterOnCallsIntent");
					serviceBinding.unregisterOnCallsIntents(TAG);
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
	 * that the download of new calls just has started.
	 * 
	 * @return The callback intent for starting to download calls.
	 * @since 1.2
	 */
	private PendingIntent getCallsIntent() {
		if (onGetCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_GETEVENTS);
			onGetCallsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onGetCallsPendingIntent;
	}

	/**
	 * This functions returns a callback intent for the callback
	 * that new calls have been downloaded.
	 * 
	 * @return The callback intent for new calls.
	 * @since 1.2
	 */
	private PendingIntent newCallsIntent() {
		if (onNewCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewCallsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewCallsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that no new calls have been downloaded.
	 * 
	 * @return The callback intent for no new calls.
	 * @since 1.2
	 */
	private PendingIntent noCallsIntent() {
		if (onNoCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NOEVENTS);
			onNoCallsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNoCallsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that an error occurred during the download of new calls.
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
	
	/**
	 * This function starts a call with the provided phone number.
	 * 
	 * @param target The phone number of the person to be called.
	 * @since 1.0
	 */
	private void callTarget(final String target)
	{
		if (m_AlertDlg != null) {
			m_AlertDlg.cancel();
		}
		
		if (target.length() == 0) {
			m_AlertDlg = new AlertDialog.Builder(this)
		
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		}
		else if (!Receiver.engine(this).call(target)) {
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
}