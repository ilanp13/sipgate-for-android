package com.sipgate.adapters;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.holder.ContactViewHolder;
import com.sipgate.ui.ContactDetailsActivity;
import com.sipgate.util.AndroidContactsClient;

public class ContactListAdapter implements ListAdapter {
	private final static String TAG = "ContactListAdapter";
	protected final Activity activity;
	private final LayoutInflater mInflater;
	private AndroidContactsClient contactsClient;

	public ContactListAdapter(Activity activity) {
		this.activity = activity;
		this.mInflater = activity.getLayoutInflater();

		this.contactsClient = new AndroidContactsClient(activity);
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
	public int getCount() {
		return this.contactsClient.getCount();
	}

	@Override
	public Object getItem(int position) {
		return this.contactsClient.getContact(position);
	}

	@Override
	public long getItemId(int position) {
		return this.contactsClient.getContact(position).getId();
	}

	@Override
	public int getItemViewType(int position) {
		// TODO FIXME
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final SipgateContact item = (SipgateContact) getItem(position);

		ContactViewHolder holder = null;
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

		String name = item.getLastName();
		if (name != null) {
			Log.d(TAG, name);
		} else {
			Log.d(TAG, "no name");
		}
		String thisFirstLetter = name.substring(0, 1);

		holder.contactName.setText(name);
		holder.category.setText(thisFirstLetter);

		Bitmap photo = item.getPhoto();
		if (photo != null) {
			holder.contactImage.setImageBitmap(photo);
			Log.d(TAG, "has pic");
		} else {
			Log.d(TAG, "no pic");
		}

		if (position >= 1) {
			SipgateContact lastItem = (SipgateContact) getItem(position - 1);
			String firstLetter = lastItem.getLastName().substring(0, 1);
			if (firstLetter.equalsIgnoreCase(thisFirstLetter)) {
				holder.category.setVisibility(View.GONE);
			} else {
				holder.category.setVisibility(View.VISIBLE);
			}
		}

		if (position < getCount() - 1) {
			SipgateContact nextItem = (SipgateContact) getItem(position + 1);
			String firstLetter = nextItem.getLastName().substring(0, 1);
			if (!firstLetter.equalsIgnoreCase(thisFirstLetter)) {
				holder.separator.setVisibility(View.GONE);
			} else {
				holder.separator.setVisibility(View.VISIBLE);
			}
		}

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		// TODO FIXME
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		if (this.getCount() > 0) {
			return false;
		}
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO FIXME
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO FIXME
	}

}
