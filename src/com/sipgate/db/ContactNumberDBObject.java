package com.sipgate.db;

import android.database.sqlite.SQLiteStatement;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.sipgate.db.base.BaseDBObject;

public class ContactNumberDBObject extends BaseDBObject
{
	private long id = 0;
	
	private String type = "";
	private String uuid = "";
	private String numberE164 = "";
	private String numberPretty = "";

	public enum PhoneType 
	{
		HOME, CELL, WORK, WORK_FAX, HOME_FAX, PAGER, OTHER, CUSTOM, ASSISTANT, CALLBACK, CAR, COMPANY_MAIN, ISDN, MAIN, MMS, OTHER_FAX, RADIO, TELEX, TTY_TDD, WORK_CELL, WORK_PAGER;
	}
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindString(1, type);
		statement.bindString(2, uuid);
		statement.bindString(3, numberE164);
		statement.bindString(4, numberPretty);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindString(1, type);
		statement.bindString(2, uuid);
		statement.bindString(3, numberE164);
		statement.bindString(4, numberPretty);
		statement.bindLong(5, id);
	}

	public String getCreateStatement()
	{
		return 	"CREATE TABLE ContactNumber (" + 
					"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
					"type VARCHAR, " +
					"uuid VARCHAR, " +
					"numberE164 VARCHAR, " +
					"numberPretty VARCHAR);" + 
				"CREATE INDEX idx_uuid_ContactNumber ON ContactNumber (uuid ASC);";
	}

	public String getDeleteStatement()
	{
		return "DELETE FROM ContactNumber WHERE id = ?";
	}

	public String getInsertStatement()
	{
		return 	"INSERT INTO ContactNumber (" + 
					"type, uuid, numberE164, numberPretty) " +
				"VALUES (?, ?, ?, ?)";
	}

	public String getTableName()
	{
		return "ContactNumber";
	}

	public String getUpdateStatement()
	{
		return 	"UPDATE ContactNumber " + 
					"SET type = ?, deviceType = ?, uuid = ?, numberE164 = ?, numberPretty = ? " + 
				"WHERE id = ?";
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getNumberE164()
	{
		return numberE164;
	}

	public void setNumberE164(String numberE164)
	{
		this.numberE164 = numberE164;
	}

	public String getNumberPretty()
	{
		return numberPretty;
	}

	public void setNumberPretty(String numberPretty)
	{
		this.numberPretty = numberPretty;
	}
	
	public PhoneType getPhoneType()
	{
		try
		{
			return PhoneType.valueOf(getType());
		}
		catch (Exception ex)
		{
			return PhoneType.OTHER;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ContactNumberDBObject)
		{
			return ((ContactNumberDBObject) obj).getId() == id;
		}
		else
		{
			return super.equals(obj);
		}
	}
}
