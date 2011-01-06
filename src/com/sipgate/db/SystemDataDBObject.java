package com.sipgate.db;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.db.base.BaseDBObject;

public class SystemDataDBObject extends BaseDBObject
{
	public final static String REGISTER_DATE = "registerDate";
	public final static String NEW_CALLS_COUNT = "newCallsCount";
	public final static String NEW_VOICEMAILS_COUNT = "newVoiceMailsCount";
		
	private String key = "";
	private String value = "";
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindString(1, key);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindString(1, key);
		statement.bindString(2, value);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindString(1, value);
		statement.bindString(2, key);	
	}

	public String[] getCreateStatement()
	{
		return new String[]	{	"CREATE TABLE SystemData (" +
									"key VARCHAR PRIMARY KEY NOT NULL, " +
									"value VARCHAR);",
								"CREATE UNIQUE INDEX uidx_key_SystemData ON SystemData (key ASC);",
		};					
	}
	
	public String getDeleteStatement()
	{		
		return "DELETE FROM SystemData WHERE key = ?";
	}

	public String getInsertStatement()
	{
		return 	"INSERT INTO SystemData (" +
					"key, value) " +
				"VALUES (?, ?)";
	}

	public String getTableName()
	{
		return "SystemData";
	}

	public String getUpdateStatement()
	{
		return 	"UPDATE SystemData " +
					"SET value = ? " +
				"WHERE key = ?";
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}
	
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SystemDataDBObject)
		{
			return ((SystemDataDBObject)obj).getKey() == key;
		}
		else
		{	
			return super.equals(obj);
		}
	}
}