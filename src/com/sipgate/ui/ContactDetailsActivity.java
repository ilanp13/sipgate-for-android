package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sipgate.R;
import com.sipgate.adapters.ContactDetailAdapter;
import com.sipgate.db.ContactNumberDBObject;
import com.sipgate.db.SipgateDBAdapter;
import com.sipgate.sipua.ui.Receiver;

@SuppressWarnings("unused")
public class ContactDetailsActivity extends Activity implements OnItemClickListener
{
	private static final String TAG = "ContactDetailsActivity";
	
	private SipgateDBAdapter sipgateDBAdapter = null;
	private ContactDetailAdapter contactDetailAdapter = null;
	private AlertDialog m_AlertDlg = null;
	
	private ListView elementList = null;
	
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		setContentView(R.layout.sipgate_contacts_detail);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		
		elementList = (ListView) findViewById(R.id.ContactPhonenumbers);
		
		sipgateDBAdapter = new SipgateDBAdapter(this);
		contactDetailAdapter = new ContactDetailAdapter(this, bundle.getString("uuid"), sipgateDBAdapter);
        
        elementList.setAdapter(contactDetailAdapter);
        elementList.setOnItemClickListener(this);
    }
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	
		if (sipgateDBAdapter != null)
		{
			sipgateDBAdapter.close();
		}
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"ContactDetails");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, getApplicationContext(), this);

		return result;
	}
		
	private void call_menu(final String target)
	{
		if (m_AlertDlg != null) 
		{
			m_AlertDlg.cancel();
		}
		
		if (target.length() == 0)
		{
			m_AlertDlg = new AlertDialog.Builder(this)
		
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		}
		else if (!Receiver.engine(this).call(target))
		{
			m_AlertDlg = new AlertDialog.Builder(this)
			.setMessage(R.string.notfast)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon22)
			.setCancelable(false)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() 
	        {
	           public void onClick(DialogInterface dialog, int id) 
	           {
	        		Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", Uri.decode(target), null));
		   		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   		    startActivity(intent);
	           }
	        })
	        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() 
	        {
	           public void onClick(DialogInterface dialog, int id) 
	           {
	                dialog.cancel();
	           }
	        })
			.show();		
		}
	}	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) 
	{
		ContactNumberDBObject contactNumberDBObject = (ContactNumberDBObject) parent.getItemAtPosition(position);
		
		call_menu(contactNumberDBObject.getNumberE164().replaceAll("tel:", "").replaceAll("dd:", ""));
	}
}
