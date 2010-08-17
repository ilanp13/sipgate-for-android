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

import com.sipgate.R;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/////////////////////////////////////////////////////////////////////
// this the main activity of Sipdroid
// for modifying it additional terms according to section 7, GPL apply
// see ADDITIONAL_TERMS.txt
/////////////////////////////////////////////////////////////////////
public class SipgateFrames extends TabActivity {
	public enum SipgateTab { DIALPAD, CONTACTS, CALLS, VM};  // FIXME: replace by class integrating the TAB_-constants
	
	private static final String TAG = "TabActivity";
	private static final int TAB_DIAL = 0;
	private static final int TAB_CONTACTS = 1;
	private static final int TAB_CALLLIST = 2;
	private static final int TAB_VMLIST = 3;
	
	private SipgateTab currentTab = SipgateTab.DIALPAD;
	private ApiServiceProvider apiClient = null;
	private TabHost tabs = null;
	private boolean vmTabVisible = false;
		
	private TabSpec tabSpecDial = null;
	private TabSpec tabSpecContacts = null;
	private TabSpec tabSpecCallList = null;
	private TabSpec tabSpecVmList = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "onCreate");

		Resources res = getResources();
		tabs = getTabHost();
		this.tabSpecDial = tabs.newTabSpec("Dialpad");
		this.tabSpecDial.setIndicator(res.getText(R.string.sipgate_tab_dialpad), res.getDrawable(R.drawable.tab_dialpad));
		this.tabSpecDial.setContent(new Intent(this, Sipgate.class));
		tabs.addTab(this.tabSpecDial);

		this.apiClient = ApiServiceProvider.getInstance(getApplicationContext());


		this.tabSpecContacts = tabs.newTabSpec("Contacts");;
		this.tabSpecContacts.setIndicator(res.getText(R.string.sipgate_tab_contacts), res.getDrawable(R.drawable.tab_contacts));
		this.tabSpecContacts.setContent(new Intent(this, ContactListActivity.class));
		tabs.addTab(this.tabSpecContacts);

		this.tabSpecCallList = tabs.newTabSpec("Calllist");
		this.tabSpecCallList.setIndicator(res.getText(R.string.sipgate_tab_calllist), res.getDrawable(R.drawable.tab_calllist));
		this.tabSpecCallList.setContent(new Intent(this, CallListActivity.class));
		tabs.addTab(this.tabSpecCallList);

		// check if used API is capable of VM-list and only start service when feature available:
		if (!this.hasVmListFeature()) {
			this.vmTabVisible = false;
			
			Log.i(TAG, "used API is NOT capable of 'VM_LIST' feature; background-service disabled ...");
		} else {
			Log.i(TAG, "used API is capable of 'VM_LIST' feature ...");

			this.addVmTab();
		}
		
		this.startService(new Intent(this, SipgateBackgroundService.class));
		Log.d(TAG, "calling setcurrenttab from oncreate");

		this.setCurrentTab(bundle);
	}

	private void setCurrentTab(Bundle bundle) {
		if (bundle != null) {
			this.currentTab = (SipgateTab) bundle.getSerializable("view");
			Log.d("bundle", this.currentTab.toString());
		} else {
			Log.e("bundle", "Not provided");
			this.currentTab = SipgateTab.DIALPAD;
		}

		switch (this.currentTab) {
		case CALLS:
			tabs.setCurrentTab(TAB_CALLLIST);
			break;
		
		case VM:
			tabs.setCurrentTab(TAB_VMLIST);
			break;

		case CONTACTS:
			tabs.setCurrentTab(TAB_CONTACTS);
			break;

		// dialpad is our default:
		default:
			tabs.setCurrentTab(TAB_DIAL);
			break;
		}
	}
	
	private void addVmTab() {
		Resources res = getResources();
		this.tabSpecVmList = tabs.newTabSpec("Voicemails");
		this.tabSpecVmList.setIndicator(res.getText(R.string.sipgate_tab_voicemail), res.getDrawable(R.drawable.tab_voicemail));
		this.tabSpecVmList.setContent(new Intent(this, EventListActivity.class));
		tabs.addTab(this.tabSpecVmList);
		
		this.vmTabVisible = true;
	}

	private void removeVmTab() {
		try {
			this.tabs.clearAllTabs();
			tabs.addTab(tabSpecDial);
			tabs.addTab(tabSpecContacts);
			tabs.addTab(tabSpecCallList);
		} catch (NullPointerException e) {
			Log.i(TAG, "removeVmTab() -> "+e.getLocalizedMessage());
		}
		
		this.vmTabVisible = false;
	}
	
	private boolean hasVmListFeature() {
		boolean hasVmListFeature = false;
		try {
			hasVmListFeature = apiClient.featureAvailable(API_FEATURE.VM_LIST);
		} catch (Exception e) {
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.getLocalizedMessage());
		}
		
		return hasVmListFeature;
	}
	
	public void onResume() {
		super.onResume();
		
		// do we need to hide the VM tab?
		if (!this.vmTabVisible && hasVmListFeature()) {
			this.addVmTab();
		} else if (vmTabVisible && !hasVmListFeature()) {
			this.removeVmTab();
		}
		
		this.startService(new Intent(this, SipgateBackgroundService.class));
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "calling setcurrenttab from onNewIntent");
		this.setCurrentTab(bundle);
	}
}
