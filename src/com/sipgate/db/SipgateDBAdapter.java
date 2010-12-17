package com.sipgate.db;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.sipgate.db.base.BaseDBAdapter;
import com.sipgate.db.base.BaseDBObject;

/**
 * The database adapter, which provides access to the sipgate SQL database.
 * 
 * @author graef
 * @version 1.0
 */
public class SipgateDBAdapter extends BaseDBAdapter
{
	private static SipgateDBAdapter sipgateDBAdapter = null;
	
	/**
	 * Default constructor.
	 * 
	 * @param context The application context
	 * @since 1.0
	 */
	public SipgateDBAdapter(Context context)
	{
		super(context, "sipgateDB.sqlite");
	}
		
	/**
	 * Gets a cursor with all the contact data.
	 * 
	 * @return A cursor with all the contact data records
	 * @since 1.0
	 */
	public Cursor getAllContactDataCursor()
	{
		return database.query("ContactData", null, null, null, null, null, "upper(displayName) asc");
	}
	
	/**
	 * Gets a cursor with all the call data.
	 * 
	 * @return A cursor with all the call data records
	 * @since 1.0
	 */
	public Cursor getAllCallDataCursor()
	{
		return database.query("CallData", null, null, null, null, null, "time desc");
	}
	
	/**
	 * Gets a cursor with all the voice mail data.
	 * 
	 * @return A cursor with all the voice mail data records
	 * @since 1.0
	 */
	public Cursor getAllVoiceMailDataCursor()
	{
		return database.query("VoiceMailData", null, null, null, null, null, "time desc");
	}
	
	/**
	 * Gets a cursor with all the system data.
	 * 
	 * @return A cursor with all the system data records
	 * @since 1.0
	 */
	public Cursor getAllSystemDataCursor()
	{
		return database.query("SystemData", null, null, null, null, null, null);
	}
	
	/**
	 * Wrapper method for method getAllContactData(boolean withNumbers)
	 * @return a Vector with all ContactDataDBObjects without numbers
	 */
	public Vector<ContactDataDBObject> getAllContactData()
	{
		return getAllContactData(false);
	}
	
	/**
	 * Gets a Vector with all the contact data.
	 * 
	 * @return A vector with all the contact data objects
	 * @since 1.0
	 */
	public Vector<ContactDataDBObject> getAllContactData(boolean withNumbers)
	{
		Vector<ContactDataDBObject> contactData = new Vector<ContactDataDBObject>();

		Cursor cursor = getAllContactDataCursor();
		
		ContactDataDBObject contact = null;
		
		try
		{
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					contact = new ContactDataDBObject();
					
					contact.setUuid(cursor.getString(0));
					contact.setFirstName(cursor.getString(1));
					contact.setLastName(cursor.getString(2));
					contact.setDisplayName(cursor.getString(3));
					
					if (withNumbers)
					{
						contact.setContactNumberDBObjects(getContactNumberDBObjectsByUuid(contact.getUuid()));
					}
					
					contactData.add(contact);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getAllContactData() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return contactData;
	}
	
	/**
	 * Gets a Vector with all the call data.
	 * 
	 * @return A vector with all the call data objects
	 * @since 1.0
	 */
	public Vector<CallDataDBObject> getAllCallData()
	{
		Vector<CallDataDBObject> callData = new Vector<CallDataDBObject>();

		Cursor cursor = getAllCallDataCursor();
		
		CallDataDBObject call = null;
		
		try
		{
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					call = new CallDataDBObject();
					
					call.setId(cursor.getLong(0));
					call.setDirection(cursor.getLong(1));
					call.setMissed(cursor.getLong(2));
					call.setRead(cursor.getLong(3));
					call.setTime(cursor.getLong(4));
					call.setLocalNumberE164(cursor.getString(5));
					call.setLocalNumberPretty(cursor.getString(6));
					call.setLocalName(cursor.getString(7));
					call.setRemoteNumberE164(cursor.getString(8));
					call.setRemoteNumberPretty(cursor.getString(9));
					call.setRemoteName(cursor.getString(10));
					call.setReadModifyUrl(cursor.getString(11));
					
					callData.add(call);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getAllCallData() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return callData;
	}
	
	/**
	 * Gets a Vector with all the voice mail data.
	 * 
	 * @return A vector with all the voice mail data objects
	 * @since 1.0
	 */
	public Vector<VoiceMailDataDBObject> getAllVoiceMailData()
	{
		Vector<VoiceMailDataDBObject> voiceMailData = new Vector<VoiceMailDataDBObject>();

		Cursor cursor = getAllVoiceMailDataCursor();
		
		VoiceMailDataDBObject voiceMail = null;
		
		try
		{
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					voiceMail = new VoiceMailDataDBObject();
					
					voiceMail.setId(cursor.getLong(0));
					voiceMail.setRead(cursor.getLong(1));
					voiceMail.setSeen(cursor.getLong(2));
					voiceMail.setTime(cursor.getLong(3));
					voiceMail.setDuration(cursor.getLong(4));
					voiceMail.setLocalNumberE164(cursor.getString(5));
					voiceMail.setLocalNumberPretty(cursor.getString(6));
					voiceMail.setLocalName(cursor.getString(7));
					voiceMail.setRemoteNumberE164(cursor.getString(8));
					voiceMail.setRemoteNumberPretty(cursor.getString(9));
					voiceMail.setRemoteName(cursor.getString(10));
					voiceMail.setTranscription(cursor.getString(11));
					voiceMail.setContentUrl(cursor.getString(12));
					voiceMail.setReadModifyUrl(cursor.getString(13));
					
					voiceMailData.add(voiceMail);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getAllVoiceMailData() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return voiceMailData;
	}
	
	/**
	 * Gets a Vector with all the system data.
	 * 
	 * @return A vector with all the system data objects
	 * @since 1.0
	 */
	public Vector<SystemDataDBObject> getAllSystemData()
	{
		Vector<SystemDataDBObject> systemData = new Vector<SystemDataDBObject>();
		
		Cursor cursor = getAllSystemDataCursor();
		
		SystemDataDBObject system = null;
		
		try
		{
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					system = new SystemDataDBObject();
					
					system.setKey(cursor.getString(0));
					system.setValue(cursor.getString(1));
										
					systemData.add(system);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getAllSystemData() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return systemData;
	}
	
	/**
	 * Gets a cursor with a specific contact data record.
	 * 
	 * @param uuid The contact uiid
	 * @return A cursor with the specific contact data record
	 * @since 1.0
	 */
	public Cursor getContactDataCursorByUuid(String uuid)
	{
		return database.query("ContactData", null, "uuid = ?", new String[]{ uuid }, null, null, null);
	}
	
	/**
	 * Gets a cursor with all contact numbers of a specific contact uuid record.
	 * 
	 * @param uuid The contact uuid
	 * @return A cursor with all contact numbers the specific contact uuid
	 * @since 1.0
	 */
	public Cursor getContactNumberCursorByUuid(String uuid)
	{
		return database.query("ContactNumber", null, "uuid = ?", new String[]{ uuid }, null, null, null);
	}
	
	/**
	 * Gets a cursor with all contact numbers of a specific contact uuid record.
	 * 
	 * @param uuid The contact uuid
	 * @return A cursor with all contact numbers the specific contact uuid
	 * @since 1.0
	 */
	public Cursor getContactNumberCursorByNumberE164(String numberE164)
	{
		return database.query("ContactNumber", null, "numberE164 = ?", new String[]{ numberE164 }, null, null, null);
	}
		
	/**
	 * Gets a cursor with a specific call record.
	 * 
	 * @param id The call id
	 * @return A cursor with the specific call data record
	 * @since 1.0
	 */
	public Cursor getCallDataCursorById(long id)
	{
		return database.query("CallData", null, "id = ?", new String[]{ String.valueOf(id) }, null, null, null);
	}
	
	/**
	 * Gets a cursor with a specific voice mail record.
	 * 
	 * @param id The voice mail id
	 * @return A cursor with the specific voice mail data record
	 * @since 1.0
	 */
	public Cursor getVoiceMailDataCursorById(long id)
	{
		return database.query("VoiceMailData", null, "id = ?", new String[]{ String.valueOf(id) }, null, null, null);
	}	
	
	/**
	 * Gets a cursor with a specific voice mail file record.
	 * 
	 * @param id The voice mail id
	 * @return A cursor with the specific voice mail data record
	 * @since 1.0
	 */
	public Cursor getVoiceMailFileCursorById(long id)
	{
		return database.query("VoiceMailFile", null, "id = ?", new String[]{ String.valueOf(id) }, null, null, null);
	}	
	
	/**
	 * Gets a cursor with a specific system data record.
	 * 
	 * @param key the of the system data record
	 * @return A cursor with the specific system data record
	 * @since 1.0
	 */
	public Cursor getSystemDataCursorById(String key)
	{
		return database.query("SystemData", null, "key = ?", new String[]{ key }, null, null, null);
	}	
	
	/**
	 * Gets an object with a specific contact record.
	 * 
	 * @param uuid The contact uuid
	 * @return An object with the specific contact data record without contact numbers
	 * @since 1.0
	 */
	public ContactDataDBObject getContactDataDBObjectByUuid(String uuid)
	{
		ContactDataDBObject contact = null;
		
		Cursor cursor = getContactDataCursorByUuid(uuid);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				contact = new ContactDataDBObject();
				
				contact.setUuid(cursor.getString(0));
				contact.setFirstName(cursor.getString(1));
				contact.setLastName(cursor.getString(2));
				contact.setDisplayName(cursor.getString(3));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getContactDataDBObjectByUuid() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
	
		return contact;
	}

	/**
	 * Gets an object with a specific contact number record.
	 * 
	 * @param numberE164 The contact number in E164 format
	 * @return An object with the specific contact number record without contact numbers
	 * @since 1.0
	 */
	public ContactNumberDBObject getContactNumberDBObjectByNumberE164(String numberE164)
	{
		ContactNumberDBObject contactNumber = null;
		
		Cursor cursor = getContactNumberCursorByNumberE164(numberE164);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				contactNumber = new ContactNumberDBObject();
				
				contactNumber.setId(cursor.getLong(0));
				contactNumber.setType(cursor.getString(1));
				contactNumber.setUuid(cursor.getString(2));
				contactNumber.setNumberE164(cursor.getString(3));
				contactNumber.setNumberPretty(cursor.getString(4));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getContactNumberDBObjectByNumberE164() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
	
		return contactNumber;
	}
	
	/**
	 * Gets an object with all contact number records of a specific uuid.
	 * 
	 * @param uuid The contact uuid
	 * @return An vector with the all contact number records of the specific uuid.
	 * @since 1.0
	 */
	public Vector<ContactNumberDBObject> getContactNumberDBObjectsByUuid(String uuid)
	{
		Vector<ContactNumberDBObject> contactNumberDBObjects = new Vector<ContactNumberDBObject>();

		Cursor cursor = getContactNumberCursorByUuid(uuid);
		
		ContactNumberDBObject contactNumber = null;
		
		try
		{
			if (cursor != null)
			{
				while (cursor.moveToNext())
				{
					contactNumber = new ContactNumberDBObject();
					
					contactNumber.setId(cursor.getLong(0));
					contactNumber.setType(cursor.getString(1));
					contactNumber.setUuid(cursor.getString(2));
					contactNumber.setNumberE164(cursor.getString(3));
					contactNumber.setNumberPretty(cursor.getString(4));
						
					contactNumberDBObjects.add(contactNumber);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getContactNumberDBObjectsByUuid() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return contactNumberDBObjects;
	}
	
	/**
	 * Gets an object with a specific contact record.
	 * 
	 * @param uuid The contact uuid
	 * @return An object with the specific contact data record with contact numbers
	 * @since 1.0
	 */
	public ContactDataDBObject getContactDataDBObjectWithContactNumbersByUuid(String uuid)
	{
		ContactDataDBObject contact = getContactDataDBObjectByUuid(uuid);
		
		Vector<ContactNumberDBObject> contactNumbers = getContactNumberDBObjectsByUuid(uuid);
		
		if (contact != null)
		{
			if (contactNumbers != null)
			{
				contact.setContactNumberDBObjects(contactNumbers);
			}
		}
		
		return contact;
	}
	
	/**
	 * Gets an object with a specific contact record.
	 * 
	 * @param numberE164 a number in e164 format of a contact
	 * @return An object with the specific contact data record
	 * @since 1.0
	 */
	public ContactDataDBObject getContactDataDBObjectByNumberE164(String numberE164)
	{
		ContactDataDBObject contact = null;
		
		ContactNumberDBObject contactNumber = getContactNumberDBObjectByNumberE164(numberE164);
		
		if (contactNumber != null)
		{
			Cursor cursor = getContactDataCursorByUuid(contactNumber.getUuid());
			
			try
			{
				if (cursor != null && cursor.moveToNext())
				{
					contact = new ContactDataDBObject();
					
					contact.setUuid(cursor.getString(0));
					contact.setFirstName(cursor.getString(1));
					contact.setLastName(cursor.getString(2));
					contact.setDisplayName(cursor.getString(3));
				}
			}
			catch (Exception e)
			{
				Log.e(getClass().getName(), "getContactDataDBObjectByNumberE164() -> " + e.getMessage());
			}
			finally
			{
				if (!cursor.isClosed() && cursor != null)
				{
					cursor.close();
				}
			}
		}
		
		return contact;
	}
	
	
	/**
	 * Gets an object with a specific call record.
	 * 
	 * @param id The call id
	 * @return An object with the specific call data record
	 * @since 1.0
	 */
	public CallDataDBObject getCallDataDBObjectById(long id)
	{
		CallDataDBObject call = null;
		
		Cursor cursor = getCallDataCursorById(id);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				call = new CallDataDBObject();
				call.setId(cursor.getLong(0));
				call.setDirection(cursor.getLong(1));
				call.setMissed(cursor.getLong(2));
				call.setRead(cursor.getLong(3));
				call.setTime(cursor.getLong(4));
				call.setLocalNumberE164(cursor.getString(5));
				call.setLocalNumberPretty(cursor.getString(6));
				call.setLocalName(cursor.getString(7));
				call.setRemoteNumberE164(cursor.getString(8));
				call.setRemoteNumberPretty(cursor.getString(9));
				call.setRemoteName(cursor.getString(10));
				call.setReadModifyUrl(cursor.getString(11));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getCallDataDBObjectById() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
	
		return call;
	}
	
	/**
	 * Gets an object with a specific voice mail record.
	 * 
	 * @param id The voice mail id
	 * @return An object with the specific voice mail data record
	 * @since 1.0
	 */
	public VoiceMailDataDBObject getVoiceMailDataDBObjectById(long id)
	{
		VoiceMailDataDBObject voiceMail = null;
		
		Cursor cursor = getVoiceMailDataCursorById(id);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				voiceMail = new VoiceMailDataDBObject();
				
				voiceMail.setId(cursor.getLong(0));
				voiceMail.setRead(cursor.getLong(1));
				voiceMail.setSeen(cursor.getLong(2));
				voiceMail.setTime(cursor.getLong(3));
				voiceMail.setDuration(cursor.getLong(4));
				voiceMail.setLocalNumberE164(cursor.getString(5));
				voiceMail.setLocalNumberPretty(cursor.getString(6));
				voiceMail.setLocalName(cursor.getString(7));
				voiceMail.setRemoteNumberE164(cursor.getString(8));
				voiceMail.setRemoteNumberPretty(cursor.getString(9));
				voiceMail.setRemoteName(cursor.getString(10));
				voiceMail.setTranscription(cursor.getString(11));
				voiceMail.setContentUrl(cursor.getString(12));
				voiceMail.setReadModifyUrl(cursor.getString(13));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getVoiceMailDataDBObjectById() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
		
		return voiceMail;
	}
	
	/**
	 * Gets an object with a specific voice mail file record.
	 * 
	 * @param id The voice mail id
	 * @return An object with the specific voice mail file record
	 * @since 1.0
	 */
	public VoiceMailFileDBObject getVoiceMailFileDBObjectById(long id)
	{
		VoiceMailFileDBObject voiceMailFile = null;
		
		Cursor cursor = getVoiceMailFileCursorById(id);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				voiceMailFile = new VoiceMailFileDBObject();
			
				voiceMailFile.setId(cursor.getLong(0));
				voiceMailFile.setValue(cursor.getBlob(1));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getVoiceMailFileDBObjectById() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
	
		return voiceMailFile;
	}
		
	/**
	 * Gets an object with a specific system data record.
	 * 
	 * @param key the key of the system data record
	 * @return An object with the specific system data record
	 * @since 1.0
	 */
	public SystemDataDBObject getSystemDataDBObjectByKey(String key)
	{
		SystemDataDBObject systemDataDBObject = null;
		
		Cursor cursor = getSystemDataCursorById(key);
		
		try
		{
			if (cursor != null && cursor.moveToNext())
			{
				systemDataDBObject = new SystemDataDBObject();
			
				systemDataDBObject.setKey(cursor.getString(0));
				systemDataDBObject.setValue(cursor.getString(1));
			}
		}
		catch (Exception e)
		{
			Log.e(getClass().getName(), "getSystemDataDBObjectByKey() -> " + e.getMessage());
		}
		finally
		{
			if (!cursor.isClosed() && cursor != null)
			{
				cursor.close();
			}
		}
	
		return systemDataDBObject;
	}
	
	/**
	 * Gets the number of the contact data records in the database.
	 * 
	 * @return The number of the contact data records in the database
	 * @since 1.0
	 */
	public long getContactDataCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from ContactData");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	/**
	 * Gets the number of the contact number records in the database.
	 * 
	 * @return The number of the contact number records in the database
	 * @since 1.0
	 */
	public long getContactNumberCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from ContactNumber");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	/**
	 * Gets the number of the call data records in the database.
	 * 
	 * @return The number of the call data records in the database
	 * @since 1.0
	 */
	public long getCallDataCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from CallData");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	/**
	 * Gets the number of the voice mail data records in the database.
	 * 
	 * @return The number of the voice mail data records in the database
	 * @since 1.0
	 */
	public long getVoiceMailDataCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from VoiceMailData");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	/**
	 * Gets the number of the voice mail file records in the database.
	 * 
	 * @return The number of the voice mail file records in the database
	 * @since 1.0
	 */
	public long getVoiceMailFileCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from VoiceMailFile");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	/**
	 * Gets the number of the system data records in the database.
	 * 
	 * @return The number of the system data records in the database
	 * @since 1.0
	 */
	public long getSystemDataCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from SystemData");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}
	
	
	/**
	 * Deletes all contact data records in the database;
	 * 
	 * @since 1.0
	 */
	public void deleteAllContactDataDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from ContactData");
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Deletes all contact number records in the database by contactId;
	 * 
	 * @since 1.0
	 */
	public void deleteAllContactNumberDBObjectsByUuid(String uuid)
	{
		SQLiteStatement statement = database.compileStatement("Delete from ContactNumber WHERE uuid = ?");
	
		statement.bindString(1, uuid);
		
		statement.execute();
		
		statement.close();
	}
	
	
	/**
	 * Deletes all call data records in the database.
	 * 
	 * @since 1.0
	 */
	public void deleteAllCallDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from CallData");
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Deletes all voice mail data records in the database.
	 * 
	 * @since 1.0
	 */
	public void deleteAllVoiceMailDataDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from VoiceMailData");
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Deletes all voice mail data records in the database.
	 * 
	 * @since 1.0
	 */
	public void deleteAllVoiceMailFileDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from VoiceMailFile");
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Deletes all system data records in the database.
	 * 
	 * @since 1.0
	 */
	public void deleteAllSystemDataDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from SystemData");
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Deletes a system data record in the database by key;
	 * 
	 * @since 1.0
	 */
	public void deleteSystemDataDBObjectByKey(String key)
	{
		SQLiteStatement statement = database.compileStatement("Delete from SystemData WHERE key = ?");
	
		statement.bindString(1, key);
		
		statement.execute();
		
		statement.close();
	}
	
	/**
	 * Inserts several contact data objects into the database
	 * 
	 * @param contactDataDBObjects A vector of the contact data objects.
	 * @since 1.0
	 */
	public void insertAllContactDataDBObjects(Vector<ContactDataDBObject> contactDataDBObjects)
	{
		if (contactDataDBObjects.size() > 0)
		{
			try
			{
				SQLiteStatement statement = database.compileStatement(contactDataDBObjects.get(0).getInsertStatement());     	                    
				
				startTransaction();
				
				for (BaseDBObject baseDBObject : contactDataDBObjects) 
				{
					baseDBObject.bindInsert(statement);
					statement.execute(); 
				}
		
				statement.close(); 
				
				commitTransaction();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				if(inTransaction())
				{
					rollbackTransaction();
				}
			}
		}
	}
		
	/**
	 * Inserts several contact number objects into the database
	 * 
	 * @param contactNumberDBObjects A vector of the contact number objects.
	 * @since 1.0
	 */
	public void insertAllContactNumberDBObjects(Vector<ContactNumberDBObject> contactNumberDBObjects)
	{
		if (contactNumberDBObjects.size() > 0)
		{
			try
			{
				SQLiteStatement statement = database.compileStatement(contactNumberDBObjects.get(0).getInsertStatement());     	                    
				
				startTransaction();
				
				for (BaseDBObject baseDBObject : contactNumberDBObjects) 
				{
					baseDBObject.bindInsert(statement);
					statement.execute(); 
				}
		
				statement.close(); 
				
				commitTransaction();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				if(inTransaction())
				{
					rollbackTransaction();
				}
			}
		}
	}
	
	/**
	 * Inserts several call data objects into the database
	 * 
	 * @param callDataDBObjects A vector of the call data objects.
	 * @since 1.0
	 */
	public void insertAllCallDBObjects(Vector<CallDataDBObject> callDataDBObjects)
	{
		if (callDataDBObjects.size() > 0)
		{
			try
			{
				SQLiteStatement statement = database.compileStatement(callDataDBObjects.get(0).getInsertStatement());     	                    
				
				startTransaction();
				
				for (BaseDBObject baseDBObject : callDataDBObjects) 
				{
					baseDBObject.bindInsert(statement);
					statement.execute(); 
				}
		
				statement.close(); 
				
				commitTransaction();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				if(inTransaction())
				{
					rollbackTransaction();
				}
			}
		}
	}
	
	/**
	 * Inserts several voice mail data objects into the database
	 * 
	 * @param voiceMailDataDBObjects A vector of the voice mail data objects.
	 * @since 1.0
	 */
	public void insertAllVoiceMailDataDBObjects(Vector<VoiceMailDataDBObject> voiceMailDataDBObjects)
	{
		if (voiceMailDataDBObjects.size() > 0)
		{
			try
			{
				SQLiteStatement statement = database.compileStatement(voiceMailDataDBObjects.get(0).getInsertStatement());     	                    
				
				startTransaction();
				
				for (BaseDBObject baseDBObject : voiceMailDataDBObjects) 
				{
					baseDBObject.bindInsert(statement);
					statement.execute(); 
				}
		
				statement.close(); 
				
				commitTransaction();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				if(inTransaction())
				{
					rollbackTransaction();
				}
			}
		}
	}
	
	/**
	 * Inserts several voice mail file objects into the database
	 * 
	 * @param voiceMailFileDBObjects A vector of the voice mail file objects.
	 * @since 1.0
	 */
	public void insertAllVoiceMailFileDBObjects(Vector<VoiceMailFileDBObject> voiceMailFileDBObjects)
	{
		if (voiceMailFileDBObjects.size() > 0)
		{
			try
			{
				SQLiteStatement statement = database.compileStatement(voiceMailFileDBObjects.get(0).getInsertStatement());     	                    
				
				startTransaction();
				
				for (BaseDBObject baseDBObject : voiceMailFileDBObjects) 
				{
					baseDBObject.bindInsert(statement);
					statement.execute(); 
				}
		
				statement.close(); 
				
				commitTransaction();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				if(inTransaction())
				{
					rollbackTransaction();
				}
			}
		}
	}
	
	@Override
	public void createTables(SQLiteDatabase database)
	{
		ContactNumberDBObject contactNumberDBObject = new ContactNumberDBObject();
		
		for(String statement : contactNumberDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}
		
		ContactDataDBObject contactDataDBObject = new ContactDataDBObject();

		for(String statement : contactDataDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}

		CallDataDBObject callDataDBObject = new CallDataDBObject();
		
		for(String statement : callDataDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}
		
		VoiceMailFileDBObject voiceMailFileDBObject = new VoiceMailFileDBObject();

		for(String statement : voiceMailFileDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}

		VoiceMailDataDBObject voiceMailDataDBObject = new VoiceMailDataDBObject();
		
		for(String statement : voiceMailDataDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}
		
		SystemDataDBObject systemDataDBObject = new SystemDataDBObject();
		
		for(String statement : systemDataDBObject.getCreateStatement())
		{
		    database.execSQL(statement);
		}
	}	
	
	@Override
	public void dropTables(SQLiteDatabase database)
	{
		ContactNumberDBObject contactNumberDBObject = new ContactNumberDBObject();
		database.execSQL(contactNumberDBObject.getDropStatement());
		
		ContactDataDBObject contactDataDBObject = new ContactDataDBObject();
		database.execSQL(contactDataDBObject.getDropStatement());

		CallDataDBObject callDataDBObject = new CallDataDBObject();
		database.execSQL(callDataDBObject.getDropStatement());

		VoiceMailFileDBObject voiceMailFileDBObject = new VoiceMailFileDBObject();
		database.execSQL(voiceMailFileDBObject.getDropStatement());

		VoiceMailDataDBObject voiceMailDataDBObject = new VoiceMailDataDBObject();
		database.execSQL(voiceMailDataDBObject.getDropStatement());	
		
		SystemDataDBObject systemDataDBObject = new SystemDataDBObject();
		database.execSQL(systemDataDBObject.getDropStatement());	
	}
}