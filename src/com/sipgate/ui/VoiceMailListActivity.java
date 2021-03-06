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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.sipgate.R;
import com.sipgate.adapters.VoiceMailListAdapter;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.SystemDataDBObject;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.db.VoiceMailFileDBObject;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.MediaConnector;
import com.sipgate.util.SipgateApplication;

/**
 * This class represents the voice mail list activity and implements
 * all it's functions.
 * 
 * @author Karsten Knuth
 * @author Marcus Hunger
 * @authos graef
 * @version 1.2
 */
public class VoiceMailListActivity extends Activity implements OnItemClickListener
{
	private static final String TAG = "VoiceMailListActivity";
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	private VoiceMailListAdapter voiceMailListAdapter = null;

	private ImageView refreshSpinner = null;
	private LinearLayout refreshView = null;
	private ListView elementList = null;
	private TextView emptyList = null;
	
	private AnimationDrawable frameAnimation = null;
	private boolean isAnimationRunning = false;
	
	private ServiceConnection serviceConnection = null;
	private EventService serviceBinding = null;

	private PendingIntent onNewVoiceMailsPendingIntent = null;
	private PendingIntent onNoVoiceMailsPendingIntent = null;
	private PendingIntent onGetVoiceMailsPendingIntent = null;
	private PendingIntent onErrorPendingIntent = null;
	
	private SipgateApplication application = null;
	private SipgateApplication.RefreshState refreshState = SipgateApplication.RefreshState.NONE;
	
	private Context appContext = null;
	
	private MediaConnector mediaConnector = null;
	private MediaController mediaController = null;
	
	private ApiServiceProvider apiClient = null;
	private Activity activity = this;
	
	private String loadingString = null;
	private String failedString = null;
	
	/**
	 * This function is called right after the class is started by an intent.
	 * 
	 * @param bundle The bundle which caused the activity to be started.
	 * @since 1.0
	 */
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_voicemail_list);
		
		refreshSpinner = (ImageView) findViewById(R.id.sipgateVoiceMailListRefreshImage);
		refreshView = (LinearLayout) findViewById(R.id.sipgateVoiceMailListRefreshView);
		elementList = (ListView) findViewById(R.id.EventListView);
		emptyList = (TextView) findViewById(R.id.EmptyEventListTextView);

		frameAnimation = (AnimationDrawable) refreshSpinner.getBackground();
		Runnable animationThread = new Runnable()
		{
			public void run()
			{
				frameAnimation.start();
			}
		};
		
		refreshView.setVisibility(View.VISIBLE);
		refreshView.post(animationThread);
		
		appContext = getApplicationContext();
		apiClient = ApiServiceProvider.getInstance(activity);
		
		sipgateDBAdapter = new SipgateDBAdapter(this);
		voiceMailListAdapter = new VoiceMailListAdapter(this, sipgateDBAdapter);
        
        elementList.setAdapter(voiceMailListAdapter);
        elementList.setOnItemClickListener(this);  
        
        mediaConnector = new MediaConnector();
        
        mediaController = new MediaController(this);
		mediaController.setMediaPlayer(mediaConnector);
		mediaController.setEnabled(true);
		mediaController.setAnchorView(elementList.getRootView());
		
		loadingString = getResources().getString(R.string.sipgate_loading);
		failedString = getResources().getString(R.string.sipgate_download_failed);
		
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
		
		SystemDataDBObject systemDataDBObject = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_VOICEMAILS_COUNT);
		
		if (systemDataDBObject != null)
		{
			systemDataDBObject.setValue(String.valueOf(0));
			
			sipgateDBAdapter.update(systemDataDBObject);
		}
		
		refreshState = application.getRefreshState();
		application.setRefreshState(SipgateApplication.RefreshState.NONE);
		
		switch (refreshState) {
			case NEW_EVENTS: 
				refreshView.setVisibility(View.GONE);
				voiceMailListAdapter.notifyDataSetChanged();
				showNewEntriesToast();
				break;
			case NO_EVENTS: 
				refreshView.setVisibility(View.GONE);
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
				voiceMailListAdapter.notifyDataSetChanged();
				break;
		}
		
		if (voiceMailListAdapter.isEmpty()) {
			elementList.setVisibility(View.GONE);
			emptyList.setVisibility(View.VISIBLE);
		} else {
			elementList.setVisibility(View.VISIBLE);
			emptyList.setVisibility(View.GONE);
		}	
		
		if(!isAnimationRunning) {
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
		
		if (sipgateDBAdapter != null)
		{
			sipgateDBAdapter.close();
		}
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
		m.createMenu(menu,"VoiceMailList");
		
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
		VoiceMailDataDBObject voiceMailDataDBObject = (VoiceMailDataDBObject) parent.getItemAtPosition(position);
		
		new DownloadVoiceMailTask().execute(voiceMailDataDBObject);
		
		markAsRead(voiceMailDataDBObject);
	}
	
	/**
	 * This function provides the background sevice with callback
	 * intent for several steps in the refresh cycle.
	 * 
	 * @since 1.2
	 */
	private void registerForBackgroundIntents()
	{
		Intent intent = new Intent(this, SipgateBackgroundService.class);
		appContext.startService(intent);

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
							Log.d(TAG, "service binding -> registerOnVoiceMailsIntent");
							serviceBinding.registerOnVoiceMailsIntents(TAG, getVoiceMailsIntent(), newVoiceMailsIntent(), noVoiceMailsIntent(), errorIntent());
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
			
			boolean bindret = appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
	public void unregisterFromBackgroungIntents()
	{
		if (serviceConnection != null) {
			try {
				if (serviceBinding != null) {
					Log.d(TAG, "service unbinding -> unregisterOnVoiceMailsIntent");
					serviceBinding.unregisterOnVoiceMailsIntents(TAG);
				}
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.v(TAG, "unbind service");
			appContext.unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	/**
	 * This functions returns a callback intent for the callback
	 * that the download of new voice mails just has started.
	 * 
	 * @return The callback intent for starting to download voice mails.
	 * @since 1.2
	 */
	private PendingIntent getVoiceMailsIntent() {
		if (onGetVoiceMailsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_GETEVENTS);
			onGetVoiceMailsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onGetVoiceMailsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that new voice mails have been downloaded.
	 * 
	 * @return The callback intent for new voice mails.
	 * @since 1.2
	 */
	private PendingIntent newVoiceMailsIntent() {
		if (onNewVoiceMailsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewVoiceMailsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewVoiceMailsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that no new voice mails have been downloaded.
	 * 
	 * @return The callback intent for no new voice mails.
	 * @since 1.2
	 */
	private PendingIntent noVoiceMailsIntent() {
		if (onNoVoiceMailsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NOEVENTS);
			onNoVoiceMailsPendingIntent = PendingIntent.getActivity(this, SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNoVoiceMailsPendingIntent;
	}
	
	/**
	 * This functions returns a callback intent for the callback
	 * that an error occurred during the download of new voice mails.
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
	 * Sends a request to the api to mark a voice mail as read
	 * when it is clicked.
	 * 
	 * @param voiceMailDataDBObject The voice mail to me marked as read.
	 */
	private void markAsRead(final VoiceMailDataDBObject voiceMailDataDBObject) 
	{
		if (!voiceMailDataDBObject.isRead()) {
			Thread markThread = new Thread()
			{
				public void run()
				{
					try {
						apiClient.setVoiceMailRead(voiceMailDataDBObject.getReadModifyUrl());
					
						voiceMailDataDBObject.setRead(true);
						
						sipgateDBAdapter.update(voiceMailDataDBObject);
					} 
					catch (Exception e) {
						Log.e(TAG, "markAsRead()", e);
					}
				}
			};
			
			markThread.start();
		}
	}
	
	/**
	 * 
	 * @author graef
	 * @version 1.1
	 */
	private class DownloadVoiceMailTask extends AsyncTask <VoiceMailDataDBObject, Void, VoiceMailFileDBObject>
	{
		private InputStream inputStream = null;
		private ByteArrayOutputStream byteArrayOutputStream = null; 
		private ProgressDialog waitDialog = null;
		
		/**
		 * Shows a wait dialog before we start downloading the file.
		 * 
		 * @since 1.1
		 */
		protected void onPreExecute()
		{
			waitDialog = ProgressDialog.show(activity, "", loadingString, true);
		}
		
		protected VoiceMailFileDBObject doInBackground(VoiceMailDataDBObject... voiceMailDataDBObjects)
		{
			VoiceMailDataDBObject voiceMailDataDBObject = voiceMailDataDBObjects[0];
			VoiceMailFileDBObject voiceMailFileDBObject = null;
			
			try {
				voiceMailFileDBObject = sipgateDBAdapter.getVoiceMailFileDBObjectById(voiceMailDataDBObject.getId());
				
				if (voiceMailFileDBObject == null) {
					inputStream = apiClient.getVoicemail(voiceMailDataDBObject.getContentUrl());
					
					if (inputStream == null) {
						throw new RuntimeException("voicemail inputstream is null");
					}
		
					byteArrayOutputStream = new ByteArrayOutputStream();
					
					byte chunk[] = new byte[1024];
					
					int length = 0;
					
					while ((length = inputStream.read(chunk)) > 0) {
						byteArrayOutputStream.write(chunk, 0, length);
					}
					
					voiceMailFileDBObject = new VoiceMailFileDBObject();
					
					voiceMailFileDBObject.setId(voiceMailDataDBObject.getId());
					voiceMailFileDBObject.setValue(byteArrayOutputStream.toByteArray());
					
					sipgateDBAdapter.insert(voiceMailFileDBObject);
				}
			}					
			catch (Exception e) {
				Log.e(TAG, e.toString(), e);
			
				if (waitDialog.isShowing()) {
					waitDialog.dismiss();
				}
			}
			finally {
				try {
					if (byteArrayOutputStream != null) {
						byteArrayOutputStream.close();
					}
				}
				catch (IOException e) {
					Log.e(TAG, e.toString(), e);
				}
				
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				}
				catch (IOException e) {
					Log.e(TAG, e.toString(), e);
				}
			}
			
			return voiceMailFileDBObject;
		} 
		
		/**
		 * Hides the wait dialog and starts the playback after the
		 * download is finished.
		 * 
		 * @since 1.1
		 */
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
	}
}