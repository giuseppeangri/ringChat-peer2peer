import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainP1 {

	public static void main(String[] args) {

		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int port1 = 8090;
		String name1 = "P1";
		Peer peer1 = new Peer(localhost, port1, name1);
		peer1.startListening();
		
	}

}
