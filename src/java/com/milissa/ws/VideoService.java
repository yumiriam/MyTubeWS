package com.milissa.ws;

import com.milissa.bean.FileBean;
import com.milissa.corbaclient.IdServer;
import com.milissa.corbaclient.IdServerHelper;
import com.milissa.rmiclient.InterfaceRMI;
import java.io.*;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.mail.util.ByteArrayDataSource;

/*
 * CORBA
 */
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/*
 * RMI
 */
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@WebService(serviceName = "VideoService")
public class VideoService {
	
	private static String RMI_HOST = "localhost";
	private static String NAMESERVICE_HOST = "miriam-laptop:2809";

	private static IdServer callCorbaServer() throws IOException {
		String[] args = {"-ORBInitRef", "NameService=corbaloc::"+NAMESERVICE_HOST};

		try {
			// Create an object request broker
			ORB orb = ORB.init(args, null);

			org.omg.CORBA.Object obj = VideoService.getObjectReference(orb);

			IdServer idserver = IdServerHelper.narrow(obj);

			return idserver;
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
			NameComponent[] path = {
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
		DataHandler handler = file.getData();
		try {
			InputStream is = handler.getInputStream();

			int dotIndex = file.getName().lastIndexOf(".");
			String fileName = file.getName().substring(0, dotIndex - 1);

			/* CORBA */
			IdServer idserver = callCorbaServer();
			String id = idserver.generateId(fileName);

			if (id != null) {
				/* RMI */
				try {
					Registry registry = LocateRegistry.getRegistry(RMI_HOST);
					InterfaceRMI stub = (InterfaceRMI) registry.lookup("MiLissaRMI");

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					byte[] buffer = new byte[100000];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) { 
						os.write(buffer, 0, bytesRead); 
					}
					
					com.milissa.rmiclient.FileBean rmiFile = new com.milissa.rmiclient.FileBean();
					rmiFile.setName(file.getName());
					rmiFile.setData(os.toByteArray());
					
					os.flush(); 
					
					os.close(); 
					is.close();

					if (stub.saveFile(rmiFile, id)) {
						/* App Engine */
						// Coisa nova
						
						return id;
					}
				} catch (Exception e) {
					System.err.println("Client exception: " + e.toString());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@WebMethod(operationName = "download")
	public FileBean download(@WebParam(name = "id") String id) {
		try {
			/* CORBA */
			IdServer idserver = callCorbaServer();
			if (idserver.verifyId(id)) {
				/* RMI */
				Registry registry = LocateRegistry.getRegistry(RMI_HOST);
				InterfaceRMI stub = (InterfaceRMI) registry.lookup("MiLissaRMI");
				
				com.milissa.rmiclient.FileBean rmiFile = stub.rescueFile(id);
				
				if (rmiFile != null) {
					FileBean downloadFile = new FileBean();
					downloadFile.setName(rmiFile.getName());
					
					InputStream is = new ByteArrayInputStream(rmiFile.getData());
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					byte[] buffer = new byte[100000];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}

					ByteArrayDataSource bds = new ByteArrayDataSource(os.toByteArray(), "application/octet-stream");
					DataHandler handler = new DataHandler(bds);

					downloadFile.setData(handler);
					
					/* App Engine */
					//downloadFile.setDescription();
					
					return downloadFile;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
