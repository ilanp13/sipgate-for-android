package com.sipgate.base.db;

import android.database.sqlite.SQLiteStatement;

public abstract class BaseDBObject
{
	public abstract String getTableName();
	
	public abstract String getCreateStatement();
	public String getDropStatement()
	{
		return "DROP TABLE " + getTableName();
	}
	
	public abstract String getInsertStatement();
	public abstract String getUpdateStatement();
	public abstract String getDeleteStatement();
	
	public abstract void bindInsert(SQLiteStatement statement);
	public abstract void bindUpdate(SQLiteStatement statement);
	public abstract void bindDelete(SQLiteStatement statement);
}
