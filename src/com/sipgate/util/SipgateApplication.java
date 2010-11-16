package com.sipgate.util;

import android.app.Application;

/**
 * This class extends the android application and is used to
 * exchange data between activities when it is not possible
 * to do so via an intent.
 * 
 * @author Karsten Knuth
 * @version 1.0
 */
public class SipgateApplication extends Application {

	public enum RefreshState {NONE, GET_EVENTS, NEW_EVENTS, NO_EVENTS, ERROR};
	
	private RefreshState refreshState = RefreshState.NONE;

	/**
	 * This function sets the refresh state for the according list
	 * activity.
	 * 
	 * @param refreshState The refresh state of the current running thread.
	 * @since 1.0
	 */
	public void setRefreshState(RefreshState refreshState) {
		this.refreshState = refreshState;
	}

	/**
	 * This function retrieves the refresh state for the according
	 * list activity.
	 * 
	 * @return The refresh state of the current running thread.
	 * @since 1.0
	 */
	public RefreshState getRefreshState() {
		return refreshState;
	}
	
}
