package asl.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import asl.Util;

public class Client implements Runnable {

	private Set<Long> ownQueues;
	private Set<Long> queuesWithMessages;

	private RequestSender rs;
	private ResponseHandler rh;
	private ResponseAcceptor ra;
	private Socket socket;

	private int numClients;
	private int messageLength;
	private int machineClientNumber;
	private int workload;
	
	private long id;

	private Request r;

	public Client(String middlewareIp, int middlewarePort, int messageLength,
			int numClients, int machineClientNumber, int workload)
			throws Exception {

		this.messageLength = messageLength;
		this.numClients = numClients;
		this.machineClientNumber = machineClientNumber;
		this.workload = workload;
		ownQueues = new HashSet<Long>();
		queuesWithMessages = new HashSet<Long>();

		this.socket = new Socket(middlewareIp, middlewarePort);
		rs = new RequestSender(this, middlewareIp, middlewarePort, socket);
		rh = new ResponseHandler(this);

		ra = new ResponseAcceptor(this, socket, rh);
		Thread t = new Thread(ra);
		t.start();
		System.out.println("Client " + this.toString() + " started.");
	}

	public void nextRequest() {
		// Thread.sleep(2000);
		switch (workload) {
		case Util.WORKLOAD_RANDOM:
			randomWorkload();
			break;
		case Util.SEND_ONLY:
			sendingOnly();
			break;
		case Util.RECEIVE_ONLY:
			receivingOnly();
			break;
		case Util.SEND_RECEIVE:
			sendReceivePair();
			break;
		case Util.CREATE_QUEUES_ONLY:
			creatingQueuesOnly();
			break;
		}

	}
	
	private void sendReceivePair() {
		if (id % 2 == 0) {
			if (ownQueues.isEmpty())
				rs.createQueue();
			else {
				long partner = id - 1;
				rs.sendMessage(partner, generateContent(), ownQueues.iterator().next());
			}
		}
		else {
			long partner = id + 1;
			rs.queryFromSenderPop(partner);
		}
	}
	
	private void creatingQueuesOnly() {
		rs.createQueue();
	}

	private void sendingOnly() {
		if (ownQueues.isEmpty())
			rs.createQueue();
		else {
			int randomClient = (int) (Math.random() * numClients + 1);
			long queueId = ownQueues.iterator().next();
			rs.sendMessage(randomClient, generateContent(), queueId);
		}
	}

	private void receivingOnly() {
		if (queuesWithMessages.isEmpty())
			rs.queryForQueuesWithMessages();
		else {
			int randomRequest = (int) (Math.random() * 4);
			long randomQueue = 0;
			int randomQueueIndex = (int) (Math.random() * ownQueues.size());
			int i = 0;
			for (long l : ownQueues) {
				if (i == randomQueueIndex)
					randomQueue = l;
				i++;
			}
			int randomClient = (int) (Math.random() * numClients + 1);

			switch (randomRequest) {
			case 0:
				rs.peekQueue(randomQueue);
				break;
			case 1:
				rs.popQueue(randomQueue);
				break;
			case 2:
				rs.queryFromSenderPeek(randomClient);
				break;
			case 3:
				rs.queryFromSenderPop(randomClient);
				break;
			}

		}
	}

	private long getRandomQueueIndex(Set<Long> q) {
		long randomQueue = 0;
		int randomQueueIndex = (int) (Math.random() * q.size());
		int i = 0;
		for (long l : q) {
			if (i == randomQueueIndex) {
				randomQueue = l;
				break;
			}
			i++;
		}
		return randomQueue;
	}

	private void randomWorkload() {
		int randomRequest = (int) (Math.random() * 8) + 1;
		long randomOwnQueue = getRandomQueueIndex(ownQueues);
		long randomQueueWithMessages = getRandomQueueIndex(queuesWithMessages);
//		Set<Long> q = new HashSet<Long>(ownQueues);
//		q.addAll(queuesWithMessages);
//		long randomQueueAll = getRandomQueueIndex(q);
		int randomClient = (int) (Math.random() * numClients + 1);

		switch (randomRequest) {
//		case 0:
//			rs.createQueue();
//			break;
		case 1:
			if (Math.random() < 0.5)
				rs.removeQueue(randomOwnQueue);
			else
				rs.removeQueue(randomQueueWithMessages);
			break;
		case 2:
			rs.createQueue();
			break;
//			if (Math.random() < 0.5)
//				rs.broadcast(generateContent(), randomOwnQueue);
//			else
//				rs.broadcast(generateContent(), randomQueueWithMessages);
//			break;
		case 3:
			if (Math.random() < 0.5)
				rs.sendMessage(randomClient, generateContent(), randomOwnQueue);
			else
				rs.sendMessage(randomClient, generateContent(), randomQueueWithMessages);
			break;
		case 4:
			rs.queryFromSenderPop(randomClient);
			break;
		case 5:
			rs.queryFromSenderPeek(randomClient);
			break;
		case 6:
			rs.peekQueue(randomQueueWithMessages);
			break;
		case 7:
			rs.popQueue(randomQueueWithMessages);
			break;
		case 8:
			rs.queryForQueuesWithMessages();
			break;
		}
	}

	public void run() {
		File f = new File(machineClientNumber + ".txt");
		if (f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				setId(Long.parseLong(br.readLine()));
				br.close();
			} catch (Exception e) {
				Util.clientErrorLogger.catching(e);
			}
		}
		rs.register();
	}

	public void terminate() {
		ra.terminate();
	}

	public void addOwnQueueId(long id) {
		ownQueues.add(id);
	}

	public void addQueueWithMessagesId(long id) {
		queuesWithMessages.add(id);
	}

	public void removeQueueId(long id) {
		ownQueues.remove(id);
		queuesWithMessages.remove(id);
	}

	public int getOwnQueueIdSetSize() {
		return ownQueues.size();
	}

	public int getQueuesWithMessagesSetSize() {
		return queuesWithMessages.size();
	}

	public void resetOwnQueueIdSet() {
		ownQueues.clear();
	}

	public void resetQueuesWithMessagesSet() {
		queuesWithMessages.clear();
	}

	public void setId(long id) {
		this.id = id;
		rs.setId(id);
		ra.setId(id);
	}

	public Request getRequest() {
		return r;
	}

	public void setRequest(Request r) {
		this.r = r;
	}

	public int getMachineClinetNumber() {
		return machineClientNumber;
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
			int serverport = Integer.parseInt(prop.getProperty("serverport"
					+ serverNumber));
			int messageLength = Integer.parseInt(prop
					.getProperty("messageLength"));
			int numClients = Integer.parseInt(prop.getProperty("numClients"));
			int clientsPerMachine = Integer.parseInt(prop
					.getProperty("clientsPerMachine"));
			input.close();
			int experimentTime = Integer.parseInt(prop
					.getProperty("experimentTime"));
			int workload = Integer.parseInt(prop.getProperty("workload"));

			ArrayList<Client> clientThreads = new ArrayList<Client>();
			for (int i = 0; i < clientsPerMachine; i++) {
				System.out.println("Starting client " + i);
				Client c = new Client(serverhost, serverport, messageLength,
						numClients, i, workload);
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
