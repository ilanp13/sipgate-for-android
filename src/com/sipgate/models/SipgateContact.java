package com.sipgate.models;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;

public class SipgateContact implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 493453955440784116L;
	private int id = 0;
	private String firstName = null;
	private String lastName = null;
	private String title = null;
	private ArrayList<SipgateContactNumber> numbers = null;
	private Bitmap photo = null;
	
	public SipgateContact(int id, String firstName, String lastName, String title, ArrayList<SipgateContactNumber> numbers, Bitmap photo){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = title;
		this.numbers = numbers;
		this.photo = photo;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public ArrayList<SipgateContactNumber> getNumbers() {
		return numbers;
	}
	
	public void setNumbers(ArrayList<SipgateContactNumber> numbers) {
		this.numbers = numbers;
	}

	public void setPhoto(Bitmap photo) {
		this.photo = photo;
	}

	public Bitmap getPhoto() {
		return photo;
	}
	
}
