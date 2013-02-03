package com.milissa.main;

import com.milissa.client.FileBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.activation.DataHandler;

public class TestClient {
	
	public static void main(String[] args) {
		File f = new File("/home/miriam/dados/Coisas/screencasts/railscasts_273-geocoder.ogv");
		String fullName = f.getName();
		int dotIndex = fullName.lastIndexOf('.');
		String name = fullName.substring(0, dotIndex-1);
		String type = fullName.substring(dotIndex, fullName.length());
		
		try {
			FileInputStream fis = new FileInputStream(f);
			int length = fis.available();
			byte[] b = new byte[length];
			DataHandler data = new DataHandler(b, type);

			FileBean uploadFile = new FileBean();
			uploadFile.setName(name);
			uploadFile.setType(type);
			uploadFile.setData(b);
			
			if (upload(uploadFile) == 1)
				System.out.println("Upload!");
			else
				System.out.println("Error!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int upload(com.milissa.client.FileBean file) {
		com.milissa.client.TestService_Service service = new com.milissa.client.TestService_Service();
		com.milissa.client.TestService port = service.getTestServicePort();
		return port.upload(file);
	}
}
