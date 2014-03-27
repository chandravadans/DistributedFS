////package FileServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;

public class RegistryServer {
	
	public static ArrayList<String> registry;
	
	public static String registryServerIP;
	public static Integer registryServerPort;
	
	
	public static void init(){
		registry=new ArrayList<String>();
	}

	public static void main(String args[]) {

		if(args.length<2){
			System.out.println("Invoked as java RegistryServer <ip_address> <port>");
			System.exit(1);
		}

		if(System.getSecurityManager()==null){

			System.setSecurityManager(new RMISecurityManager());

		}

		try {

			RegistryImpl stub=new RegistryImpl();

			Naming.rebind("rmi://"+args[0]+":"+args[1]+"/RegistryServer",stub);
			
			/*registryServerIP=args[0];
			registryServerPort=Integer.parseInt(args[1]);*/
			BufferedWriter configStore=new BufferedWriter(new FileWriter(new File("config.txt")));
			configStore.append(args[0]+"\n");
			configStore.append(args[1]+"\n");
			configStore.close();
			
			//System.out.println(Config.RegistryServerIP);
			
			System.out.println("Registry Server up and running!");

		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}


}
