
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer {

    private InetAddress myAddress;
    private int myPort;
    private String myName;

    private ServerSocket serverSocket;
    private Socket socket;

    private InetAddress successor;
    private InetAddress predecessor;

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

                System.out.println("I PROCESS A JOIN");

                this.setSuccessor(source);

                InetAddress address;

                if (this.getSuccessor() == null) {
                    address = this.myAddress;
                } else {
                    address = this.getSuccessor();
                }

                Message msg = new Message(RingProtocol.JOIN_RESPONSE, address);
                sendMessage(msg, source, port);

                break;

            }

            case RingProtocol.JOIN_RESPONSE: {

                System.out.println("I PROCESS A JOIN_RESPONSE");

                this.setPredecessor(source);

                InetAddress joinSuccessor = (InetAddress) message.getContent();

                this.setSuccessor(joinSuccessor);

                Message msg = new Message(RingProtocol.SET_PREDECESSOR, this.myAddress);
                sendMessage(msg, joinSuccessor, port);

                break;

            }

            case RingProtocol.SET_PREDECESSOR: {

                InetAddress predecessor = (InetAddress) message.getContent();
                this.setPredecessor(predecessor);

                break;

            }

            case RingProtocol.TEXT: {

                if(message.getRecipient() == this.myAddress) {
                    String string = (String) message.getContent();
                    System.out.println(string);
                }
                else {
                    sendMessage(message, this.successor, port);
                }

                break;

            }

        }

    }

    public void join(InetAddress address, int port) {
        System.out.println("join");
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
