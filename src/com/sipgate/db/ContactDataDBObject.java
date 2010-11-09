package com.sipgate.db;

import java.util.Vector;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.db.base.BaseDBObject;

public class ContactDataDBObject extends BaseDBObject
{
	private String uuid = "";
	
	private String firstName = "";
	private String lastName = "";
	private String displayName = "";
	
	private Vector<ContactNumberDBObject> contactNumberDBObjects = new Vector<ContactNumberDBObject>();
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindString(1, uuid);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindString(1, uuid);
		
		statement.bindString(2, firstName);
		statement.bindString(3, lastName);
		statement.bindString(4, displayName);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindString(1, firstName);
		statement.bindString(2, lastName);
		statement.bindString(3, displayName);

		statement.bindString(4, uuid);
	}

	public String getCreateStatement()
	{
		return 	"CREATE TABLE ContactData (" +
					"uuid VARCHAR PRIMARY KEY NOT NULL UNIQUE, " +
					"firstName VARCHAR, " +
					"lastName VARCHAR, " +
					"displayName VARCHAR);" + 
				"CREATE INDEX idx_uuid_ContactData ON ContactData (uuid ASC);" + 
				"CREATE TRIGGER delete_ContactData_uuid_ContactNumber_uuid BEFORE DELETE ON ContactData " +
				"FOR EACH ROW BEGIN " +
					"DELETE FROM ContactNumber WHERE ContactNumber.uuid = OLD.uuid;" +
				"END;";	
	}
	
	public String getDeleteStatement()
	{
		return "DELETE FROM ContactData WHERE uuid = ?";
	}

	public String getInsertStatement()
	{
		return 	"INSERT INTO ContactData (" +
					"uuid, firstName, lastName, displayName) " +
				"VALUES (?, ?, ?, ?)";
	}

	public String getTableName()
	{
		return "ContactData";
	}

	public String getUpdateStatement()
	{
		return 	"UPDATE ContactData " +
					"SET firstName = ?, lastName = ?, displayName = ? " +
				"WHERE uuid = ?";
	}
	
	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Vector<ContactNumberDBObject> getContactNumberDBObjects()
	{
		return contactNumberDBObjects;
	}

	public void setContactNumberDBObjects(Vector<ContactNumberDBObject> contactNumberDBObjects)
	{
		this.contactNumberDBObjects = contactNumberDBObjects;
	}
	
	public void addContactNumberDBObject(ContactNumberDBObject contactNumberDBObject)
	{
		this.contactNumberDBObjects.add(contactNumberDBObject);
	}
	
	public void removeContactNumberDBObject(ContactNumberDBObject contactNumberDBObject)
	{
		this.contactNumberDBObjects.remove(contactNumberDBObject);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ContactDataDBObject)
		{
			return ((ContactDataDBObject) obj).uuid.equals(uuid);
		}
		else
		{
			return super.equals(obj);
		}
	}
}
