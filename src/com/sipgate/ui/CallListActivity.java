package com.sipgate.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.sipgate.R;
import com.sipgate.api.types.Call;
import com.sipgate.api.types.Event;
import com.sipgate.api.types.Voicemail;
import com.sipgate.models.SipgateCallData;
import com.sipgate.models.holder.CallViewHolder;
import com.sipgate.models.holder.EventViewHolder;
import com.sipgate.ui.EventListActivity.MediaConnector;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.XmlrpcClient;

public class CallListActivity extends Activity {
	
	private ArrayAdapter<SipgateCallData> callListAdapter;
	private static final String TAG = "CallListActivity";

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipgate_call_list);
		
		final LayoutInflater mInflater = getLayoutInflater();

		ListView elementList = (ListView) findViewById(R.id.CalllistListView);

		elementList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				// TODO: Add Click Handler
			}
		});

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

				if(callDirection.equals("outgoing")) {
					holder.callerNameView.setText(item.getCallTargetName());
					holder.callerNumberView.setText(item.getCallTargetNumberPretty());
				}
				
				if(callDirection.equals("incoming")) {
					holder.callerNameView.setText(item.getCallSourceName());
					holder.callerNumberView.setText(item.getCallSourceNumberPretty());
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

				return convertView;
			}

		};
		elementList.setAdapter(callListAdapter);
		showCalls(new ArrayList<SipgateCallData>(0)); // begin with empty list
		getCalls();
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
		ApiServiceProvider api = ApiServiceProvider.getInstance(getApplicationContext());
		try {
			ArrayList<SipgateCallData> calls = api.getCalls();
			showCalls(calls);
		} catch (Exception e) {
			Log.e(TAG,"foo");
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
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"CallList");
		
		return result;
	}
	
}

