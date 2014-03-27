import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Client {

	public static ArrayList<String> replicas;
	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Input format: <filename> <Registry_IP> <Registry_Port>");
			return ;
		} 

		try {

			//Locate registry
			Registry registry = LocateRegistry.getRegistry(args[1],Integer.parseInt(args[2]));

			if(registry==null){
				System.out.println("Failed to get registry!");
				System.exit(1);
			}

			if (System.getSecurityManager() == null) { 
				System.setSecurityManager(new RMISecurityManager()); 
			}


			String registryServerURL = "rmi://" + args[1] + ":" + args[2] + "/RegistryServer";

			System.out.println("I will try to invoke the remote method from  " + registryServerURL);

			RegistryInterface remoteObj =
					(RegistryInterface) Naming.lookup(registryServerURL);
			
			
			//Get list of FileServers
			String[] servers=remoteObj.getFileServers();
			
			replicas=new ArrayList<String>();

			//Decide master server (for write)
			String masterServerIP=null;
			Integer masterServerPort=null;
			String masterServerName = null;
			Long timestamp=Long.MAX_VALUE;
			for(String s:servers){
				String[]parts=s.split("*");
				Long this_ts=Long.parseLong(parts[2].split("_")[1]);
				if(this_ts<timestamp){
					masterServerIP=parts[0];
					masterServerPort=Integer.parseInt(parts[1]);
					masterServerName=parts[2];
				}
			}
			
			//Populate Replicas (for reads)
			for(String s:servers){
				String[]parts=s.split("*");
				if(!parts[0].equalsIgnoreCase(masterServerIP))
					replicas.add(s);
			}
			
			

			//Now we have the master server to which we send the writes.
			String masterServerURL = "rmi://" + masterServerIP + ":" + masterServerPort + "/"+masterServerName;
			ReadWriteInterface remoteWriteObj =
					(ReadWriteInterface) Naming.lookup(masterServerURL);
			
			System.out.println("Sending file "+args[0]+ "to master server "+masterServerName);
			xmit(remoteWriteObj, args[0]);
			System.out.println("*** Done! ***");

/*
			System.out.println("Receiving file "+args[0]);
			rcv(remoteObj, args[0]);
			System.out.println("*** Done! ***");*/


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static void xmit(ReadWriteInterface stub, String filename) {

		try {

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
			byte buffer[] = new byte[65536];
			int chunk=1;
			int size, res;
			while((size=bis.read(buffer, 0, 65536)) != -1) {

				System.out.println("chunk #"+chunk);
				if(size<65536) {

					byte toWrite[] = makeCopy(size,buffer);
					res = stub.FileWrite64K(filename, chunk, toWrite);
				} 
				else {
					res = stub.FileWrite64K(filename, chunk, buffer);
				}

				System.out.println("Chunk size = " + size);
				if (res == -1) {
					System.err.println("Error. Chunk size not a multiple of 64k. Didn't write chunk.");
				}
				chunk++;
			}
			System.out.println("File transmitted successfully! :)");
			bis.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private static void rcv(ReadWriteInterface stub, String filename) {

		try {
			long numOfChunks = stub.NumFileChunks(filename);
			System.out.println("Number of chunks = " + numOfChunks);
			new File("output").mkdir();

			if(numOfChunks==0)
				return;

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("output/" + filename)));

			for(int i=1; i<=numOfChunks; i++) {
				byte b[] =stub.FileRead64K(filename, i);
				System.out.println("Number of Bytes: " + b.length  + "  " + i);
				bos.write(b);
			}

			bos.flush();	
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static byte[] makeCopy(int size, byte[] b) {

		byte data[] = new byte[size];
		for(int i=0;i<size;i++) {
			data[i]=b[i];
		}
		return data;
	}
}
