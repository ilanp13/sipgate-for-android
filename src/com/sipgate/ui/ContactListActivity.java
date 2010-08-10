package com.sipgate.ui;

import java.util.ArrayList;
import java.util.Comparator;

import com.sipgate.R;
import com.sipgate.models.SipgateContact;
import com.sipgate.models.holder.ContactViewHolder;
import com.sipgate.util.AndroidContactsClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactListActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "ContactListActivity";
	
	private AndroidContactsClient contactsClient = null;
	private ArrayAdapter<SipgateContact> contactListAdapter = null;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.sipgate_contacts_list);
		
        final LayoutInflater mInflater = getLayoutInflater();
        ListView elementList = (ListView) findViewById(R.id.ContactsListView);
        
        elementList.setOnItemClickListener(this);
        
        contactListAdapter = new ArrayAdapter<SipgateContact>(this, R.layout.sipgate_contacts_list_bit) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		  ContactViewHolder holder = null;
                  if (convertView == null) {
                      convertView = mInflater.inflate(R.layout.sipgate_contacts_list_bit, null);
                      holder = new ContactViewHolder();
                      holder.contactName = (TextView)convertView.findViewById(R.id.contactName);
                      holder.contactImage = (ImageView)convertView.findViewById(R.id.contactPhoto);
                      holder.category = (TextView)convertView.findViewById(R.id.ContactsLetterTextView);
                      holder.separator = (View)convertView.findViewById(R.id.ContactsSeparator);
                      convertView.setTag(holder);
                  } else {
                      holder = (ContactViewHolder) convertView.getTag();
                  }
                  SipgateContact item = getItem(position);
                  
                  String name = item.getLastName();
                  if (name != null) {
                	  Log.d(TAG, name);
                  } else {
                	  Log.d(TAG, "no name");
                  }
                  String thisFirstLetter = name.substring(0, 1);
                             
                  holder.contactName.setText(name);
                  holder.category.setText(thisFirstLetter);
                  
                  Bitmap photo = item.getPhoto();
                  if (photo != null) {
                	  holder.contactImage.setImageBitmap(photo);
                	  Log.d(TAG, "has pic");
                  } else {
                	  Log.d(TAG, "no pic");
                  }
                   
                  if (position >= 1) {
                	  SipgateContact lastItem = getItem(position -1);
                	  String firstLetter = lastItem.getLastName().substring(0, 1);
                	  if (firstLetter.equals(thisFirstLetter)) {
                		  holder.category.setVisibility(View.GONE);
                	  } else {
                		  holder.category.setVisibility(View.VISIBLE);
                	  }
                  }
                  
                  if (position < getCount() - 1) {
                	  SipgateContact nextItem = getItem(position + 1);
                	  String firstLetter = nextItem.getLastName().substring(0, 1);
                	  if (!firstLetter.equals(thisFirstLetter)) {
                		  holder.separator.setVisibility(View.GONE);
                	  } else {
                		  holder.separator.setVisibility(View.VISIBLE);
                	  }
                  }
                  
                  return convertView;
        	}
        };
        
        elementList.setAdapter(contactListAdapter);
        
		this.contactsClient = new AndroidContactsClient(this);
		
		getContacts();
		
	}
	
    protected void getContacts() {
    	try {
    		if (this.contactsClient != null) {
    			ArrayList<SipgateContact> contacts = this.contactsClient.getContacts();
    			showContacts(contacts);
    		} else {
    			Log.d(TAG,"no service binding");
    		}	
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showContacts(ArrayList<SipgateContact> contacts) {
		contactListAdapter.clear();
		Log.i(TAG,"showContacts");
		boolean itemsAdded = false;
		
		if (contacts != null){
			for (SipgateContact item: contacts){
				contactListAdapter.add(item);
				itemsAdded = true;
			}
			
			contactListAdapter.sort(new Comparator<SipgateContact>() {
	
				public int compare(SipgateContact a, SipgateContact b) {
					if (a == null && b != null) {
						return 1;
					}
					if (b == null && a != null) {
						return -1;
					}
					if (b == a) {
						return 0;
					}
					return 1 * a.getLastName().compareTo(b.getLastName());
				}
			});
		}
		ListView contactlist = (ListView) findViewById(R.id.ContactsListView);
		TextView emptylist = (TextView) findViewById(R.id.EmptyContactListTextView);
		if (itemsAdded) {
			contactlist.setVisibility(View.VISIBLE);
			emptylist.setVisibility(View.GONE);
		} else {
			contactlist.setVisibility(View.GONE);
			emptylist.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getContacts();
	}

	
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
		// TODO Auto-generated method stub
		Log.d(TAG, "click()");
		
		SipgateContact contact = (SipgateContact) parent.getItemAtPosition(position);
		
		Intent intent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
		intent.putExtra("contactId", contact.getId()); // TODO Determine ContactID and put as Extra!
		startActivity(intent);
	}

}
