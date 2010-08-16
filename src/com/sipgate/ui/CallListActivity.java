package com.sipgate.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.sipgate.R;
import com.sipgate.models.SipgateCallData;
import com.sipgate.models.holder.CallViewHolder;
import com.sipgate.service.EventService;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.util.AndroidContactsClient;
import com.sipgate.util.ApiServiceProvider;

public class CallListActivity extends Activity {
	
	private ArrayAdapter<SipgateCallData> callListAdapter;
	private static final String TAG = "CallListActivity";
	private AlertDialog m_AlertDlg;
	private AndroidContactsClient contactsClient;
	private String unknownCaller = null;
	private ServiceConnection serviceConnection;
	private EventService serviceBinding = null;
	private PendingIntent onNewCallsPendingIntent;

	private void initStrings() {
		unknownCaller = getResources().getString( R.string.sipgate_unknown_caller);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		startScanService();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopservice();
	}
	
	private void startScanService() {
		Context ctx = getApplicationContext();

		Log.v(TAG, "enter startScanService");
		Intent startIntent = new Intent(this, SipgateBackgroundService.class);
		ctx.startService(startIntent);

		if (serviceConnection == null) {
			Log.d(TAG, "service connection is null");
			serviceConnection = new ServiceConnection() {

				public void onServiceDisconnected(ComponentName arg0) {
					Log.d(TAG, "service disconnected");
					serviceBinding = null;
				}

				public void onServiceConnected(ComponentName name,
						IBinder binder) {
					Log.v(TAG, "service " + name + " connected");
					try {
						Log.d(TAG, "serviceBinding set");
						serviceBinding = (EventService) binder;
						try {
							Log.d(TAG, "serviceBinding registerOnEventsIntent");
							serviceBinding.registerOnEventsIntent(getNewMessagesIntent());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						getCalls();
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
				}
			};
			Intent intent = new Intent(this, SipgateBackgroundService.class);
			Log.d(TAG, "bindService");
			boolean bindret = ctx.bindService(intent, serviceConnection,
					Context.BIND_AUTO_CREATE);

			Log.v(TAG, "leave startScanService: " + bindret);
		} else {
			Log.d(TAG, "service connection is not null");
		}

	}
	
	private void stopservice() {
		if (serviceConnection != null) {
			try {
				if (serviceBinding != null) {
					serviceBinding
							.unregisterOnEventsIntent(getNewMessagesIntent());
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
		if (onNewCallsPendingIntent == null) {
			Intent onChangedIntent = new Intent(this, SipgateFrames.class);
			onChangedIntent.putExtra("view", SipgateFrames.SipgateTab.CALLS);
			onChangedIntent.setAction(SipgateBackgroundService.ACTION_NEWEVENTS);
			onNewCallsPendingIntent = PendingIntent.getActivity(this,
					SipgateBackgroundService.REQUEST_NEWEVENTS, onChangedIntent, 0);
		}
		return onNewCallsPendingIntent;
	}
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		initStrings();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipgate_call_list);
		
		final LayoutInflater mInflater = getLayoutInflater();

		ListView elementList = (ListView) findViewById(R.id.CalllistListView);

		elementList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				SipgateCallData call = (SipgateCallData) parent.getItemAtPosition(position);
				String direction = call.getCallDirection();
				if(direction.equals("incoming")) call_menu(call.getCallSourceNumberE164());
				if(direction.equals("outgoing")) call_menu(call.getCallTargetNumberE164());
			}
		});
		contactsClient = new AndroidContactsClient(this);

		callListAdapter = new ArrayAdapter<SipgateCallData>(this, R.layout.sipgate_call_list_bit, R.id.CallerNameTextView) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CallViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.sipgate_call_list_bit, null);
					holder = new CallViewHolder();
					holder.callerNameView = (TextView) convertView.findViewById(R.id.CallerNameTextView);
					holder.callerNumberView = (TextView) convertView.findViewById(R.id.CallerNumberTextView);
					holder.callTimeView = (TextView) convertView.findViewById(R.id.DateTimeTextView);
					holder.callTypeIconView = (ImageView) convertView.findViewById(R.id.CallTypeImage);
					holder.callButtonView = (ImageView) convertView.findViewById(R.id.CallImageButton);
					holder.categoryTextView = (TextView) convertView.findViewById(R.id.CategoryTextView);
					convertView.setTag(holder);
				} else {
					holder = (CallViewHolder) convertView.getTag();
				}
				
				SipgateCallData item = (SipgateCallData) getItem(position);

				String callDirection = item.getCallDirection();
				Boolean callMissed = item.getCallMissed();
				
				if(callDirection.equals("incoming") && callMissed == false) {
					holder.callTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.icon_incoming));
				}
				if(callDirection.equals("incoming") && callMissed == true) {
					holder.callTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.icon_missed));
				}
				if(callDirection.equals("outgoing")) {
					holder.callTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.icon_outgoing));
				}

				String targetName = contactsClient.getContactName(item.getCallTargetNumberPretty());
				String sourceName = contactsClient.getContactName(item.getCallSourceNumberPretty());
				String targetNumber = item.getCallTargetNumberPretty();
				String sourceNumber = item.getCallSourceNumberPretty();
				
				if(targetName.equals(targetNumber)) targetName = unknownCaller;
				if(sourceName.equals(sourceNumber)) sourceName = unknownCaller;
				
				if(callDirection.equals("outgoing")) {
					holder.callerNameView.setText(targetName);
					holder.callerNumberView.setText(targetNumber);
				}
				
				if(callDirection.equals("incoming")) {
					holder.callerNameView.setText(sourceName);
					holder.callerNumberView.setText(sourceNumber);
				}
				
				Date callTime = item.getCallTime();
				String thisDay = formatDateAsDay(callTime);
				holder.callTimeView.setText(formatDateAsTime(callTime));
				holder.categoryTextView.setText(thisDay);
				holder.categoryTextView.setVisibility(View.VISIBLE);

				if (position > 0) {
					SipgateCallData lastItem = getItem(position - 1);
					Date lastTime = lastItem.getCallTime();
					String lastDay = formatDateAsDay(lastTime);
					if (lastDay.equals(thisDay)) {
						holder.categoryTextView.setVisibility(View.GONE);
					} else {
						holder.categoryTextView.setVisibility(View.VISIBLE);
					}
				}

				setRead(item);
				
				return convertView;
			}

		};
		elementList.setAdapter(callListAdapter);
		showCalls(new ArrayList<SipgateCallData>(0)); // begin with empty list
		getCalls();
	}
	
	private void setRead(final SipgateCallData callData) {
		Thread t = new Thread() {
			public void start() {
				try {
					String url = callData.getCallReadModifyUrl();
					ApiServiceProvider apiClient = ApiServiceProvider
							.getInstance(getApplicationContext());
					apiClient.setCallRead(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Log.e(TAG, "marking call as read... ");
		t.start();
	}
	
	protected String formatDateAsDay(Date d) {
		SimpleDateFormat dateformatterPretty = new SimpleDateFormat(
				getResources().getString(R.string.dateTimeFormatForDay));
		return dateformatterPretty.format(d);
	}

	protected String formatDateAsTime(Date d) {
		SimpleDateFormat dateformatterPretty = new SimpleDateFormat(
				getResources().getString(R.string.dateTimeFormatForTime));
		return dateformatterPretty.format(d);
	}	
	
	public void getCalls() {
		try {
			if (serviceBinding != null) {
				List<SipgateCallData> calls = serviceBinding.getCalls();
				if (calls != null) {
					showCalls(calls);
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

	private void showCalls(List<SipgateCallData> calls) {
		callListAdapter.clear();
		Log.i(TAG, "showCalls " + new Date().toString());
		boolean itemsAdded = false;
		if(calls == null) return;
		for (SipgateCallData item : calls) {
			callListAdapter.add(item);
			itemsAdded = true;
		}
		
		callListAdapter.sort(new Comparator<SipgateCallData>() {

			public int compare(SipgateCallData a, SipgateCallData b) {
				if (a == null && b != null) {
					return 1;
				}
				if (b == null && a != null) {
					return -1;
				}
				if (b == a) {
					return 0;
				}
				return -1 * a.getCallTime().compareTo(b.getCallTime());
			}
		});

		ListView eventlist = (ListView) findViewById(R.id.CalllistListView);
		TextView emptylist = (TextView) findViewById(R.id.EmptyCallListTextView);
		
		if (itemsAdded) {
			eventlist.setVisibility(View.VISIBLE);
			emptylist.setVisibility(View.GONE);
		} else {
			eventlist.setVisibility(View.GONE);
			emptylist.setVisibility(View.VISIBLE);
		}
		
		Log.i(TAG, "showEvents done " + new Date().toString());
	}
	
	

	
	@Override
	public void onResume() {
		super.onResume();

		startScanService();
		serviceRefresh();
		getCalls();
	}	
	
	private void serviceRefresh() {
		if (serviceBinding != null) {
			try {
				serviceBinding.refreshCalls();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"CallList");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		OptionsMenu m = new OptionsMenu();
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
		if (action != null && action.equals(SipgateBackgroundService.ACTION_NEWEVENTS)) {
			getCalls();
		} else {
			super.onNewIntent(intent);
		}
		getCalls();
	}

	void call_menu(String target)
	{
		if (m_AlertDlg != null) 
		{
			m_AlertDlg.cancel();
		}
		if (target.length() == 0)
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		else if (!Receiver.engine(this).call(target))
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.notfast)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
	}	
}

