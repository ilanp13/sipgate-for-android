package com.sipgate.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import com.sipgate.R;
import com.sipgate.models.SipgateCallData;
import com.sipgate.util.ApiServiceProvider;

public class CallListActivity extends Activity {
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipgate_call_list);
		
		ApiServiceProvider client = ApiServiceProvider.getInstance(getApplicationContext());
//		try {
//			ArrayList<SipgateCallData> calls = client.getCalls();
//			calls.add(new SipgateCallData());
//		} catch (Exception e) {
//			Log.e("Liste", e.getLocalizedMessage());
//		}

	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"CallList");
		
		return result;
	}
	
}

