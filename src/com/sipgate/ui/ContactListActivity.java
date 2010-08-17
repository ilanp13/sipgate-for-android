package com.sipgate.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.sipgate.R;
import com.sipgate.adapters.ContactListAdapter;

public class ContactListActivity extends Activity {
	private static final String TAG = "ContactListActivity";
	
	private ContactListAdapter contactListAdapter = null;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.sipgate_contacts_list);
		
		ListView elementList = (ListView) findViewById(R.id.ContactsListView);

        contactListAdapter = new ContactListAdapter(this);
        
        elementList.setAdapter(contactListAdapter);
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
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

}
