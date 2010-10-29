	package com.sipgate.db;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.db.base.BaseDBObject;

public class VoiceMailFileDBObject extends BaseDBObject
{
	private long id = 0;
	private byte[] value = null;
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
		statement.bindBlob(2, value);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindBlob(1, value);
		statement.bindLong(2, id);
	}

	public String getCreateStatement()
	{
		return "CREATE TABLE VoiceMailFile (id INTEGER PRIMARY KEY NOT NULL, value BLOB)";
	}

	public String getDeleteStatement()
	{		
		return "DELETE FROM VoiceMailFile WHERE id = ?";
	}

	public String getInsertStatement()
	{
		return "INSERT INTO VoiceMailFile (id, value) VALUES (?, ?)";
	}

	public String getTableName()
	{
		return "VoiceMailFile";
	}

	public String getUpdateStatement()
	{
		return "UPDATE VoiceMailFile SET value = ? WHERE id = ?";
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public byte[] getValue()
	{
		return value;
	}
	
	public void setValue(byte[] value)
	{
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof VoiceMailFileDBObject)
		{
			return ((VoiceMailFileDBObject)obj).getId() == id;
		}
		else
		{	
			return super.equals(obj);
		}
	}
}