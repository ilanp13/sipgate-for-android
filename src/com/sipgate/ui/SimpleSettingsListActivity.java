package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sipgate.R;
import com.sipgate.adapters.SimpleSettingsAdapter;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

/**
 * This class represents the call list activity and implements all
 * it's functions.
 * 
 * @author Karsten Knuth
 * @version 1.0
 */
@SuppressWarnings("unused")
public class SimpleSettingsListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "SimpleSettingsListActivity";
	
	private SimpleSettingsAdapter settingsListAdapter = null;
	private ListView elementList = null;
	
	private Intent intent = null;
	private SettingsClient settingsClient = null;
	private SipgateDBAdapter sipgateDBAdapter = null;
	private ApiServiceProvider apiServiceProvider = null;
	
	/**
	 * This function is called right after the class is started by an intent.
	 * 
	 * @param bundle The bundle which caused the activity to be started.
	 * @since 1.0
	 */
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_simple_preferences_list);
		
		elementList = (ListView) findViewById(R.id.sipgateSettingsListView);
		
		settingsListAdapter = new SimpleSettingsAdapter(this);
        
        elementList.setAdapter(settingsListAdapter);
        elementList.setOnItemClickListener(this);
        
        settingsClient = SettingsClient.getInstance(getApplicationContext());
        sipgateDBAdapter = new SipgateDBAdapter(getApplicationContext());
        apiServiceProvider = ApiServiceProvider.getInstance(getApplicationContext());
    }
	
	/**
	 * This function is called when an item in the call list was clicked.
	 * 
	 * @param parent The View containing the clicked item.
	 * @param view ?
	 * @param position The position of the clicked item in the list.
	 * @param id The id of the clicked item.
	 * @since 1.0
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		Log.d(TAG, String.valueOf(position));
		
		switch (position) {
		case 0:

			if (ApiServiceProvider.getInstance(getApplicationContext()).isRegistered()){
				new AlertDialog.Builder(this)
					.setMessage(R.string.alert_unregister)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							settingsClient.unRegisterExtension();
							apiServiceProvider.unRegister();
							
							NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					        notificationManager.cancelAll();

							Receiver.engine(getApplicationContext()).halt();
							
							stopService(new Intent(getApplicationContext(),SipgateBackgroundService.class));
							stopService(new Intent(getApplicationContext(),RegisterService.class));
            				
							sipgateDBAdapter.dropTables(sipgateDBAdapter.getDatabase());
							sipgateDBAdapter.createTables(sipgateDBAdapter.getDatabase());
							
							intent = new Intent(getApplicationContext(), Login.class);
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
		case 1:
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
		case 3:
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
		case 4:
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
		case 5:
			intent = new Intent(this, SettingsRefreshActivity.class);
			startActivity(intent);
			break;
		case 6:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	private void checkAvailability(){
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