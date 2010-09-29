package com.sipgate.db;

import java.util.Date;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.base.db.BaseDBObject;

public class CallDataDBObject extends BaseDBObject
{
	public final static long INCOMING = 0;
	public final static long OUTGOING = 1;
	
	private long id = 0;
	private long direction = 0;
	private long missed = 0;
	private long read = 0;
	private long time = 0;
		
	private String targetNumberE164 = null;
	private String targetNumberPretty = null;
	private String targetName = null;

	private String sourceNumberE164 = null;
	private String sourceNumberPretty = null;
	private String sourceName = null;
	
	private String readModifyUrl = null;
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindLong(1, direction);
		statement.bindLong(2, missed);
		statement.bindLong(3, read);
		statement.bindLong(4, time);
		
		statement.bindString(5, targetNumberE164);	
		statement.bindString(6, targetNumberPretty);
		statement.bindString(7, targetName);
		
		statement.bindString(8, sourceNumberE164);
		statement.bindString(9, sourceNumberPretty);
		statement.bindString(10, sourceName);
		
		statement.bindString(11, readModifyUrl);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindLong(1, direction);
		statement.bindLong(2, missed);
		statement.bindLong(3, read);
		statement.bindLong(4, time);
		
		statement.bindString(5, targetNumberE164);
		statement.bindString(6, targetNumberPretty);
		statement.bindString(7, targetName);
		
		statement.bindString(8, sourceNumberE164);
		statement.bindString(9, sourceNumberPretty);
		statement.bindString(10, sourceName);
		
		statement.bindString(11, readModifyUrl);
		
		statement.bindLong(12, id);
	}

	public String getCreateStatement()
	{
		return "CREATE TABLE CallData (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, direction INTEGER, missed INTEGER, read INTEGER, time INTEGER, targetNumberE164 VARCHAR, targetNumberPretty VARCHAR, targetName VARCHAR, sourceNumberE164 VARCHAR, sourceNumberPretty VARCHAR, sourceName VARCHAR, readModifyUrl VARCHAR)";
	}

	public String getDeleteStatement()
	{		
		return "DELETE FROM CallData WHERE id = ?";
	}

	public String getInsertStatement()
	{
		return "INSERT INTO CallData (direction, missed, read, time, targetNumberE164, targetNumberPretty, targetName, sourceNumberE164, sourceNumberPretty, sourceName, readModifyUrl) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	public String getTableName()
	{
		return "CallData";
	}

	public String getUpdateStatement()
	{
		return "UPDATE CallData SET direction = ?, missed = ?, read = ?, time = ?, targetNumberE164 = ?, targetNumberPretty = ?, targetName = ?, sourceNumberE164 = ?, sourceNumberPretty = ?, sourceName = ?, readModifyUrl = ? WHERE id = ?";
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

	public void setRead(long read)
	{
		this.read = read;
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

	public String getTargetNumberE164()
	{
		return targetNumberE164;
	}

	public void setTargetNumberE164(String targetNumberE164)
	{
		this.targetNumberE164 = targetNumberE164;
	}

	public String getTargetNumberPretty()
	{
		return targetNumberPretty;
	}

	public void setTargetNumberPretty(String targetNumberPretty)
	{
		this.targetNumberPretty = targetNumberPretty;
	}

	public String getTargetName()
	{
		return targetName;
	}

	public void setTargetName(String targetName)
	{
		this.targetName = targetName;
	}

	public String getSourceNumberE164()
	{
		return sourceNumberE164;
	}

	public void setSourceNumberE164(String sourceNumberE164)
	{
		this.sourceNumberE164 = sourceNumberE164;
	}

	public String getSourceNumberPretty()
	{
		return sourceNumberPretty;
	}

	public void setSourceNumberPretty(String sourceNumberPretty)
	{
		this.sourceNumberPretty = sourceNumberPretty;
	}

	public String getSourceName()
	{
		return sourceName;
	}

	public void setSourceName(String sourceName)
	{
		this.sourceName = sourceName;
	}

	public String getReadModifyUrl()
	{
		return readModifyUrl;
	}

	public void setReadModifyUrl(String readModifyUrl)
	{
		this.readModifyUrl = readModifyUrl;
	}
}
