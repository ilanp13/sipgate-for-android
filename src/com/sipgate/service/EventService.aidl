package com.sipgate.service;

import com.sipgate.api.types.Event;
import com.sipgate.models.SipgateCallData;

interface EventService {

	List<Event> getVoicemails();
	List<SipgateCallData> getCalls();
	
	void registerOnVoicemailsIntent(in PendingIntent i);
	void unregisterOnVoicemailsIntent(in PendingIntent i);
	void registerOnCallsIntent(in PendingIntent i);
	void unregisterOnCallsIntent(in PendingIntent i);
	
	void refreshVoicemails();
	void refreshCalls();
}
