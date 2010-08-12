package com.sipgate.service;

import com.sipgate.api.types.Event;

interface EventService {

	List<Event> getEvents();
	
	void registerOnEventsIntent(in PendingIntent i);
	void unregisterOnEventsIntent(in PendingIntent i);
	void refreshEvents();
}
