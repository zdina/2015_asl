import java.util.LinkedList;
import java.util.Queue;

public class Server {

	private static Queue<ClientRequest> requestQueue;
	private int port;
	
	// AnswerQueue

	// 4321
	public Server(int port) throws Exception {
		System.out.println("Server started");
		requestQueue = new LinkedList<ClientRequest>();
		this.port = port;
		startRequestProcessor();
		startDatabaseProcessor();
		
	}
	
	private void startRequestProcessor() throws Exception {
		RequestProcessor rp = new RequestProcessor(port, this);
		Thread t  = new Thread(rp);
		t.start();
	}
	
	private void startDatabaseProcessor() throws Exception {
		DatabaseProcessor db = new DatabaseProcessor(this);
		Thread t = new Thread(db);
		t.start();
	}
	
	public synchronized void addToRequestQueue(ClientRequest cr) {
		requestQueue.add(cr);
	}
	
	public synchronized ClientRequest removeFromRequestQueue() {
		return requestQueue.poll();
	}
	
	
	public synchronized Queue<ClientRequest> getRequestQueue() {
		return requestQueue;
	}

	public static void main(String[] args) throws Exception {

		new Server(4321);
		// to clean up resources
		// server.close();
	}

}
