package com.sipgate.ui;

import com.sipgate.R;
import com.sipgate.service.SipgateBackgroundService;
import com.sipgate.util.ApiServiceProvider;
import com.sipgate.util.SipgateApplication;
import com.sipgate.util.ApiServiceProvider.API_FEATURE;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * This class holds the frame view and functions as dispatcher for
 * the content activities.
 * 
 * @author Karsten Knuth
 * @author Sipdroid
 * @version 1.2
 */
public class SipgateFrames extends TabActivity 
{
	public enum SipgateTab { DIALPAD, CONTACTS, CALLS, VM};
	
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

	/**
	 * This function is called right after the activity gets started
	 * and is used to initiate several variables.
	 * 
	 * @since 1.0
	 */
	public void onCreate(Bundle icicle) 
	{
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
		}
		else {
			Log.i(TAG, "used API is capable of 'VM_LIST' feature ...");

			this.addVmTab();
		}
		
		startService(new Intent(this, SipgateBackgroundService.class));
		
		Log.d(TAG, "calling setcurrenttab from oncreate");
		this.setCurrentTab(bundle);
	}
	
	/**
	 * This function is called every time the activity comes to the
	 * foreground and executed code.
	 */
	public void onResume() {
		super.onResume();
		
		// do we need to hide the VM tab?
		if (!this.vmTabVisible && hasVmListFeature()) {
			this.addVmTab();
		}
		else if (vmTabVisible && !hasVmListFeature()) {
			this.removeVmTab();
		}
		
		this.startService(new Intent(this, SipgateBackgroundService.class));
	}
	
	/**
	 * This function gets called each time the activity recieves an
	 * intent while it is already in memory.
	 * 
	 * @param intent The intent that reactivated the activity.
	 * @since 1.1
	 */
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		SipgateApplication application = (SipgateApplication) getApplication();
		
		if (action != null && action.equals(SipgateBackgroundService.ACTION_NEWEVENTS)) {
			application.setRefreshState(SipgateApplication.RefreshState.NEW_EVENTS);
			tabs.setCurrentTab(tabs.getCurrentTab());
		}
		else if (action != null && action.equals(SipgateBackgroundService.ACTION_NOEVENTS)) {
			application.setRefreshState(SipgateApplication.RefreshState.NO_EVENTS);
			tabs.setCurrentTab(tabs.getCurrentTab());
		}
		else if (action != null && action.equals(SipgateBackgroundService.ACTION_GETEVENTS)) {
			application.setRefreshState(SipgateApplication.RefreshState.GET_EVENTS);
			tabs.setCurrentTab(tabs.getCurrentTab());
		}
		else if (action != null && action.equals(SipgateBackgroundService.ACTION_ERROR)) {
			application.setRefreshState(SipgateApplication.RefreshState.ERROR);
			tabs.setCurrentTab(tabs.getCurrentTab());
		}
		else {
			application.setRefreshState(SipgateApplication.RefreshState.NONE);
			Bundle bundle = intent.getExtras();
			Log.d(TAG, "calling setcurrenttab from onNewIntent");
			this.setCurrentTab(bundle);
		}
	}

	/**
	 * This function sets the current tab according to the "view"
	 * parameter in the intents extra data.
	 * 
	 * @param bundle The bundle containing all the extra data that was provided with the intent.
	 * @since 1.0
	 */
	private void setCurrentTab(Bundle bundle) {
		if (bundle != null) {
			this.currentTab = (SipgateTab) bundle.getSerializable("view");
			Log.d("bundle", this.currentTab.toString());
		}
		else {
			Log.d("bundle", "Not provided");
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
	
	/**
	 * This function checks the api client for the availability
	 * of the voice mail list featue. 
	 * 
	 * @return A boolen that holds whether the feature is available.
	 * @since 1.0
	 */
	private boolean hasVmListFeature() {
		boolean hasVmListFeature = false;
		try {
			hasVmListFeature = apiClient.featureAvailable(API_FEATURE.VM_LIST);
		}
		catch (Exception e) {
			Log.w(TAG, "startScanService() exception in call to featureAvailable() -> " + e.toString());
		}
		
		return hasVmListFeature;
	}
	
	/**
	 * This function adds the voice mail tab to the tab view.
	 * 
	 * @since 1.0
	 */
	private void addVmTab() {
		Resources res = getResources();
		this.tabSpecVmList = tabs.newTabSpec("Voicemails");
		this.tabSpecVmList.setIndicator(res.getText(R.string.sipgate_tab_voicemail), res.getDrawable(R.drawable.tab_voicemail));
		this.tabSpecVmList.setContent(new Intent(this, VoiceMailListActivity.class));
		tabs.addTab(this.tabSpecVmList);
		
		this.vmTabVisible = true;
	}

	/**
	 * This function removes the voice mail tab from the tab view.
	 * 
	 * @since 1.0
	 */
	private void removeVmTab() {
		try {
			this.tabs.clearAllTabs();
			tabs.addTab(tabSpecDial);
			tabs.addTab(tabSpecContacts);
			tabs.addTab(tabSpecCallList);
		}
		catch (NullPointerException e) {
			Log.i(TAG, "removeVmTab() -> "+e.toString());
		}
		
		this.vmTabVisible = false;
	}
}
