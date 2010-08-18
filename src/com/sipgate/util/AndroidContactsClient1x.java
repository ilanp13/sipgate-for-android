package com.sipgate.util;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;

import com.sipgate.R;
import com.sipgate.interfaces.ContactsInterface;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.SipgateContactNumber;

@SuppressWarnings("deprecation")
public class AndroidContactsClient1x implements ContactsInterface {
	private final String TAG = "AndroidContactsClient1x";

	private Activity activity = null;

	private Cursor managedCursor;

	public AndroidContactsClient1x(Activity activity) {
		this.activity = activity;

		// Form an array specifying which columns to return.
		String[] projection = new String[] { People._ID, People.NAME, People.PRIMARY_PHONE_ID, };

		// Make the query.
		this.managedCursor = this.activity.managedQuery(People.CONTENT_URI, projection, // Which
				// columns
				// to
				// return
				People.NUMBER+" IS NOT NULL", null, getContactSortOrder());
	}

	public ArrayList<SipgateContact> getContacts() {
		ArrayList<SipgateContact> contactsList = null;

		if (managedCursor.moveToFirst()) {
			contactsList = new ArrayList<SipgateContact>();
			do {
				// Get the field values
				Integer id = managedCursor.getInt(managedCursor.getColumnIndex(People._ID));
				String lastName = managedCursor.getString(managedCursor.getColumnIndex(People.NAME));
				if (lastName == null) {
					Log.d(TAG, "no name");
					continue;
				}

				String firstName = null;
				String title = null;

				ArrayList<SipgateContactNumber> numbers = getPhoneNumbers(id);
				if (numbers == null)
					continue;

				Bitmap photo = getPhoto(id);

				contactsList.add(new SipgateContact(id, firstName, lastName, title, numbers, photo));

			} while (managedCursor.moveToNext());
		}

		return contactsList;
	}

	public String getContactName(String phoneNumber) {
		// define the columns I want the query to return
		String[] projection = new String[] { Contacts.Phones.DISPLAY_NAME, Contacts.Phones.NUMBER };
		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(phoneNumber));
		// query time
		Cursor c = this.activity.managedQuery(contactUri, projection, null, null, null);
		// if the query returns 1 or more results
		// return the first result
		if (c.moveToFirst()) {
			String name = c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
			return name;
		}
		// return the original number if no match was found
		return phoneNumber;
	}

	public SipgateContact getContactById(Integer id) {
		SipgateContact contact = null;

		if (managedCursor.moveToFirst()) {
			do {
				Integer tempID = managedCursor.getInt(managedCursor.getColumnIndex(People._ID));

				if (tempID == id) {
					String lastName = managedCursor.getString(managedCursor.getColumnIndex(People.NAME));
					if (lastName != null) {
						String firstName = null;
						String title = null;

						ArrayList<SipgateContactNumber> numbers = getPhoneNumbers(id);

						Bitmap photo = getPhoto(id);

						contact = new SipgateContact(id, firstName, lastName, title, numbers, photo);
					}
					break;
				}
			} while (managedCursor.moveToNext());
		}

		return contact;
	}

	public SipgateContact getContact(Integer index) {
		SipgateContact contact = null;

		try {
			if (managedCursor.moveToPosition(index)) {
				Integer id = managedCursor.getInt(managedCursor.getColumnIndex(People._ID));

				String lastName = managedCursor.getString(managedCursor.getColumnIndex(People.NAME));
				if (lastName != null) {
					String firstName = null;
					String title = null;

					ArrayList<SipgateContactNumber> numbers = getPhoneNumbers(id);

					Bitmap photo = getPhoto(id);

					contact = new SipgateContact(id, firstName, lastName, title, numbers, photo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return contact;
	}

	private ArrayList<SipgateContactNumber> getPhoneNumbers(Integer id) {
		ArrayList<SipgateContactNumber> numbers = null;
		Cursor personCursor = this.activity.managedQuery(Contacts.Phones.CONTENT_URI, null, Contacts.Phones.PERSON_ID
				+ " = ?", new String[] { id.toString() }, null);

		if (personCursor.moveToFirst()) {
			numbers = new ArrayList<SipgateContactNumber>();
			do {
				String number = personCursor.getString(personCursor.getColumnIndex(Contacts.Phones.NUMBER));
				String unformattedNumber = personCursor.getString(personCursor.getColumnIndex(Contacts.Phones.NUMBER))
						.replace("-", "").replace(" ", "");
				SipgateContactNumber.PhoneType type = null;

				switch (personCursor.getInt(personCursor.getColumnIndex(Contacts.Phones.TYPE))) {
				case Contacts.Phones.TYPE_CUSTOM:
					type = SipgateContactNumber.PhoneType.CUSTOM;
					break;
				case Contacts.Phones.TYPE_FAX_HOME:
					type = SipgateContactNumber.PhoneType.HOME_FAX;
					break;
				case Contacts.Phones.TYPE_FAX_WORK:
					type = SipgateContactNumber.PhoneType.WORK_FAX;
					break;
				case Contacts.Phones.TYPE_HOME:
					type = SipgateContactNumber.PhoneType.HOME;
					break;
				case Contacts.Phones.TYPE_MOBILE:
					type = SipgateContactNumber.PhoneType.MOBILE;
					break;
				case Contacts.Phones.TYPE_OTHER:
					type = SipgateContactNumber.PhoneType.OTHER;
					break;
				case Contacts.Phones.TYPE_PAGER:
					type = SipgateContactNumber.PhoneType.PAGER;
					break;
				case Contacts.Phones.TYPE_WORK:
					type = SipgateContactNumber.PhoneType.WORK;
					break;
				default:
					break;
				}

				Log.d(TAG, number);

				numbers.add(new SipgateContactNumber(type, number, unformattedNumber));
			} while (personCursor.moveToNext());
		}

		return numbers;
	}

	private Bitmap getPhoto(Integer id) {
		Bitmap photo = People.loadContactPhoto(this.activity.getApplicationContext(), ContentUris.withAppendedId(
				People.CONTENT_URI, id), R.drawable.ic_contact_picture, null);
		return photo;
	}

	@Override
	public int getCount() {
		return managedCursor.getCount();
	}
	
	private String getContactSortOrder() {
		return People.NAME+" ASC";
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		this.managedCursor.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.managedCursor.unregisterDataSetObserver(observer);
	}

}
