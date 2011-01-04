package com.sipgate.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.models.holder.ContactViewHolder;

public class ContactListAdapter extends BaseAdapter implements SectionIndexer
{
	private final static String TAG = "ContactListAdapter";

	private SipgateDBAdapter sipgateDBAdapter = null;
	
	private LayoutInflater mInflater = null;
	
	private Vector<ContactDataDBObject> contactDataDBObjects = null;
	
	private ContactViewHolder holder = null;
	
	private ContactDataDBObject currentContactDataDBObject = null;
	private ContactDataDBObject lastContactDataDBObject = null;
	private ContactDataDBObject nextContactDataDBObject = null;
		
	private String currentDisplayName = null;
	private String lastDisplayName = null;
	private String nextDisplayName = null;
	
	private String currentFirstLetter = null;
	private String lastFirstLetter = null;
	private String nextFirstLetter = null;
		
	private HashMap <String, Integer> index = null;
	private Object[] sections = null;
		
	public ContactListAdapter(Activity activity, SipgateDBAdapter sipgateDBAdapter) 
	{
		this.sipgateDBAdapter = sipgateDBAdapter;
	
		mInflater = activity.getLayoutInflater();
		
		index = new HashMap<String, Integer>();
		
		contactDataDBObjects = sipgateDBAdapter.getAllContactData();

		refreshIndex(contactDataDBObjects);
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
			return contactDataDBObjects.elementAt(position);
		}
	
		return null;
	}
	
	public long getItemId(int position) 
	{
		if (getCount() >= position)
		{
			return position;
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
			convertView = mInflater.inflate(R.layout.sipgate_contacts_list_bit, null);
			holder = new ContactViewHolder();
			holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
			holder.category = (TextView) convertView.findViewById(R.id.ContactsLetterTextView);
			holder.separator = (View) convertView.findViewById(R.id.ContactsSeparator);
			convertView.setTag(holder);

		} else 
		{
			holder = (ContactViewHolder) convertView.getTag();
		}
				
		currentContactDataDBObject = (ContactDataDBObject) getItem(position);

		if (currentContactDataDBObject != null)
		{
			if (currentContactDataDBObject.getDisplayName() != null ||  currentContactDataDBObject.getDisplayName().length() > 0)
			{
				currentDisplayName = currentContactDataDBObject.getDisplayName();
			}
			else
			{
				currentDisplayName = currentContactDataDBObject.getFirstName();
				
				if (currentDisplayName.length() > 0)
				{
					currentDisplayName += " ";
				}
				
				currentDisplayName += currentContactDataDBObject.getLastName();
			}
			
			currentFirstLetter = currentDisplayName.substring(0,1).toUpperCase();
 
			holder.contactName.setText(currentDisplayName);
			holder.category.setText(currentFirstLetter);
						
			if (position > 0) 
			{
				lastContactDataDBObject = (ContactDataDBObject) getItem(position - 1);
			
				if (lastContactDataDBObject.getDisplayName() != null ||  lastContactDataDBObject.getDisplayName().length() > 0)
				{
					lastDisplayName = lastContactDataDBObject.getDisplayName();
				}
				else
				{
					lastDisplayName = lastContactDataDBObject.getFirstName();
					
					if (lastDisplayName.length() > 0)
					{
						lastDisplayName += " ";
					}
					
					lastDisplayName += lastContactDataDBObject.getLastName();
				}
				
				lastFirstLetter = lastDisplayName.substring(0,1).toUpperCase();
								
				if (lastFirstLetter.equalsIgnoreCase(currentFirstLetter)) 
				{
					holder.category.setVisibility(View.GONE);
				} 
				else 
				{
					holder.category.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				holder.category.setVisibility(View.VISIBLE);
			}
						
			if (position < getCount() - 1) 
			{
				nextContactDataDBObject = (ContactDataDBObject) getItem(position + 1);
			
				if (nextContactDataDBObject.getDisplayName() != null ||  nextContactDataDBObject.getDisplayName().length() > 0)
				{
					nextDisplayName = nextContactDataDBObject.getDisplayName();
				}
				else
				{
					nextDisplayName = nextContactDataDBObject.getFirstName();
					
					if (nextDisplayName.length() > 0)
					{
						nextDisplayName += " ";
					}
					
					nextDisplayName += nextContactDataDBObject.getLastName();
				}
				
				nextFirstLetter = nextDisplayName.substring(0, 1).toUpperCase();
				
				if (!nextFirstLetter.equalsIgnoreCase(currentFirstLetter)) 
				{
					holder.separator.setVisibility(View.GONE);
				}
				else 
				{
					holder.separator.setVisibility(View.VISIBLE);
				}
			}
		}
		
		return convertView;
	}
	
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return (contactDataDBObjects.size() == 0);
	}

	@Override
	public int getCount()
	{
		return contactDataDBObjects.size();
	}
	
	@Override
	public void notifyDataSetChanged() 
	{
		try
		{
			contactDataDBObjects = sipgateDBAdapter.getAllContactData();
		
			refreshIndex(contactDataDBObjects);
		}
		catch (Exception e) 
		{
			Log.e(TAG, "notifyDataSetChanged()", e);
		}
		
		super.notifyDataSetChanged();
	}

	public void refreshIndex(Vector<ContactDataDBObject> contactDataDBObjects)
	{	
		int size = contactDataDBObjects.size();
		
		ContactDataDBObject contactDataDBObject = null;
		
		index.clear();
		
		for (int i = size - 1; i >= 0; i--) 
		{
			contactDataDBObject = contactDataDBObjects.get(i);
			
			if (contactDataDBObject.getDisplayName() != null ||  contactDataDBObject.getDisplayName().length() > 0)
			{
				currentDisplayName = contactDataDBObject.getDisplayName();
			}
			else
			{
				currentDisplayName = contactDataDBObject.getFirstName();
				
				if (currentDisplayName.length() > 0)
				{
					currentDisplayName += " ";
				}
				
				currentDisplayName += contactDataDBObject.getLastName();
			}
			
			currentFirstLetter = currentDisplayName.substring(0,1).toUpperCase();
			
	        index.put(currentFirstLetter, i);
        } 
				
		Set<String> keys = index.keySet();
		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>(); 
		
		while (it.hasNext()) 
		{
			keyList.add(it.next());
		}
		
		Collections.sort(keyList);
		
		sections = new String[keyList.size()];
		
		keyList.toArray(sections);
	}
		
	@Override
	public int getPositionForSection(int section)
	{
		return index.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position)
	{
		return 0;
	}

	@Override
	public Object[] getSections()
	{
		return sections;		
	}
}
