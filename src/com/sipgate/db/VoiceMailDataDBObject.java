package com.sipgate.db;

import java.util.Date;

import android.database.sqlite.SQLiteStatement;

import com.sipgate.db.base.BaseDBObject;

public class VoiceMailDataDBObject extends BaseDBObject
{
	private long id = 0;
	
	private long read = 0;
	private long seen = 0;
	private long time = 0;
	private long duration = 0;	
	
	private String localNumberE164 = null;
	private String localNumberPretty = null;
	private String localName = null;

	private String remoteNumberE164 = null;
	private String remoteNumberPretty = null;
	private String remoteName = null;
	
	private String transcription = null;
	
	private String contentUrl = null;
	private String localFileUrl = null;
	private String readModifyUrl = null;
	
	public void bindDelete(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
	}

	public void bindInsert(SQLiteStatement statement)
	{
		statement.bindLong(1, id);
		statement.bindLong(2, read);
		statement.bindLong(3, seen);
		statement.bindLong(4, time);
		statement.bindLong(5, duration);
				
		statement.bindString(6, localNumberE164);	
		statement.bindString(7, localNumberPretty);
		statement.bindString(8, localName);
		
		statement.bindString(9, remoteNumberE164);
		statement.bindString(10, remoteNumberPretty);
		statement.bindString(11, remoteName);
		
		statement.bindString(12, transcription);
		statement.bindString(13, contentUrl);
		statement.bindString(14, localFileUrl);
		statement.bindString(15, readModifyUrl);
	}

	public void bindUpdate(SQLiteStatement statement)
	{
		statement.bindLong(1, read);
		statement.bindLong(2, seen);
		statement.bindLong(3, time);
		statement.bindLong(4, duration);
				
		statement.bindString(5, localNumberE164);	
		statement.bindString(6, localNumberPretty);
		statement.bindString(7, localName);
		
		statement.bindString(8, remoteNumberE164);
		statement.bindString(9, remoteNumberPretty);
		statement.bindString(10, remoteName);
		
		statement.bindString(11, transcription);
		statement.bindString(12, contentUrl);
		statement.bindString(13, localFileUrl);
		statement.bindString(14, readModifyUrl);
		
		statement.bindLong(15, id);
	}

	public String getCreateStatement()
	{
		return "CREATE TABLE VoiceMailData (id INTEGER PRIMARY KEY NOT NULL, read INTEGER, seen INTEGER, time INTEGER, duration INTEGER, localNumberE164 VARCHAR, localNumberPretty VARCHAR, localName VARCHAR, remoteNumberE164 VARCHAR, remoteNumberPretty VARCHAR, remoteName VARCHAR, transcription VARCHAR, contentUrl VARCHAR, localFileUrl VARCHAR, readModifyUrl VARCHAR)";
	}

	public String getDeleteStatement()
	{		
		return "DELETE FROM VoiceMailData WHERE id = ?";
	}

	public String getInsertStatement()
	{
		return "INSERT INTO VoiceMailData (id, read, seen, time, duration, localNumberE164, localNumberPretty, localName, remoteNumberE164, remoteNumberPretty, remoteName, transcription, contentUrl, localFileUrl, readModifyUrl) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	public String getTableName()
	{
		return "VoiceMailData";
	}

	public String getUpdateStatement()
	{
		return "UPDATE VoiceMailData SET read = ?, seen = ?, time = ?, duration = ?, localNumberE164 = ?, localNumberPretty = ?, localName = ?, remoteNumberE164 = ?, remoteNumberPretty = ?, remoteName = ?, transcription = ?, contentUrl = ?, localFileUrl = ?, readModifyUrl = ? WHERE id = ?";
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
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
	
	public boolean isSeen()
	{
		return (seen > 0);
	}

	public long getSeen()
	{
		return seen;
	}
	
	public void setSeen(long seen)
	{
		this.seen = seen;
	}

	public void setSeen(String seen)
	{
		this.seen = (seen.equals("true") ? 1 : 0);
	}
	
	public void setSeen(boolean seen)
	{
		this.seen = (seen ? 1 : 0);
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

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	public String getTranscription()
	{
		return transcription;
	}

	public void setTranscription(String transcription)
	{
		this.transcription = transcription;
	}

	public String getContentUrl()
	{
		return contentUrl;
	}

	public void setContentUrl(String contentUrl)
	{
		this.contentUrl = contentUrl;
	}

	public String getLocalFileUrl()
	{
		return localFileUrl;
	}

	public void setLocalFileUrl(String localFileUrl)
	{
		this.localFileUrl = localFileUrl;
	}
}