
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

public class Peer {

    private InetAddress myAddress;
    private int myPort;
    private String myName;

    private ServerSocket serverSocket;
    private Socket socket;

    private InetAddress successor;
    private InetAddress predecessor;
    
    private Map<InetAddress, String> peers = new HashMap<InetAddress, String>();

    public Peer(InetAddress myAddress, int myPort, String myName) {
        super();
        this.myAddress = myAddress;
        this.myPort = myPort;
        this.myName = myName;
    }

    public InetAddress getMyAddress() {
        return myAddress;
    }

    public int getMyPort() {
        return myPort;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public InetAddress getSuccessor() {
        return successor;
    }

    public void setSuccessor(InetAddress successor) {
        this.successor = successor;
    }

    public InetAddress getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(InetAddress predecessor) {
        this.predecessor = predecessor;
    }
    
	public Map<InetAddress, String> getPeers() {
		return peers;
	}

	public void setPeers(Map<InetAddress, String> peers) {
		this.peers = peers;
	}

	public void startListening() {

        try {
            this.serverSocket = new ServerSocket(this.myPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(this.getMyName() + " - START LISTENING on port " + this.myPort);

        new Thread(new Runnable() {
            public void run() {
                System.out.println("run");
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        InetSocketAddress remote = (InetSocketAddress) socket.getRemoteSocketAddress();
                        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
                        Message message = (Message) inStream.readObject();
                        processMessage(message, remote.getAddress(), socket.getLocalPort());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }

    private void processMessage(Message message, InetAddress source, int port) {

        // System.out.println(source.getHostAddress());
        // System.out.println(port);

        switch (message.getType()) {

            case RingProtocol.JOIN: {

                System.out.println("I PROCESS A JOIN FROM " +source.getHostAddress() +":" +port);

                InetAddress address;

                if (this.getSuccessor() == null) {
                    address = this.myAddress;
                } else {
                    address = this.getSuccessor();
                }
                
                this.setSuccessor(source);

                Message msg = new Message(RingProtocol.JOIN_RESPONSE, address);
                sendMessage(msg, source, port);

                break;

            }

            case RingProtocol.JOIN_RESPONSE: {

                System.out.println("I PROCESS A JOIN_RESPONSE FROM " +source.getHostAddress() +":" +port);

                this.setPredecessor(source);

                InetAddress joinSuccessor = (InetAddress) message.getContent();

                this.setSuccessor(joinSuccessor);

                Message msg = new Message(RingProtocol.SET_PREDECESSOR, this.myAddress);
                sendMessage(msg, joinSuccessor, port);
                
                SimpleEntry<InetAddress, String> me = new SimpleEntry<InetAddress, String>(this.myAddress, this.myName);    
                msg = new Message(RingProtocol.NEW_PEER, me);
                sendMessage(msg, this.successor, port);

                break;

            }

            case RingProtocol.SET_PREDECESSOR: {
            	
                System.out.println("I PROCESS A SET_PREDECESSOR FROM " +source.getHostAddress() +":" +port);

                InetAddress predecessor = (InetAddress) message.getContent();
                this.setPredecessor(predecessor);

                break;

            }

            case RingProtocol.TEXT: {

                if( message.getRecipient().equals(this.myAddress) ) {
                    System.out.println("I PROCESS A TEXT FROM " +source.getHostAddress() +":" +port);
                    String string = (String) message.getContent();
                    System.out.println(string);
                }
                else {
                    System.out.println("I FORWARD A TEXT FROM " +source.getHostAddress() +":" +port);
                    sendMessage(message, this.successor, port);
                }

                break;

            }
            
            case RingProtocol.NEW_PEER: {
            	
                System.out.println("I PROCESS A NEW_PEER FROM " +source.getHostAddress() +":" +port);
            	
            	SimpleEntry<InetAddress, String> newPeer = (SimpleEntry<InetAddress, String>) message.getContent();
            	
            	// se il nuovo peer non sono io stesso
            	if( !newPeer.getKey().equals(this.myAddress) ) {
            		
                    System.out.println("NEW PEER: " +newPeer.getKey().getHostAddress() +" - " +newPeer.getValue());
            		
            		// aggiungo il nuovo peer alla HashMap
            		this.peers.put(newPeer.getKey(), newPeer.getValue());
            		
            		// rispondo al nuovo peer inviando i miei dati
                    SimpleEntry<InetAddress, String> me = new SimpleEntry<InetAddress, String>(this.myAddress, this.myName);    
            		Message msg = new Message(RingProtocol.NEW_PEER_RESPONSE, me, newPeer.getKey());
            		sendMessage(msg, this.successor, port);
            		
            		// inoltro l'avviso di NEW_PEER al resto dell'anello
            		msg = new Message(RingProtocol.NEW_PEER, newPeer);
            		sendMessage(msg, this.successor, port);
            		
            	}
            	
            	break;
            	
            }
            
            case RingProtocol.NEW_PEER_RESPONSE: {
            	
                System.out.println("I PROCESS A NEW_PEER_RESPONSE FROM " +source.getHostAddress() +":" +port);
            	
            	if( message.getRecipient().equals(this.myAddress) ) {
            		
                	SimpleEntry<InetAddress, String> newPeer = (SimpleEntry<InetAddress, String>) message.getContent();
                	
                    System.out.println("NEW PEER: " +newPeer.getKey().getHostAddress() +" - " +newPeer.getValue());
                	
                	// aggiungo il nuovo peer alla HashMap
            		this.peers.put(newPeer.getKey(), newPeer.getValue());
                	
            	}
            	
            	break;
            	
            }

        }

    }

    public void join(InetAddress address, int port) {
    	Message joinMessage = new Message(RingProtocol.JOIN, "join request");
        sendMessage(joinMessage, address, port);
    }

    public void sendMessage(Message message, InetAddress destAddress, int destPort) {
        Socket socket;
        try {
            socket = new Socket(destAddress, destPort);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(message);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
