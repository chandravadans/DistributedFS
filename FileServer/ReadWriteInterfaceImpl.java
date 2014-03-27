////package FileServer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ReadWriteInterfaceImpl extends UnicastRemoteObject implements ReadWriteInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadWriteInterfaceImpl() throws RemoteException{}

	public int FileWrite64K(String filename, long offset, byte[] data)
		throws IOException, RemoteException {

			System.out.println("Server writing " + filename + ", chunk " + offset + " having length " + data.length);
			if (offset==1) {
				File dir = new File(filename);
				if(dir.exists())
					System.out.println("Directory already exists");
				else {
					boolean mkdir = dir.mkdirs();
					if(mkdir)
						System.out.println("New directory created");
					else
						System.out.println("Error in making directory");				
				}
			}
			if(data.length < 65536) {
				System.err.println("Data isn't a multiple of 64k.");
				return -1;
			}

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename + "/chunk" + offset));
			bos.write(data);
			bos.flush();
			bos.close();

			return data.length;
		}

	public long NumFileChunks(String filename) throws IOException,
	       RemoteException {

		       File f=new File(filename);
		       System.out.println("Number of child files " + f.list().length);
		       return f.list().length;
	       }

	public byte[] FileRead64K(String filename, long chunkNumber) throws IOException,
	       RemoteException {

		       byte b[] = new byte[65536];
		       FileInputStream fis = new FileInputStream(filename + "/chunk" + chunkNumber);
		       int len = fis.read(b, 0, 65536);
		       fis.close();
		       return b;
	       }
}







