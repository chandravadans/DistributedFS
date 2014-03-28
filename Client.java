import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
			ArrayList<String> servers=remoteObj.getFileServers();

			replicas=new ArrayList<String>();

			//Decide master server (for write)
			String masterServerIP=null;
			Integer masterServerPort=null;
			String masterServerName = null;
			Long timestamp=Long.MAX_VALUE;
			for(String s:servers){
				System.out.println("Received "+s);
				String[]parts=s.split("#");
				Long this_ts=Long.parseLong(parts[0].split("_")[1]);
				if(this_ts<timestamp){
					masterServerIP=parts[1];
					masterServerPort=Integer.parseInt(parts[2]);
					masterServerName=parts[0];
					timestamp=this_ts;
				}
			}

			//Populate Replicas (for reads)
			for(String s:servers){
				String[]parts=s.split("#");
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

			System.out.println("\n\n\nAttempting to receive the file back...");
			
			//Now for the receiving part
			//Calculate number of chunks
			long numOfChunks = remoteWriteObj.NumFileChunks(args[0]);
			System.out.println("Number of chunks = " + numOfChunks);

			long numberOfReplicas=(long)replicas.size();
			
			System.out.println("Number of replicas = " + numberOfReplicas);
			
			int currentChunk=1;
			
			//If servers < chunks, each server will read multiple chunks serially
			if(numberOfReplicas<numOfChunks){

				int numPasses;
				if(numOfChunks%numberOfReplicas==0)
					numPasses=(int) (numOfChunks/numberOfReplicas);
				else
					numPasses=(int) (numOfChunks/numberOfReplicas+1);

				for(int i=0;i<numPasses;i++){

					ExecutorService executor = Executors.newFixedThreadPool((int)numberOfReplicas);
					for(int t=0;t<numberOfReplicas;t++){

						int serverNumber=(int) (currentChunk%numberOfReplicas);
						
						String serverAddress=replicas.get(serverNumber);
						System.out.println("Reading chunk number  "+currentChunk+ " from replica: "+serverAddress.split("#")[0]);
						String[]parts=serverAddress.split("#");
						String ip=parts[1];
						int port=Integer.parseInt(parts[2]);
						String name=parts[0];
						String uri="rmi://" + ip + ":" + port + "/"+name;
						Runnable worker=new ClientThread(uri, args[0], currentChunk);
						executor.execute(worker);
						currentChunk++;
					}
					executor.shutdown();
					while(!executor.isTerminated()){

					}
					System.out.println("Pass finished. Next chunk to read: "+currentChunk);
				}
			}
			else{
				//1 thread per chunk
				ExecutorService executor = Executors.newFixedThreadPool((int)numOfChunks);
				for(int i=0;i<numOfChunks;i++){
					int serverNumber=i;
					String serverAddress=replicas.get(serverNumber);
					System.out.println("Reading chunk number  "+currentChunk+ " from replica: "+serverAddress.split("#")[0]);
					String[]parts=serverAddress.split("#");
					String ip=parts[1];
					int port=Integer.parseInt(parts[2]);
					String name=parts[0];
					String uri="rmi://" + ip + ":" + port + "/"+name;
					Runnable worker=new ClientThread(uri, args[0], currentChunk);
					executor.execute(worker);
					currentChunk++;
				}
				executor.shutdown();
				while(!executor.isTerminated()){

				}
				System.out.println("Finished");
			}
			
			//Reconstruct from parts
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("output/"+args[0])));
			
			for(int i=1;i<currentChunk;i++){
				BufferedReader br=new BufferedReader(new FileReader(new File("output/"+args[0]+"_chunks/"+i+".txt")));
				int s;
				bw.flush();
				s=br.read();
				while(s!=-1){
					bw.write(s);
					s=br.read();
				}
				br.close();
				
			}
			bw.close();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		
		
		
		System.out.println("*****Done receiving the file.********");
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

/*
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

	}*/

	private static byte[] makeCopy(int size, byte[] b) {

		byte data[] = new byte[size];
		for(int i=0;i<size;i++) {
			data[i]=b[i];
		}
		return data;
	}
}
