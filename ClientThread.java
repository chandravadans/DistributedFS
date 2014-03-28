import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.Naming;


public class ClientThread implements Runnable{

	String fname;
	Integer offset;
	String serverAddress;

	public ClientThread(String serverAddress,String fname, int offset){
		this.serverAddress=serverAddress;
		this.fname=fname;
		this.offset=offset;
	}
	@Override
	public void run() {


		try {

			//Get remote object
			ReadWriteInterface stub =
					(ReadWriteInterface) Naming.lookup(serverAddress);

			//Open the output file
			
			new File("output/"+fname+"_chunks").mkdirs();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("output/"+fname+"_chunks/"+ offset+".txt")));
			
			byte b[] =stub.FileRead64K(fname, offset);
			System.out.println("Number of Bytes: " + b.length  + "  " + offset);
			bos.write(b);
			bos.flush();	
			bos.close();
			System.out.println(fname+"_"+offset+" Delivered");
		} 
		catch (Exception e) {

			/*e.printStackTrace();
			return;*/
		}
	}
}
