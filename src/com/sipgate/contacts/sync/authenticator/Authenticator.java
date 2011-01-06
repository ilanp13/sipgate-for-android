package com.sipgate.contacts.sync.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

import com.sipgate.util.Constants;

public  class Authenticator extends AbstractAccountAuthenticator
{
	private Context context = null;
	
	public Authenticator(Context context)
	{
		super(context);
		this.context = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException
	{
		Account account = new Account("sipgate", Constants.ACCOUNT_TYPE);
	
		AccountManager am = AccountManager.get(context);
		
		am.addAccountExplicitly(account, null, null);
		
		Bundle result = new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_NAME, "sipgate");
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		return result;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException
	{
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
	{
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
	{
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType)
	{
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
	{
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException
	{
		return null;
	}	
}
