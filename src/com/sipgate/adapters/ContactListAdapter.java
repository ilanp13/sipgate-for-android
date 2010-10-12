package com.sipgate.adapters;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.holder.ContactViewHolder;
import com.sipgate.util.AndroidContactsClient;

public class ContactListAdapter extends BaseAdapter
{
	private final static String TAG = "ContactListAdapter";
	
	private final LayoutInflater mInflater;
	private AndroidContactsClient contactsClient;
	private HashMap<Integer,SipgateContact> contactsCacheMap = null;
	
	private ContactViewHolder holder = null;
	
	private SipgateContact sipgateContact = null;
	private SipgateContact lastSipgateContact = null;
	private SipgateContact nextSipgateContact = null;
	private SipgateContact currentContact = null;
	
	private Bitmap photo = null;
	private String displayName = null;
	private String currentFirstLetter = null;
	private String lastFirstLetter = null;
	private String nextFirstLetter = null;

	public ContactListAdapter(Activity activity) 
	{
		mInflater = activity.getLayoutInflater();
		contactsClient = new AndroidContactsClient(activity);
		contactsCacheMap = new HashMap<Integer, SipgateContact>();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public Object getItem(int position) {
		return this.getContact(position);
	}

	@Override
	public long getItemId(int position) {
		SipgateContact contact = this.getContact(position);
	
		if (contact != null) {
			return contact.getId();
		}
		
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_contacts_list_bit, null);
			holder = new ContactViewHolder();
			holder.contactName = (TextView) convertView.findViewById(R.id.contactName);
			holder.contactImage = (ImageView) convertView.findViewById(R.id.contactPhoto);
			holder.category = (TextView) convertView.findViewById(R.id.ContactsLetterTextView);
			holder.separator = (View) convertView.findViewById(R.id.ContactsSeparator);
			convertView.setTag(holder);

		} else {
			holder = (ContactViewHolder) convertView.getTag();
		}
		
		sipgateContact = (SipgateContact) getItem(position);

		if (sipgateContact == null) {
			Log.e(TAG, "item at position " + position + " is null");
			return null;
		}	

		displayName = sipgateContact.getDisplayName();
		if (displayName != null) {
			Log.d(TAG, displayName);
		} else {
			Log.d(TAG, "no name");
		}
		currentFirstLetter = displayName.substring(0, 1);

		holder.contactName.setText(displayName);
		holder.category.setText(currentFirstLetter);

		photo = sipgateContact.getPhoto();
		if (photo != null) {
			holder.contactImage.setImageBitmap(photo);
			Log.d(TAG, "has pic");
		} else {
			Log.d(TAG, "no pic");
		}

		if (position >= 1) {
			lastSipgateContact = (SipgateContact) getItem(position - 1);
			lastFirstLetter = lastSipgateContact.getDisplayName().substring(0, 1);
			if (lastFirstLetter.equalsIgnoreCase(currentFirstLetter)) {
				holder.category.setVisibility(View.GONE);
			} else {
				holder.category.setVisibility(View.VISIBLE);
			}
		}

		if (position < getCount() - 1) {
			nextSipgateContact = (SipgateContact) getItem(position + 1);
			
			// TODO fix contacts!!!
			
			if (nextSipgateContact != null)
			{
				nextFirstLetter = nextSipgateContact.getDisplayName().substring(0, 1);
				if (!nextFirstLetter.equalsIgnoreCase(currentFirstLetter)) {
					holder.separator.setVisibility(View.GONE);
				} else {
					holder.separator.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				holder.separator.setVisibility(View.VISIBLE);
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
		return (contactsClient.getCount() == 0);
	}
	
	@Override
	public int getCount() {
		return contactsClient.getCount();
	}
	
	@Override
	public void notifyDataSetChanged() {
		contactsCacheMap.clear();
		
		super.notifyDataSetChanged();
	}
	
	private SipgateContact getContact(int position) {
		currentContact = contactsCacheMap.get(position);
		if (currentContact == null) {
			currentContact = contactsClient.getContact(position);
			contactsCacheMap.put(position, currentContact);
		}
		if (currentContact == null) {
			Log.w(TAG, "getContact returning null. contactsmap has " + contactsCacheMap.size() + " items. adapter has " + getCount() + " items");
		}
		return currentContact;
	}
}
