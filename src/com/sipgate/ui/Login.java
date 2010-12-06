package com.sipgate.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

@SuppressWarnings("unused")
public class Login extends Activity implements OnClickListener 
{
	private final String TAG = "Login";
	
	private Button okButton = null;
	private EditText username = null;
	private EditText password = null;

	private SettingsClient settingsClient = null;
	private ApiServiceProvider apiServiceProvider = null;
	
	private ProgressDialog progressDialog = null;
	
	private Context context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sipgate_setup_login);

		okButton = (Button) findViewById(id.okButton);
		okButton.setOnClickListener(this);
		
		username = (EditText) findViewById(R.id.inputUsername);
		password = (EditText) findViewById(R.id.inputPassword);
		
		settingsClient = SettingsClient.getInstance(this);
		apiServiceProvider = ApiServiceProvider.getInstance(this);
		
		username.setText(settingsClient.getWebusername());
		password.setText(settingsClient.getWebpassword());
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
		showWait();
		
		String user = username.getText().toString();
		String pass = password.getText().toString();

		if ((user.length()) > 0 && (pass.length() > 0)) 
		{
			try
			{
				apiServiceProvider.register(user, pass);
				
				if (apiServiceProvider.isRegistered()) 
				{
					Intent setupIntent = new Intent(context, Setup.class);
					startActivity(setupIntent);
				}
				else 
				{
					showWrongCredentialsToast();
				}
			}
			catch (NetworkProblemException e) 
			{
				showNetworkProblemToast();
			} 
			catch (ApiException e) 
			{
				showWrongCredentialsToast();
			} 
		}
		else 
		{
			showNoCredentialsToast();
		}
		
		hideWait();
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
				progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.sipgate_wait), true);
				Looper.loop();
			}
		}).start();
	}

	private void hideWait() 
	{
		okButton.setClickable(true);
		okButton.setEnabled(true);

		try
		{
			Thread.sleep(250);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
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
				Toast.makeText(context, getResources().getString(R.string.sipgate_wrong_credentials), Toast.LENGTH_LONG).show();
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
				Toast.makeText(context, getResources().getString(R.string.sipgate_network_problem), Toast.LENGTH_LONG).show();
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
				Toast.makeText(context, getResources().getString(R.string.sipgate_no_credentials), Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}).start();
	}
}
