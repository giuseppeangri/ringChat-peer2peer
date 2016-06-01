import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainP2 {

	public static void main(String[] args) {

		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int port1 = 8090;

		String name2 = "P2";
		Peer peer2 = new Peer(localhost, port1, name2);
		peer2.startListening();
		
		InetAddress gigi = null;		
		try {
			gigi = InetAddress.getByName("172.19.35.146");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		peer2.join(gigi, port1);
	
//		Message m = new Message(RingProtocol.TEXT, "Ciao");
//		peer2.sendMessage(m, peer2.getSuccessor(), port1);
		
	}

}
