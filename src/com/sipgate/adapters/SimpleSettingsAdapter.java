package com.sipgate.adapters;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.models.SipgateBalanceData;
import com.sipgate.models.holder.SimpleSettingsCheckboxViewHolder;
import com.sipgate.models.holder.SimpleSettingsInfoViewHolder;
import com.sipgate.models.holder.SimpleSettingsStandardViewHolder;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SettingsClient;

@SuppressWarnings("unused")
public class SimpleSettingsAdapter extends BaseAdapter {
	
	private enum ElementType {
		INFOELEMENT (R.layout.sipgate_simple_preferences_list_bit_with_info),
		CHECKBOXELEMENT (R.layout.sipgate_simple_preferences_list_bit_with_checkbox),
		TEXTELEMENT (R.layout.sipgate_simple_preferences_list_bit);
		
		private int id;
		
		ElementType(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
	}
	
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

	private SimpleSettingsInfoViewHolder infoHolder = null;
	private SimpleSettingsStandardViewHolder textHolder = null;
	private SimpleSettingsCheckboxViewHolder checkboxHolder = null;

	private SettingsClient settings = null;
	private ApiServiceProvider apiServiceProvider = null;
	private SipgateBalanceData balanceData = null;
	private boolean balanceProblem = false;
	private Context context = null;

	public SimpleSettingsAdapter(Activity activity) {
		this.context = activity.getApplicationContext();
		mInflater = activity.getLayoutInflater();

		account = activity.getResources().getString(
				R.string.simple_settings_account);
		telephone = activity.getResources().getString(
				R.string.simple_settings_extension);
		balance = activity.getResources().getString(
				R.string.simple_settings_balance);

		overWireless = activity.getResources().getString(
				R.string.simple_settings_wlan);
		over3G = activity.getResources().getString(R.string.simple_settings_3g);

		refresh = activity.getResources().getString(
				R.string.simple_settings_refresh_timers);
		experts = activity.getResources().getString(
				R.string.simple_settings_advanced);

		settings = SettingsClient.getInstance(activity.getApplicationContext());
		apiServiceProvider = ApiServiceProvider.getInstance(activity
				.getApplicationContext());


		startGetBalance();
	}

	/**
	 * gets users balance asynchronously, creating a thread for that.
	 * sends a BALANCE_RESULT_MSG to signal end of execution.
	 * this.balanceProblem shows success.
	 * on success, balance is written to balanceData. 
	 */
	private void startGetBalance() {
		// register handler for messages sent by child-threads:
		final Handler balanceThreadMessageHandler = new Handler() {
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
				balanceThreadMessageHandler.sendMessage(Message.obtain(
						balanceThreadMessageHandler, BALANCE_RESULT_MSG));
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

	

	/**
	 * inflates a new listview-element, including it's holder
	 * holder is set as the view's tag
	 * @param type
	 * @return
	 */
	private View createListElementView(ElementType type) {
		View convertView;
		convertView = mInflater.inflate(type.getId(), null);
		convertView.setTag(createHolder(type, convertView));
		return convertView;
	}

	/**
	 * creates a holder-object, fills in view-components. return-type depends on type
	 * @param type
	 * @param convertView the convertView passed to getView() 
	 * @return
	 */
	private Object createHolder(ElementType type, View convertView) {

		switch (type) {
		case INFOELEMENT:
			SimpleSettingsInfoViewHolder infoHolder = new SimpleSettingsInfoViewHolder();
			infoHolder.textView = (TextView) convertView
			.findViewById(R.id.sipgateSettingsInfoName);
			infoHolder.infoTextView = (TextView) convertView
			.findViewById(R.id.sipgateSettingsInfoInfo);
			infoHolder.spinnerView = (ImageView) convertView
			.findViewById(R.id.preference_spinner);
			return infoHolder;
		case CHECKBOXELEMENT:
			SimpleSettingsCheckboxViewHolder checkboxHolder = new SimpleSettingsCheckboxViewHolder();
			checkboxHolder.checkedTextView = (CheckedTextView) convertView
					.findViewById(R.id.sipgateSettingsCheckedBoxView);
			return checkboxHolder;
		case TEXTELEMENT:
			SimpleSettingsStandardViewHolder textHolder = new SimpleSettingsStandardViewHolder();
			textHolder.textView = (TextView) convertView
					.findViewById(R.id.sipgateSettingsStandardName);
			return textHolder;
		default:
			return null;
		}
	}

	/**
	 * transforms {@link SipgateBalanceData} into a String
	 * @param balanceData
	 * @return {@link String} to be presented to the user
	 */
	private String createBalanceString(SipgateBalanceData balanceData) {
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
		return balanceString;
	}

	/**
	 * shows/hides an imageview and starts/stops its animation 
	 * @param spinnerView is an imageview with the spinning animation set as background
	 * @param visible
	 */
	private void showSpinner(ImageView spinnerView, boolean visible) {
		
		if (visible) {
			final AnimationDrawable frameAnimation = (AnimationDrawable) spinnerView.getBackground();
			Runnable animationStarter = new Runnable() {
				public void run() {
					frameAnimation.start();
				}
			};
			spinnerView.post(animationStarter);
			spinnerView.setVisibility(View.VISIBLE);
		} else {
			final AnimationDrawable frameAnimation = (AnimationDrawable) infoHolder.spinnerView.getBackground();
			Runnable animationStopper = new Runnable() {
				public void run() {
					frameAnimation.stop();
				}
			};
			infoHolder.spinnerView.post(animationStopper);
			infoHolder.spinnerView.setVisibility(View.GONE);
		}
	}

	/**
	 * creates infoviews for listview-elements
	 * @see getView
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	private View getInfoView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = createListElementView(ElementType.INFOELEMENT);
			infoHolder = (SimpleSettingsInfoViewHolder) convertView.getTag(); // cast is save here. createListElementView guarantees it
		} else {
			infoHolder = (SimpleSettingsInfoViewHolder) convertView.getTag();  // cast is save here. createListElementView guarantees it
		}

		switch (position) {
		case 0:
			infoHolder.textView.setText(account);
			infoHolder.infoTextView.setText(settings.getWebusername());
			infoHolder.spinnerView.setVisibility(View.GONE);
			break;
		case 1:
			infoHolder.textView.setText(telephone);
			infoHolder.infoTextView.setText(settings.getExtensionAlias());
			infoHolder.spinnerView.setVisibility(View.GONE);
			break;
		case 2:
			infoHolder.textView.setText(balance);


			if (balanceData == null) {
				if (balanceProblem) {
					infoHolder.infoTextView.setText(context.getResources().getString(
							R.string.sipgate_simple_settings_balance_problem));
				} else {
					infoHolder.infoTextView.setText("");
					showSpinner(infoHolder.spinnerView, true);
				}
			} else {
				showSpinner(infoHolder.spinnerView, false);

				String balanceString = createBalanceString(balanceData);
				infoHolder.infoTextView.setText(balanceString + " " + balanceData.getCurrency());
			} 
			break;
		default:

			break;
		}

		return convertView;
	}
	
	/**
	 * creates checkboxviews for listview-elements
	 * see getView
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	private View getCheckboxView(int position, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = createListElementView(ElementType.CHECKBOXELEMENT);
			checkboxHolder = (SimpleSettingsCheckboxViewHolder) convertView.getTag();
		} else {
			checkboxHolder= (SimpleSettingsCheckboxViewHolder) convertView.getTag();  // cast is save here.
		}

		switch (position) {
		case 3:
			checkboxHolder.checkedTextView.setText(overWireless);
			checkboxHolder.checkedTextView.setChecked(settings.getUseWireless());
			break;
		case 4:
			checkboxHolder.checkedTextView.setText(over3G);
			checkboxHolder.checkedTextView.setChecked(settings.getUse3G());
			break;
		default:
			break;
		}

		return convertView;
	}

	/**
	 * creates textviews for listview-elements
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	private View getStandardView(int position, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = createListElementView(ElementType.TEXTELEMENT);
			textHolder = (SimpleSettingsStandardViewHolder) convertView.getTag(); // cast is save here. createListElementView guarantees it
		} else {
			textHolder = (SimpleSettingsStandardViewHolder) convertView.getTag(); // cast is save here. createListElementView guarantees it
		}
		
		switch (position) {
		case 5:
			textHolder.textView.setText(refresh);
			break;
		case 6:
			textHolder.textView.setText(experts);
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
