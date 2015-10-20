package asl.middleware;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import asl.middleware.database.DatabaseProcessor;
import asl.middleware.request.RequestProcessor;
import asl.middleware.response.ResponseProcessor;

public class Server {

	private static Queue<RequestWrapper> requestQueue;
	private static Queue<RequestWrapper> responseQueue;
	private static Map<Long, Socket> clients;
	private long idCount;

	private ServerSocket server;

	public Server(int port, String database, String user, int numConnections,
			int numThreads) throws Exception {
		requestQueue = new LinkedList<RequestWrapper>();
		responseQueue = new LinkedList<RequestWrapper>();
		clients = new ConcurrentHashMap<Long, Socket>();
		server = new ServerSocket(port);
		idCount = 0;

		startProcessor(new RequestProcessor(this));
		startProcessor(new DatabaseProcessor(this, database, user, numConnections, numThreads));
		startProcessor(new ResponseProcessor(this));
		System.out.println("Server started.");
		registerClients();
	}

	public void registerClients() throws Exception {
		while (true) {
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

	public synchronized void addToRequestQueue(RequestWrapper cr) {
		requestQueue.add(cr);
	}

	public synchronized RequestWrapper removeFromRequestQueue() {
		return requestQueue.poll();
	}

	public synchronized void addToResponseQueue(RequestWrapper cr) {
		responseQueue.add(cr);
	}

	public synchronized RequestWrapper removeFromResponseQueue() {
		return responseQueue.poll();
	}

	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		InputStream input = new FileInputStream("config.properties");
		prop.load(input);
		int serverport = Integer.parseInt(prop.getProperty("serverport"));
		String database = prop.getProperty("database");
		String user = prop.getProperty("user");
		int numConnections = Integer.parseInt(prop
				.getProperty("numConnections"));
		int numThreads = Integer.parseInt(prop.getProperty("numThreads"));
		input.close();

		new Server(serverport, database, user, numConnections, numThreads);
		// to clean up resources
		// server.close();
	}

}
