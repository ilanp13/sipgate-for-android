package com.sipgate.ui;

import net.oauth.OAuthAccessor;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.sipgate.R;
import com.sipgate.exceptions.OAuthRegisterException;
import com.sipgate.exceptions.OAuthUnregisterException;
import com.sipgate.util.Oauth;

public class AuthorizationActivity extends Activity implements OnClickListener {
	private static final String TAG = "AuthorizationActivity";
	public static OAuthAccessor accessor = null;
	private Oauth oauthComponent = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash);
		
		this.oauthComponent = Oauth.getInstance(getApplicationContext());

		Log.i(TAG, "AuthorizationActivity onCreate Call");
	}

	protected void onResume() {
		super.onResume();

		if (this.oauthComponent.isOauthIntent(this.getIntent()) && this.oauthComponent.registrationInProgress()) {
			try {
				this.oauthComponent.register(this.getIntent());
			} catch (OAuthRegisterException e) {
				Log.e(TAG, e.getLocalizedMessage());
				try {
					this.oauthComponent.unRegister();
				} catch (OAuthUnregisterException e1) {
					Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
				}
			}
		} else if (!this.oauthComponent.isRegistered() && !this.oauthComponent.registrationInProgress()) {
			try {
				doLogin();
			} catch (Exception e) {
				Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
			}
		} else if (!this.oauthComponent.isRegistered() && this.oauthComponent.registrationInProgress()){
			try {
				this.oauthComponent.unRegister();
			} catch (OAuthUnregisterException e) {
				Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
			}
			launchMainActivity();
		}

		// now we are registered
		if (this.oauthComponent.isRegistered()) {
			this.launchBackgroundService();
			this.launchSetupActivity();
		}
	}

	private void launchBackgroundService() {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.si.sipgate.SYNCSERVICE");
		startService(serviceIntent);
	}

	/**
	 * @Method are used for checking token
	 */
	public void doLogin() throws Exception {
		try {
			this.oauthComponent.authorize();
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage());
		}
	}

	/**
	 * @Method are used for Launch Setup Activity
	 */
	private void launchSetupActivity() {
		Intent setupIntent = new Intent(this, com.sipgate.ui.Setup.class);
		startActivity(setupIntent);
	}
	
	private void launchMainActivity() {
		//Intent mainIntent = new Intent(this, SipgateVoiceMailApps.class);
		//mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//startActivity(mainIntent);
	}

	public void onClick(View v) {
		launchSetupActivity();
	}
}
