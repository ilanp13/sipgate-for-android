package com.sipgate.util;

import java.util.ArrayList;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Build;

import com.sipgate.interfaces.ContactsInterface;
import com.sipgate.models.SipgateContact;

public class AndroidContactsClient implements ContactsInterface {

	private ContactsInterface contactsInterface = null;
	
	public AndroidContactsClient(Activity activity) {
		if (Integer.parseInt(Build.VERSION.SDK) >= 5){
			this.contactsInterface = new AndroidContactsClient2x(activity);
		}
		else{
			this.contactsInterface = new AndroidContactsClient1x(activity);
		}	
	}
	
	public ArrayList<SipgateContact> getContacts() {
		return this.contactsInterface.getContacts();
	}

	
	public SipgateContact getContact(Integer index) {
		return this.contactsInterface.getContact(index);
	}
	
	public SipgateContact getContactById(Integer id) {
		return this.contactsInterface.getContactById(id);
	}
	
	public String getContactName(String phoneNumber) {
		return this.contactsInterface.getContactName(phoneNumber);
	}

	@Override
	public int getCount() {
		return this.contactsInterface.getCount();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		this.contactsInterface.registerDataSetObserver(observer);
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.contactsInterface.unregisterDataSetObserver(observer);
	}

	@Override
	public ArrayList<SipgateContact> getContacts(boolean withPicture)
	{
		return this.contactsInterface.getContacts(withPicture);
	}

	@Override
	public SipgateContact getContactById(Integer id, boolean withPicture)
	{
		return this.contactsInterface.getContactById(id, withPicture);
	}

	@Override
	public SipgateContact getContact(Integer index, boolean withPicture)
	{
		return this.contactsInterface.getContact(index, withPicture);
	}
	
}
