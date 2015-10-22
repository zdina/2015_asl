package asl.middleware;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import asl.Util;
import asl.middleware.database.DatabaseProcessor;
import asl.middleware.request.RequestProcessor;
import asl.middleware.response.ResponseProcessor;

public class Server {

	private static ConcurrentLinkedQueue<RequestWrapper> requestQueue;
	private static ConcurrentLinkedQueue<RequestWrapper> responseQueue;
	private static Map<Long, Socket> clients;
	private long idCount;
	private int idRangeEnd;

	private ServerSocket server;

	public Server(int port, String database, String user, int numConnections,
			int numThreads, int idRangeStart, int idRangeEnd) throws Exception {
		requestQueue = new ConcurrentLinkedQueue<RequestWrapper>();
		responseQueue = new ConcurrentLinkedQueue<RequestWrapper>();
		clients = new ConcurrentHashMap<Long, Socket>();
		server = new ServerSocket(port);
		idCount = idRangeStart;
		this.idRangeEnd = idRangeEnd;

		startProcessor(new RequestProcessor(this));
		startProcessor(new DatabaseProcessor(this, database, user,
				numConnections, numThreads));
		startProcessor(new ResponseProcessor(this));
		System.out.println("Server started.");
		registerClients();
	}

	public void registerClients() throws Exception {
		while (idCount <= idRangeEnd) {
			clients.put(idCount, server.accept());
			idCount++;
		}
	}

	public Map<Long, Socket> getClients() {
		return clients;
	}

	public Socket getSocket(long clientId) {
		return clients.get(clientId);
	}

	private void startProcessor(Runnable p) throws Exception {
		Thread t = new Thread(p);
		t.start();
	}

	public void addToRequestQueue(RequestWrapper cr) {
		requestQueue.add(cr);
	}

	public RequestWrapper removeFromRequestQueue() {
		return requestQueue.poll();
	}

	public void addToResponseQueue(RequestWrapper cr) {
		responseQueue.add(cr);
	}

	public RequestWrapper removeFromResponseQueue() {
		return responseQueue.poll();
	}

	public static void main(String[] args) {
		try {
			int serverNumber = Integer.parseInt(args[0]);
			Properties prop = new Properties();
			InputStream input = new FileInputStream("config.properties");
			prop.load(input);
			int serverport = Integer.parseInt(prop.getProperty("serverport"
					+ serverNumber));
			String database = prop.getProperty("database");
			String user = prop.getProperty("user");
			int numConnections = Integer.parseInt(prop
					.getProperty("numConnections"));
			int numThreads = Integer.parseInt(prop.getProperty("numThreads"));
			int numClients = Integer.parseInt(prop.getProperty("numClients"));
			int idRangeStart = 0;
			int idRangeEnd = 0;
			if (serverNumber == 1) {
				idRangeStart = 1;
				idRangeEnd = numClients / 2;
			} else {
				idRangeStart = numClients / 2 + 1;
				idRangeEnd = numClients;
			}
			input.close();

			new Server(serverport, database, user, numConnections, numThreads,
					idRangeStart, idRangeEnd);
			// to clean up resources
			// server.close();
		} catch (Exception e) {
			Util.serverErrorLogger.catching(e);
		}
	}

}
