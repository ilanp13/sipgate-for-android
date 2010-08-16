package com.sipgate.service;

import com.sipgate.api.types.Event;
import com.sipgate.models.SipgateCallData;

interface EventService {

	List<Event> getVoicemails();
	List<SipgateCallData> getCalls();
	
	void registerOnEventsIntent(in PendingIntent i);
	void unregisterOnEventsIntent(in PendingIntent i);
	void refreshVoicemails();
	void refreshCalls();
}
