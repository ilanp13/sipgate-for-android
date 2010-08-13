package com.sipgate.util;

import com.sipgate.R;
import com.sipgate.ui.SipgateFramesMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationClient {
	private static final String TAG = "NotificationClient";
	
	public enum NotificationType { VOICEMAIL, CALL };
	
	private static NotificationClient singleton = null;
	private NotificationManager notificationManager = null;
	private Context context = null;

	private NotificationClient(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	synchronized public static NotificationClient getInstance(Context context) {
		if (singleton == null) {
			singleton = new NotificationClient(context);
		}

		return singleton;
	}
	
	public void setNotification(NotificationClient.NotificationType notificationType, int drawable, String message) {
		Notification notification = new Notification(drawable, message, 0 );
		notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;

		Intent notificationIntent = new Intent(this.context, SipgateFramesMessage.class);

		Log.d("createNewMessagesNotification","Executed");
		PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this.context, this.context.getResources().getText(R.string.sipgate), message, contentIntent);
		notificationManager.notify(notificationType.ordinal(), notification);
		Log.d(TAG,"send notification");
	}
	
	public void deleteNotification(NotificationClient.NotificationType notificationType) {
		this.notificationManager.cancel(notificationType.ordinal());
	}
	
}
