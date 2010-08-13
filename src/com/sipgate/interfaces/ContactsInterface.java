package com.sipgate.interfaces;

import java.util.ArrayList;

import com.sipgate.models.SipgateContact;

public interface ContactsInterface {

	public ArrayList<SipgateContact> getContacts();
	
	public SipgateContact getContact(Integer id);
	
	public String getContactName(String phoneNumber);
	
}
