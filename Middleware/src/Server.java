import java.util.LinkedList;
import java.util.Queue;

public class Server {

	private static Queue<ClientProxy> requestQueue;
	private static Queue<ClientProxy> responseQueue;

	private int port;

	// 4321
	public Server(int port) throws Exception {
		System.out.println("Server started.");
		requestQueue = new LinkedList<ClientProxy>();
		responseQueue = new LinkedList<ClientProxy>();
		this.port = port;
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
