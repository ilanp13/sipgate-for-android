package com.sipgate.service;

interface EventService {
	void registerOnContactsIntents(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	void registerOnCallsIntents(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	void registerOnVoiceMailsIntents(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	
	void unregisterOnContactsIntents(in String tag);
	void unregisterOnCallsIntents(in String tag);
	void unregisterOnVoiceMailsIntents(in String tag);

	void initContactRefreshTimer();
	void initCallRefreshTimer();
	void initVoicemailRefreshTimer();
}
