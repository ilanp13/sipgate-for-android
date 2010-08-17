package com.sipgate.api.types;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Event implements Parcelable {
	
	protected static final String TAG = "api types";
	private String numberPretty = "";
	private String numberE164 = "";
	private String read = "";
	private String starred = "";
	private String location = "";
	private String direction = "";
	private String createOn = "";

	protected Event() {
		
	}

	public static Event fromXMLNode(Node node, Class<? extends Event> c) {
		
		Event e;
		try {
			e = c.newInstance();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element fstElmnt = (Element) node;
			e.setCreateOn(getElement(fstElmnt, "created"));		
			e.setDirection(getElement(fstElmnt, "direction"));
			String read = getRead(fstElmnt);
			e.setRead(read);
			e.setStarred(getStarred(fstElmnt));
			e.setLocation(getElement(fstElmnt, "location"));
			String numberE164 = getNumber_numberE164(fstElmnt);
			e.setNumberE164(numberE164);
			String numberPretty = getNumberPretty(fstElmnt);
			if (numberPretty.trim().length() > 0)
				e.setNumberPretty(numberPretty);
			else {
				if (numberE164.trim().length() > 0)
					e.setNumberPretty(numberE164);
				else
					e.setNumberPretty(getAlias(fstElmnt)); // TODO
				// huh?
			}
		}
		return e;
	}
	
	
	protected static String getElement(Element fstElmnt, String name) {
		NodeList fstNmElmntLst = fstElmnt.getElementsByTagName(name);
		Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
		if (fstNmElmnt == null) {
			Log.w(TAG,"event without " + name + " element");
			return null;
		}
		NodeList fstNm = fstNmElmnt.getChildNodes();

		return ((Node) fstNm.item(0)).getNodeValue();
	}
	
	/**
	 * @Method are used for getting(return) Element of read Field and it's value
	 */
	private static String getRead(Element fstElmnt) {
		NodeList lstNmElmntLst1 = fstElmnt.getElementsByTagName("read");
		Node thNode = lstNmElmntLst1.item(0);
		if (thNode == null) {
			return null;
		}
		Element lstNmElmnt1 = (Element) thNode;
		NodeList endpoint = lstNmElmnt1.getElementsByTagName("value");
		Element test = (Element) endpoint.item(0);
		NodeList lstNm1 = test.getChildNodes();
		return ((Node) lstNm1.item(0)).getNodeValue();
	}
	
	

	/**
	 * @Method are used for getting(return) Element of starred Field and it's
	 *         value
	 */
	private static String getStarred(Element fstElmnt) {
		NodeList lstNmElmntLst1 = fstElmnt.getElementsByTagName("starred");
		Node thNode = lstNmElmntLst1.item(0);
		Element lstNmElmnt1 = (Element) thNode;
		NodeList endpoint = lstNmElmnt1.getElementsByTagName("value");
		Element test = (Element) endpoint.item(0);
		NodeList lstNm1 = test.getChildNodes();
		return ((Node) lstNm1.item(0)).getNodeValue();
	}

	
	
	/**
	 * @Method are used for getting(return) Element of Number Field and return
	 *         only numberE164 (Anonymous)
	 */
	private static String getNumber_numberE164(Element fstElmnt) {
		String str = "";
		NodeList lstNmElmntLst1 = fstElmnt.getElementsByTagName("sources");
		Node thNode = lstNmElmntLst1.item(0);
		Element lstNmElmnt1 = (Element) thNode;
		NodeList n = lstNmElmnt1.getChildNodes();
		for (int k = 0; k < n.getLength(); k++) {
			Node cn = n.item(k);
			if (cn.getNodeType() == Node.ELEMENT_NODE) {
				Element fe = (Element) cn;
				NodeList lstNmElmntLst2 = fe.getElementsByTagName("numberE164");
				Element lstNmElmnt3 = (Element) lstNmElmntLst2.item(0);
				if (lstNmElmnt3 != null) {
					NodeList lstNm2 = lstNmElmnt3.getChildNodes();
					if (lstNm2.item(0) != null) {
						str = ((Node) lstNm2.item(0)).getNodeValue();
					}
				}
			}
		}
		return str;
	}
	
	/**
	 * @Method are used for getting(return) Element of Extension Field and it's
	 *         alias
	 */
	private static String getAlias(Element fstElmnt) {
		NodeList lstNmElmntLst1 = fstElmnt.getElementsByTagName("extension");
		Node thNode = lstNmElmntLst1.item(0);
		Element lstNmElmnt1 = (Element) thNode;
		NodeList endpoint = lstNmElmnt1.getElementsByTagName("alias");
		Element test = (Element) endpoint.item(0);
		if (test != null) {
			NodeList lstNm1 = test.getChildNodes();
			return ((Node) lstNm1.item(0)).getNodeValue();
		} else {
			return "";
		}
	}
	
	/**
	 * @Method are used for getting(return) Element of Number Field and return
	 *         only number pretty
	 */
	private  static String getNumberPretty(Element fstElmnt) {
		String str = "";
		NodeList lstNmElmntLst1 = fstElmnt.getElementsByTagName("sources");
		Node thNode = lstNmElmntLst1.item(0);
		Element lstNmElmnt1 = (Element) thNode;
		NodeList n = lstNmElmnt1.getChildNodes();
		for (int k = 0; k < n.getLength(); k++) {
			Node cn = n.item(k);
			if (cn.getNodeType() == Node.ELEMENT_NODE) {
				Element fe = (Element) cn;
				NodeList lstNmElmntLst2 = fe.getElementsByTagName("numberPretty");
				Element lstNmElmnt3 = (Element) lstNmElmntLst2.item(0);
				if (lstNmElmnt3 != null) {
					NodeList lstNm2 = lstNmElmnt3.getChildNodes();
					if (lstNm2.item(0) != null) {
						str = ((Node) lstNm2.item(0)).getNodeValue();
					}
				}
			}
		}
		return str;
	}
	
	public String getNumberPretty() {
		return numberPretty;
	}

	public void setNumberPretty(String number) {
		this.numberPretty = number;
	}

	public String getNumberE164() {
		return numberE164;
	}
	
	public String getNumberE164WithoutScheme() {
		int p = numberE164.indexOf(":");
		if (p >= 0) {
			return numberE164.substring(p+1);
		} else {
			return numberE164;
		}
	}

	public void setNumberE164(String numberE164) {
		this.numberE164 = numberE164;
	}
	
	public boolean isRead() {
		if (read == null || read.equals("") || read.equals("false")) {
			return false;
		} else {
			return true;
		}
	}

	public void setRead(String read) {
		this.read = read;
	}

	public String getStarred() {
		return starred;
	}

	public void setStarred(String starred) {
		this.starred = starred;
	}
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}


	public String getCreateOn() {
		return createOn;
	}
	
	public Date getCreateOnAsDate() {
		try {
			if (createOn == null) {
				return new Date(0);
			}
			SimpleDateFormat dateformatterIso = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			return dateformatterIso.parse(createOn, new ParsePosition(0));
		} catch (IllegalArgumentException e) {
			Log.e(TAG,"badly formated date");
			
		}
		return new Date(0);
	}
	
	

	public void setCreateOn(String createOn) {
		this.createOn = createOn;
	}


	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Event(Parcel in) {
		readFromParcel(in);
	}
	
	protected void readFromParcel(Parcel in) {
		numberPretty = in.readString();
		numberE164  = in.readString();
		read = in.readString();
		starred = in.readString();
		location = in.readString();
		direction = in.readString();
		createOn = in.readString();
	}


	public void writeToParcel(Parcel out, int flags) {
		out.writeString(numberPretty);
		out.writeString(numberE164);
		out.writeString(read);
		out.writeString(starred);
		out.writeString(location);
		out.writeString(direction);
		out.writeString(createOn);
	}
	
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {

    	public Event createFromParcel(Parcel in) {
                return new Event(in);
        }

        public Event[] newArray(int size) {
                return new Event[size];
        }
    };

	

}
