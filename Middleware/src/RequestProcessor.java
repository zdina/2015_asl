import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RequestProcessor implements Runnable {

	private ServerSocket server;
	private Server middleware;

	private ThreadPoolExecutor executor;
	private boolean running;

	public RequestProcessor(int port, Server middleware) throws Exception {
		server = new ServerSocket(port);
		this.middleware = middleware;
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		running = true;
	}

	public void run() {

		int count = 1;
		while (running) {
			RequestWorker cw;
			try {
				cw = new RequestWorker(server.accept(), count, middleware);
				executor.execute(cw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
		}

	}

	public void shutdown() {
		running = false;
		executor.shutdown();
	}

	// public static void main(String[] args) throws Exception {
	// new RequestProcessor(4321);
	// }

}
