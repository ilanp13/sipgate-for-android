package com.sipgate.sipua.phone;

import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.Contacts.People;
import android.text.GetChars;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.sipgate.R;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.db.SystemDataDBObject;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.util.NotificationClient;
import com.sipgate.util.PhoneNumberFormatter;

/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2006 The Android Open Source Project
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * {@hide}
 */
@SuppressWarnings("deprecation")
public class Connection
{
    public enum DisconnectCause {
        NOT_DISCONNECTED,   /* has not yet disconnected */
        INCOMING_MISSED,    /* an incoming call that was missed and never answered */
        NORMAL,             /* normal; remote */
        LOCAL,              /* normal; local hangup */
        BUSY,               /* outgoing call to busy line */
        CONGESTION,         /* outgoing call to congested network */
        MMI,                /* not presently used; dial() returns null */
        INVALID_NUMBER,     /* invalid dial string */
        LOST_SIGNAL,
        LIMIT_EXCEEDED,     /* eg GSM ACM limit exceeded */
        INCOMING_REJECTED,  /* an incoming call that was rejected */
        POWER_OFF,          /* radio is turned off explicitly */
        OUT_OF_SERVICE,     /* out of service */
        SIM_ERROR,          /* No SIM, SIM locked, or other SIM error */
        CALL_BARRED,        /* call was blocked by call barrring */
        FDN_BLOCKED         /* call was blocked by fixed dial number */
    }

    Object userData;

    /* Instance Methods */

    /** 
     * Gets address (e.g., phone number) associated with connection
     * TODO: distinguish reasons for unavailablity
     *
     * @return address or null if unavailable
     */
    String address,address2;
    public String getAddress() {
    	return address;
    }
    public String getAddress2() {
    	return address2;
    }
    public void setAddress(String a,String b) {
    	address = a;
    	address2 = b;
    }

    Call c;
    /**
     * @return Call that owns this Connection, or null if none
     */
    public Call getCall() {
    	return c;
    }
    public void setCall(Call a) {
    	c = a;
    }

     /**
     * Returns "NOT_DISCONNECTED" if not yet disconnected
     */
    public DisconnectCause getDisconnectCause() {
    	return DisconnectCause.NORMAL;
    }

   /**
     * If this Connection is connected, then it is associated with
     * a Call.
     * 
     * Returns getCall().getState() or Call.State.IDLE if not
     * connected
     */
    public Call.State getState()
    {
        Call c;

        c = getCall();

        if (c == null) { 
            return Call.State.IDLE;
        } else {
            return c.getState();
        }
    }
    
    /**
     * isAlive()
     * 
     * @return true if the connection isn't disconnected
     * (could be active, holding, ringing, dialing, etc)
     */
    public boolean
    isAlive()
    {
        return getState().isAlive();
    }

    /**
     * Returns true if Connection is connected and is INCOMING or WAITING
     */
    public boolean
    isRinging()
    {
        return getState().isRinging();
    }

    /**
     * 
     * @return the userdata set in setUserData()
     */
    public Object getUserData()
    {
        return userData;
    }

    /**
     * 
     * @param userdata user can store an any userdata in the Connection object.
     */
    public void setUserData(Object userdata)
    {
        this.userData = userdata;
    }
    
    public static Uri addCall(CallerInfo ci, Context context, String number, boolean isPrivateNumber, int callType, long start, int duration) 
    {       
        if (TextUtils.isEmpty(number)) 
        {
            if (isPrivateNumber) 
            {
                number = CallerInfo.PRIVATE_NUMBER;
            } 
            else 
            {
                number = CallerInfo.UNKNOWN_NUMBER;
            }
        }
        
        SipgateDBAdapter sipgateDBAdapter = new SipgateDBAdapter(context);
        
        CallDataDBObject callDataDBObject = new CallDataDBObject();
        
        callDataDBObject.setId(System.currentTimeMillis());
        callDataDBObject.setTemp(true);
        callDataDBObject.setRead(false);
        callDataDBObject.setTime(start);
        
        switch (callType)
		{
			case CallLog.Calls.MISSED_TYPE:
				callDataDBObject.setMissed(true);
				callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				break;
			case CallLog.Calls.INCOMING_TYPE:
				callDataDBObject.setDirection(CallDataDBObject.INCOMING);
				break;
			case CallLog.Calls.OUTGOING_TYPE:
				callDataDBObject.setDirection(CallDataDBObject.OUTGOING);
				break;
			default:
				break;
		}
        
        PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter();
        
        phoneNumberFormatter.initWithFreestyle(number, Locale.getDefault().getCountry());
       
        callDataDBObject.setRemoteNumberE164(phoneNumberFormatter.e164NumberWithPrefix("+"));
        callDataDBObject.setRemoteNumberPretty(phoneNumberFormatter.formattedPhoneNumberFromStringWithCountry(phoneNumberFormatter.e164NumberWithPrefix("+"), Locale.getDefault().getCountry()));
    	
    	sipgateDBAdapter.insert(callDataDBObject);
    	
    	if (callDataDBObject.isMissed())
        {
    		NotificationClient notifyClient = new NotificationClient(context); 
    		
    		SystemDataDBObject notifyCallsCount = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_CALLS_COUNT);
			SystemDataDBObject notifyTempCallsCount = sipgateDBAdapter.getSystemDataDBObjectByKey(SystemDataDBObject.NOTIFY_TEMP_CALLS_COUNT);
			
    	    int callsCount = 1;
    	    
    	    if (notifyTempCallsCount != null)
	        {
    	    	callsCount = callsCount + Integer.valueOf(notifyTempCallsCount.getValue());
    	    	
	        	notifyTempCallsCount.setValue(String.valueOf(callsCount));
	        	
	        	sipgateDBAdapter.update(notifyTempCallsCount);
	        }
	        else
	        {
	        	notifyTempCallsCount = new SystemDataDBObject();
	        	notifyTempCallsCount.setKey(SystemDataDBObject.NOTIFY_TEMP_CALLS_COUNT);
	        	notifyTempCallsCount.setValue(String.valueOf(callsCount));
	        	
	        	sipgateDBAdapter.insert(notifyTempCallsCount);
	        }
	        
	        if (notifyCallsCount != null)
	        {
	        	callsCount = callsCount + Integer.valueOf(notifyCallsCount.getValue());
	        }
	        
	        if(callsCount == 1)
	        {
	            notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.sipgate_call_list_icon_notification, String.format((String) context.getResources().getText(R.string.sipgate_a_new_call), Integer.valueOf(callsCount)));
	        }
			else 
			{
			    notifyClient.setNotification(NotificationClient.NotificationType.CALL, R.drawable.sipgate_call_list_icon_notification, String.format((String) context.getResources().getText(R.string.sipgate_new_calls), Integer.valueOf(callsCount)));
		    }
	    }
    	
    	sipgateDBAdapter.close();
    	
    	ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues(5);

        if (number.contains("&"))
        	number = number.substring(0,number.indexOf("&"));
        values.put(Calls.NUMBER, number);
        values.put(Calls.TYPE, Integer.valueOf(callType));
        values.put(Calls.DATE, Long.valueOf(start));
        values.put(Calls.DURATION, Long.valueOf(duration));
        values.put(Calls.NEW, Integer.valueOf(1));
        if (ci != null) {
            values.put(Calls.CACHED_NAME, ci.name);
            values.put(Calls.CACHED_NUMBER_TYPE, ci.numberType);
            values.put(Calls.CACHED_NUMBER_LABEL, ci.numberLabel);
        }

        if ((ci != null) && (ci.person_id > 0)) {
            People.markAsContacted(resolver, ci.person_id);
        }

        Uri result = resolver.insert(Calls.CONTENT_URI, values);
        
        return result;
    }

    boolean incoming;
    
    public void setIncoming(boolean a) {
    	incoming = a;
    }
    
    public boolean isIncoming() {
    	return incoming;
    }
    
    public long date;
    
    public void log(long start)
	{
	    String number = getAddress();
        long duration = (start != 0 ? SystemClock.elapsedRealtime()-start : 0);
	    boolean isPrivateNumber = false; // TODO: need API for isPrivate()
	
	    // Set the "type" to be displayed in the call log (see constants in CallLog.Calls)
	    int callLogType;
	    if (isIncoming()) {
	        callLogType = (duration == 0 ?
	                       CallLog.Calls.MISSED_TYPE :
	                       CallLog.Calls.INCOMING_TYPE);
	    } else {
	        callLogType = CallLog.Calls.OUTGOING_TYPE;
	    }
	
	    Object o = getUserData();
        CallerInfo ci;
        if ((o == null) || (o instanceof CallerInfo)){
            ci = (CallerInfo) o;
        } else {
            ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
        }
        if (callLogType == CallLog.Calls.MISSED_TYPE)
        	Receiver.onText(Receiver.MISSED_CALL_NOTIFICATION, ci != null && ci.name != null?ci.name:number, android.R.drawable.stat_notify_missed_call, 0);

        addCall(ci, Receiver.mContext, number, isPrivateNumber, callLogType, date, (int) duration / 1000);
	}
}