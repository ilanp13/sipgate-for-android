package com.sipgate.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
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
public class SimpleSettingsAdapter extends BaseAdapter {
	private static final int BALANCE_RETRY_COUNT = 3;
	private static final int BALANCE_RETRY_DELAY = 1000;

	private final static String TAG = "SimpleSettingsAdapter";

	protected static final int BALANCE_RESULT_MSG = 0;

	private LayoutInflater mInflater = null;

	private String account = null;
	private String telephone = null;
	private String balance = null;

	private String overWireless = null;
	private String over3G = null;

	private String refresh = null;
	private String experts = null;

	private Drawable checkboxOff = null;
	private Drawable checkboxOn = null;

	private SimpleSettingsInfoViewHolder infoHolder = null;
	private SimpleSettingsStandardViewHolder standardHolder = null;
	private SimpleSettingsCheckboxViewHolder checkboxHolder = null;

	private SettingsClient settings = null;
	private ApiServiceProvider apiServiceProvider = null;
	private SipgateBalanceData balanceData = null;
	private boolean balanceProblem = false;
	private Context context = null;

	private HashMap<String, String> nameCache = null;

	private Handler balanceThreadMessageHandler = null;

	private List<DataSetObserver> dataSetObservers = null;

	public SimpleSettingsAdapter(Activity activity) {
		this.context = activity.getApplicationContext();
		mInflater = activity.getLayoutInflater();

		account = activity.getResources().getString(R.string.simple_settings_account);
		telephone = activity.getResources().getString(R.string.simple_settings_extension);
		balance = activity.getResources().getString(R.string.simple_settings_balance);

		overWireless = activity.getResources().getString(R.string.simple_settings_wlan);
		over3G = activity.getResources().getString(R.string.simple_settings_3g);

		refresh = activity.getResources().getString(R.string.simple_settings_refresh_timers);
		experts = activity.getResources().getString(R.string.simple_settings_advanced);

		checkboxOff = activity.getResources().getDrawable(R.drawable.btn_check_off);
		checkboxOn = activity.getResources().getDrawable(R.drawable.btn_check_on);

		settings = SettingsClient.getInstance(activity.getApplicationContext());
		apiServiceProvider = ApiServiceProvider.getInstance(activity.getApplicationContext());

		// register handler for messages sent by child-threads:
		this.balanceThreadMessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case BALANCE_RESULT_MSG:
					notifyDataSetChanged();
					break;
				}
			}
		};

		// run thread that will fetch the balance
		Thread balanceThread = new Thread() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				int retryCount = 0;
				while (balanceData == null && retryCount++ <= BALANCE_RETRY_COUNT) {
					// try to get balanceData
					try {
						balanceData = apiServiceProvider.getBillingBalance();
					} catch (Exception e) {
						e.printStackTrace();
					}

					// if there was a problem, balanceData is null
					if (balanceData == null) {
						try {
							// take a little sleep before we try again
							Thread.sleep(BALANCE_RETRY_DELAY);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						balanceProblem = true;
					} else {
						balanceProblem = false;
					}
				}

				// notify the main thread after success
				balanceThreadMessageHandler
						.sendMessage(Message.obtain(balanceThreadMessageHandler, BALANCE_RESULT_MSG));
			}
		};
		balanceThread.start();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		switch (position) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (position) {
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

	private View getInfoView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit_with_info, null);
			infoHolder = new SimpleSettingsInfoViewHolder();
			infoHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsInfoName);
			infoHolder.infoTextView = (TextView) convertView.findViewById(R.id.sipgateSettingsInfoInfo);
			convertView.setTag(infoHolder);
		} else {
			Log.d(TAG, convertView.getTag().getClass().toString());
			infoHolder = (SimpleSettingsInfoViewHolder) convertView.getTag();
		}

		switch (position) {
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

			// balance data is fetched async. but the view is notified upon
			// availability
			if (balanceData != null) {
				double balanceAmount = (double) Double.parseDouble(balanceData.getTotal());
				Double roundedBalance = new Double(Math.floor(balanceAmount * 100.) / 100.);
				String[] balanceArray = roundedBalance.toString().split("[.]");
				String balanceString = null;
				String separator = context.getResources().getString(R.string.sipgate_decimal_separator);
				if (balanceArray.length == 1) {
					balanceString = balanceArray[0] + separator + "00";
				} else if (balanceArray[1].length() == 1) {
					balanceString = balanceArray[0] + separator + balanceArray[1] + "0";
				} else {
					balanceString = balanceArray[0] + separator + balanceArray[1];
				}
				infoHolder.infoTextView.setText(balanceString + " " + balanceData.getCurrency());
			} else if (balanceProblem) {
				infoHolder.infoTextView.setText(context.getResources().getString(
						R.string.sipgate_simple_settings_balance_problem));
			}
			break;
		default:
			break;
		}

		return convertView;
	}

	private View getCheckboxView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit_with_checkbox, null);
			checkboxHolder = new SimpleSettingsCheckboxViewHolder();
			checkboxHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsCheckboxName);
			checkboxHolder.imageView = (ImageView) convertView.findViewById(R.id.sipgateSettingsCheckboxBox);
			convertView.setTag(checkboxHolder);
		} else {
			checkboxHolder = (SimpleSettingsCheckboxViewHolder) convertView.getTag();
		}

		switch (position) {
		case 3:
			checkboxHolder.textView.setText(overWireless);
			Log.e(TAG, settings.getUseWireless().toString());
			if (settings.getUseWireless()) {
				checkboxHolder.imageView.setImageDrawable(checkboxOn);
			} else {
				checkboxHolder.imageView.setImageDrawable(checkboxOff);
			}
			break;
		case 4:
			checkboxHolder.textView.setText(over3G);
			if (settings.getUse3G()) {
				checkboxHolder.imageView.setImageDrawable(checkboxOn);
			} else {
				checkboxHolder.imageView.setImageDrawable(checkboxOff);
			}
			break;
		default:
			break;
		}

		return convertView;
	}

	private View getStandardView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.sipgate_simple_preferences_list_bit, null);
			standardHolder = new SimpleSettingsStandardViewHolder();
			standardHolder.textView = (TextView) convertView.findViewById(R.id.sipgateSettingsStandardName);
			convertView.setTag(standardHolder);
		} else {
			standardHolder = (SimpleSettingsStandardViewHolder) convertView.getTag();
		}

		switch (position) {
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

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public int getCount() {
		return 7;
	}

}
