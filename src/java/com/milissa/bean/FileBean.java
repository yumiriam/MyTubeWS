package com.milissa.bean;

import javax.activation.DataHandler;

public class FileBean {
	private String name;
	private String type;
	private DataHandler data;

	public DataHandler getData() {
		return data;
	}

	public void setData(DataHandler data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
