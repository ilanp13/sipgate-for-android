package com.sipgate.service;

interface EventService {
	void registerOnContactsIntent(in PendingIntent i);
	void unregisterOnContactsIntent(in PendingIntent i);
	void registerOnCallsIntent(in PendingIntent i);
	void unregisterOnCallsIntent(in PendingIntent i);
	void registerOnVoiceMailsIntent(in PendingIntent i);
	void unregisterOnVoiceMailsIntent(in PendingIntent i);
	
	void refreshContacts();
	void refreshVoicemails();
	void refreshCalls();
}
