package com.sipgate.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReciever extends BroadcastReceiver {
	private static final String TAG = "BootReciever";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG,"recieved boot message");
		Intent serviceIntent = new Intent(context,EventServiceImpl.class);
		serviceIntent.setAction(EventServiceImpl.ACTION_START_ON_BOOT);
		context.startService(serviceIntent);
	}
	

}
