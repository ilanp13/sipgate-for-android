package com.sipgate.ui;

import java.io.Serializable;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sipgate.R;
import com.sipgate.R.id;
import com.sipgate.exceptions.NetworkProblemException;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.util.ApiServiceProvider;

public class Login extends Activity implements OnClickListener {
	private final String TAG = "Login";
	private Button okButton;

	private ApiServiceProvider apiServiceProvider = null;

	private void showWait() {
		okButton.setClickable(false);
		okButton.setEnabled(false);
		showWaitToast();
	}

	private void hideWait() {
		okButton.setClickable(true);
		okButton.setEnabled(true);
	}

	private void showWrongCredentialsToast() {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(
				R.string.sipgate_wrong_credentials), duration);
		toast.show();
	}

	private void showNetworkProblemToast() {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(
				R.string.sipgate_network_problem), duration);
		toast.show();
	}

	private void showNoCredentialsToast() {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(getApplicationContext(),
				getResources().getString(R.string.sipgate_no_credentials), duration);
		toast.show();
	}

	private void showWaitToast() {
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast
				.makeText(getApplicationContext(), getResources().getString(R.string.sipgate_wait), duration);
		toast.show();
	}

	private void openSetupActivity(Serializable data) {
		try {
			Intent intent = new Intent(getApplicationContext(), Setup.class);
			intent.putExtra("com.sipgate.ui.credentials", data);
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.w(TAG, e.getLocalizedMessage());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.apiServiceProvider = ApiServiceProvider.getInstance(getApplicationContext());
		if (apiServiceProvider.isRegistered()) {
			Intent authorizationIntent = new Intent(this, Setup.class);
			startActivity(authorizationIntent);
		}

		setContentView(R.layout.sipgate_setup_login);

		okButton = (Button) findViewById(id.okButton);
		okButton.setOnClickListener(this);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		optionsMenu m = new optionsMenu();
		m.createMenu(menu,"Login");
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		optionsMenu m = new optionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}

	public void onClick(View v) {
		EditText username = (EditText) findViewById(R.id.inputUsername);
		EditText password = (EditText) findViewById(R.id.inputPassword);

		try {
			String user = username.getText().toString();
			String pass = password.getText().toString();

			if ((user.length()) > 0 && (pass.length() > 0)) {
				SipgateProvisioningData data = null;
				showWait();

				ApiServiceProvider apiProvider = ApiServiceProvider.getInstance(getApplicationContext());
				apiProvider.register(user, pass);

				data = apiProvider.getProvisioningData();

				hideWait();
				openSetupActivity(data);
			} else {
				showNoCredentialsToast();
			}
		} catch (NetworkProblemException e) {
			showNetworkProblemToast();
		} catch (Exception e) {
			// TODO FIXME we need to differenciate between credentials- and other errors !!!
			showWrongCredentialsToast();
			e.printStackTrace();
		} finally {
			hideWait();
		}

	}

}
