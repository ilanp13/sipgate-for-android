package com.sipgate.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.sipgate.R;
import com.sipgate.adapters.ContactListAdapter;
import com.sipgate.models.SipgateContact;

public class ContactListActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "ContactListActivity";
	
	private ContactListAdapter contactListAdapter = null;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.sipgate_contacts_list);
		
		ListView elementList = (ListView) findViewById(R.id.ContactsListView);

        contactListAdapter = new ContactListAdapter(this);
        
        elementList.setAdapter(contactListAdapter);
        
        elementList.setOnItemClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"ContactList");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
		
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
		Log.d(TAG, "onItemClick() position "+position+"; id "+id);
		
		SipgateContact contact = (SipgateContact) parent.getItemAtPosition(position);
		Intent intent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
		intent.putExtra("contactId", contact.getId());
		startActivity(intent);
	}

}
