package com.sipgate.db;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.sipgate.base.db.BaseDBAdapter;

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
			call.setTargetNumberE164(cursor.getString(5));
			call.setTargetNumberPretty(cursor.getString(6));
			call.setTargetName(cursor.getString(7));
			call.setSourceNumberE164(cursor.getString(8));
			call.setSourceNumberPretty(cursor.getString(9));
			call.setSourceName(cursor.getString(10));
			call.setReadModifyUrl(cursor.getString(11));
			
			callData.add(call);
		}
		
		cursor.close();
		
		return callData;
	}
	
	public Cursor getCallDataCursor(long id)
	{
		return database.query("CallData", null, "id = ?", new String[]{ String.valueOf(id) }, null, null, null);
	}
	
	public CallDataDBObject getCallData(long id)
	{
		Cursor cursor = getCallDataCursor(id);
		
		CallDataDBObject call = null;
		
		while (cursor.moveToNext())
		{
			call = new CallDataDBObject();
			
			call.setId(cursor.getLong(0));
			call.setDirection(cursor.getLong(1));
			call.setMissed(cursor.getLong(2));
			call.setRead(cursor.getLong(3));
			call.setTime(cursor.getLong(4));
			call.setTargetNumberE164(cursor.getString(5));
			call.setTargetNumberPretty(cursor.getString(6));
			call.setTargetName(cursor.getString(7));
			call.setSourceNumberE164(cursor.getString(8));
			call.setSourceNumberPretty(cursor.getString(9));
			call.setSourceName(cursor.getString(10));
			call.setReadModifyUrl(cursor.getString(11));
				
			break;
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

	public void removeAllCallData()
	{
		SQLiteStatement statement = database.compileStatement("Delete from CallData");
		
		statement.execute();
		
		statement.close();
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