package com.sipgate.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sipgate.R;

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
	
		Intent intent = new Intent(getApplicationContext(), Login.class);
		startActivity(intent);
	}	
	
}
