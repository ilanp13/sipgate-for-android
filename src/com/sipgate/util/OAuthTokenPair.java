package com.sipgate.util;

import com.sipgate.exceptions.OAuthTokenPairException;

public class OAuthTokenPair {
	private String token = null;
	private String secret = null;
	
	public OAuthTokenPair(String token, String secret) throws OAuthTokenPairException {
		super();
		if (token.length() > 0 && secret.length() > 0) {
			this.token = token;
			this.secret = secret;
		} else {
			throw new OAuthTokenPairException();
		}
	}
	
	public OAuthTokenPair() {
		super();
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public String toString() {
		return "OAuthTokenPair [secret=" + secret + ", token=" + token + "]";
	}
}
