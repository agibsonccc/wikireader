package com.ccc.sendalyzeit.reader.impl;

import com.ccc.sendalyzeit.reader.core.Document;

public class DocumentImpl implements Document {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6636491856264700055L;
	private long id;
	private String title;
	private String text;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	 
}
