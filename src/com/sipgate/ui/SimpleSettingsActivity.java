package com.sipgate.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

public class SimpleSettingsActivity extends Activity implements OnClickListener {
	private static final String TAG = "SimpleSettingsActivity";
	private SipgateDBAdapter sipgateDBAdapter = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.simple_settings);

		setContentView(R.layout.sipgate_simple_preferences);

		Log.i(TAG, "AuthorizationActivity onCreate Call");
		
		sipgateDBAdapter = new SipgateDBAdapter(this);
		
		SettingsClient settings = SettingsClient.getInstance(getApplicationContext());
		
		TableRow accountSettings = (TableRow) findViewById(R.id.sipgateSettingsAccountRow);
		accountSettings.setOnClickListener(this);
		TableRow accountSettingsValue = (TableRow) findViewById(R.id.sipgateSettingsAccountRowValue);
		accountSettingsValue.setOnClickListener(this);
		
		TextView account = (TextView) findViewById(R.id.sipgateSettingsAccount);
		account.setText(settings.getWebusername());

		TableRow extensionSettings = (TableRow) findViewById(R.id.sipgateSettingsExtensionRow);
		extensionSettings.setOnClickListener(this);
		TableRow extensionSettingsValue = (TableRow) findViewById(R.id.sipgateSettingsExtensionRowValue);
		extensionSettingsValue.setOnClickListener(this);

		TextView extension = (TextView) findViewById(R.id.sipgateSettingsExtension);
		extension.setText(settings.getExtensionAlias());

		TableLayout balanceTable = (TableLayout) findViewById(R.id.sipgateSettingsBalanceTable);
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

		SettingsClient settingsClient = SettingsClient
				.getInstance(getApplicationContext());

		TableRow wirelessSettings = (TableRow) findViewById(R.id.sipgateSettingsWirelessRow);
		wirelessSettings.setOnClickListener(this);
		CheckBox wirelessCheckBox = (CheckBox) findViewById(R.id.sipgateSettingsWireless);
		wirelessCheckBox.setOnClickListener(this);
		wirelessCheckBox.setChecked(settingsClient.getUseWireless());

		TableRow threeGSettings = (TableRow) findViewById(R.id.sipgateSettings3GRow);
		threeGSettings.setOnClickListener(this);
		CheckBox threeGCheckBox = (CheckBox) findViewById(R.id.sipgateSettings3G);
		threeGCheckBox.setOnClickListener(this);
		threeGCheckBox.setChecked(settingsClient.getUse3G());

		TableRow advancedSettings = (TableRow) findViewById(R.id.sipgateSettingsAdvancedRow);
		advancedSettings.setOnClickListener(this);
	}

	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		if (sipgateDBAdapter != null)
		{
			sipgateDBAdapter.close();
		}
	}
	
	public void onClick(View v) {
		int id = v.getId();
		SettingsClient settingsClient = SettingsClient
				.getInstance(getApplicationContext());
		Intent intent = null;

		TextView extension = (TextView) findViewById(R.id.sipgateSettingsExtension);
		
		switch (id) {
		case R.id.sipgateSettingsAccountRow:
		case R.id.sipgateSettingsAccountRowValue:

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
		case R.id.sipgateSettingsExtensionRowValue:
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
