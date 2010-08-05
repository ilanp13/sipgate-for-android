package com.sipgate.models;

import java.io.Serializable;

public class SipgateContactNumber implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8279156041422307461L;

	public enum PhoneType {
		HOME, MOBILE, WORK, WORK_FAX, HOME_FAX, PAGER, OTHER, CUSTOM, // 1.x
		ASSISTANT, CALLBACK, CAR, COMPANY_MAIN, ISDN, MAIN, MMS, OTHER_FAX, // additional 2.x
		RADIO, TELEX, TTY_TDD, WORK_MOBILE, WORK_PAGER;//additional 2.x
	}
	
	private PhoneType phoneType;
	private String phoneNumber;
	private String unformattedPhoneNumber;
	
	public SipgateContactNumber(PhoneType phoneType, String phoneNumber, String unformattedPhoneNumber) {
		this.phoneNumber = phoneNumber;
		this.unformattedPhoneNumber = unformattedPhoneNumber;
		this.phoneType = phoneType;
	}
	
	public PhoneType getPhoneType() {
		return phoneType;
	}
	
	public void setPhoneType(PhoneType phoneType) {
		this.phoneType = phoneType;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setUnformattedPhoneNumber(String unformattedPhoneNumber) {
		this.unformattedPhoneNumber = unformattedPhoneNumber;
	}

	public String getUnformattedPhoneNumber() {
		return unformattedPhoneNumber;
	}

}
