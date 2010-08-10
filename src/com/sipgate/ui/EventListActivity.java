package com.sipgate.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.api.types.Event;
import com.sipgate.api.types.Voicemail;
import com.sipgate.exceptions.DownloadException;
import com.sipgate.models.holder.EventViewHolder;
import com.sipgate.service.EventService;
import com.sipgate.service.EventServiceImpl;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.Constants;
//import com.sipgate.util.Oauth;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

public class EventListActivity extends Activity {
	private static final int REFRESH_MENU_ITEM = 0;
	private static final String TAG = "EventListActivity";

//	private Oauth oauth;
	private ArrayAdapter<Event> eventListAdapter;
	private ServiceConnection serviceConnection;
	private EventService serviceBinding = null;
	private boolean serviceStopped = false;
	private PendingIntent onNewEventsPendingIntent;
	private MediaConnector mediaConnector;
	private MediaController mediaController;
	private String voicemailFromText;
	private String secondsText;
	private ApiServiceProvider apiClient = null;
	private boolean hastVmListFeature = false;

	/**
	 * optimization: gets Strings from resources. must be called from onCreate()
	 */
	private void initStrings() {
		voicemailFromText = getResources().getString(R.string.sipgate_voicemail_from);
		secondsText = getResources().getString(R.string.sipgate_seconds);
	}

	@Override
	protected void onStart() {
		super.onStart();
		startScanService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopservice();
	}

	private void startScanService() {
		Context ctx = getApplicationContext();

		// ... and only start service when feature available:
		if (!this.hastVmListFeature) {
			Log.w(TAG, "used API is not capable of 'VM_LIST' feature; not starting service!");
		} else {
			Log.v(TAG, "enter startScanService");
			Intent startIntent = new Intent(this, EventServiceImpl.class);
			ctx.startService(startIntent);

			if (serviceConnection == null) {
				serviceConnection = new ServiceConnection() {
	
					public void onServiceDisconnected(ComponentName arg0) {
						Log.d(TAG, "service disconnected");
						serviceBinding = null;
					}
	
					public void onServiceConnected(ComponentName name, IBinder binder) {
						Log.v(TAG, "service " + name + " connected");
						try {
							if (!serviceStopped) {
								Log.d(TAG, "serviceBinding set");
								serviceBinding = (EventService) binder;
								try {
									Log.d(TAG, "serviceBinding registerOnEventsIntent");
									serviceBinding.registerOnEventsIntent(getNewMessagesIntent());
								} catch (RemoteException e) {
									e.printStackTrace();
								}
								getEvents();
	
							} else {
								Log.d(TAG, "service is stopped");
							}
						} catch (ClassCastException e) {
							e.printStackTrace();
						}
					}
				};
				Intent intent = new Intent(this, EventServiceImpl.class);
				Log.d(TAG, "bindService");
				ctx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	
				Log.v(TAG, "leave startScanService");
			}
		}
	}

	private void stopservice() {
		if (serviceConnection != null) {
			try {
				if (serviceBinding != null) {
					serviceStopped = true;
					serviceBinding.unregisterOnEventsIntent(getNewMessagesIntent());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "calling unbind");
			getApplicationContext().unbindService(serviceConnection);
			serviceConnection = null;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		stopservice();
	}

	private PendingIntent getNewMessagesIntent() {
		if (onNewEventsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.putExtra("view", SipgateFrames.SipgateTab.VM);
			onChangedIntent.setAction(EventServiceImpl.ACTION_NEWEVENTS);
			onNewEventsPendingIntent = PendingIntent.getActivity(this, EventServiceImpl.REQUEST_NEWEVENTS,
					onChangedIntent, 0);
		}
		return onNewEventsPendingIntent;
	}

	private void initApi() {
		if (this.apiClient == null) {
			this.apiClient = ApiServiceProvider.getInstance(getApplicationContext());

			try {
				this.hastVmListFeature = this.apiClient.featureAvailable(API_FEATURE.VM_LIST);
			} catch (Exception e) {
				Log.w(TAG, "startScanService() exception in call to featureAvailable() -> "+e.getLocalizedMessage());
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initStrings();
		setContentView(R.layout.eventlist);

		initApi();

//		oauth = Oauth.getInstance(this);

		final LayoutInflater mInflater = getLayoutInflater();

		ListView elementList = (ListView) findViewById(R.id.EventListView);

		mediaConnector = new MediaConnector();

		mediaController = new MediaController(this);
		mediaController.setMediaPlayer(mediaConnector);
		mediaController.setEnabled(true);
		mediaController.setAnchorView(elementList.getRootView());

		elementList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				Voicemail voicemail = (Voicemail) parent.getItemAtPosition(position);
				showPlayerDialog(voicemail);
			}
		});

		eventListAdapter = new ArrayAdapter<Event>(this, R.layout.eventelement, R.id.EventTitle) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				EventViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.eventelement, null);
					holder = new EventViewHolder();
					holder.titleView = (TextView) convertView.findViewById(R.id.EventTitle);
					holder.dateView = (TextView) convertView.findViewById(R.id.DateTextView);
					holder.categoryView = (TextView) convertView.findViewById(R.id.CategoryTextView);
					holder.transcriptionView = (TextView) convertView.findViewById(R.id.TranscriptionTextView);
					holder.iconVM = (ImageView) convertView.findViewById(R.id.IconView);
					convertView.setTag(holder);
				} else {
					holder = (EventViewHolder) convertView.getTag();
				}
				Event item = getItem(position);

				Date createdOn = item.getCreateOnAsDate();

				String thisDay = formatDateAsDay(item.getCreateOnAsDate());

				if (item.isRead()) {
					holder.titleView.setTypeface(Typeface.DEFAULT);
				} else {
					holder.titleView.setTypeface(Typeface.DEFAULT_BOLD);
				}

				holder.dateView.setText(formatDateAsTime(createdOn));
				holder.categoryView.setText(thisDay);
				holder.categoryView.setVisibility(View.VISIBLE);

				if (item.getClass().equals(Voicemail.class)) {
					showVoicemailDetails(holder, (Voicemail) item);
				}

				if (position > 0) {
					Event lastItem = getItem(position - 1);
					String lastDay = formatDateAsDay(lastItem.getCreateOnAsDate());
					if (lastDay.equals(thisDay)) {
						holder.categoryView.setVisibility(View.GONE);
					} else {
						holder.categoryView.setVisibility(View.VISIBLE);
					}
				}

				return convertView;
			}

			private void showVoicemailDetails(EventViewHolder holder, Voicemail item) {

				holder.titleView.setText(voicemailFromText + ": " + item.getNumberPretty());

				String transcription = item.getTranscription();

				if (transcription == null || transcription.equals("")) {
					holder.transcriptionView.setText("" + item.getDuration() + " " + secondsText);
				} else {
					holder.transcriptionView.setText(transcription);
				}
			}
		};
		elementList.setAdapter(eventListAdapter);
		getEvents();
		showEvents(new ArrayList<Event>(0)); // begin with empty list
	}

	protected void showPlayerDialog(Voicemail voicemail) {
		try {
			this.mediaConnector.pause();
			// TODO: change to async call
			this.mediaConnector.setMp3(MediaUrlPlayer.download(voicemail, getApplicationContext()));
			this.mediaConnector.start();
			mediaController.show(0);
		} catch (DownloadException e) {
			e.printStackTrace();
		}
		setRead(voicemail);
	}

	private void setRead(final Voicemail voiceMail) {
		Thread t = new Thread() {
			public void start() {
				try {
					String url = String.format(Constants.VOICEMAILREAD_URL, voiceMail.getVoicemail_id());
					ApiServiceProvider apiClient = ApiServiceProvider.getInstance(getApplicationContext());
					apiClient.setVoicemailRead(url);
				} catch (RuntimeException e) {
//					Log.e(TAG, "RuntimeException, setting voicemail to read");
					e.printStackTrace();
				} catch (Exception e) {
//					Log.e(TAG, e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		};
		Log.e(TAG, "marking vmail as read... ");
		t.start();
	}

	protected String formatDateAsDay(Date d) {
		SimpleDateFormat dateformatterPretty = new SimpleDateFormat(getResources().getString(
				R.string.dateTimeFormatForDay));
		return dateformatterPretty.format(d);
	}

	protected String formatDateAsTime(Date d) {
		SimpleDateFormat dateformatterPretty = new SimpleDateFormat(getResources().getString(
				R.string.dateTimeFormatForTime));
		return dateformatterPretty.format(d);
	}

	public void getEvents() {
		try {
			if (serviceBinding != null) {
				List<Event> events = serviceBinding.getEvents();
				if (events != null) {
					showEvents(events);
				} else {
					Log.d(TAG, "got 'null' events result");
				}
			} else {
				Log.d(TAG, "no service binding");
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void showEvents(List<Event> events) {
		eventListAdapter.clear();
		Log.i(TAG, "showEvents");
		boolean itemsAdded = false;
		for (Event item : events) {
			// only add voicemail-events. that's about to change in future
			// versions
			if (Voicemail.class.isInstance(item)) {
				eventListAdapter.add(item);
				itemsAdded = true;
			}
		}

		eventListAdapter.sort(new Comparator<Event>() {

			public int compare(Event a, Event b) {
				if (a == null && b != null) {
					return 1;
				}
				if (b == null && a != null) {
					return -1;
				}
				if (b == a) {
					return 0;
				}
				return -1 * a.getCreateOnAsDate().compareTo(b.getCreateOnAsDate());
			}
		});

		ListView eventlist = (ListView) findViewById(R.id.EventListView);
		TextView emptylist = (TextView) findViewById(R.id.EmptyEventListTextView);
		if (itemsAdded) {
			eventlist.setVisibility(View.VISIBLE);
			emptylist.setVisibility(View.GONE);
		} else {
			eventlist.setVisibility(View.GONE);
			emptylist.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		initApi();

//		Intent i = getIntent();
//
//		if (i != null && oauth.isOauthIntent(getIntent()) && oauth.registrationInProgress()) {
//			try {
//				Log.d(TAG, "oauth intent :) " + i.getDataString());
//				oauth.register(i);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		Log.v(TAG, "onResume");
		//if (serviceBinding == null) {
		startScanService();
		getEvents();
		//}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		optionsMenu m = new optionsMenu();
		m.createMenu(menu,"EventList");
		
		return result;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		optionsMenu m = new optionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
	
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		stopservice();
	}

	

	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "received intent: " + intent);
		Log.v(TAG, "action: " + intent.getAction());

		String action = intent.getAction();
		if (action != null && action.equals(EventServiceImpl.ACTION_NEWEVENTS)) {
			getEvents();
		} else {
			super.onNewIntent(intent);
		}
		getEvents();
	}

	public class MediaConnector implements MediaController.MediaPlayerControl {

		private MediaPlayer mediaPlayer;

		public MediaConnector() {
			mediaPlayer = new MediaPlayer();
		}

		@Override
		public int getBufferPercentage() {
			return 0;
		}

		@Override
		public int getCurrentPosition() {
			return mediaPlayer.getCurrentPosition();
		}

		@Override
		public int getDuration() {
			return mediaPlayer.getDuration();
		}

		@Override
		public boolean isPlaying() {
			return mediaPlayer.isPlaying();
		}

		@Override
		public void pause() {
			mediaPlayer.pause();
		}

		@Override
		public void seekTo(int pos) {
			mediaPlayer.seekTo(pos);
		}

		public void setMp3(String location) {
			mediaPlayer.stop();
			// TODO: find out, why this new instance is needed; seems not to
			// work with
			// existing MediaPlayer instance
			mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setDataSource(location);
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void start() {
			mediaPlayer.start();
		}

		@Override
		public boolean canPause() {
			return true;
		}

		@Override
		public boolean canSeekBackward() {
			return true;
		}

		@Override
		public boolean canSeekForward() {
			return true;
		}
    }

}
