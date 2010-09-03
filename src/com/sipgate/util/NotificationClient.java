package com.sipgate.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.sipgate.R;
import com.sipgate.ui.SipgateFrames;

/**
 * Handles the notifications for calls and voicemails and allows to remove them individually.
 * 
 * @author Karsten Knuth
 * @version 1.0
 *
 */
public class NotificationClient {
	private static final String TAG = "NotificationClient";
	
	/**
	 * Definition for the notification type.
	 * 
	 * @author Karsten Knuth
	 * @version 1.0
	 *
	 */
	public enum NotificationType { RESERVED, REGISTER_NOTIFICATION, CALL_NOTIFICATION, MISSED_CALL_NOTIFICATION, AUTO_ANSWER_NOTIFICATION, MWI_NOTIFICATION, VOICEMAIL, CALL };
	
	private NotificationManager notificationManager = null;
	private Context context = null;

	/**
	 * Constructor for the class
	 * 
	 * @param context
	 * @since 1.0
	 */
	public NotificationClient(Context context) {
		/*
		 * save the context for later use and initialise the NotificationManager
		 */
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
		
	/**
	 * Adds a notification of a certain type with a certain message and an icon.
	 * 
	 * @param notificationType The notification type
	 * @param drawable The icon to use in the notification bar
	 * @param message The message to be displayed
	 * @since 1.0
	 */
	public void setNotification(NotificationClient.NotificationType notificationType, int drawable, String message) {
		Notification notification = new Notification(drawable, message, 0 );
		notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

		/*
		 * add the right tab, which will be opened when the user clicks on the notification000
		 */
		Intent notificationIntent = new Intent(this.context, SipgateFrames.class);
		Bundle extras = new Bundle();
		switch (notificationType) {
			case CALL:
				extras.putSerializable("view", SipgateFrames.SipgateTab.CALLS);
				break;
			case VOICEMAIL:
				Log.d(TAG, "notification view: voicemail");
				extras.putSerializable("view", SipgateFrames.SipgateTab.VM);
				break;
			default:
				Log.d(TAG, "notification view: unknown");
				break;	
		}
		notificationIntent.putExtras(extras);

		Log.d("createNewMessagesNotification","Executed, type: " + notificationType);
		notificationIntent.setData(Uri.parse("sipgatetab:" + notificationType));
		
		
		PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
		
		/*
		 * add the new notification to the bar
		 */
		notification.setLatestEventInfo(this.context, this.context.getResources().getText(R.string.sipgate), message, contentIntent);
		
		notificationManager.notify(notificationType.ordinal(), notification);
		
		Log.d(TAG,"send notification");
	}
	
	/**
	 * Deletes all notification of the provided type from the notification bar
	 * 
	 * @param notificationType The notification type that you want to remove from the bar
	 * @since 1.0
	 */
	public void deleteNotification(NotificationClient.NotificationType notificationType) {
		this.notificationManager.cancel(notificationType.ordinal());
	}
	
}
