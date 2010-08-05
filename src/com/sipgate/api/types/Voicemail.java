package com.sipgate.api.types;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Parcel;
import android.util.Log;


public class Voicemail extends Event {

	private String voicemail = null;
	private String voicemail_id = "";
	private String transcription = null;
	private String content_url = "";
	private String voicemail_from = "";
	private String received_on = "";
	private String duration = "";

	public Voicemail() {}
	
	@Override
	public String toString() {
		return String.format("Voicemail(%s): from: %s at: %s id: %s" ,
				hashCode(),getNumberE164(),received_on,voicemail_id); 	
	}
	
	public Voicemail(Parcel in) {
		super(in);		
	}
	
	@Override
	protected void readFromParcel(Parcel in) {
		super.readFromParcel(in);
		
		voicemail = in.readString();
		voicemail_id  = in.readString();
		transcription = in.readString();
		content_url = in.readString();
		voicemail_from = in.readString();
		received_on = in.readString();
		duration = in.readString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		
		out.writeString(voicemail);
		out.writeString(voicemail_id);
		out.writeString(transcription);
		out.writeString(content_url);
		out.writeString(voicemail_from);
		out.writeString(received_on);
		out.writeString(duration);
	}

	public static Voicemail fromXMLNode(Node node) {
		
		Voicemail voiceMail = (Voicemail) Event.fromXMLNode(node, Voicemail.class);

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) node;
			
			voiceMail.setDuration(getElement(fstElmnt, "duration"));
			voiceMail.setTranscription(getElement(fstElmnt, "transcription"));
		    voiceMail.setContent_url(getVoicemailContent(fstElmnt));
			voiceMail.setVoicemail_id(getElement(fstElmnt, "id"));
			
		}
		
		return voiceMail;
	}

	private static String getVoicemailContent(Element fstElmnt) {
		try {
			NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("content");
			Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
			if (fstNmElmnt == null) {
				Log.w(TAG,"voicemail without content");
				return null;
			}
			
			fstNmElmntLst= fstNmElmnt.getElementsByTagName("get");
			fstNmElmnt = (Element) fstNmElmntLst.item(0);
			if (fstNmElmnt == null) {
				Log.w(TAG,"voicemail without get");
				return null;
			}
			
			NodeList fstNm = fstNmElmnt.getChildNodes();
			return ((Node) fstNm.item(0)).getNodeValue();
		} catch (DOMException e) {
			Log.e(TAG,"error, getting voicemail content");
			e.printStackTrace();
		}
		return null;
	}

	
	

	public String getVoicemail() {
		if (voicemail == null || voicemail.length() < 1 || voicemail.equalsIgnoreCase("")) {
			return null;
		}
		return voicemail;
	}

	public void setVoicemail(String voicemail) {
		this.voicemail = voicemail;
	}

	public String getVoicemail_id() {
		return voicemail_id;
	}

	public void setVoicemail_id(String voicemailId) {
		voicemail_id = voicemailId;
	}

	public String getTranscription() {
		if (transcription == null || transcription.length() < 1 || transcription.equals("")) {
			return null;
		}
		
		return transcription;
	}

	public void setTranscription(String transcription) {
		if (transcription == null || transcription.length() < 1 || transcription.equals("")) {
			this.transcription = null;
		} else {
			this.transcription = transcription;
		}
	}

	public String getContent_url() {
		return content_url;
	}

	public void setContent_url(String contentUrl) {
		content_url = contentUrl;
	}

	public String getVoicemail_from() {
		return voicemail_from;
	}

	public void setVoicemail_from(String voicemailFrom) {
		voicemail_from = voicemailFrom;
	}

	public String getReceived_on() {
		return received_on;
	}

	public void setReceived_on(String receivedOn) {
		received_on = receivedOn;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}






}
