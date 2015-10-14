package asl.client;

import java.util.ArrayList;
import java.util.Random;

public class Client implements Runnable {

	private ArrayList<Long> queues;

	private RequestSender rs;

	private int numClients;

	// private BufferedReader in;

	private int messageLength;

	// "Dinas-MacBook-Air.local"
	// 4321
	public Client(String middlewareIp, int middlewarePort, int ownPort,
			int messageLength, int numClients) throws Exception {
		queues = new ArrayList<Long>();
		rs = new RequestSender(middlewareIp, middlewarePort, ownPort);
		// in = new BufferedReader(new
		// InputStreamReader(socket.getInputStream()));
		this.messageLength = messageLength;
		this.numClients = numClients;

		ResponseAcceptor ra = new ResponseAcceptor(ownPort,
				new ResponseHandler(this));
		Thread t = new Thread(ra);
		t.start();
		System.out.println("Client " + this.toString() + " started.");
	}

	public void run() {
		try {
			rs.register();

			// rs.createQueue();
			// rs.removeQueue(queues.get(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addQueueId(long id) {
		if (!queues.contains(id))
			queues.add(id);
	}

	public void removeQueueId(long id) {
		queues.remove(id);
	}

	public void setId(long id) {
		rs.setId(id);
	}

	public void register() {
		rs.register();
	}

	public void nextRequest() {
		// here should be a random request chosen to be generated
		// Thread.sleep(2000);
		// makes the requests sequential and the server ALWAYS has to reply!
		// could send message to himself!
		
		if (queues.isEmpty())
			rs.createQueue(); // or check for queues that hold messages!
		else if (Math.random() < 0.7) {
			long randomQueue = queues.get((int) (Math.random() * queues.size()));
			rs.peekQueue(randomQueue);
		}
		else if (Math.random() < 0.7) {
			long randomQueue = queues.get((int) (Math.random() * queues.size()));
			int receiverId = (int) (Math.random() * numClients + 1); // assuming that the Ids start with 1!!
			rs.sendMessage(receiverId, generateContent(), randomQueue);
		}
		else {
			rs.queryForQueuesWithMessages();
		}
	}

	private String generateContent() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(messageLength);
		for (int i = 0; i < messageLength; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		new Thread(new Client("10.0.1.70", 4321, 1234, 200, 3)).start();
//		new Thread(new Client("10.0.1.70", 4321, 2345, 200, 3)).start();
//		new Thread(new Client("10.0.1.70", 4321, 3456, 200, 3)).start();
	}

	// public void sendMessage(int receiverId) {
	// Message message = new Message(id, receiverId, messageLength);
	// out.println(message.getSendRequest());
	// }

	// public String receiveMessage() {
	// String line = "";
	// try {
	// line = in.readLine();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return line;
	// }

}
