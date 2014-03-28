//package Registry;

import java.rmi.Remote;
import java.util.ArrayList;

public interface RegistryInterface extends Remote{
	
	//Registers a server with the RegistryServer.
	// True, if caller has been made the master, false if not
	boolean RegisterServer(String name);
	
	//Gets the rmi urls of all the servers registered with the RegistryServer.
	// First entry is the master
	ArrayList<String> getFileServers();
}
