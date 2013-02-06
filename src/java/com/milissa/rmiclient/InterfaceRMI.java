package com.milissa.rmiclient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceRMI extends Remote {
	// métodos para o WS invocar
	RMIFileBean rescueFile(String id) throws RemoteException;
	boolean saveFile(RMIFileBean fb, String id) throws RemoteException;
}
