package com.milissa.rmiclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceRMI extends Remote {
	// métodos para o WS invocar
	FileBean rescueFile(String id) throws RemoteException;
	boolean saveFile(FileBean fb, String id) throws RemoteException;
}
