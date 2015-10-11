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
	private BufferedReader in;

	private int messageLength;

	// "Dinas-MacBook-Air.local"
	// 4321
	public Client(String socketName, int socketPort, int messageLength) throws InterruptedException, IOException {
		try {
			socket = new Socket(socketName, socketPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.messageLength = messageLength;
		register();
		socket.close();
	}

	private void register() throws IOException {
		out.println(Util.REGISTER_REQUEST_CODE + " " + name);
//		System.out.println(in.readLine());
		// try {
		// this.id = Integer.parseInt(in.readLine());
		// System.out.println(id);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// register();
		// }
	}

	public static void main(String[] args) throws Exception {
		Client c = new Client("Dinas-MacBook-Air.local", 4321, 200);
	}

	private void askForContacts() {
		out.println(Util.CONTACTS_REQUEST_CODE);
		try {
			String contactsLine = in.readLine();
			String[] contactStrings = contactsLine.split(" ");
			contacts = new int[contactStrings.length];
			for (int i = 0; i < contacts.length; i++)
				contacts[i] = Integer.parseInt(contactStrings[i]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(int receiverId) {
		Message message = new Message(id, receiverId, messageLength);
		out.println(message.getSendRequest());
	}

	public String receiveMessage() {
		String line = "";
		try {
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}

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
