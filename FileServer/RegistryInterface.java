////package FileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryInterface extends Remote{
	
	//Registers a server with the RegistryServer.
	// True, if caller has been made the master, false if not
	boolean RegisterServer(String name) throws RemoteException;
	
	//Gets the rmi urls of all the servers registered with the RegistryServer.
	// First entry is the master
	String[] getFileServers() throws RemoteException;
}
