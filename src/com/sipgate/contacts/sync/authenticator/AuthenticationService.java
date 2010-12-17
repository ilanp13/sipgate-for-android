package com.sipgate.contacts.sync.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuthenticationService extends Service
{
	private static final String TAG = "AuthenticationService";
	private Authenticator mAuthenticator;

	@Override
	public void onCreate()
	{
		Log.v(TAG, "sipgate contacts authenticator started");
		mAuthenticator = new Authenticator(this);
	}

	@Override
	public void onDestroy()
	{
		Log.v(TAG, "sipgate contacts authenticator stopped");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return mAuthenticator.getIBinder();
	}
}
