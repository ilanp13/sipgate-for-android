package com.sipgate.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.RawContacts;

import com.sipgate.R;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.PhoneNumberFormatter;
import com.sipgate.util.SettingsClient;

/**
 * The start activity of the application.
 * 
 * @author knuth
 * @author greaf
 * @version 1.1
 * 
 */
public class StartSipgate extends Activity
{
	private ApiServiceProvider apiServiceProvider = null;
	private SettingsClient settingsClient = null;

	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		/*
		 * show the splash screen on startup
		 */
		setContentView(R.layout.splash);
		
		/*
		 * initialise the phone number formatter
		 */
		PhoneNumberFormatter formatter = new PhoneNumberFormatter();
		Locale locale = Locale.getDefault();
		formatter.formattedPhoneNumberFromStringWithCountry("0", locale.getCountry());
	}

	public void onStart()
	{
		super.onStart();
	}

	public void onResume()
	{
		super.onResume();
	
		new Thread (new Runnable()
		{
			public void run()
			{
				try
				{
					settingsClient = SettingsClient.getInstance(getApplicationContext());
					apiServiceProvider = ApiServiceProvider.getInstance(getApplicationContext());
					
					if (settingsClient.isProvisioned()) 
					{
						if (apiServiceProvider.isRegistered()) 
						{
							Intent sipgateFramesIntent = new Intent(getApplicationContext(), SipgateFrames.class);
							startActivity(sipgateFramesIntent);
						}
						else
						{
							apiServiceProvider.register(settingsClient.getWebusername(), settingsClient.getWebpassword());
							
							Intent setupIntent = new Intent(getApplicationContext(), SipgateFrames.class);
							startActivity(setupIntent);
						}
					}
					else
					{
						Intent setupIntent = new Intent(getApplicationContext(), Login.class);
						startActivity(setupIntent);
					}
				}
				catch (ApiException e)
				{
					settingsClient.cleanAllCredentials();
					
					Intent loginIntent = new Intent(getApplicationContext(), Login.class);
					startActivity(loginIntent);
				}
				catch (NetworkProblemException e)
				{
					Intent loginIntent = new Intent(getApplicationContext(), Login.class);
					startActivity(loginIntent);
				}
			}
		}).start();		
	}
}
