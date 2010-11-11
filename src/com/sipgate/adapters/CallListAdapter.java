
package com.sipgate.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.models.holder.CallViewHolder;
import com.sipgate.util.ApiServiceProvider;

public class CallListAdapter extends BaseAdapter
{
	private final static String TAG = "CallListAdapter";

	private LayoutInflater mInflater = null;
	
	private ApiServiceProvider apiClient = null;
		
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private Vector<CallDataDBObject> callDataDBObjects = null;

	private String unknownCallerString = null;
	private String noNumberString = null;
	
	private CallViewHolder holder = null;
	
	private CallDataDBObject currentCallDataDBObject = null;
	private CallDataDBObject lastCallDataDBObject = null;
	private CallDataDBObject nextCallDataDBObject = null;
	
	private String remoteName = null;
	private String remoteNumberPretty = null;
	private String remoteNumber = null;
	
	private Calendar currentDayCalendar = null;
	private Calendar lastDayCalendar = null;
	private Calendar nextDayCalendar = null;
	
	private SimpleDateFormat timeFormatter = null;
	private SimpleDateFormat dateFormatter = null;
		
	private long callDirection = 0;
	private boolean isMissed = false;
	private boolean isRead = false;
	
	private Drawable incomingIcon = null;
	private Drawable missedIcon = null;
	private Drawable outgoingIcon = null;
	
	private HashMap<String, String> nameCache = null;
	
	public CallListAdapter(Activity activity) 
	{
		mInflater = activity.getLayoutInflater();
		
		apiClient = ApiServiceProvider.getInstance(activity);
		
		sipgateDBAdapter = SipgateDBAdapter.getInstance(activity);
		
		callDataDBObjects = sipgateDBAdapter.getAllCallData();
				
		unknownCallerString = activity.getResources().getString(R.string.sipgate_unknown_caller);
		noNumberString = activity.getResources().getString(R.string.sipgate_no_number);		
		
		incomingIcon = activity.getResources().getDrawable(R.drawable.icon_incoming);
		missedIcon = activity.getResources().getDrawable(R.drawable.icon_missed);
		outgoingIcon = activity.getResources().getDrawable(R.drawable.icon_outgoing);
		
		dateFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.dateTimeFormatForDay));
		timeFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.dateTimeFormatForTime));
		
		currentDayCalendar = Calendar.getInstance();
		lastDayCalendar = Calendar.getInstance();
		nextDayCalendar = Calendar.getInstance();
		
		nameCache = new HashMap<String, String>();
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
		if (getCount() > position)
		{
			return callDataDBObjects.elementAt(position);
		}
	
		return null;
	}
	
	public long getItemId(int position) 
	{
		if (getCount() > position)
		{
			return callDataDBObjects.elementAt(position).getId();
		}
		
		return 0;
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
			holder.separator = (View) convertView.findViewById(R.id.CallSeparator);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (CallViewHolder) convertView.getTag();
		}
		
		currentCallDataDBObject = (CallDataDBObject) getItem(position);

		if (currentCallDataDBObject != null)
		{
			callDirection = currentCallDataDBObject.getDirection();
			isMissed = currentCallDataDBObject.isMissed();
			isRead = currentCallDataDBObject.isRead();
			
			if(callDirection == CallDataDBObject.INCOMING && !isMissed) 
			{
				holder.callTypeIconView.setImageDrawable(incomingIcon);
			}
			else if(callDirection == CallDataDBObject.INCOMING && isMissed) 
			{
				holder.callTypeIconView.setImageDrawable(missedIcon);
			}
			else 
			{
				holder.callTypeIconView.setImageDrawable(outgoingIcon);
			}
		
			remoteNumber = currentCallDataDBObject.getRemoteNumberE164();
			remoteNumberPretty = currentCallDataDBObject.getRemoteNumberPretty();
			remoteName = currentCallDataDBObject.getRemoteName();
			
			if(remoteNumber == null || remoteNumber.length() == 0 || remoteNumber.equals("+anonymous")) 
			{
				remoteNumber = noNumberString;
				remoteName = unknownCallerString;
			}
			else
			{
				if (remoteName == null || remoteName.length() == 0 || remoteName.equals(remoteNumberPretty))
				{
					if (nameCache.containsKey(remoteNumber))
					{
						remoteName = nameCache.get(remoteNumber);
					}				
					else
					{
						ContactDataDBObject contactDataDBObject = sipgateDBAdapter.getContactDataDBObjectByNumberE164(remoteNumber);
						
						if (contactDataDBObject != null)
						{
							if (contactDataDBObject.getDisplayName() != null ||  contactDataDBObject.getDisplayName().length() > 0)
							{
								remoteName = contactDataDBObject.getDisplayName();
							}
							else
							{
								remoteName = contactDataDBObject.getFirstName();
								
								if (remoteName.length() > 0)
								{
									remoteName += " ";
								}
								
								remoteName += contactDataDBObject.getLastName();
							}
						}
						else
						{
							remoteName = unknownCallerString;
						}
						
						nameCache.put(remoteNumber, remoteName);
					}
				}
			}
			
			if(remoteNumberPretty == null || remoteNumberPretty.length() == 0 || remoteNumberPretty.equals("+anonymous")) 
			{
				remoteNumberPretty = remoteNumber;
			}
						
			if (isRead) 
			{
				holder.callerNameView.setTypeface(Typeface.DEFAULT);
				holder.callerNumberView.setTypeface(Typeface.DEFAULT);
			} 
			else 
			{
				holder.callerNameView.setTypeface(Typeface.DEFAULT_BOLD);
				holder.callerNumberView.setTypeface(Typeface.DEFAULT_BOLD);
			}
			
			holder.callerNameView.setText(remoteName);
			holder.callerNumberView.setText(remoteNumberPretty);
		
			currentDayCalendar.setTimeInMillis(currentCallDataDBObject.getTime());
			
			holder.callTimeView.setText(timeFormatter.format(currentDayCalendar.getTime()));
			holder.categoryTextView.setText(dateFormatter.format(currentDayCalendar.getTime()));
			
			if (position > 0) 
			{
				lastCallDataDBObject = (CallDataDBObject)getItem(position - 1);
			
				lastDayCalendar.setTimeInMillis(lastCallDataDBObject.getTime());
							
				if (lastDayCalendar.get(Calendar.DAY_OF_YEAR) == currentDayCalendar.get(Calendar.DAY_OF_YEAR) &&
					lastDayCalendar.get(Calendar.YEAR) == currentDayCalendar.get(Calendar.YEAR)) 
				{
					holder.categoryTextView.setVisibility(View.GONE);
				} 
				else 
				{
					holder.categoryTextView.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				holder.categoryTextView.setVisibility(View.VISIBLE);
			}
			
			if (position < getCount() - 1)
			{
				nextCallDataDBObject = (CallDataDBObject)getItem(position + 1);
				
				nextDayCalendar.setTimeInMillis(nextCallDataDBObject.getTime());
				
				if (nextDayCalendar.get(Calendar.DAY_OF_YEAR) != currentDayCalendar.get(Calendar.DAY_OF_YEAR) ||
					nextDayCalendar.get(Calendar.YEAR) != currentDayCalendar.get(Calendar.YEAR))
				{
					holder.separator.setVisibility(View.GONE);
				}
				else
				{
					holder.separator.setVisibility(View.VISIBLE);
				}
			}
			
			
			markAsRead(currentCallDataDBObject); 
		}
	
		return convertView;
	}
	
	private void markAsRead(final CallDataDBObject callDataDBObject) 
	{
		if (!callDataDBObject.isRead()) 
		{
			Thread markThread = new Thread()
			{
				public void run()
				{
					try 
					{
						try
						{
							apiClient.setCallRead(callDataDBObject.getReadModifyUrl());
						}
						catch (FeatureNotAvailableException fex)
						{
							Log.w(TAG, "markAsRead()", fex);
						}
					
						callDataDBObject.setRead(true);
						
						sipgateDBAdapter.update(callDataDBObject);
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
	
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return (callDataDBObjects.size() == 0);
	}

	@Override
	public int getCount()
	{
		return callDataDBObjects.size();
	}
	
	@Override
	public void notifyDataSetChanged() 
	{
		try
		{
			callDataDBObjects = sipgateDBAdapter.getAllCallData();
			
			nameCache.clear();
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyDataSetChanged()", e);
		}
				
		super.notifyDataSetChanged();
	}
}
