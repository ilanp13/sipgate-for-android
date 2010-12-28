package com.sipgate.db;

import java.util.Date;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.db.base.BaseDBObject;

public class CallDataDBObject extends BaseDBObject
{
	public final static long INCOMING = 0;
	public final static long OUTGOING = 1;
	
	private long id = 0;
	private long direction = 0;
	private long missed = 0;
	private long read = 0;
	private long time = 0;
		
	private String localNumberE164 = "";
	private String localNumberPretty = "";
	private String localName = "";

	private String remoteNumberE164 = "";
	private String remoteNumberPretty = "";
	private String remoteName = "";
	
	private String readModifyUrl = "";
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
		statement.bindLong(2, direction);
		statement.bindLong(3, missed);
		statement.bindLong(4, read);
		statement.bindLong(5, time);
		
		statement.bindString(6, localNumberE164);	
		statement.bindString(7, localNumberPretty);
		statement.bindString(8, localName);
		
		statement.bindString(9, remoteNumberE164);
		statement.bindString(10, remoteNumberPretty);
		statement.bindString(11, remoteName);
		
		statement.bindString(12, readModifyUrl);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindLong(1, direction);
		statement.bindLong(2, missed);
		statement.bindLong(3, read);
		statement.bindLong(4, time);
		
		statement.bindString(5, localNumberE164);
		statement.bindString(6, localNumberPretty);
		statement.bindString(7, localName);
		
		statement.bindString(8, remoteNumberE164);
		statement.bindString(9, remoteNumberPretty);
		statement.bindString(10, remoteName);
		
		statement.bindString(11, readModifyUrl);
		
		statement.bindLong(12, id);
	}

	public String[] getCreateStatement()
	{
		return new String[]	{	"CREATE TABLE CallData (" +
									"id INTEGER PRIMARY KEY NOT NULL, " +
									"direction INTEGER, " +
									"missed INTEGER, " +
									"read INTEGER, " +
									"time INTEGER, " +
									"localNumberE164 VARCHAR, " +
									"localNumberPretty VARCHAR, " +
									"localName VARCHAR, " +
									"remoteNumberE164 VARCHAR, " +
									"remoteNumberPretty VARCHAR, " +
									"remoteName VARCHAR, " +
									"readModifyUrl VARCHAR);",
								"CREATE UNIQUE INDEX uidx_id_CallData ON CallData (id ASC);",
								"CREATE TRIGGER delete_CallData_Max100 AFTER INSERT ON CallData BEGIN " +
									"DELETE FROM CallData WHERE time < (SELECT time FROM CallData ORDER BY time DESC LIMIT 99,1); " +
								"END;"
		};
	}

	public String getDeleteStatement()
	{		
		return 	"DELETE FROM CallData WHERE id = ?";
	}

	public String getInsertStatement()
	{
		return 	"INSERT INTO CallData (" +
					"id, direction, missed, read, time, localNumberE164, localNumberPretty, " +
					"localName, remoteNumberE164, remoteNumberPretty, remoteName, readModifyUrl) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	public String getTableName()
	{
		return "CallData";
	}

	public String getUpdateStatement()
	{
		return 	"UPDATE CallData " +
					"SET direction = ?, missed = ?, read = ?, time = ?, localNumberE164 = ?, localNumberPretty = ?, " +
					"localName = ?, remoteNumberE164 = ?, remoteNumberPretty = ?, remoteName = ?, readModifyUrl = ? " +
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

	public long getDirection()
	{
		return direction;
	}

	public void setDirection(long direction)
	{
		this.direction = direction;
	}

	public boolean isMissed()
	{
		return (missed > 0);
	}
	
	public void setMissed(long missed)
	{
		this.missed = missed;
	}

	public void setMissed(boolean missed)
	{
		this.missed = (missed ? 1 : 0);
	}

	public boolean isRead()
	{
		return (read > 0);
	}

	public long getRead()
	{
		return read;
	}
	
	public void setRead(long read)
	{
		this.read = read;
	}

	public void setRead(String read)
	{
		this.read = (read.equals("true") ? 1 : 0);
	}
	
	public void setRead(boolean read)
	{
		this.read = (read ? 1 : 0);
	}
	
	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}
	
	public Date getCallAsDate()
	{
		return new Date(time);
	}

	public String getLocalNumberE164()
	{
		return localNumberE164;
	}

	public void setLocalNumberE164(String localNumberE164)
	{
		this.localNumberE164 = localNumberE164;
	}

	public String getLocalNumberPretty()
	{
		return localNumberPretty;
	}

	public void setLocalNumberPretty(String localNumberPretty)
	{
		this.localNumberPretty = localNumberPretty;
	}

	public String getLocalName()
	{
		return localName;
	}

	public void setLocalName(String localName)
	{
		this.localName = localName;
	}

	public String getRemoteNumberE164()
	{
		return remoteNumberE164;
	}

	public void setRemoteNumberE164(String remoteNumberE164)
	{
		this.remoteNumberE164 = remoteNumberE164;
	}

	public String getRemoteNumberPretty()
	{
		return remoteNumberPretty;
	}

	public void setRemoteNumberPretty(String remoteNumberPretty)
	{
		this.remoteNumberPretty = remoteNumberPretty;
	}

	public String getRemoteName()
	{
		return remoteName;
	}

	public void setRemoteName(String remoteName)
	{
		this.remoteName = remoteName;
	}

	public String getReadModifyUrl()
	{
		return readModifyUrl;
	}

	public void setReadModifyUrl(String readModifyUrl)
	{
		this.readModifyUrl = readModifyUrl;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof CallDataDBObject)
		{
			return ((CallDataDBObject)obj).getId() == id;
		}
		else
		{	
			return super.equals(obj);
		}
	}
}
