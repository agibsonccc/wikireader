package com.ccc.sendalyzeit.reader.core;

import java.io.Serializable;

public interface Document extends Serializable {

	public long getId();
	public void setId(long id);
	
	
	public String getTitle();
	public void setTitle(String title);
	
	public String getText();
	public void setText(String text);
	
}
