package com.sipgate.service;

interface EventService {
	void registerOnContactsIntent(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	void registerOnCallsIntents(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	void registerOnVoiceMailsIntent(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	
	void unregisterOnContactsIntent(in String tag);
	void unregisterOnCallsIntents(in String tag);
	void unregisterOnVoiceMailsIntent(in String tag);

	void initContactRefreshTimer();
	void initCallRefreshTimer();
	void initVoicemailRefreshTimer();
}
