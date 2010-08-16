package com.sipgate.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sipgate.R;
import com.sipgate.util.PhoneNumberFormatter;

/**
 * The start activity of the application.
 * 
 * @author knuth
 * @version 1.1
 *
 */
public class StartSipgate extends Activity {
//	private static final String TAG = "StartSipgate";
	
	/**
	 * Overwritten onCreate procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		/*
		 * show the splash screen on startup
		 */
		setContentView(R.layout.splash);
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
	 * Overwritten onCreate procedure
	 * 
	 * @since 1.0
	 */
	@Override
	public void onResume() {
		super.onResume();

		/*
		 * initialise the phone number formatter
		 */
		PhoneNumberFormatter formatter = new PhoneNumberFormatter();
		Locale locale = Locale.getDefault();
		formatter.formattedPhoneNumberFromStringWithCountry("0", locale.getCountry());
	
		/*
		 * redirect the user to the login activity
		 */
		Intent intent = new Intent(getApplicationContext(), Login.class);
		startActivity(intent);
	}	
	
}
