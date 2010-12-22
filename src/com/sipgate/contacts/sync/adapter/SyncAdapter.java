package com.sipgate.contacts.sync.adapter;

import java.util.ArrayList;
import java.util.Vector;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import com.sipgate.db.ContactDataDBObject;
import com.sipgate.db.ContactNumberDBObject;
import com.sipgate.db.SipgateDBAdapter;

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
	private Context context = null;
	
	public SyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
		
		this.context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
	{	
		SipgateDBAdapter sipgateDBAdapter = new SipgateDBAdapter(context);
		
		Vector <ContactDataDBObject> contactDataDBObjects = sipgateDBAdapter.getAllContactData(true);
 		sipgateDBAdapter.close();
	    
 		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
 		Builder builder = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
															.withSelection(RawContacts.ACCOUNT_NAME + "=?", 
																	       new String[]{String.valueOf(account.name)});
				
		operations.add(builder.build());
		operations.addAll(getInsertOperations(contactDataDBObjects, account));
 		
		try
		{
			getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		catch (OperationApplicationException e)
		{
			e.printStackTrace();
		}
	}
	
	public ArrayList<ContentProviderOperation> getInsertOperations(Vector <ContactDataDBObject> contactDataDBObjects, Account account)
	{
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		Builder builder = null;
		
		ContactDataDBObject contactDataDBObject = null;
		ContactNumberDBObject contactNumberDBObject = null;
		
		int backRef = 0;
		
		for (int i=0; i < contactDataDBObjects.size(); i++)
		{
			backRef = operations.size();
			
			contactDataDBObject = contactDataDBObjects.get(i);
			
			builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
			builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
			builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
			operations.add(builder.build());
			
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
			builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
			builder.withValue(StructuredName.DISPLAY_NAME, contactDataDBObject.getDisplayName());
			builder.withValue(StructuredName.FAMILY_NAME, contactDataDBObject.getLastName());
			builder.withValue(StructuredName.GIVEN_NAME, contactDataDBObject.getFirstName());
			operations.add(builder.build());
			
			for (int j=0; j < contactDataDBObject.getContactNumberDBObjects().size(); j++)
			{
				contactNumberDBObject = contactDataDBObject.getContactNumberDBObjects().get(j);
				
				builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
				builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
				builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				builder.withValue(Phone.TYPE, contactNumberDBObject.getTypeAsCommonDataKindsPhoneInt());
				builder.withValue(Phone.NUMBER, contactNumberDBObject.getNumberE164());
				operations.add(builder.build());
			}
		}
		
		return operations;
	}
}
