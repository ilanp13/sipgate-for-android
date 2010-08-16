package com.sipgate.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Recieves the boot broadcast and starts the background service
 * 
 * @author Marcus Hunger
 * @version 1.0
 *
 */
public class BootReciever extends BroadcastReceiver {
	private static final String TAG = "BootReciever";
	
	/**
	 * Overwritten onRecieve procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG,"recieved boot message");
		/*
		 * start the background service
		 */
		Intent serviceIntent = new Intent(context,SipgateBackgroundService.class);
		serviceIntent.setAction(SipgateBackgroundService.ACTION_START_ON_BOOT);
		context.startService(serviceIntent);
	}
	

}
