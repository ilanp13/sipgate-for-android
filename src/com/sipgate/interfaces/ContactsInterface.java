package com.sipgate.interfaces;

import java.util.ArrayList;

import android.database.DataSetObserver;

import com.sipgate.models.SipgateContact;

public interface ContactsInterface {

	public ArrayList<SipgateContact> getContacts();
	
	public SipgateContact getContactById(Integer id);
	
	public SipgateContact getContact(Integer index);
	
	public String getContactName(String phoneNumber);
	
	public int getCount();
	
	public void registerDataSetObserver(DataSetObserver observer);

	public void unregisterDataSetObserver(DataSetObserver observer);

}
