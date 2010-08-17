package com.sipgate.interfaces;

import java.util.ArrayList;

import com.sipgate.models.SipgateContact;

public interface ContactsInterface {

	public ArrayList<SipgateContact> getContacts();
	
	public SipgateContact getContactById(Integer id);
	
	public SipgateContact getContact(Integer index);
	
	public String getContactName(String phoneNumber);
	
	public int getCount();
}
