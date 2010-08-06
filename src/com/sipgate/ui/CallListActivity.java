package com.sipgate.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sipgate.models.SipgateCallData;
import com.sipgate.util.ApiServiceProvider;

public class CallListActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		ApiServiceProvider client = ApiServiceProvider.getInstance(getApplicationContext());
		try {
			ArrayList<SipgateCallData> calls = client.getCalls();
			calls.add(new SipgateCallData());
		} catch (Exception e) {
			Log.e("Liste", e.getLocalizedMessage());
		}

	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}	
	
}

