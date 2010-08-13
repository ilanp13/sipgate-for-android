package com.sipgate.models;

import java.io.Serializable;
import java.util.Date;

public class SipgateCallData implements Serializable {

	private class SipgateEndpointData implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8701506242154628479L;
		private String numberE164 = null;
		private String numberPretty = null;
		private String name = null;
		
		public void setNumberE164(String numberE164) {
			this.numberE164 = numberE164;
		}
		public String getNumberE164() {
			return numberE164;
		}
		public void setNumberPretty(String numberPretty) {
			this.numberPretty = numberPretty;
		}
		public String getNumberPretty() {
			return numberPretty;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}	
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1737101473384001118L;
	private String id = null;
	private String direction = null;
	private Boolean missed = null;
	private Boolean isRead = null;
	private Date time = null;
	private SipgateEndpointData target = null;
	private SipgateEndpointData source = null;
	private String readModifyUrl = null;
	
	public void setCallId(String id) {
		this.id = id;
	}
	
	public String getCallId() {
		return id;
	}
	
	public void setCallDirection(String direction) {
		this.direction = direction;
	}
	
	public String getCallDirection() {
		return direction;
	}
	
	public void setCallMissed(Boolean type) {
		this.missed = type;
	}
	
	public Boolean getCallMissed() {
		return missed;
	}
	
	public void setCallTarget(String numberE164, String numberPretty, String name) {
		this.target = new SipgateEndpointData();
		this.target.setNumberE164(numberE164);
		this.target.setNumberPretty(numberPretty);
		this.target.setName(name);
	}
	
	public String getCallTargetNumberE164() {
		return target.getNumberE164();
	}
	
	public String getCallTargetNumberPretty() {
		return target.getNumberPretty();
	}
	
	public String getCallTargetName() {
		return target.getName();
	}
	
	public void setCallSource(String numberE164, String numberPretty, String name) {
		this.source = new SipgateEndpointData();
		this.source.setNumberE164(numberE164);
		this.source.setNumberPretty(numberPretty);
		this.source.setName(name);
	}
	
	public String getCallSourceNumberE164() {
		return source.getNumberE164();
	}
	
	public String getCallSourceNumberPretty() {
		return source.getNumberPretty();
	}
	
	public String getCallSourceName() {
		return source.getName();
	}
	
	public void setCallTime(Date time) {
		this.time = time;
	}
	
	public Date getCallTime() {
		return time;
	}

	public void setCallRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Boolean getCallRead() {
		return isRead;
	}
	
	public void setCallReadModifyUrl(String readModifyUrl) {
		this.readModifyUrl = readModifyUrl;
	}

	public String getCallReadModifyUrl() {
		return readModifyUrl;
	}

}
