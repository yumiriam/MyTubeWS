/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.milissa.ws;

import com.milissa.bean.FileBean;
import com.milissa.corbaclient.*;
import com.sun.xml.ws.util.ByteArrayDataSource;

import java.io.*;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

@WebService(serviceName = "VideoService")
public class VideoService {
	
	private static String callCorbaServer() throws IOException	 {
		String[] args = {"-ORBInitRef", "NameService=corbaloc::miriam-laptop:2809"};
						
		try {
			// Create an object request broker
			ORB orb = ORB.init(args, null);

			org.omg.CORBA.Object obj =  VideoService.getObjectReference(orb);

			IdServer idserver = IdServerHelper.narrow(obj);

			return idserver.generateId("LISSA!!!");
		} catch (org.omg.CORBA.TRANSIENT exception) {
			System.out.println("Caught system exception TRANSIENT -- unable to contact the server.");
			exception.printStackTrace(System.out);
		} catch (org.omg.CORBA.SystemException exception) {
			exception.printStackTrace(System.out);
		} catch (Exception exception) {
			exception.printStackTrace(System.out);
		}
		
		return null;
	}
	
	private static org.omg.CORBA.Object getObjectReference(ORB orb) {
		try {
			// Obtain object reference for name service ...
			org.omg.CORBA.Object object = orb.resolve_initial_references("NameService");

			//NamingContext namingContext = NamingContextHelper.narrow(object);
			NamingContextExt namingContext = NamingContextExtHelper.narrow(object);
			NameComponent [] path = { 
					new NameComponent("test", "my_context"), 
					new NameComponent("IdServer", "Object") 
			};
			return namingContext.resolve(path);
		} catch (org.omg.CORBA.ORBPackage.InvalidName exception) {
			exception.printStackTrace(System.out);
		} catch (org.omg.CosNaming.NamingContextPackage.NotFound exception) {
			System.out.println(exception.why.value());
			exception.printStackTrace(System.out);
		} catch (org.omg.CosNaming.NamingContextPackage.CannotProceed exception) {
			exception.printStackTrace(System.out);
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName exception) {
			exception.printStackTrace(System.out);
		} catch (org.omg.CORBA.COMM_FAILURE exception) {
			exception.printStackTrace(System.out);
		} catch (Exception exception) {
			exception.printStackTrace(System.out);
		}

		return null;
	}

	@WebMethod(operationName = "upload")
	public String upload(@WebParam(name = "file") FileBean file) {
		//TODO upload
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
			
			System.out.println(callCorbaServer());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@WebMethod(operationName = "download")
	public FileBean download(@WebParam(name = "id") String id) {
		//TODO download
		
		try {
			FileInputStream fis = new FileInputStream(
				new File("/home/miriam/uploads/bergamot.jpg")
			);
			
			FileBean fileBean = new FileBean();
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[100000];
			int bytesRead;
			while ((bytesRead = fis.read(b)) != -1) {
				bos.write(b, 0, bytesRead);
			}
			
			ByteArrayDataSource bds = new ByteArrayDataSource(bos.toByteArray(), "application/octet-stream");
			
			DataHandler handler = new DataHandler(bds);
			
			fileBean.setData(handler);
			fileBean.setName("railscasts_273-geocode");
			fileBean.setType("ogv");
			
			bos.flush();
			bos.close();
			fis.close();
			
			return fileBean;
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
}
