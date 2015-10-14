package asl.middleware;
import java.util.LinkedList;
import java.util.Queue;

import asl.middleware.database.DatabaseProcessor;
import asl.middleware.request.RequestProcessor;
import asl.middleware.response.ResponseProcessor;

public class Server {

	private static Queue<ClientProxy> requestQueue;
	private static Queue<ClientProxy> responseQueue;

	// 4321
	public Server(int port) throws Exception {
		System.out.println("Server started.");
		requestQueue = new LinkedList<ClientProxy>();
		responseQueue = new LinkedList<ClientProxy>();
		
		startProcessor(new RequestProcessor(port, this));
		startProcessor(new DatabaseProcessor(this));
		startProcessor(new ResponseProcessor(this));
	}

	private void startProcessor(Processor p) throws Exception {
		Thread t = new Thread(p);
		t.start();
	}

	public synchronized void addToRequestQueue(ClientProxy cr) {
		requestQueue.add(cr);
	}

	public synchronized ClientProxy removeFromRequestQueue() {
		return requestQueue.poll();
	}

	public synchronized void addToResponseQueue(ClientProxy cr) {
		responseQueue.add(cr);
	}

	public synchronized ClientProxy removeFromResponseQueue() {
		return responseQueue.poll();
	}

	public static void main(String[] args) throws Exception {

		new Server(4321);
		// to clean up resources
		// server.close();
	}

}
