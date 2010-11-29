package com.sipgate.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sipgate.R;
import com.sipgate.R.id;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.RegisterService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

public class Login extends Activity implements OnClickListener 
{
	private final String TAG = "Login";
	private Button okButton;

	private ApiServiceProvider apiServiceProvider = null;
	private ProgressDialog progressDialog = null;
	
	private Context context = this;
	
	boolean purged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sipgate_setup_login);

		context = this;

		okButton = (Button) findViewById(id.okButton);
		okButton.setOnClickListener(this);
		
		showWait();

		SettingsClient settingsClient = SettingsClient.getInstance(this);
		
		apiServiceProvider = ApiServiceProvider.getInstance(getApplicationContext());
	
		if (apiServiceProvider.isRegistered()) 
		{
			if (settingsClient.isProvisioned()) 
			{
				Intent intent = new Intent(this, com.sipgate.ui.SipgateFrames.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			else
			{
				Intent authorizationIntent = new Intent(this, Setup.class);
				startActivity(authorizationIntent);
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
						
			SipgateDBAdapter sipgateDBAdapter = new SipgateDBAdapter(this);

			sipgateDBAdapter.dropTables(sipgateDBAdapter.getDatabase());
			sipgateDBAdapter.createTables(sipgateDBAdapter.getDatabase());
	
			sipgateDBAdapter.close();
			
			showNoCredentialsToast();
		}
		
		hideWait();	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"Login");
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}

	public void onClick(View v) 
	{
		EditText username = (EditText) findViewById(R.id.inputUsername);
		EditText password = (EditText) findViewById(R.id.inputPassword);

		try 
		{
			String user = username.getText().toString();
			String pass = password.getText().toString();

			if ((user.length()) > 0 && (pass.length() > 0)) 
			{
				showWait();
				
				apiServiceProvider.register(user, pass);

				if (apiServiceProvider.isRegistered()) {
					Intent authorizationIntent = new Intent(this, Setup.class);
					startActivity(authorizationIntent);
				}
				else {
					showWrongCredentialsToast();
				}
			} 
			else {
				showNoCredentialsToast();
			}
		} 
		catch (NetworkProblemException e) {
			showNetworkProblemToast();
		} 
		catch (ApiException e) {
			showWrongCredentialsToast();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally
		{
			hideWait();
		}
	}
		
	private void showWait() 
	{
		okButton.setClickable(false);
		okButton.setEnabled(false);
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				
				if (progressDialog != null)
				{
					progressDialog.show();
				}
				else
				{
					progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.sipgate_wait), true);
				}
				
				Looper.loop();
			}
		}).start();
	}

	private void hideWait() 
	{
		okButton.setClickable(true);
		okButton.setEnabled(true);

		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.cancel();
		}	
	}

	private void showWrongCredentialsToast() 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_wrong_credentials), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}

	private void showNetworkProblemToast() 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_network_problem), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}

	private void showNoCredentialsToast() 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_no_credentials), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}
}
