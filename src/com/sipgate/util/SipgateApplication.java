package com.sipgate.util;

import android.app.Application;

public class SipgateApplication extends Application {

	public enum RefreshState {NONE, GET_EVENTS, NEW_EVENTS, NO_EVENTS, ERROR};
	
	private RefreshState refreshState = RefreshState.NONE;

	public void setRefreshState(RefreshState refreshState) {
		this.refreshState = refreshState;
	}

	public RefreshState getRefreshState() {
		return refreshState;
	}
	
}
