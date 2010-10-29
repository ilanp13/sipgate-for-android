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
		CallDataDBObject callDataDBObject = new CallDataDBObject();
	
		database.execSQL(callDataDBObject.getCreateStatement());

		VoiceMailFileDBObject voiceMailFileDBObject = new VoiceMailFileDBObject();
		
		database.execSQL(voiceMailFileDBObject.getCreateStatement());

		VoiceMailDataDBObject voiceMailDataDBObject = new VoiceMailDataDBObject();
		
		database.execSQL(voiceMailDataDBObject.getCreateStatement());		
		database.execSQL(voiceMailDataDBObject.getCreateTriggerStatement());	
	}

	@Override
	public void dropTables(SQLiteDatabase database)
	{
		CallDataDBObject callDataDBObject = new CallDataDBObject();
	
		database.execSQL(callDataDBObject.getDropStatement());
		
		VoiceMailDataDBObject voiceMailDataDBObject = new VoiceMailDataDBObject();
		
		database.execSQL(voiceMailDataDBObject.getDropStatement());
		
		VoiceMailFileDBObject voiceMailFileDBObject = new VoiceMailFileDBObject();
		
		database.execSQL(voiceMailFileDBObject.getDropStatement());
	}
}