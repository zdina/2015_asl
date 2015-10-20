package asl.middleware;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
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

	// 4321
	public Server(int port) throws Exception {
		requestQueue = new LinkedList<RequestWrapper>();
		responseQueue = new LinkedList<RequestWrapper>();
		clients = new ConcurrentHashMap<Long, Socket>();
		server = new ServerSocket(port);
		idCount = 0;
		
		startProcessor(new RequestProcessor(this));
		startProcessor(new DatabaseProcessor(this));
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

		new Server(4321);
		// to clean up resources
		// server.close();
	}

}
