////package FileServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class FileServer {

	public static boolean isMaster;
	public static String name;
	static String registryIP;
	static Integer registryPort;

	public FileServer() {}

	public static void main(String args[]) {

		
		//Arguments: <FileServerIP> <FileServerPort>
		if(System.getSecurityManager()==null){

			System.setSecurityManager(new RMISecurityManager());
		}

		try {
			
			BufferedReader in=new BufferedReader(new FileReader(new File("config.txt")));
			registryIP=in.readLine();
			registryPort=Integer.parseInt(in.readLine());
			in.close();

			System.out.println("Registry server located at "+registryIP+":"+registryPort);
			//Register with Registry Server
			registerWithRegistryServer(args[0],args[1]);

			//Register with local registry
			ReadWriteInterfaceImpl stub=new ReadWriteInterfaceImpl();
			Naming.rebind("rmi://"+args[0]+":"+args[1]+"/"+name,stub);
			System.out.println("File Server with name "+name+" up and running!");

		} catch (Exception e) {
			System.err.println("File Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	static void registerWithRegistryServer(String ip, String port) throws RemoteException, MalformedURLException, NotBoundException{

		//The IP and port refer to the FileServer's IP and port. Registry Server's IP and port are stored
		// in Config.java
		Registry registry = LocateRegistry.getRegistry(registryIP,registryPort);

		if(registry==null){
			System.out.println("Failed to get RegistryServer!");
			return;
		}

		if (System.getSecurityManager() == null) { 
			System.setSecurityManager(new RMISecurityManager()); 
		}

		String registryServerURL = "rmi://" + registryIP + ":" + registryPort + "/RegistryServer";

		System.out.println("I will try to invoke RegistryServer from  " + registryServerURL);

		RegistryInterface remoteObj =
				(RegistryInterface) Naming.lookup(registryServerURL);

		name="Server_"+System.currentTimeMillis();

		//Register with name*ip*port
		if(remoteObj.RegisterServer(name+"#"+ip+"#"+port))
			isMaster=true;
		else
			isMaster=false;
		//Actually, you can split and choose the one with least name as the master.Problem solved! :P
	}
}