/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.sipgate.ui;


import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sipgate.R;
import com.sipgate.sipua.UserAgent;
import com.sipgate.sipua.ui.Receiver;
import com.sipgate.sipua.ui.Settings;
import com.sipgate.util.PhoneNumberFormatter;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class Sipgate extends Activity implements OnClickListener, OnLongClickListener {
	//private static final String TAG = "Dialpad";

	public static final boolean release = true;
	public static final boolean market = false;

	private TextView txtCallee;
	private float maxTextHeight = 0;
	
	private Vibrator vib;
	private AlertDialog m_AlertDlg;
	private String numberToDial = "";
	
	@Override
	public void onStart() {
		super.onStart();
		if (!Receiver.engine(this).isRegistered())
			Receiver.engine(this).register();
		if (Receiver.engine(this).isRegistered()) {
		    //ContentResolver content = getContentResolver();
		    //Cursor cursor = content.query(Calls.CONTENT_URI,
		    //        PROJECTION, Calls.NUMBER+" like ?", new String[] { "%@%" }, Calls.DEFAULT_SORT_ORDER);
		    //CallsAdapter adapter = new CallsAdapter(this, cursor);
		    //sip_uri_box.setAdapter(adapter);
		}
	}
	
	public static class CallsAdapter extends CursorAdapter implements Filterable {
	    public CallsAdapter(Context context, Cursor c) {
	        super(context, c);
	        mContent = context.getContentResolver();
	    }
	
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	        final LayoutInflater inflater = LayoutInflater.from(context);
	        final TextView view = (TextView) inflater.inflate(
	                android.R.layout.simple_dropdown_item_1line, parent, false);
	        view.setText(cursor.getString(1));
	        return view;
	    }
	
	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	    	
	        String phoneNumber = cursor.getString(1);
	        String cachedName = cursor.getString(2);
	        if (cachedName != null && cachedName.trim().length() > 0)
	        	phoneNumber += " <" + cachedName + ">";	  
	        
	        ((TextView) view).setText(phoneNumber);
	    }
	
	    @Override
	    public String convertToString(Cursor cursor) {
	        return cursor.getString(1);
	    }
	
	    @Override
	    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
	        if (getFilterQueryProvider() != null) {
	            return getFilterQueryProvider().runQuery(constraint);
	        }
	
	        StringBuilder buffer;
	        String[] args;
	        buffer = new StringBuilder();
	        buffer.append(Calls.NUMBER);
	        buffer.append(" LIKE ?");
	        args = new String[] { (constraint != null && constraint.length() > 0?
	       				constraint.toString() : "%@") + "%"};
	
	        return mContent.query(Calls.CONTENT_URI, PROJECTION,
	                buffer.toString(), args,
	                Calls.DEFAULT_SORT_ORDER);
	    }
	
	    private ContentResolver mContent;        
	}
	
	private static final String[] PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.CACHED_NAME
	};
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sipgate);

		on(this,true);

		ImageButton callButton = (ImageButton) findViewById(R.id.call_button);
		callButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				call_menu();
			}
		});

		ImageButton contactsButton = (ImageButton) findViewById(R.id.contacts_button);
		contactsButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(), SipgateFrames.class);
				myIntent.putExtra("view", SipgateFrames.SipgateTab.CONTACTS);
				startActivity(myIntent);
			}
		});

		ImageButton dialOne = (ImageButton) findViewById(R.id.one);
		ImageButton dialTwo = (ImageButton) findViewById(R.id.two);
		ImageButton dialThree = (ImageButton) findViewById(R.id.three);
		ImageButton dialFour = (ImageButton) findViewById(R.id.four);
		ImageButton dialFive = (ImageButton) findViewById(R.id.five);
		ImageButton dialSix = (ImageButton) findViewById(R.id.six);
		ImageButton dialSeven = (ImageButton) findViewById(R.id.seven);
		ImageButton dialEight = (ImageButton) findViewById(R.id.eight);
		ImageButton dialNine = (ImageButton) findViewById(R.id.nine);
		ImageButton dialStar = (ImageButton) findViewById(R.id.star);
		ImageButton dialZero = (ImageButton) findViewById(R.id.zero);
		ImageButton dialPound = (ImageButton) findViewById(R.id.pound);
		ImageButton dialBackspace = (ImageButton) findViewById(R.id.backspace);
		
		txtCallee = (TextView) findViewById(R.id.txt_callee);
		maxTextHeight = txtCallee.getTextSize();
		
		dialOne.setOnClickListener(this);
		dialOne.setOnLongClickListener(this);
		dialTwo.setOnClickListener(this);
		dialTwo.setOnLongClickListener(this);
		dialThree.setOnClickListener(this);
		dialThree.setOnLongClickListener(this);
		dialFour.setOnClickListener(this);
		dialFour.setOnLongClickListener(this);
		dialFive.setOnClickListener(this);
		dialFive.setOnLongClickListener(this);
		dialSix.setOnClickListener(this);
		dialSix.setOnLongClickListener(this);
		dialSeven.setOnClickListener(this);
		dialSeven.setOnLongClickListener(this);
		dialEight.setOnClickListener(this);
		dialEight.setOnLongClickListener(this);
		dialNine.setOnClickListener(this);
		dialNine.setOnLongClickListener(this);
		dialStar.setOnClickListener(this);
		dialStar.setOnLongClickListener(this);
		dialZero.setOnClickListener(this);
		dialZero.setOnLongClickListener(this);
		dialPound.setOnClickListener(this);
		dialPound.setOnLongClickListener(this);
		dialBackspace.setOnClickListener(this);
		dialBackspace.setOnLongClickListener(this);

		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	void call_menu()
	{
		final String target = this.numberToDial;
		
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

	public static boolean on(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Settings.PREF_ON, Settings.DEFAULT_ON);
	}

	public static void on(Context context,boolean on) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		edit.putBoolean(Settings.PREF_ON, on);
		edit.commit();
        if (on) Receiver.engine(context).isRegistered();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Receiver.call_state != UserAgent.UA_STATE_IDLE) Receiver.moveTop();
		this.txtCallee.setText("");
		this.txtCallee.scrollTo(0, 0);
		this.numberToDial = "";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		OptionsMenu m = new OptionsMenu();
		m.createMenu(menu,"sipgate");
		
		return result;
	}


	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);
		OptionsMenu m = new OptionsMenu();
		m.selectItem(item, this.getApplicationContext(), this);

		return result;
	}
	
	public static String getVersion() {
		return getVersion(Receiver.mContext);
	}
	
	public static String getVersion(Context context) {
		final String unknown = "Unknown";
		
		if (context == null) {
			return unknown;
		}
		
		try {
			return context.getPackageManager()
				   .getPackageInfo(context.getPackageName(), 0)
				   .versionName;
		} catch(NameNotFoundException ex) {}
		
		return unknown;		
	}
	
	public boolean onLongClick(View v) {
		int id = v.getId();
		switch(id) {
			case R.id.backspace:
				addDigit("##");
				break;
			case R.id.zero:
				addDigit("+");
				break;
			case R.id.one:
				addDigit("1");
				break;
			case R.id.two:
				addDigit("2");
				break;
			case R.id.three:
				addDigit("3");
				break;
			case R.id.four:
				addDigit("4");
				break;
			case R.id.five:
				addDigit("5");
				break;
			case R.id.six:
				addDigit("6");
				break;
			case R.id.seven:
				addDigit("7");
				break;
			case R.id.eight:
				addDigit("8");
				break;
			case R.id.nine:
				addDigit("9");
				break;
			case R.id.star:
				addDigit("*");
				break;
			case R.id.pound:
				addDigit("#");
				break;
			default:
				break;
		}
		return true;
	}
	
	public void onClick(View v) {
		int id = v.getId();
		String digit = "";
		switch(id) {
			case R.id.one:
				digit = "1";
				break;
			case R.id.two:
				digit = "2";
				break;
			case R.id.three:
				digit = "3";
				break;
			case R.id.four:
				digit = "4";
				break;
			case R.id.five:
				digit = "5";
				break;
			case R.id.six:
				digit = "6";
				break;
			case R.id.seven:
				digit = "7";
				break;
			case R.id.eight:
				digit = "8";
				break;
			case R.id.nine:
				digit = "9";
				break;
			case R.id.star:
				digit = "*";
				break;
			case R.id.zero:
				digit = "0";
				break;
			case R.id.pound:
				digit = "#";
				break;
			case R.id.backspace:
				digit = "--";
				break;
			default:
				break;		
		}
		addDigit(digit);
	}
	
	private void addDigit(String digit) 
	{
		vib.vibrate(20);
		
		if(digit.equals("--") && this.numberToDial.length()>0) 
		{
			numberToDial = numberToDial.substring(0, numberToDial.length()-1);
		} 
		else if(digit.equals("##")) 
		{
			numberToDial = "";
		}
		else if(!digit.equals("--") && !digit.equals("##")) 
		{
			numberToDial += digit;
		}
		
		PhoneNumberFormatter formatter = new PhoneNumberFormatter();
		Locale locale = Locale.getDefault();
		String formattedNumber = formatter.formattedPhoneNumberFromStringWithCountry(numberToDial, locale.getCountry());
		
		if (txtCallee.getText().equals(""))
		{
			txtCallee.setTextSize(maxTextHeight);	
		}
		
		txtCallee.setText(formattedNumber);
				
		float textWidth = txtCallee.getPaint().measureText((String) txtCallee.getText());
		float textHeight = txtCallee.getTextSize();
		float maxWidth = txtCallee.getWidth()-txtCallee.getTotalPaddingLeft()-txtCallee.getTotalPaddingRight()-txtCallee.getCompoundDrawablePadding();
		
		while ((float)textWidth <= (float)maxWidth && (float)textHeight < (float)maxTextHeight && (digit.equals("--")))
		{
			textHeight = textHeight * (float) 1.01;
			
			txtCallee.setTextSize(textHeight);
		
			textWidth = txtCallee.getPaint().measureText((String) txtCallee.getText());
		}
		
		while ((float)textWidth >= (float)maxWidth)
		{
			textHeight = textHeight * (float) 0.99;
			
			txtCallee.setTextSize(textHeight);
		
			textWidth = txtCallee.getPaint().measureText((String) txtCallee.getText());
		}
	}	
}
