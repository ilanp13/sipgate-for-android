package com.sipgate.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sipgate.R;
import com.sipgate.util.PhoneNumberFormatter;

public class StartSipgate extends Activity {
//	private static final String TAG = "Start";
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		setContentView(R.layout.splash);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		PhoneNumberFormatter formatter = new PhoneNumberFormatter();
		Locale locale = Locale.getDefault();
		formatter.formattedPhoneNumberFromStringWithCountry("0", locale.getCountry());
	
		Intent intent = new Intent(getApplicationContext(), Login.class);
		startActivity(intent);
	}	
	
}
