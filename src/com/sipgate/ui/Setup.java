package com.sipgate.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sipgate.R;
import com.sipgate.R.id;
import com.sipgate.api.types.MobileExtension;
import com.sipgate.api.types.RegisteredMobileDevice;
import com.sipgate.exceptions.ApiException;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.models.SipgateProvisioningData;
import com.sipgate.models.SipgateProvisioningExtension;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.PhoneNumberFormatter;
import com.sipgate.util.SettingsClient;

public class Setup extends Activity implements OnClickListener, TextWatcher
{ 
	private final String TAG = "Setup";
	private Spinner extensionSpinner;
	private HashMap<String, SipgateProvisioningExtension> extensionsMap = new HashMap<String, SipgateProvisioningExtension>();
	private Button okButton;
	private String registrar = null;
	private String outboundProxy = null;
	
	private SettingsClient settingsClient = null;
	
	private boolean isVoiceAccount = false;
	
	private ProgressDialog progressDialog = null;
	
	private Context context = null;
	
	private EditText numberText = null;
	private PhoneNumberFormatter formatter = null;
	private Locale locale = null;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sipgate_setup);
		
		context = this;
		
		okButton = (Button) findViewById(id.okButton);
		okButton.setOnClickListener(this);
		
		formatter = new PhoneNumberFormatter();
		locale = Locale.getDefault();	
		
		showWait();
		
		settingsClient = SettingsClient.getInstance(getApplicationContext());
			
		if ((isVoiceAccount = isVoiceAccount()))
		{
			prepareVoiceSetup();
		} 
		else 
		{
			prepareNonVoiceSetup();
		}
			
		hideWait();
	}
	
	private void setNonVoiceLayoutVisible(boolean visible) 
	{
		View teamlayout = (View) findViewById(R.id.setupTeamLayout);
		
		if (visible) {
			teamlayout.setVisibility(View.VISIBLE);
		} else {
			teamlayout.setVisibility(View.GONE);
		}
	}
	
	private void setVoiceLayoutVisible(boolean visible) 
	{
		View voicelayout = (View) findViewById(R.id.setupVoiceLayout);
		
		if (visible) {
			voicelayout.setVisibility(View.VISIBLE);
		} else {
			voicelayout.setVisibility(View.GONE);
		}
	}
	
	private void prepareNonVoiceSetup() 
	{
		setNonVoiceLayoutVisible(true);
		setVoiceLayoutVisible(false);
		
		SipgateProvisioningData provisioningData = getProvisioningData();
		
		if (provisioningData == null) {
			Log.e(TAG, "no provisioningdata for team setup");
			return;
		}

		ArrayList<SipgateProvisioningExtension> extensions = null;
		ArrayList<String> aliases = new ArrayList<String>();
		
		registrar = provisioningData.getRegistrar();
		outboundProxy = provisioningData.getOutboundProxy();
		
		extensions = provisioningData.getExtensions();

		if (extensions != null) {
			for (SipgateProvisioningExtension extension : extensions) {
				if (extension.getAlias().length() > 0) {
					extensionsMap.put(extension.getAlias(), extension);
					aliases.add(extension.getAlias());
				} else {
					extensionsMap.put(extension.getSipid(), extension);
					aliases.add(extension.getSipid());
				}
			}
		} else {
			Log.e(TAG, "no extensions in provisioningdata");
		}

		extensionSpinner = (Spinner) findViewById(com.sipgate.R.id.extensionList);

		MyArrayAdapter adapter = new MyArrayAdapter(this,R.layout.sipgate_spinner_row,R.id.text, aliases);
		extensionSpinner.setAdapter( adapter );
	}

	private SipgateProvisioningData getProvisioningData() 
	{
		SipgateProvisioningData provisioningData = null;
		
		try{
			provisioningData = ApiServiceProvider.getInstance(this).getProvisioningData();
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
		}
		return provisioningData;
	}
	
	private void prepareVoiceSetup() 
	{
		setNonVoiceLayoutVisible(false);
		setVoiceLayoutVisible(true);
		
		numberText = (EditText) findViewById(R.id.mobilePhoneNumberText);
		numberText.addTextChangedListener(this);
		numberText.setCursorVisible(false);
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		String lineNumber = tm.getLine1Number();
	
		if (lineNumber == null)
		{
			try 
			{
				Vector<RegisteredMobileDevice> registeredMobileDevices = ApiServiceProvider.getInstance(this).getRegisteredMobileDevices();
		
				if (registeredMobileDevices != null && registeredMobileDevices.size() > 0)
				{
					lineNumber = registeredMobileDevices.get(0).getDeviceNumberE164();
				}
			} 
			catch (FeatureNotAvailableException e) 
			{
				e.printStackTrace();
			}
			catch (ApiException e)
			{
				e.printStackTrace();
			}			
		}
		
		if (lineNumber != null && lineNumber.length() > 0) 
		{	
			lineNumber = formatter.formattedPhoneNumberFromStringWithCountry(lineNumber, locale.getCountry());
			
			numberText.setText(lineNumber);
			numberText.setSelection(lineNumber.length());
		}
	}
	
	private boolean isVoiceAccount() 
	{
		try 
		{
			String baseProducType = ApiServiceProvider.getInstance(this).getBaseProductType();
			
			return (baseProducType != null && baseProducType.equals("voice"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		} 
		catch (FeatureNotAvailableException e) 
		{
			e.printStackTrace();
		}			
		
		return false;
	}
	
	private void showWait() 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Looper.prepare();
				progressDialog = ProgressDialog.show(context, "", getResources().getString(R.string.sipgate_load_extension), true);
				Looper.loop();
			}
		}).start();
	}

	private void hideWait() 
	{
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
	
	private class MyArrayAdapter extends ArrayAdapter<String>
	{
	    public MyArrayAdapter(Context context, int resource,int textViewResourceId, ArrayList<String> objects)
	    {
	    	super(context, resource, textViewResourceId, objects);
	    }       
	}

	private MobileExtension getMobileExtension(String phoneNumber) 
	{
		MobileExtension mobileExtension = null;
		
		String model = android.os.Build.MODEL;
		
		String vendor = "unknown";
		
		if (VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE)
		{
			vendor = android.os.Build.MANUFACTURER;
		}
		
		String firmware = android.os.Build.VERSION.RELEASE;

		try
		{
			mobileExtension = ApiServiceProvider.getInstance(this).setupMobileExtension(phoneNumber, model, vendor, firmware);
		}
		catch (FeatureNotAvailableException e)
		{
			e.printStackTrace();
		}	
		
		return mobileExtension;
	}
	
	public void onClick(View v) 
	{
		
		String username = null;
		String password = null;
		String outboundproxy = null;
		String registrar = null;
		String alias = null;

		if (isVoiceAccount) 
		{
			try {
				
				String phoneNumber = numberText.getText().toString();
				
				formatter.initWithFreestyle(phoneNumber, locale.getCountry());
				
				phoneNumber = formatter.e164NumberWithPrefix("");
				
				MobileExtension mobileExtension = getMobileExtension(phoneNumber);

				if (mobileExtension != null) {
					finishExtensionConfiguration(mobileExtension.getExtensionId(), mobileExtension.getPassword(), mobileExtension.getOutboundProxy(), mobileExtension.getRegistrar(), mobileExtension.getAlias());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// that's it. setting up the extension failed. we should be sorry.
			Toast.makeText(this, getResources().getString(R.string.sipgate_error_unable_to_create_mobile_extension), Toast.LENGTH_SHORT).show();
		} else {
			boolean success = false;
		
			do {
				alias = (String) extensionSpinner.getSelectedItem();
				if (alias == null) {
					Log.e(TAG, "selected item is null");
					break;
				}
				SipgateProvisioningExtension extension = extensionsMap.get(alias);
				if (extension == null) {
					Log.e(TAG, "extension from map is null");
					break;
				}
				
				settingsClient.registerExtension(extension.getSipid(), extension.getPassword(),
						extension.getAlias(), this.outboundProxy, this.registrar);
				username = extension.getSipid();
				password = extension.getPassword();
				outboundproxy = this.outboundProxy;
				registrar = this.registrar;
				alias = extension.getAlias();

				finishExtensionConfiguration(username, password, outboundproxy,	registrar, alias);
				success = true;
			} while (false);
			if (!success) {
				Toast.makeText(this, getResources().getString(R.string.sipgate_unable_to_provision), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void finishExtensionConfiguration(String username, String password, String outboundproxy, String registrar, String alias) 
	{
		if(settingsClient.isFirstRegistration()){
			settingsClient.registerExtension(username, password, alias, outboundproxy, registrar);
		} else {
			settingsClient.reregisterExtension(username, password, alias, outboundproxy, registrar);
		}

		Log.d(TAG, "finishing extension configuration: " + username + " " + password + " " + outboundproxy + " " + registrar + " " + alias);
		
		Receiver.engine(this).halt();
		Receiver.engine(this).StartEngine();
		
		try {
			Intent sipgateFramesIntent = new Intent(this, SipgateFrames.class);
			startActivity(sipgateFramesIntent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		String phoneNumber = s.toString();
		
		phoneNumber = formatter.formattedPhoneNumberFromStringWithCountry(phoneNumber, locale.getCountry());
		
		if (!phoneNumber.equals(numberText.getText().toString()))
		{
			numberText.setText(phoneNumber);
			numberText.setSelection(phoneNumber.length());
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		
	}
	
	@Override
	public void onBackPressed()
	{
	}
}


