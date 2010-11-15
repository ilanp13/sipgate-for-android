package com.sipgate.service;

interface EventService {
	void registerOnContactsIntent(in PendingIntent i);
	void unregisterOnContactsIntent(in PendingIntent i);
	void registerOnCallsIntents(in String s, in PendingIntent i);
	void unregisterOnCallsIntents(in String s);
	void registerOnVoiceMailsIntent(in PendingIntent i);
	void unregisterOnVoiceMailsIntent(in PendingIntent i);

	void initCallRefreshTimer();
	void initVoicemailRefreshTimer();
	void initContactRefreshTimer();
}
