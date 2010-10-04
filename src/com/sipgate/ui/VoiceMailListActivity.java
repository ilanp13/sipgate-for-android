package com.sipgate.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.Constants;
import com.sipgate.util.MediaConnector;

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
	
	private DownloadVoiceMailTask downloadVoiceMailTask = null;
	
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

	private PendingIntent getNewMessagesIntent() 
	{
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
	
	private void markAsRead(final VoiceMailDataDBObject voiceMailDataDBObject) 
	{
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		VoiceMailDataDBObject voiceMailDataDBObject = (VoiceMailDataDBObject) parent.getItemAtPosition(position);
	
		new DownloadVoiceMailTask().execute(voiceMailDataDBObject);
	}
	
	private class DownloadVoiceMailTask extends AsyncTask <VoiceMailDataDBObject, Void, VoiceMailDataDBObject>
	{
		private InputStream inputStream = null;
		private FileOutputStream fileOutputStream = null; 
		private ProgressDialog waitDialog = null;
		
		protected VoiceMailDataDBObject doInBackground(VoiceMailDataDBObject... voiceMailDataDBObjects)
		{
			VoiceMailDataDBObject voiceMailDataDBObject = voiceMailDataDBObjects[0];
			
			String localURL = voiceMailDataDBObject.getLocalFileUrl();
			
			if (localURL.length() > 0) {
				File file = new File(localURL);
				
				if (file.exists()) {
					return voiceMailDataDBObject;
				}
			}
			
			localURL = Constants.MP3_DOWNLOAD_DIR + "/" + voiceMailDataDBObject.getId() + ".mp3";
			
			try {
				Log.d(TAG, voiceMailDataDBObject.getContentUrl());
				
				inputStream = apiClient.getVoicemail(voiceMailDataDBObject.getContentUrl());
				
				if (inputStream == null) {
					throw new RuntimeException("voicemail inputstream is null");
				}

				fileOutputStream = new FileOutputStream(localURL);
				
				byte chunk[] = new byte[1024];
				
				int length = 0;
				
				while ((length = inputStream.read(chunk)) > 0) {
					fileOutputStream.write(chunk, 0, length);
				}
				
				voiceMailDataDBObject.setLocalFileUrl(localURL);
				
				sipgateDBAdapter = new SipgateDBAdapter(activity);
				sipgateDBAdapter.update(voiceMailDataDBObject);
			}					
			catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
			finally {
				try {
					fileOutputStream.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
				
				try {
					inputStream.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			
				if (sipgateDBAdapter != null) {
					sipgateDBAdapter.close();
				}
			}
			
			File file = new File(localURL);
			
			if (file.exists()) {
				return voiceMailDataDBObject;
			}
			else
			{
				return null;
			}
		} 
		
		@Override
		protected void onPostExecute(VoiceMailDataDBObject voiceMailDataDBObject)
		{
			waitDialog.dismiss();
			
			if (mediaConnector.isPlaying()) {
				mediaConnector.pause();
			}
			
			mediaConnector.setMp3(voiceMailDataDBObject.getLocalFileUrl());
			mediaConnector.start();
			mediaController.show(0);
			
			markAsRead(voiceMailDataDBObject);
		}
		
		@Override
		protected void onPreExecute()
		{
			waitDialog = ProgressDialog.show(activity, "", getResources().getString(R.string.sipgate_loading), true);
		}
	}
}