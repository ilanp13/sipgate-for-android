package com.sipgate.ui;

import java.util.ArrayList;
import java.util.Locale;


import com.sipgate.R;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.SipgateContactNumber;
import com.sipgate.models.holder.ContactDetailViewHolder;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.util.AndroidContactsClient;
import com.sipgate.util.PhoneNumberFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactDetailsActivity extends Activity implements
		OnItemClickListener {
	private static final String TAG = "ContactDetailsActivity";

	private AndroidContactsClient contactsClient = null;
	private ArrayAdapter<SipgateContactNumber> phonenumbersAdapter = null;
	private AlertDialog m_AlertDlg;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.sipgate_contacts_detail);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		Integer contactId = (Integer) bundle.getInt("contactId");

		Log.d("ContactDetails", "ID: " + contactId.toString());

		contactsClient = new AndroidContactsClient(this);

		SipgateContact currentContact = null;
		currentContact = (SipgateContact) contactsClient.getContact(contactId);

		TextView contactName = (TextView) findViewById(R.id.ContactName);
		ImageView contactPhoto = (ImageView) findViewById(R.id.ContactPhoto);
		ListView elementList = (ListView) findViewById(R.id.ContactPhonenumbers);

		Log.d("ContactDetails", "Name: " + currentContact.getLastName());

		contactName.setText(currentContact.getLastName());
		contactPhoto.setImageBitmap(currentContact.getPhoto());

		final LayoutInflater mInflater = getLayoutInflater();

		elementList.setOnItemClickListener(this);

		phonenumbersAdapter = new ArrayAdapter<SipgateContactNumber>(this,
				R.layout.sipgate_contacts_detail_bit) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ContactDetailViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.sipgate_contacts_detail_bit, null);
					holder = new ContactDetailViewHolder();
					holder.contactNumberType = (TextView) convertView
							.findViewById(R.id.ContactNumberType);
					holder.contactNumberValue = (TextView) convertView
							.findViewById(R.id.ContactNumberValue);
					// holder.contactCallButton = (ImageButton)
					// findViewById(R.id.ContactCallButton);
					convertView.setTag(holder);
				} else {
					holder = (ContactDetailViewHolder) convertView.getTag();
				}
				SipgateContactNumber item = getItem(position);

				String number = item.getPhoneNumber();
				if (number != null) {
					Log.d(TAG, number);
				} else {
					Log.d(TAG, "no number");
				}

				String type = item.getPhoneType().toString();
				SipgateContactNumber.PhoneType phoneType = item.getPhoneType();

				switch (phoneType) {
				case HOME:
					type = getString(R.string.sipgate_phonetype_home);
					break;
				case MOBILE:
					type = getString(R.string.sipgate_phonetype_mobile);
					break;
				case WORK:
					type = getString(R.string.sipgate_phonetype_work);
					break;
				case WORK_FAX:
					type = getString(R.string.sipgate_phonetype_work_fax);
					break;
				case HOME_FAX:
					type = getString(R.string.sipgate_phonetype_home_fax);
					break;
				case PAGER:
					type = getString(R.string.sipgate_phonetype_pager);
					break;
				case OTHER:
					type = getString(R.string.sipgate_phonetype_other);
					break;
				case CUSTOM:
					type = getString(R.string.sipgate_phonetype_custom);
					break;
				case ASSISTANT:
					type = getString(R.string.sipgate_phonetype_assistant);
					break;
				case CALLBACK:
					type = getString(R.string.sipgate_phonetype_callback);
					break;
				case CAR:
					type = getString(R.string.sipgate_phonetype_car);
					break;
				case COMPANY_MAIN:
					type = getString(R.string.sipgate_phonetype_company_main);
					break;
				case ISDN:
					type = getString(R.string.sipgate_phonetype_isdn);
					break;
				case MAIN:
					type = getString(R.string.sipgate_phonetype_main);
					break;
				case MMS:
					type = getString(R.string.sipgate_phonetype_mms);
					break;
				case OTHER_FAX:
					type = getString(R.string.sipgate_phonetype_other_fax);
					break;
				case RADIO:
					type = getString(R.string.sipgate_phonetype_radio);
					break;
				case TELEX:
					type = getString(R.string.sipgate_phonetype_telex);
					break;
				case TTY_TDD:
					type = getString(R.string.sipgate_phonetype_tty_tdd);
					break;
				case WORK_MOBILE:
					type = getString(R.string.sipgate_phonetype_work_mobile);
					break;
				case WORK_PAGER:
					type = getString(R.string.sipgate_phonetype_work_pager);
					break;
				default:
					type = getString(R.string.sipgate_phonetype_unknown);
					break;
				}
				holder.contactNumberType.setText(type);

				PhoneNumberFormatter formatter = new PhoneNumberFormatter();
				Locale locale = Locale.getDefault();
				String numberPretty = formatter.formattedPhoneNumberFromStringWithCountry(number, locale.getCountry());
				if(number.length()>0 && number.substring(0,1).equals("+")) numberPretty = "+" + numberPretty;
				
				holder.contactNumberValue.setText(numberPretty);

				return convertView;
			}
		};

		elementList.setAdapter(phonenumbersAdapter);

		showContact(currentContact.getNumbers());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"ContactDetails");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}

	private void showContact(ArrayList<SipgateContactNumber> numbers) {
		phonenumbersAdapter.clear();
		Log.i(TAG, "showContact");
		if (numbers != null) {
			for (SipgateContactNumber item : numbers) {
				Log.d("showContact", (String) item.getPhoneNumber().toString());
				phonenumbersAdapter.add(item);
			}
		}
		ListView numberlist = (ListView) findViewById(R.id.ContactPhonenumbers);
		numberlist.setVisibility(View.VISIBLE);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position,
			long id) {
		// TODO Auto-generated method stub
		SipgateContactNumber number = (SipgateContactNumber) parent.getItemAtPosition(position);
		Log.d(TAG, "click()");
		String dialnumber = number.getUnformattedPhoneNumber();
		Log.d("Number",dialnumber);
		call_menu(dialnumber);
	}

	void call_menu(String target) {
		if (m_AlertDlg != null) {
			m_AlertDlg.cancel();
		}
		if (target.length() == 0)
			m_AlertDlg = new AlertDialog.Builder(this).setMessage(
					R.string.empty).setTitle(R.string.app_name).setIcon(
					R.drawable.icon22).setCancelable(true).show();
		else if (!Receiver.engine(this).call(target))
			m_AlertDlg = new AlertDialog.Builder(this).setMessage(
					R.string.notfast).setTitle(R.string.app_name).setIcon(
					R.drawable.icon22).setCancelable(true).show();
	}

}
