package com.sipgate.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.adapters.VoiceMailListAdapter;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.db.VoiceMailFileDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.MediaConnector;

public class VoiceMailListActivity extends Activity implements OnItemClickListener
{
	private static final String TAG = "VoiceMailListActivity";
	
	private VoiceMailListAdapter voiceMailListAdapter = null;
	
	private ImageView refreshSpinner = null;
	private LinearLayout refreshView = null;
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private AnimationDrawable frameAnimation = null;
	private Thread animationThread = null;
	private boolean isAnimationRunning = false;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;
	private PendingIntent onNewVoiceMailPendingIntent = null;
	
	private Context appContext = null;
	
	private MediaConnector mediaConnector = null;
	private MediaController mediaController = null;
	
	private ApiServiceProvider apiClient = null;
	private Activity activity = this;
	
	private SipgateDBAdapter sipgateDBAdapter = null;

	private String loadingString = null;
	private String failedString = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_voicemail_list);
		
		sipgateDBAdapter = SipgateDBAdapter.getInstance(this);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateVoiceMailListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateVoiceMailListRefreshView);
		elementList = (ListView) findViewById(R.id.EventListView);
		emptyList = (TextView) findViewById(R.id.EmptyEventListTextView);

		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		animationThread = new Thread() {
			public void run() {
				frameAnimation.start();
			}
		};
		
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
		
		loadingString = getResources().getString(R.string.sipgate_loading);
		failedString = getResources().getString(R.string.sipgate_download_failed);
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
					try 
					{
						apiClient.setVoiceMailRead(voiceMailDataDBObject.getReadModifyUrl());
					
						voiceMailDataDBObject.setRead(true);
						
						sipgateDBAdapter.update(voiceMailDataDBObject);
					} 
					catch (Exception e)
					{
						Log.e(TAG, "markAsRead()", e);
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
		
		markAsRead(voiceMailDataDBObject);
	}
	
	private class DownloadVoiceMailTask extends AsyncTask <VoiceMailDataDBObject, Void, VoiceMailFileDBObject>
	{
		private InputStream inputStream = null;
		private ByteArrayOutputStream byteArrayOutputStream = null; 
		private ProgressDialog waitDialog = null;
		
		protected VoiceMailFileDBObject doInBackground(VoiceMailDataDBObject... voiceMailDataDBObjects)
		{
			VoiceMailDataDBObject voiceMailDataDBObject = voiceMailDataDBObjects[0];
			VoiceMailFileDBObject voiceMailFileDBObject = null;
			
			try 
			{
				voiceMailFileDBObject = sipgateDBAdapter.getVoiceMailFileDBObjectById(voiceMailDataDBObject.getId());
				
				if (voiceMailFileDBObject == null)
				{
					inputStream = apiClient.getVoicemail(voiceMailDataDBObject.getContentUrl());
					
					if (inputStream == null) 
					{
						throw new RuntimeException("voicemail inputstream is null");
					}
		
					byteArrayOutputStream = new ByteArrayOutputStream();
					
					byte chunk[] = new byte[1024];
					
					int length = 0;
					
					while ((length = inputStream.read(chunk)) > 0) 
					{
						byteArrayOutputStream.write(chunk, 0, length);
					}
					
					voiceMailFileDBObject = new VoiceMailFileDBObject();
					
					voiceMailFileDBObject.setId(voiceMailDataDBObject.getId());
					voiceMailFileDBObject.setValue(byteArrayOutputStream.toByteArray());
					
					sipgateDBAdapter.insert(voiceMailFileDBObject);
				}
			}					
			catch (Exception e) 
			{
				Log.e(TAG, e.getLocalizedMessage(), e);
			
				if (waitDialog.isShowing())
				{
					waitDialog.dismiss();
				}
			}
			finally 
			{
				try 
				{
					if (byteArrayOutputStream != null) 
					{
						byteArrayOutputStream.close();
					}
				}
				catch (IOException e) 
				{
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
				
				try 
				{
					if (inputStream != null) 
					{
						inputStream.close();
					}
				}
				catch (IOException e) 
				{
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			}
			
			return voiceMailFileDBObject;
		} 
		
		@Override
		protected void onPostExecute(VoiceMailFileDBObject voiceMailFileDBObject)
		{
			if (waitDialog.isShowing())
			{
				waitDialog.dismiss();
			}
			
			if (mediaConnector.isPlaying()) {
				mediaConnector.pause();
			}
			
			if (voiceMailFileDBObject != null)
			{
				mediaConnector.setMp3(getCacheDir(), voiceMailFileDBObject.getValue());
				mediaConnector.start();
				mediaController.show(0);
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(failedString)
				       .setCancelable(false)
				       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.dismiss();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
		
		@Override
		protected void onPreExecute()
		{
			waitDialog = ProgressDialog.show(activity, "", loadingString, true);
		}
	}
}