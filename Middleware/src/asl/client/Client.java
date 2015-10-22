package asl.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import asl.Util;

public class Client implements Runnable {

	private ArrayList<Long> queues;

	private RequestSender rs;
	private ResponseHandler rh;
	private ResponseAcceptor ra;
	private Socket socket;

	private int numClients;
	private int messageLength;

	public Client(String middlewareIp, int middlewarePort, int messageLength,
			int numClients) throws Exception {

		this.messageLength = messageLength;
		this.numClients = numClients;
		queues = new ArrayList<Long>();

		this.socket = new Socket(middlewareIp, middlewarePort);
		rs = new RequestSender(middlewareIp, middlewarePort, socket);
		rh = new ResponseHandler(this);

		ra = new ResponseAcceptor(rs, socket, rh);
		Thread t = new Thread(ra);
		t.start();
		System.out.println("Client " + this.toString() + " started.");
	}

	public void run() {
		rs.register();
	}

	public void terminate() {
		ra.terminate();
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
			// assuming that ids start with 1
			int receiverId = (int) (Math.random() * numClients + 1);
			rs.sendMessage(receiverId, generateContent(), randomQueue);
		} else if (Math.random() < 0.5) {
			rs.createQueue();
		} else
			rs.queryForQueuesWithMessages();
	}

	private String generateContent() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(messageLength);
		for (int i = 0; i < messageLength; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

	public static void main(String[] args) {
		try {
			int serverNumber = Integer.parseInt(args[0]);
			Properties prop = new Properties();
			InputStream input = new FileInputStream("config.properties");
			prop.load(input);
			String serverhost = prop.getProperty("serverhost" + serverNumber);
			int serverport = Integer.parseInt(prop.getProperty("serverport" + serverNumber));
			int messageLength = Integer.parseInt(prop
					.getProperty("messageLength"));
			int numClients = Integer.parseInt(prop.getProperty("numClients"));
			int clientsPerMachine = Integer.parseInt(prop.getProperty("clientsPerMachine"));
			input.close();
			int experimentTime = Integer.parseInt(prop.getProperty("experimentTime"));

			ArrayList<Client> clientThreads = new ArrayList<Client>();
			for (int i = 0; i < clientsPerMachine; i++) {
				System.out.println("Starting client " + i);
				Client c = new Client(serverhost, serverport, messageLength,
						numClients);
				Thread t = new Thread(c);
				t.start();
				clientThreads.add(c);
			}
			
			Thread.sleep(experimentTime * 60000);
			for (int i = 0; i < clientsPerMachine; i++) {
				clientThreads.get(i).terminate();
			}

		} catch (Exception e) {
			Util.clientErrorLogger.catching(e);
			e.printStackTrace();
		}
	}

}
