package com.sipgate.db.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public abstract class BaseDBAdapter extends SQLiteOpenHelper
{
	protected static BaseDBAdapter baseDBAdapter = null;
	
	protected SQLiteDatabase database = null;
	protected String databaseName = "";
	protected Context context = null;

	public BaseDBAdapter(Context context, String databaseName)
	{
		super(context, databaseName, null, 2);
		
		this.databaseName = databaseName;
		this.context = context;
		this.database = getWritableDatabase();
	}
	
	public void close()
	{
		database.close();
	}
	
	public long insert(BaseDBObject baseDBObject)
	{
		SQLiteStatement statement = database.compileStatement(baseDBObject.getInsertStatement());     	                    
		
		baseDBObject.bindInsert(statement);
		
		long lastId = statement.executeInsert(); 
		
		statement.close(); 
	
		return lastId;
	}
	
	public void update(BaseDBObject baseDBObject)
	{
		SQLiteStatement statement = database.compileStatement(baseDBObject.getUpdateStatement());     	                    
		
		baseDBObject.bindUpdate(statement);
		
		statement.execute(); 
		statement.close(); 
	}
	
	public void delete(BaseDBObject baseDBObject)
	{
		SQLiteStatement statement = database.compileStatement(baseDBObject.getDeleteStatement());     	                    
		
		baseDBObject.bindDelete(statement);
		
		statement.execute(); 
		statement.close(); 
	}
	
	public void onCreate(SQLiteDatabase database)
	{
		createTables(database);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(db.getPath(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		dropTables(database);
		createTables(database);
	}
	
	public void startTransaction()
	{
		database.beginTransaction();
	}
	
	public void commitTransaction()
	{
		database.setTransactionSuccessful();
		database.endTransaction();
	}
	
	public void rollbackTransaction()
	{
		database.endTransaction();
	}
	
	public boolean inTransaction()
	{
		return database.inTransaction();
	}
	
	public SQLiteDatabase getDatabase()
	{
		return database;
	}
	
	public abstract void createTables(SQLiteDatabase database);
	public abstract void dropTables(SQLiteDatabase database);	
}