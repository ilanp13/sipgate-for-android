package com.sipgate.models;

import java.io.Serializable;

public class SipgateBalanceData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6164594903220210483L;
	private String currency = null;
	private String totalIncludingVat = null;
	private String vatPercent = null;

	public String getCurrency() {
		return currency;
	}

	public void setCurreny(String currency) {
		this.currency = currency;
	}

	public String getTotal() {
		return totalIncludingVat;
	}

	public void setTotal(String total) {
		this.totalIncludingVat = total;
	}
	
	public String getVatPercent() {
		return vatPercent;
	}

	public void setVatPercent(String vat) {
		this.vatPercent = vat;
	}
}
