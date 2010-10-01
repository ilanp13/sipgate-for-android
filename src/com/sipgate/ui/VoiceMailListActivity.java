package com.sipgate.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.adapters.VoiceMailListAdapter;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.exceptions.DownloadException;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;

public class VoiceMailListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "VoiceMailListActivity";
	
	private VoiceMailListAdapter voiceMailListAdapter = null;
	
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	private PendingIntent onNewVoiceMailPendingIntent = null;
	
	private Context appContext = null;
	
	private MediaConnector mediaConnector = null;
	private MediaController mediaController = null;
	
	private ApiServiceProvider apiClient = null;
	private Activity activity = this;
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_voicemail_list);
		
		elementList = (ListView) findViewById(R.id.EventListView);
		emptyList = (TextView) findViewById(R.id.EmptyEventListTextView);

		appContext = getApplicationContext();
		apiClient = ApiServiceProvider.getInstance(activity);
		
		voiceMailListAdapter = new VoiceMailListAdapter(this);
        
        elementList.setAdapter(voiceMailListAdapter);
        elementList.setOnItemClickListener(this);  
        
        mediaConnector = new MediaConnector();
        
        mediaController = new MediaController(this);
		mediaController.setMediaPlayer(mediaConnector);
		mediaController.setEnabled(true);
		mediaController.setAnchorView(elementList.getRootView());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		startScanActivity();		
		
		voiceMailListAdapter.notifyDataSetChanged();
		
		if (voiceMailListAdapter.isEmpty()) {
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
							Log.d(TAG, "service binding -> registerOnVoiceMailsIntent");
							serviceBinding.registerOnVoiceMailsIntent(getNewMessagesIntent());
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
					Log.d(TAG, "service unbinding -> unregisterOnVoiceMailsIntent");
					serviceBinding.unregisterOnVoiceMailsIntent(getNewMessagesIntent());
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
		if (onNewVoiceMailPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFramesVoiceMails.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewVoiceMailPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewVoiceMailPendingIntent;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"VoiceMailList");
		
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
	
	protected void showPlayerDialog(VoiceMailDataDBObject voiceMailDataDBObject) {
		try {
			this.mediaConnector.pause();
			// TODO: change to async call
			this.mediaConnector.setMp3(MediaUrlPlayer.download(voiceMailDataDBObject,
					getApplicationContext()));
			this.mediaConnector.start();
			mediaController.show(0);
		} catch (DownloadException e) {
			e.printStackTrace();
		}
		markAsRead(voiceMailDataDBObject);
	}
	
	private void markAsRead(final VoiceMailDataDBObject voiceMailDataDBObject) {
		if (!voiceMailDataDBObject.isRead()) {
			Thread markThread = new Thread(){
				public void run()
				{
					sipgateDBAdapter = new SipgateDBAdapter(activity);
					
					voiceMailDataDBObject.setRead(true);
					sipgateDBAdapter.update(voiceMailDataDBObject);

					sipgateDBAdapter.close();
					
					try {
						apiClient.setVoiceMailRead(voiceMailDataDBObject.getReadModifyUrl());
					} 
					catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			
			markThread.start();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) 
	{
		VoiceMailDataDBObject voicemail = (VoiceMailDataDBObject) parent.getItemAtPosition(position);
		showPlayerDialog(voicemail);
	}
	
}