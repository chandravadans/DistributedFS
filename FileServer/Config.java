////package FileServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Config {
	
	public static String RegistryServerIP;
	public static Integer RegistryServerPort;
	public static void main(String[] args) throws UnknownHostException {
		System.out.println(InetAddress.getLocalHost());
	}

}
