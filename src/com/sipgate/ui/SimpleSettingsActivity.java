package com.sipgate.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

public class SimpleSettingsActivity extends Activity implements OnClickListener, OnTouchListener {
	private static final String TAG = "SimpleSettingsActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.simple_settings);

		setContentView(R.layout.sipgate_simple_preferences);

		Log.i(TAG, "AuthorizationActivity onCreate Call");

		SettingsClient settings = SettingsClient.getInstance(getApplicationContext());

		// Account Selection Row
		LinearLayout accountSettings = (LinearLayout) findViewById(R.id.sipgateSettingsAccountRow);
		accountSettings.setOnClickListener(this);
		accountSettings.setOnTouchListener(this);
		TextView account = (TextView) findViewById(R.id.sipgateSettingsAccount);
		account.setText(settings.getWebusername());

		// Extension Chooser Row
		LinearLayout extensionSettings = (LinearLayout) findViewById(R.id.sipgateSettingsExtensionRow);
		extensionSettings.setOnClickListener(this);
		extensionSettings.setOnTouchListener(this);
		TextView extension = (TextView) findViewById(R.id.sipgateSettingsExtension);
		extension.setText(settings.getExtensionAlias());

		// Balance Row
		LinearLayout balanceTable = (LinearLayout) findViewById(R.id.sipgateSettingsBalanceGroup);
		TextView balance = (TextView) findViewById(R.id.sipgateSettingsBalance);
		ApiServiceProvider apiClient = ApiServiceProvider.getInstance(getApplicationContext());
		SipgateBalanceData accountBalance = null;
		try {
			accountBalance = apiClient.getBillingBalance();
		} catch (Exception e) {
			e.printStackTrace();
			//Log.e(TAG, e.getLocalizedMessage());
		}
		if (accountBalance != null) {
			double balanceAmount = (double) Double.parseDouble(accountBalance.getTotal());
			Double roundedBalance = new Double(Math.floor( balanceAmount * 100. ) / 100.);
			String[] balanceArray = roundedBalance.toString().split("[.]");
			String balanceString = null;
			String separator = getResources().getString(R.string.sipgate_decimal_separator);
			if (balanceArray.length == 1){
				balanceString = balanceArray[0] + separator + "00";
			}
			else if (balanceArray[1].length() == 1) {
				balanceString = balanceArray[0] + separator + balanceArray[1] + "0";
			} 
			else {
				balanceString = balanceArray[0] + separator + balanceArray[1];
			}
			balance.setText( balanceString + " " + accountBalance.getCurrency());
		} else {
			balanceTable.setVisibility(View.GONE);
		}

		// VoIP over WLAN Setting Row
		TableLayout wirelessSettings = (TableLayout) findViewById(R.id.sipgateSettingsWirelessRow);
		wirelessSettings.setOnClickListener(this);
		wirelessSettings.setOnTouchListener(this);
		CheckBox wirelessCheckBox = (CheckBox) findViewById(R.id.sipgateSettingsWireless);
		wirelessCheckBox.setChecked(settings.getUseWireless());
		wirelessCheckBox.setOnClickListener(this);
		wirelessCheckBox.setOnTouchListener(this);
		
		// VoIP over 3G Setting Row
		TableLayout threeGSettings = (TableLayout) findViewById(R.id.sipgateSettings3GRow);
		threeGSettings.setOnClickListener(this);
		threeGSettings.setOnTouchListener(this);
		CheckBox threeGCheckBox = (CheckBox) findViewById(R.id.sipgateSettings3G);
		threeGCheckBox.setChecked(settings.getUse3G());
		threeGCheckBox.setOnClickListener(this);
		threeGCheckBox.setOnTouchListener(this);

		// Advanced Settings Row
		LinearLayout advancedSettings = (LinearLayout) findViewById(R.id.sipgateSettingsAdvancedRow);
		advancedSettings.setOnClickListener(this);
		advancedSettings.setOnTouchListener(this);

		// Event Refresh Settings Row
		LinearLayout refreshSettings = (LinearLayout) findViewById(R.id.sipgateSettingsRefreshRow);
		refreshSettings.setOnClickListener(this);
		refreshSettings.setOnTouchListener(this);
	}

	protected void onResume() {
		super.onResume();
	}

/**
 * Handler for OnTouch-Events (needed for optic feedback on menu items to imitate the native behaviour)
 * 
 * @author niepel
 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch(action) {
			case MotionEvent.ACTION_OUTSIDE:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				v.setBackgroundColor(0xFF000000);
				break;
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundColor(0xFFFFC700);
		}
		return false;
	}
	
	public void onClick(View v) {
		int id = v.getId();
		v.setBackgroundColor(0xFF000000);
		SettingsClient settingsClient = SettingsClient
				.getInstance(getApplicationContext());
		Intent intent = null;

		TextView extension = (TextView) findViewById(R.id.sipgateSettingsExtension);
		
		switch (id) {
		case R.id.sipgateSettingsAccountRow:

			if (ApiServiceProvider.getInstance(getApplicationContext()).isRegistered()){
				new AlertDialog.Builder(this)
					.setMessage(R.string.alert_unregister)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							TextView account = (TextView) findViewById(R.id.sipgateSettingsAccount);
							account.setText("");
							TextView extension = (TextView) findViewById(R.id.sipgateSettingsExtension);
							extension.setText("");
							
							NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					        notificationManager.cancelAll();

							Receiver.engine(getApplicationContext()).halt();
							
							stopService(new Intent(getApplicationContext(),SipgateBackgroundService.class));
							stopService(new Intent(getApplicationContext(),RegisterService.class));
							
					        SettingsClient.getInstance(getApplicationContext()).unRegisterExtension();
							
							ApiServiceProvider.getInstance(getApplicationContext()).unRegister();
            		
							SipgateDBAdapter sipgateDBAdapter = SipgateDBAdapter.getInstance(getApplicationContext());
							
							sipgateDBAdapter.dropTables(sipgateDBAdapter.getDatabase());
							sipgateDBAdapter.createTables(sipgateDBAdapter.getDatabase());
							
							Intent intent = new Intent(getApplicationContext(), Login.class);
							startActivity(intent);
						}
					})
					.setNegativeButton(R.string.no, null)
					.show();
			} else {
				Receiver.engine(getApplicationContext()).halt();
				intent = new Intent(getApplicationContext(), Login.class);
				startActivity(intent);
			}
			break;
		case R.id.sipgateSettingsExtensionRow:
			extension.setText("");
			
			settingsClient.unRegisterExtension();
			Receiver.engine(getApplicationContext()).halt();

			if (ApiServiceProvider.getInstance(getApplicationContext()).isRegistered()){
				intent = new Intent(this, Setup.class);
				startActivity(intent);
			}
			else {
				intent = new Intent(this, Login.class);
				startActivity(intent);
			}
			break;
		case R.id.sipgateSettingsWireless:
		case R.id.sipgateSettingsWirelessRow:
			if (settingsClient.getUseWireless()) {
				settingsClient.setUseWireless(false);
				checkAvailability();
			} else {
				settingsClient.setUseWireless(true);
			}
			CheckBox wirelessCheckBox = (CheckBox) findViewById(R.id.sipgateSettingsWireless);
			wirelessCheckBox.setChecked(settingsClient.getUseWireless());
			Receiver.engine(this).halt();
			Receiver.engine(this).StartEngine();
			break;
		case R.id.sipgateSettings3G:
		case R.id.sipgateSettings3GRow:
			if (settingsClient.getUse3G()) {
				settingsClient.setUse3G(false);
				checkAvailability();
			} else {
				new AlertDialog.Builder(this)
					.setMessage(R.string.alert_3g_cost)
					.setPositiveButton(
						R.string.ok, null)
					.show();
				settingsClient.setUse3G(true);
			}
			CheckBox threeGCheckBox = (CheckBox) findViewById(R.id.sipgateSettings3G);
			threeGCheckBox.setChecked(settingsClient.getUse3G());
			Receiver.engine(this).halt();
			Receiver.engine(this).StartEngine();
			break;
		case R.id.sipgateSettingsAdvancedRow:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.sipgateSettingsRefreshRow:
			intent = new Intent(this, SettingsRefreshActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}

	}
	
	private void checkAvailability(){
		SettingsClient settingsClient = SettingsClient.getInstance(getApplicationContext());
		Log.i(TAG, "foo");
		Log.i(TAG, settingsClient.getUse3G().toString() + " " + settingsClient.getUseWireless());
		if (!settingsClient.getUse3G() && !settingsClient.getUseWireless()){
			Log.i(TAG, "false");
			new AlertDialog.Builder(this)
				.setMessage(R.string.alert_not_available)
				.setPositiveButton(R.string.ok, null)
				.show();
		}
	}

}
