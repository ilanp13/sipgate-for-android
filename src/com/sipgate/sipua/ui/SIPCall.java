package com.sipgate.sipua.ui;

/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.sipgate.R;
import com.sipgate.util.PhoneNumberFormatter;

public class SIPCall extends Activity {

	String target;
	AlertDialog m_AlertDlg = null;
	
	void callSIP(String uri) 
	{
		if (uri.indexOf(":") >= 0) {
			target = uri.substring(uri.indexOf(":")+1);
				
			PhoneNumberFormatter formatter = new PhoneNumberFormatter();
			target = formatter.unformattedPhoneNumberFromString(target);
			
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
			   		    
		                finish();
		           }
		        })
		        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() 
		        {
		           public void onClick(DialogInterface dialog, int id) 
		           {
		                dialog.cancel();
		                
		                finish();
		           }
		        })
		        .show();
			}
			else
			{
                finish();
			}
		}
	}
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		
		Intent intent;
		Uri uri;
		Sipdroid.on(this,true);
		
		if ((intent = getIntent()) != null && (uri = intent.getData()) != null)
		{
			callSIP(Uri.decode(uri.toString()));
		}
	}
}
