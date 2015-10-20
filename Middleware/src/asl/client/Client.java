package asl.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

public class Client implements Runnable {

	private ArrayList<Long> queues;

	private RequestSender rs;
	private ResponseHandler rh;
	private Socket socket;

	private int numClients;
	private int messageLength;

	// "Dinas-MacBook-Air.local"
	// 4321
	public Client(String middlewareIp, int middlewarePort, int messageLength,
			int numClients) throws Exception {
		this.messageLength = messageLength;
		this.numClients = numClients;
		
		queues = new ArrayList<Long>();

		this.socket = new Socket(middlewareIp, middlewarePort);
		rs = new RequestSender(middlewareIp, middlewarePort, socket);
		rh = new ResponseHandler(this);

		ResponseAcceptor ra = new ResponseAcceptor(socket, rh);
		Thread t = new Thread(ra);
		t.start();
		System.out.println("Client " + this.toString() + " started.");
	}

	public void run() {
		try {
			rs.register();
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

	public void nextRequest() throws IOException {
		// Thread.sleep(2000);

		if (queues.isEmpty())
			rs.createQueue(); // or check for queues that hold messages!
		else if (Math.random() < 0.2)
			rs.queryFromSender(1);
		else if (Math.random() < 0.4) {
			long randomQueue = queues
					.get((int) (Math.random() * queues.size()));
			rs.removeQueue(randomQueue);
		} else if (Math.random() < 0.7) {
			long randomQueue = queues
					.get((int) (Math.random() * queues.size()));
			int receiverId = (int) (Math.random() * numClients + 1); // assuming
																		// that
																		// the
																		// Ids
																		// start
																		// with
																		// 1!!
			rs.sendMessage(receiverId, generateContent(), randomQueue);
		} else {
			rs.createQueue();
			// rs.queryForQueuesWithMessages();
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
		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		prop.load(input);
		String serverhost = prop.getProperty("serverhost");
		int serverport = Integer.parseInt(prop.getProperty("serverport"));
		int messageLength = Integer.parseInt(prop.getProperty("messageLength"));
		int numClients = Integer.parseInt(prop.getProperty("numClients"));
		input.close();
		
		// "dryad02.ethz.ch" //12341
		new Thread(
				new Client(serverhost, serverport, messageLength, numClients))
				.start();
		// new Thread(new Client("10.0.1.70", 4321, 200, 3)).start();
		// new Thread(new Client("10.0.1.70", 4321, 200, 3)).start();
	}

}
