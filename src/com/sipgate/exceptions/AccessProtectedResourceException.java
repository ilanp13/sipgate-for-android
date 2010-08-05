package com.sipgate.exceptions;

public class AccessProtectedResourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1770275855578146083L;

	public AccessProtectedResourceException() {
		super();
	}
	
	public AccessProtectedResourceException(String message) {
		super(message);
	}
}
