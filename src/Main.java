import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

	public static void main(String[] args) {

		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		InetAddress address = null;		
		try {
			address = InetAddress.getByName("192.168.169.1301");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int port = 8090;

		String name = "Mac OS X";
		
		Peer peer = new Peer(localhost, port, name);
		
		peer.startListening();

		peer.join(address, port);
	
//		Message m = new Message(RingProtocol.TEXT, "Ciao");
//		peer2.sendMessage(m, peer2.getSuccessor(), port1);
		
	}

}
