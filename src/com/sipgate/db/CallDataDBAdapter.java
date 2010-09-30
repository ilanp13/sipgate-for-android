package com.sipgate.db;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.sipgate.base.db.BaseDBAdapter;
import com.sipgate.base.db.BaseDBObject;

public class CallDataDBAdapter extends BaseDBAdapter
{
	public CallDataDBAdapter(Context context)
	{
		super(context, "callDataDB.sqlite");
	}
	
	public Cursor getAllCallDataCursor()
	{
		return database.query("CallData", null, null, null, null, null, null);
	}
	
	public Vector<CallDataDBObject> getAllCallData()
	{
		Vector<CallDataDBObject> callData = new Vector<CallDataDBObject>();

		Cursor cursor = getAllCallDataCursor();
		
		CallDataDBObject call = null;
		
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
		
		cursor.close();
		
		return callData;
	}
	
	public Cursor getCallDataCursorById(long id)
	{
		return database.query("CallData", null, "id = ?", new String[]{ String.valueOf(id) }, null, null, null);
	}
	
	public CallDataDBObject getCallDataDBObjectById(long id)
	{
		Cursor cursor = getCallDataCursorById(id);
		
		CallDataDBObject call = null;
		
		if (cursor.moveToNext())
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
			
		cursor.close();
	
		return call;
	}
	
	public long getCallDataCount()
	{
		long count = 0;
		
		SQLiteStatement statement = database.compileStatement("Select count(*) from CallData");
		
		count = statement.simpleQueryForLong();
		
		statement.close();
		
		return count;
	}

	public void deleteAllCallDBObjects()
	{
		SQLiteStatement statement = database.compileStatement("Delete from CallData");
		
		statement.execute();
		
		statement.close();
	}
	
	public void insert(Vector<CallDataDBObject> callDataDBObjects)
	{
		if (callDataDBObjects.size() > 0)
		{
			database.beginTransaction();
			
			SQLiteStatement statement = database.compileStatement(callDataDBObjects.get(0).getInsertStatement());     	                    
			
			for (BaseDBObject baseDBObject : callDataDBObjects) 
			{
				baseDBObject.bindInsert(statement);
				statement.execute(); 
			}
		
			statement.close(); 
			
			database.setTransactionSuccessful();
			database.endTransaction();
		}
	}
	
	@Override
	public void createTables(SQLiteDatabase database)
	{
		CallDataDBObject callDataDBObject = new CallDataDBObject();
	
		database.execSQL(callDataDBObject.getCreateStatement());
	}

	@Override
	public void dropTables(SQLiteDatabase database)
	{
		CallDataDBObject callDataDBObject = new CallDataDBObject();
	
		database.execSQL(callDataDBObject.getDropStatement());
	}
}