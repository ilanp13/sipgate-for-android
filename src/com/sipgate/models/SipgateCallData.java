package com.sipgate.models;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class SipgateCallData implements Parcelable {
	protected static final String TAG = "SipgateCallData";

	private class SipgateEndpointData implements Parcelable {

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
		
		public SipgateEndpointData() {
		}
		
		@SuppressWarnings("unused")
		public SipgateEndpointData(Parcel in) {
			readFromParcel(in);
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(numberE164);
			out.writeString(numberPretty);
			out.writeString(name);
			
		}	
		public void readFromParcel(Parcel in) {
			this.numberE164 = in.readString();
			this.numberPretty = in.readString();
			this.name = in.readString();
			
		}	
	}
	
	private String id = null;
	private String direction = null;
	private String missed = null;
	private String isRead = null;
	private String time = null;
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
	
	public void setCallMissed(String type) {
		this.missed = type;
	}
	
	public Boolean getCallMissed() {
		if (missed.equals("true")) {
			return true;
		}
		return false;
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
	
	public void setCallTime(String time) {
		this.time = time;
	}
	
	public Date getCallTime() {
		try {
			if (time == null) {
				return new Date(0);
			}
			SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			return dateformatterIso.parse(time, new ParsePosition(0));
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "badly formated date");
			
		}
		return new Date(0);
	}

	public void setCallRead(String isRead) {
		this.isRead = isRead;
	}

	public Boolean getCallRead() {
		if (isRead.equals("true")) {
			return true;
		}
		return false;
	}
	
	public void setCallReadModifyUrl(String readModifyUrl) {
		this.readModifyUrl = readModifyUrl;
	}

	public String getCallReadModifyUrl() {
		return readModifyUrl;
	}

	public SipgateCallData() {
	}
	
	public SipgateCallData(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(direction);
		out.writeString(missed);
		out.writeString(isRead);
		out.writeString(time);
		out.writeParcelable((Parcelable)target, 0);
		out.writeParcelable((Parcelable)source, 0);
		out.writeString(readModifyUrl);
		
	}
	
	public void readFromParcel(Parcel in) {
		this.id = in.readString();
		this.direction = in.readString();
		this.missed = in.readString();
		this.isRead = in.readString();
		this.time = in.readString();
		//TODO: ????
		this.target = in.readParcelable(null);
		this.source = in.readParcelable(null);
		this.readModifyUrl = in.readString();
		
	}
	
    public static final Parcelable.Creator<SipgateCallData> CREATOR = new Parcelable.Creator<SipgateCallData>() {

    	public SipgateCallData createFromParcel(Parcel in) {
                return new SipgateCallData(in);
        }

        public SipgateCallData[] newArray(int size) {
                return new SipgateCallData[size];
        }
    };

}
