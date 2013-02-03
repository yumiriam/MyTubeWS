/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.milissa.ws;

import com.milissa.bean.FileBean;
import java.io.*;
import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author miriam
 */
@WebService(serviceName = "TestService")
public class TestService {

	/**
	 * This is a sample web service operation
	 */
	@WebMethod(operationName = "hello")
	public String hello(@WebParam(name = "name") String txt) {
		return "Hello " + txt + " !";
	}

	/**
	 * Web service operation
	 */
	@WebMethod(operationName = "upload")
	public int upload(@WebParam(name = "file") FileBean file) {
		
		DataHandler handler = file.getData();
		try {
			InputStream is = handler.getInputStream();

			OutputStream os = new FileOutputStream(
							new File("/home/miriam/uploads/"+ file.getName() +"."+ file.getType()));
			byte[] b = new byte[100000];
			int bytesRead;
			while ((bytesRead = is.read(b)) != -1) {
				os.write(b, 0, bytesRead);
			}
			
			os.flush();
			os.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
}
