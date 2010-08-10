package com.sipgate.util;

import java.io.InputStream;
import java.util.ArrayList;

import com.sipgate.R;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.sipgate.interfaces.ContactsInterface;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.SipgateContactNumber;

public class AndroidContactsClient2x implements ContactsInterface {
	private final String TAG = "AndroidContactsClient2x";

	private Activity activity = null;
	
	public AndroidContactsClient2x(Activity activity){
		this.activity = activity;
	}
	
	
	public ArrayList<SipgateContact> getContacts() {
		ArrayList<SipgateContact> contactsList = null;
		
		// Make the query. 
		Cursor managedCursor = this.activity.managedQuery(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

		
		if (managedCursor.moveToFirst()) {
			contactsList = new ArrayList<SipgateContact>();
			do {
	            // Get the field values
				Integer id = managedCursor.getInt(managedCursor.getColumnIndex(ContactsContract.Contacts._ID));
	            String lastName = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	            if (lastName == null){
	            	Log.d(TAG, "no name");
	            	continue;
	            }
	            
	            String firstName = null;
	            String title = null;

	            ArrayList<SipgateContactNumber> numbers = getPhonerNumbers(id);
	            
	            Bitmap photo = getPhoto(id);
	           
	            contactsList.add(new SipgateContact(id, firstName, lastName, title, numbers, photo));

			} while (managedCursor.moveToNext());
		}
		
		return contactsList;
	}

	
	public SipgateContact getContact(Integer id) {
		SipgateContact contact = null;
		
		// Make the query. 
		Cursor managedCursor = this.activity.managedQuery(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

		
		if (managedCursor.moveToFirst()) {
			do {
				Integer tempID = managedCursor.getInt(managedCursor.getColumnIndex(ContactsContract.Contacts._ID));
				
				if (tempID == id){
		            // Get the field values
			        String lastName = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		            if (lastName != null){
		                String firstName = null;
		                String title = null;
		
		                ArrayList<SipgateContactNumber> numbers = getPhonerNumbers(id);
		                
		                Bitmap photo = getPhoto(id);
		               
		                contact = new SipgateContact(id, firstName, lastName, title, numbers, photo);
		            }
		            break;
				}
			} while (managedCursor.moveToNext());
		}
		
		return contact;
	}
	
	private ArrayList<SipgateContactNumber> getPhonerNumbers(Integer id) {
        ArrayList<SipgateContactNumber> numbers = null;
        
    	
        Cursor personCursor = this.activity.managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		    null, 
		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		    new String[]{id.toString()}, null);


    	if (personCursor.moveToFirst()){
    		numbers = new ArrayList<SipgateContactNumber>();
        	do{
        		String number = personCursor.getString(personCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        		String unformattedNumber = personCursor.getString(personCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "").replace(" ", "");
        		SipgateContactNumber.PhoneType type = null;
        		
        		switch (personCursor.getInt(personCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))){
        			case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
        				type = SipgateContactNumber.PhoneType.ASSISTANT;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
        				type = SipgateContactNumber.PhoneType.CALLBACK;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
        				type = SipgateContactNumber.PhoneType.CAR;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
        				type = SipgateContactNumber.PhoneType.COMPANY_MAIN;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
        				type = SipgateContactNumber.PhoneType.HOME_FAX;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
        				type = SipgateContactNumber.PhoneType.WORK_FAX;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
        				type = SipgateContactNumber.PhoneType.HOME;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
        				type = SipgateContactNumber.PhoneType.ISDN;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
        				type = SipgateContactNumber.PhoneType.MAIN;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
        				type = SipgateContactNumber.PhoneType.MMS;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
        				type = SipgateContactNumber.PhoneType.MOBILE;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
        				type = SipgateContactNumber.PhoneType.OTHER;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
        				type = SipgateContactNumber.PhoneType.OTHER_FAX;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
        				type = SipgateContactNumber.PhoneType.PAGER;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
        				type = SipgateContactNumber.PhoneType.RADIO;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
        				type = SipgateContactNumber.PhoneType.TELEX;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
        				type = SipgateContactNumber.PhoneType.TTY_TDD;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        				type = SipgateContactNumber.PhoneType.WORK;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
        				type = SipgateContactNumber.PhoneType.WORK_MOBILE;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
        				type = SipgateContactNumber.PhoneType.WORK_PAGER;
        				break;
        			case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
        				type = SipgateContactNumber.PhoneType.CUSTOM;
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
    	// get photos
    	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
    	InputStream input = null;
    	input = ContactsContract.Contacts.openContactPhotoInputStream(this.activity.getContentResolver(), uri);
    	Bitmap photo = null;
    	if (input != null) {
    		photo = BitmapFactory.decodeStream(input);
    	} else {
    		photo = BitmapFactory.decodeResource(this.activity.getResources(), R.drawable.ic_contact_picture);
    	}
    	return photo;
	}

}
