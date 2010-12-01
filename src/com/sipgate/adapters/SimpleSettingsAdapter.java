
package com.sipgate.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.exceptions.FeatureNotAvailableException;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.holder.CallViewHolder;
import com.sipgate.models.holder.SimpleSettingsCheckboxViewHolder;
import com.sipgate.models.holder.SimpleSettingsInfoViewHolder;
import com.sipgate.models.holder.SimpleSettingsStandardViewHolder;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

@SuppressWarnings("unused")
public class SimpleSettingsAdapter extends BaseAdapter
{
	private final static String TAG = "SimpleSettingsAdapter";

	private LayoutInflater mInflater = null;
	
	private String account = null;
	private String telephone = null;
	private String balance = null;
	
	private String overWireless = null;
	private String over3G = null;
	
	private String refresh = null;
	private String experts = null;
	
	private SimpleSettingsInfoViewHolder infoHolder = null;
	private SimpleSettingsStandardViewHolder standardHolder = null;
	private SimpleSettingsCheckboxViewHolder checkboxHolder = null;
	
	private SettingsClient settings = null;
	private ApiServiceProvider apiServiceProvider = null;
	private SipgateBalanceData balanceData = null;
	private Context context = null;
	
	private HashMap<String, String> nameCache = null;
	
	public SimpleSettingsAdapter(Activity activity) 
	{
		this.context = activity.getApplicationContext();
		mInflater = activity.getLayoutInflater();
		
		account = activity.getResources().getString(R.string.simple_settings_account);
		telephone = activity.getResources().getString(R.string.simple_settings_extension);
		balance = activity.getResources().getString(R.string.simple_settings_balance);
		
		overWireless = activity.getResources().getString(R.string.simple_settings_wlan);
		over3G = activity.getResources().getString(R.string.simple_settings_3g);
		
		refresh = activity.getResources().getString(R.string.simple_settings_refresh_timers);
		experts = activity.getResources().getString(R.string.simple_settings_advanced);
		
		settings = SettingsClient.getInstance(activity.getApplicationContext());
		apiServiceProvider = ApiServiceProvider.getInstance(activity.getApplicationContext());
	}
		
	public boolean areAllItemsEnabled() 
	{
		return true;
	}
	
	public boolean isEnabled(int position) 
	{
		return true;
	}
		
	public Object getItem(int position) 
	{
		return null;
	}
	
	public long getItemId(int position) 
	{
		return position;
	}
	
	public int getItemViewType(int position) 
	{
		switch(position) {
		case 0:
		case 1:
		case 2:
			return 0;
		case 3:
		case 4:
			return 1;
		case 5:
		case 6:
			return 2;
		default:
			return 0;
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		switch(position) {
		case 0:
		case 1:
		case 2:
			return getInfoView(position, convertView, parent);
		case 3:
		case 4:
			return getCheckboxView(position, convertView, parent);
		case 5:
		case 6:
			return getStandardView(position, convertView, parent);
		default:
			return null;
		}
	}
	
	private View getInfoView(int position, View convertView, ViewGroup parent)
	{		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit_with_info, null);
			infoHolder = new SimpleSettingsInfoViewHolder();
			infoHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsInfoName);
			infoHolder.infoTextView = (TextView) convertView.findViewById(R.id.sipgateSettingsInfoInfo);
			convertView.setTag(infoHolder);
		} 
		else {
			infoHolder = (SimpleSettingsInfoViewHolder) convertView.getTag();
		}
		
		switch(position) {
			case 0:
				infoHolder.textView.setText(account);
				infoHolder.infoTextView.setText(settings.getWebusername());
				break;
			case 1:
				infoHolder.textView.setText(telephone);
				infoHolder.infoTextView.setText(settings.getExtensionAlias());
				break;
			case 2:
				infoHolder.textView.setText(balance);
				
				try {
					balanceData = apiServiceProvider.getBillingBalance();
				} catch (Exception e) {
					e.printStackTrace();
					//Log.e(TAG, e.getLocalizedMessage());
				}
				if (balanceData != null) {
					double balanceAmount = (double) Double.parseDouble(balanceData.getTotal());
					Double roundedBalance = new Double(Math.floor( balanceAmount * 100. ) / 100.);
					String[] balanceArray = roundedBalance.toString().split("[.]");
					String balanceString = null;
					String separator = context.getResources().getString(R.string.sipgate_decimal_separator);
					if (balanceArray.length == 1){
						balanceString = balanceArray[0] + separator + "00";
					}
					else if (balanceArray[1].length() == 1) {
						balanceString = balanceArray[0] + separator + balanceArray[1] + "0";
					} 
					else {
						balanceString = balanceArray[0] + separator + balanceArray[1];
					}
					infoHolder.infoTextView.setText( balanceString + " " + balanceData.getCurrency());
				}
				break;
			default:
				break;
		}
	
		return convertView;
	}
	
	private View getCheckboxView(int position, View convertView, ViewGroup parent)
	{		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit_with_checkbox, null);
			checkboxHolder = new SimpleSettingsCheckboxViewHolder();
			checkboxHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsCheckboxName);
			convertView.setTag(checkboxHolder);
		} 
		else {
			checkboxHolder = (SimpleSettingsCheckboxViewHolder) convertView.getTag();
		}
		
		switch(position) {
			case 3:
				checkboxHolder.textView.setText(overWireless);
				break;
			case 4:
				checkboxHolder.textView.setText(over3G);
				break;
			default:
				break;
		}
	
		return convertView;
	}
	
	private View getStandardView(int position, View convertView, ViewGroup parent)
	{		
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit, null);
			standardHolder = new SimpleSettingsStandardViewHolder();
			standardHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsStandardName);
			convertView.setTag(standardHolder);
		} 
		else {
			standardHolder = (SimpleSettingsStandardViewHolder) convertView.getTag();
		}
		
		switch(position) {
			case 5:
				standardHolder.textView.setText(refresh);
				break;
			case 6:
				standardHolder.textView.setText(experts);
				break;
			default:
				break;
		}
	
		return convertView;
	}
		
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public int getCount()
	{
		return 7;
	}
}
