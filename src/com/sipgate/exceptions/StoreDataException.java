package com.sipgate.exceptions;

/**
 * This exception is used if any problem occurs while trying to store new data in the database
 * 
 * @author Karsten Knuth
 */
public class StoreDataException extends Exception {

	/**
	 * This is the standard constructor
	 */
	public StoreDataException() {
		super();
	}
	
	/**
	 * This is the standard constructor that adds a message to the exception
	 * 
	 * @param message
	 */
	public StoreDataException(String message) {
		super(message);
	}

	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = -3881147138051585128L;
	
}
