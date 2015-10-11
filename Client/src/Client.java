import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Client {

	private int id;
	private int[] contacts;
	private String name = "client1";

	private Socket socket;
	private PrintWriter out;
	
	private String socketName;
	private int port;
	
//	private BufferedReader in;
	
	private final int RESPONSE_PORT = 1234;

	private int messageLength;

	// "Dinas-MacBook-Air.local"
	// 4321
	public Client(String socketName, int port, int messageLength)
			throws InterruptedException, IOException, Exception {
		this.socketName = socketName;
		this.port = port;
//		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.messageLength = messageLength;
		
		ResponseAcceptor ra = new ResponseAcceptor(RESPONSE_PORT);
		Thread t = new Thread(ra);
		t.start();
		
		register();
	}
	
	private void startConnection() throws Exception {
		socket = new Socket(socketName, port);
		out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	private void stopConnection() throws Exception {
		socket.close();
		socket = null;
		out = null;
	}

	private void register() throws Exception {
		startConnection();
		String request = Util.REGISTER_REQUEST_CODE + " " + name + " " + RESPONSE_PORT;
		out.println(request);
		System.out.println("Request sent: " + request);
		// receive confirmation!
		stopConnection();

	}

	public static void main(String[] args) throws Exception {
		Client c = new Client("10.0.1.70", 4321, 200);
	}

	private void askForContacts() {
//		out.println(Util.CONTACTS_REQUEST_CODE);
//		try {
//			String contactsLine = in.readLine();
//			String[] contactStrings = contactsLine.split(" ");
//			contacts = new int[contactStrings.length];
//			for (int i = 0; i < contacts.length; i++)
//				contacts[i] = Integer.parseInt(contactStrings[i]);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void sendMessage(int receiverId) {
		Message message = new Message(id, receiverId, messageLength);
		out.println(message.getSendRequest());
	}

//	public String receiveMessage() {
//		String line = "";
//		try {
//			line = in.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return line;
//	}

	public void createQueue() {
		// send to multiple people?
		// send message to queue instead of person? need to specify the queue?
		// query for queues where messages for them are waiting.. return at most
		// one? or return one per queue? or just queue ids?
		// store a file with client id on the client machine once registered?
	}

	public void removeQueue() {

	}

}
