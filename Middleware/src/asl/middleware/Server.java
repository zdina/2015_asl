package asl.middleware;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import asl.Util;
import asl.middleware.database.DatabaseProcessor;

public class Server {

	private static ConcurrentLinkedQueue<RequestWrapper> requestQueue;
	private static ConcurrentLinkedQueue<RequestWrapper> responseQueue;
	private static Map<Long, Socket> clients;
	private static Map<Long, Long> internalClientIdsToDbIds;
	private long idCount;

	private ServerSocket server;

	public Server(int port, String database, String user, String password,
			int numThreads) throws Exception {
		requestQueue = new ConcurrentLinkedQueue<RequestWrapper>();
		responseQueue = new ConcurrentLinkedQueue<RequestWrapper>();
		clients = new ConcurrentHashMap<Long, Socket>();
		internalClientIdsToDbIds = new ConcurrentHashMap<Long, Long>();
		server = new ServerSocket(port);
		idCount = 1;

		startProcessor(new RequestProcessor(this));
		startProcessor(new DatabaseProcessor(this, database, user, password, numThreads));
		startProcessor(new ResponseProcessor(this));
		
		Timer timer = new Timer();
		timer.schedule(new QueueLogger(this), 0, 1000);
		
		System.out.println("Server started.");
		registerClients();
	}

	public void registerClients() throws Exception {
		while (true) {
			clients.put(idCount, server.accept());
			idCount++;
		}
	}
	
	public void setClientDbId(long internalId, long dbId) {
		internalClientIdsToDbIds.put(internalId, dbId);
	}
	
	public long getClientDbId(long internalId) {
		return internalClientIdsToDbIds.get(internalId);
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
	
	public int getRequestQueueSize() {
		return requestQueue.size();
	}

	public int getResponseQueueSize() {
		return responseQueue.size();
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
			String password = prop.getProperty("password");
			int numThreads = Integer.parseInt(prop.getProperty("numThreads"));
			input.close();

			new Server(serverport, database, user, password, numThreads);
			// to clean up resources
			// server.close();
		} catch (Exception e) {
			Util.serverErrorLogger.catching(e);
			e.printStackTrace();
		}
	}

}
