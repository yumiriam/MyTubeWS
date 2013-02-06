package com.milissa.rmiclient;

import java.io.Serializable;

public class FileBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] data;
	private String name;
    
	public FileBean(){

	}

	public byte[] getData() {
					return data;
	}

	public void setData(byte[] data) {
					this.data = data;
	}

	public String getName() {
					return name;
	}

	public void setName(String name) {
					this.name = name;
	}
}
