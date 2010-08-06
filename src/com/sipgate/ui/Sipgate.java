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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.util.Log;
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
	private static final String TAG = "Dialpad";

	public static final boolean release = true;
	public static final boolean market = false;

	private TextView txtCallee;
	private float txtSize;
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
//		sip_uri_box = (AutoCompleteTextView) findViewById(R.id.txt_callee);
//		sip_uri_box = (TextView) findViewById(R.id.txt_callee);
//		sip_uri_box.setOnKeyListener(new OnKeyListener() {
//		    public boolean onKey(View v, int keyCode, KeyEvent event) {
//		        if (event.getAction() == KeyEvent.ACTION_DOWN &&
//		        		keyCode == KeyEvent.KEYCODE_ENTER) {
//		          call_menu();
//		          return true;
//		        }
//		        return false;
//		    }
//		});
//		sip_uri_box.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				call_menu();
//			}
//		});
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
		
		this.txtCallee = (TextView) findViewById(R.id.txt_callee);
		this.txtSize = this.txtCallee.getTextSize();
		
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

		this.vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		final Context mContext = this;
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Settings.PREF_MESSAGE, Settings.DEFAULT_MESSAGE)) {
			
		} else if (PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.PREF_PREF, Settings.DEFAULT_PREF).equals(Settings.VAL_PREF_PSTN) &&
				!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Settings.PREF_NODEFAULT, Settings.DEFAULT_NODEFAULT))
			new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_default)
	            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                		Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
	                		edit.putString(Settings.PREF_PREF, Settings.VAL_PREF_SIP);
	                		edit.commit();	
	                    }
	                })
	            .setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	
	                    }
	                })
	            .setNegativeButton(R.string.dontask, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                		Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
	                		edit.putBoolean(Settings.PREF_NODEFAULT, true);
	                		edit.commit();
	                    }
	                })
				.show();
	}
	
	void call_menu()
	{
		String target = this.numberToDial;
		if (m_AlertDlg != null) 
		{
			m_AlertDlg.cancel();
		}
		if (target.length() == 0)
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.empty)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
		else if (!Receiver.engine(this).call(target))
			m_AlertDlg = new AlertDialog.Builder(this)
				.setMessage(R.string.notfast)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon22)
				.setCancelable(true)
				.show();
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
		uncrackButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		optionsMenu m = new optionsMenu();
		m.createMenu(menu);
		
		return result;
	}


	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		optionsMenu m = new optionsMenu();
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
				if(this.txtCallee.getText().toString().equals("22884646#*")) {
					crackButtons();  // KonamiCode FTW
				} else {
					addDigit("5");
				}
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
				uncrackButtons();
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
	
	private void addDigit(String digit) {
		this.vib.vibrate(20);
		//String text = this.txtCallee.getText().toString();
		if(digit == "--" && this.numberToDial.length()>0) {
			this.numberToDial = this.numberToDial.substring(0,this.numberToDial.length()-1);
		} else if(digit == "##") {
			this.numberToDial = "";
		} else if(digit!="--" && digit!="##") {
			this.numberToDial += digit;
		}
		PhoneNumberFormatter formatter = new PhoneNumberFormatter();
		//formatter.init();
		Locale locale = Locale.getDefault();
		
		String formattedNumber = formatter.formattedPhoneNumberFromStringWithCountry(this.numberToDial, locale.getCountry());
		this.txtCallee.setText(formattedNumber);
		Log.d(TAG, this.numberToDial);
		
		float size = 0;
		float height = 0;
		do {
			size = this.txtCallee.getPaint().measureText((String) this.txtCallee.getText());
			if(size>(this.txtCallee.getWidth()-100)) {
				this.txtCallee.setTextSize(this.txtCallee.getTextSize() * ((float) .99));
				//this.txtCallee.scrollTo((int)size-this.txtCallee.getWidth()+100,0);
			}
		} while (size>(this.txtCallee.getWidth()-100));
		if (!numberToDial.equals("")) {
			do {
				size = this.txtCallee.getPaint().measureText((String) this.txtCallee.getText());
				height = this.txtCallee.getTextSize();
				if(size <= (this.txtCallee.getWidth()-100) * ((float) .99) && height <= this.txtSize) {
					this.txtCallee.setTextSize(this.txtCallee.getTextSize() * ((float) 1.01));
					//this.txtCallee.scrollTo(0,0);
				}
			} while (size <= (this.txtCallee.getWidth()-100) * ((float) .99) && height <= this.txtSize); 
		} else {
			this.txtCallee.setTextSize(this.txtSize);
		}
	}
	
	private void crackButtons() {
		MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.crackedglass);
	    mp.start();
		Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vib.cancel();
		vib.vibrate(1000);
		this.vib.cancel();
		this.vib.vibrate(1000);
		ImageButton dialOne = (ImageButton) findViewById(R.id.one);
		ImageButton dialTwo = (ImageButton) findViewById(R.id.two);
		ImageButton dialThree = (ImageButton) findViewById(R.id.three);
		ImageButton dialFour = (ImageButton) findViewById(R.id.four);
		ImageButton dialFive = (ImageButton) findViewById(R.id.five);
		ImageButton dialSix = (ImageButton) findViewById(R.id.six);
		ImageButton dialSeven = (ImageButton) findViewById(R.id.seven);
		ImageButton dialEight = (ImageButton) findViewById(R.id.eight);
		ImageButton dialNine = (ImageButton) findViewById(R.id.nine);
		dialOne.setImageDrawable(getResources().getDrawable(R.drawable.crack1));
		dialTwo.setImageDrawable(getResources().getDrawable(R.drawable.crack2));
		dialThree.setImageDrawable(getResources().getDrawable(R.drawable.crack3));
		dialFour.setImageDrawable(getResources().getDrawable(R.drawable.crack4));
		dialFive.setImageDrawable(getResources().getDrawable(R.drawable.crack5));
		dialSix.setImageDrawable(getResources().getDrawable(R.drawable.crack6));
		dialSeven.setImageDrawable(getResources().getDrawable(R.drawable.crack7));
		dialEight.setImageDrawable(getResources().getDrawable(R.drawable.crack8));
		dialNine.setImageDrawable(getResources().getDrawable(R.drawable.crack9));
	}
	private void uncrackButtons() {
		ImageButton dialOne = (ImageButton) findViewById(R.id.one);
		ImageButton dialTwo = (ImageButton) findViewById(R.id.two);
		ImageButton dialThree = (ImageButton) findViewById(R.id.three);
		ImageButton dialFour = (ImageButton) findViewById(R.id.four);
		ImageButton dialFive = (ImageButton) findViewById(R.id.five);
		ImageButton dialSix = (ImageButton) findViewById(R.id.six);
		ImageButton dialSeven = (ImageButton) findViewById(R.id.seven);
		ImageButton dialEight = (ImageButton) findViewById(R.id.eight);
		ImageButton dialNine = (ImageButton) findViewById(R.id.nine);
		dialOne.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_1));
		dialTwo.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_2));
		dialThree.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_3));
		dialFour.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_4));
		dialFive.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_5));
		dialSix.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_6));
		dialSeven.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_7));
		dialEight.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_8));
		dialNine.setImageDrawable(getResources().getDrawable(R.drawable.taste_ziffer_9));
	}
	
}
