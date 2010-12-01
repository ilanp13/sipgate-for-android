package com.sipgate.ui;

import java.util.Locale;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import com.sipgate.R;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
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
		
		settingsClient = SettingsClient.getInstance(this);
		apiServiceProvider = ApiServiceProvider.getInstance(this);
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
				if (apiServiceProvider.isRegistered()) 
				{
					if (settingsClient.isProvisioned()) 
					{
						Intent sipgateFramesIntent = new Intent(getApplicationContext(), SipgateFrames.class);
						startActivity(sipgateFramesIntent);
					}
					else
					{
						Intent setupIntent = new Intent(getApplicationContext(), Setup.class);
						startActivity(setupIntent);
					}
				}
				else
				{
					settingsClient.purgeWebuserCredentials();
					settingsClient.unRegisterExtension();
					
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			        notificationManager.cancelAll();

					stopService(new Intent(getApplicationContext(),SipgateBackgroundService.class));
					stopService(new Intent(getApplicationContext(),RegisterService.class));
				
					Receiver.engine(getApplicationContext()).halt();
								
					SipgateDBAdapter sipgateDBAdapter = new SipgateDBAdapter(getApplicationContext());

					sipgateDBAdapter.dropTables(sipgateDBAdapter.getDatabase());
					sipgateDBAdapter.createTables(sipgateDBAdapter.getDatabase());
			
					sipgateDBAdapter.close();
					
					Intent loginIntent = new Intent(getApplicationContext(), Login.class);
					startActivity(loginIntent);
				}
			}
		}).start();		
	}
}
