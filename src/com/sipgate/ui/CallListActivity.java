package com.sipgate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sipgate.R;
import com.sipgate.adapters.CallListAdapter;
import com.sipgate.db.CallDataDBObject;
import com.sipgate.sipua.ui.Receiver;

public class CallListActivity extends Activity implements OnItemClickListener 
{
	private static final String TAG = "CallListActivity";
	
	private CallListAdapter callListAdapter = null;
	private AlertDialog m_AlertDlg = null;
	
	private ListView elementList = null;
	private TextView emptyList = null;
	
	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		setContentView(R.layout.sipgate_call_list);
		
		elementList = (ListView) findViewById(R.id.CalllistListView);
		emptyList = (TextView) findViewById(R.id.EmptyCallListTextView);

		callListAdapter = new CallListAdapter(this);
        
        elementList.setAdapter(callListAdapter);
        elementList.setOnItemClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (callListAdapter.isEmpty()) {
			elementList.setVisibility(View.GONE);
			emptyList.setVisibility(View.VISIBLE);
		} else {
			elementList.setVisibility(View.VISIBLE);
			emptyList.setVisibility(View.GONE);
		}
		
		callListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"CallList");
		
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		boolean result = super.onOptionsItemSelected(item);
		
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
	
	private void call_menu(String target)
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
				.setCancelable(true)
				.show();
		}
	}	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) 
	{
		CallDataDBObject callDataDBObject = (CallDataDBObject) parent.getItemAtPosition(position);
		call_menu(callDataDBObject.getSourceNumberE164().replaceAll("tel:", ""));
	}
}