package com.sipgate.service;

interface EventService {
	void registerOnContactsIntent(in PendingIntent i);
	void unregisterOnContactsIntent(in PendingIntent i);
	void registerOnCallsIntents(String tag, in PendingIntent getEventsIntent, in PendingIntent newEventsIntent, in PendingIntent noEventsIntent, in PendingIntent errorIntent);
	void unregisterOnCallsIntents(in String tag);
	void registerOnVoiceMailsIntent(in PendingIntent i);
	void unregisterOnVoiceMailsIntent(in PendingIntent i);

	void initCallRefreshTimer();
	void initVoicemailRefreshTimer();
	void initContactRefreshTimer();
}
