package com.sipgate.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.VoiceMailDataDBObject;
import com.sipgate.models.holder.VoiceMailViewHolder;

public class VoiceMailListAdapter extends BaseAdapter
{
	private final static String TAG = "VoiceMailListAdapter";

	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private LayoutInflater mInflater = null;
	
	private Vector<VoiceMailDataDBObject> voiceMailDataDBObjects = null;
	
	private String unknownCallerString = null;
	private String secondsText = null;
	
	private VoiceMailViewHolder holder = null;
	
	private VoiceMailDataDBObject currentVoiceMailDataDBObject = null;
	private VoiceMailDataDBObject lastVoiceMailDataDBObject = null;
		
	private String remoteName = null;
	private String remoteNumberPretty = null;

	private Calendar currentDayCalendar = null;
	private Calendar lastDayCalendar = null;
	
	private SimpleDateFormat timeFormatter = null;
	private SimpleDateFormat dateFormatter = null;
		
	private boolean isRead = false;
	private String transcription = null;
	
	private Drawable readIcon = null;
	private Drawable unreadIcon = null;
	
	public VoiceMailListAdapter(Activity activity, SipgateDBAdapter sipgateDBAdapter) 
	{
		this.sipgateDBAdapter = sipgateDBAdapter;
		
		mInflater = activity.getLayoutInflater();
		
		voiceMailDataDBObjects = sipgateDBAdapter.getAllVoiceMailData();
				
		unknownCallerString = activity.getResources().getString(R.string.sipgateUnknownCaller);
		secondsText =  activity.getResources().getString(R.string.sipgate_seconds);

		dateFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.sipgateDateTimeFormatForDay));
		timeFormatter = new SimpleDateFormat(activity.getResources().getString(R.string.sipgateDateTimeFormatForTime));
		
		currentDayCalendar = Calendar.getInstance();
		lastDayCalendar = Calendar.getInstance();
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
		if (getCount() >= position)
		{
			return voiceMailDataDBObjects.elementAt(position);
		}
	
		return null;
	}
	
	public long getItemId(int position) 
	{
		if (getCount() >= position)
		{
			return voiceMailDataDBObjects.elementAt(position).getId();
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
			convertView = mInflater.inflate(R.layout.sipgate_voicemail_list_bit, null);
			holder = new VoiceMailViewHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.EventTitle);
			holder.timeView = (TextView) convertView.findViewById(R.id.DateTextView);
			holder.categoryView = (TextView) convertView.findViewById(R.id.sipgateVoiceMailListBitCategoryTextView);
			holder.transcriptionView = (TextView) convertView.findViewById(R.id.TranscriptionTextView);
			holder.iconVM = (ImageView) convertView.findViewById(R.id.sipgateVoiceMailListBitIconView);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (VoiceMailViewHolder) convertView.getTag();
		}
		
		currentVoiceMailDataDBObject = (VoiceMailDataDBObject) getItem(position);

		if (currentVoiceMailDataDBObject != null)
		{
			isRead = currentVoiceMailDataDBObject.isRead();
					
			if(isRead) 
			{
				holder.nameView.setTypeface(Typeface.DEFAULT);
			}
			else 
			{
				holder.nameView.setTypeface(Typeface.DEFAULT_BOLD);
			}
	
			remoteNumberPretty = currentVoiceMailDataDBObject.getRemoteNumberPretty();
			remoteName = currentVoiceMailDataDBObject.getRemoteName();
			
			if (remoteName == null || remoteName.length() == 0)
			{
				remoteName = remoteNumberPretty;
				
				if (remoteName == null || remoteName.length() == 0)
				{
					remoteName = unknownCallerString;
				}
			}
			
			holder.nameView.setText(remoteName);
					
			currentDayCalendar.setTimeInMillis(currentVoiceMailDataDBObject.getTime());
			
			holder.categoryView.setText(dateFormatter.format(currentDayCalendar.getTime()));
			holder.timeView.setText(timeFormatter.format(currentDayCalendar.getTime()));
			
			transcription = currentVoiceMailDataDBObject.getTranscription();
			
			if (transcription.length() == 0) 
			{
				holder.transcriptionView.setText(currentVoiceMailDataDBObject.getDuration() + " " + secondsText);
			}
			else 
			{
				holder.transcriptionView.setText(transcription);
			}
			
			holder.categoryView.setVisibility(View.VISIBLE);
	
			if (position > 0) 
			{
				lastVoiceMailDataDBObject = (VoiceMailDataDBObject)getItem(position - 1);
			
				lastDayCalendar.setTimeInMillis(lastVoiceMailDataDBObject.getTime());
							
				if (lastDayCalendar.get(Calendar.DAY_OF_YEAR) == currentDayCalendar.get(Calendar.DAY_OF_YEAR) &&
					lastDayCalendar.get(Calendar.YEAR) == currentDayCalendar.get(Calendar.YEAR)) 
				{
					holder.categoryView.setVisibility(View.GONE);
				} 
				else 
				{
					holder.categoryView.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				holder.categoryView.setVisibility(View.VISIBLE);
			}

			markAsSeen(currentVoiceMailDataDBObject); 
		}
		
		markAsSeen(currentVoiceMailDataDBObject); 
		
		return convertView;
	}
	
	private void markAsSeen(final VoiceMailDataDBObject voiceMailDataDBObject) 
	{
		if (!voiceMailDataDBObject.isSeen()) 
		{
			Thread markThread = new Thread()
			{
				public void run()
				{
					try 
					{
						voiceMailDataDBObject.setSeen(true);
						
						sipgateDBAdapter.update(voiceMailDataDBObject);
					} 
					catch (Exception e)
					{
						Log.e(TAG, "markAsSeen()", e);
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
		return (voiceMailDataDBObjects.size() == 0);
	}

	@Override
	public int getCount()
	{
		return voiceMailDataDBObjects.size();
	}
	
	@Override
	public void notifyDataSetChanged() 
	{
		Log.d(TAG, "notifyDataSetChanged()");
		
		try
		{
			voiceMailDataDBObjects = sipgateDBAdapter.getAllVoiceMailData();
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyDataSetChanged()", e);
		}
		
		super.notifyDataSetChanged();
	}
}
