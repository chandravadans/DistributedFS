////package FileServer;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RegistryImpl extends UnicastRemoteObject implements RegistryInterface {

	protected RegistryImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean RegisterServer(String name) throws RemoteException{

		if(RegistryServer.registry==null){

			RegistryServer.init();
			RegistryServer.registry.add(name);
			System.out.println("REGSVR: Master Server Registered: "+name);
			return true;
		}
		else{

			//TODO Check for duplicates
			RegistryServer.registry.add(name);
			System.out.println("REGSVR: Registered "+name);
			return false;
		}
	}

	@Override
	public ArrayList<String> getFileServers() throws RemoteException{

		//System.out.println("In getFileServers, nunmber of servers is "+RegistryServer.registry.size());
		return RegistryServer.registry; 
	}
}
