package com.sipgate.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.CallDataDBAdapter;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.models.holder.CallViewHolder;
import com.sipgate.util.AndroidContactsClient;

public class CallListAdapter extends BaseAdapter
{
	private final static String TAG = "CallListAdapter";

	private final LayoutInflater mInflater;
	
	private AndroidContactsClient contactsClient;
	
	private ArrayList<DataSetObserver> observerRegistry = null;
	private CallDataDBAdapter callDataDBAdapter = null;
	
	private Vector<CallDataDBObject> callData = null;
	private HashMap<String, String> contactNameCache = null; 	
	
	private String unknownCaller = null;
	private String noNumber = null;
	
	private CallViewHolder holder = null;
	
	private CallDataDBObject item = null;
	private CallDataDBObject lastItem = null;
	
	private String sourceName = null;
	private String sourceNumberPretty = null;
	private String sourceNumber = null;
	
	private Calendar currentDay = null;
	private Calendar lastDay = null;
	
	private SimpleDateFormat timeFormatter = null;
	private SimpleDateFormat dateFormatter = null;
		
	private long callDirection = 0;
	private boolean callMissed = false;
	
	private Drawable incomingIcon = null;
	private Drawable missedIcon = null;
	private Drawable outgoingIcon = null;
	
	private Activity activity = null;
	
	public CallListAdapter(Activity activity) 
	{
		this.activity = activity;
		
		mInflater = activity.getLayoutInflater();
		
		contactsClient = new AndroidContactsClient(activity);
		
		callDataDBAdapter = new CallDataDBAdapter(activity);
		callData = callDataDBAdapter.getAllCallData();
		callDataDBAdapter.close();
		
		contactNameCache = new HashMap<String, String>();
		
		observerRegistry = new ArrayList<DataSetObserver>();
				
		unknownCaller = activity.getResources().getString(R.string.sipgate_unknown_caller);
		noNumber = activity.getResources().getString(R.string.sipgate_no_number);		
		
		incomingIcon = activity.getResources().getDrawable(R.drawable.icon_incoming);
		missedIcon = activity.getResources().getDrawable(R.drawable.icon_missed);
		outgoingIcon = activity.getResources().getDrawable(R.drawable.icon_outgoing);
		
		dateFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.dateTimeFormatForDay));
		timeFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.dateTimeFormatForTime));
		
		currentDay = Calendar.getInstance();
		lastDay = Calendar.getInstance();
	}
		
	public boolean areAllItemsEnabled() 
	{
		return true;
	}
	
	public boolean isEnabled(int position) 
	{
		return true;
	}
		
	public Object getItem(int position) 
	{
		return callData.elementAt(position);
	}
	
	public long getItemId(int position) 
	{
		return callData.elementAt(position).getId();
	}
	
	@Override
	public int getItemViewType(int position) 
	{
		return 0;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if (convertView == null) 
		{
			convertView = mInflater.inflate(R.layout.sipgate_call_list_bit, null);
			holder = new CallViewHolder();
			holder.callerNameView = (TextView) convertView.findViewById(R.id.CallerNameTextView);
			holder.callerNumberView = (TextView) convertView.findViewById(R.id.CallerNumberTextView);
			holder.callTimeView = (TextView) convertView.findViewById(R.id.DateTimeTextView);
			holder.callTypeIconView = (ImageView) convertView.findViewById(R.id.CallTypeImage);
			holder.callButtonView = (ImageView) convertView.findViewById(R.id.CallImageButton);
			holder.categoryTextView = (TextView) convertView.findViewById(R.id.CategoryTextView);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (CallViewHolder) convertView.getTag();
		}
		
		item = (CallDataDBObject) getItem(position);

		callDirection = item.getDirection();
		callMissed = item.isMissed();
		
		if(callDirection == CallDataDBObject.INCOMING && !callMissed) 
		{
			holder.callTypeIconView.setImageDrawable(incomingIcon);
		}
		else if(callDirection == CallDataDBObject.INCOMING && callMissed) 
		{
			holder.callTypeIconView.setImageDrawable(missedIcon);
		}
		else 
		{
			holder.callTypeIconView.setImageDrawable(outgoingIcon);
		}

		sourceName = null;
		sourceNumberPretty = item.getSourceNumberPretty();
		
		if(sourceNumberPretty.length() > 0) 
		{
			if (!contactNameCache.containsKey(sourceNumberPretty))
			{
				contactNameCache.put(sourceNumberPretty, contactsClient.getContactName(sourceNumberPretty));
			}
						
			sourceName = contactNameCache.get(sourceNumberPretty);
		}
		
		if (sourceName == null) 
		{
			sourceName = item.getSourceName();
			
			if (sourceName == null || sourceName.length() == 0 || sourceName.equals(sourceNumberPretty))
			{
				sourceName = unknownCaller;
			}
		}
		
		sourceNumber = item.getSourceNumberPretty();
	
		if(sourceNumber == null || sourceNumber.length() == 0 || sourceNumber.equals("+anonymous")) 
		{
			sourceNumber = noNumber;
		}
		
		holder.callerNameView.setText(sourceName);
		holder.callerNumberView.setText(sourceNumber);

		currentDay.setTimeInMillis(item.getTime());
		
		holder.callTimeView.setText(timeFormatter.format(currentDay.getTime()));
		holder.categoryTextView.setText(dateFormatter.format(currentDay.getTime()));
		holder.categoryTextView.setVisibility(View.VISIBLE);

		if (position > 0) 
		{
			lastItem = (CallDataDBObject)getItem(position - 1);
		
			lastDay.setTimeInMillis(lastItem.getTime());
						
			if (lastDay.get(Calendar.DAY_OF_YEAR) == currentDay.get(Calendar.DAY_OF_YEAR) &&
				lastDay.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR)) 
			{
				holder.categoryTextView.setVisibility(View.GONE);
			} 
			else 
			{
				holder.categoryTextView.setVisibility(View.VISIBLE);
			}
		}
		
		//setRead(item);
		
		return convertView;
	}
	
	/*
	private void setRead(final SipgateCallData callData) {
		if (!callData.getCallRead()) {
			Thread t = new Thread() {
				public void start() {
					try {
						String url = callData.getCallReadModifyUrl();
						ApiServiceProvider apiClient = ApiServiceProvider
								.getInstance(context);
						apiClient.setCallRead(url);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			Log.e(TAG, "marking call as read... ");
			t.start();
		}
	}
	*/
	
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) 
	{
		if (!observerRegistry.contains(observer)) 
		{
			observerRegistry.add(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) 
	{
		if (observerRegistry.contains(observer)) 
		{
			observerRegistry.remove(observer);
		}
	}

	@Override
	public boolean isEmpty()
	{
		return (callData.size() > 0);
	}

	@Override
	public int getCount()
	{
		return callData.size();
	}
	
	@Override
	public void notifyDataSetChanged() {
		
		callDataDBAdapter = new CallDataDBAdapter(activity);
		callData = callDataDBAdapter.getAllCallData();
		callDataDBAdapter.close();
		
		super.notifyDataSetChanged();
	}
}
