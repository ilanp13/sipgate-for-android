package com.sipgate.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.sipgate.ui.SipgateFrames;

/**
 * Redirects the user to the call tab in the main activity.
 * 
 * @author graef
 * @version 1.0
 *
 */
public class SipgateFramesContacts extends Activity  {
	
	/**
	 * Overwritten onCreate procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	/**
	 * Overwritten onStart procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onStart() {
		super.onStart();
	}
	
	/**
	 * Overwritten onResume procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onResume() {
		super.onResume();
		/*
		 * redirect the user to main activity
		 */
        Intent notificationIntent = new Intent(getApplicationContext(), SipgateFrames.class);
        notificationIntent.putExtra("view", SipgateFrames.SipgateTab.CONTACTS);
        startActivity(notificationIntent);
	}
}
