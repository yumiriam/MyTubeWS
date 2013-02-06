package com.milissa.ws;

import com.milissa.appengine.Descricao;
import com.milissa.appengine.AppEngineInterface;
import com.milissa.rmiclient.InterfaceRMI;
import com.milissa.rmiclient.RMIFileBean;
import com.milissa.bean.FileBean;
import com.milissa.corbaclient.IdServer;
import com.milissa.corbaclient.IdServerHelper;
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
	private static String CORBA_NAMESERVICE_HOST = "miriam-laptop:2809";

	private static IdServer callCorbaServer() throws IOException {
		String[] args = {"-ORBInitRef", "NameService=corbaloc::"+CORBA_NAMESERVICE_HOST};

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
			String id = idserver.generateId(fileName.toUpperCase());

			if (id != null) {
				/* RMI */
				try {
					Registry registry = LocateRegistry.getRegistry(RMI_HOST);
					InterfaceRMI stub = (InterfaceRMI) registry.lookup("InterfaceRMI");

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					byte[] buffer = new byte[100000];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) { 
						os.write(buffer, 0, bytesRead); 
					}
					
					RMIFileBean rmiFile = new RMIFileBean();
					rmiFile.setName(file.getName());
					rmiFile.setData(os.toByteArray());
					
					os.flush(); 
					
					os.close(); 
					is.close();

					if (stub.saveFile(rmiFile, id)) {
						/* App Engine */
						Descricao desc = new Descricao();
						desc.setId(id);
						desc.setDescricao(file.getDescription());
						AppEngineInterface descServer = new AppEngineInterface();
						descServer.enviarArquivo(desc);
						
						return id;
					}
				} catch (Exception e) {
					System.err.println("Client exception: " + e.toString());
				}
			}
		} catch (IOException e) {
			System.err.println("IO exception: " + e.toString());
		}
		return null;
	}

	@WebMethod(operationName = "download")
	public FileBean download(@WebParam(name = "id") String id) {
		try {
			/* CORBA */
			IdServer idserver = callCorbaServer();
			id = id.toUpperCase();
			if (idserver.verifyId(id)) {
				/* RMI */
				Registry registry = LocateRegistry.getRegistry(RMI_HOST);
				InterfaceRMI stub = (InterfaceRMI) registry.lookup("InterfaceRMI");
				
				RMIFileBean rmiFile = stub.rescueFile(id);
				
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
					AppEngineInterface descServer = new AppEngineInterface();
					Descricao desc =  descServer.receberArquivo(id);
					downloadFile.setDescription(desc.getDescricao());
					
					return downloadFile;
				}
			}

		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
		}
		return null;
	}
}
