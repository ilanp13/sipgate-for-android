package com.sipgate.exceptions;

import org.apache.http.StatusLine;

public class RestClientException extends Exception {
	private static final long serialVersionUID = 3537033023421413725L;
	private StatusLine statusLine = null;

	private RestClientException() {
		super();
	}
	
	public RestClientException(StatusLine statusLine) {
		this();
		this.statusLine  = statusLine;
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}
}
