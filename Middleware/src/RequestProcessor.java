import java.io.IOException;
import java.net.ServerSocket;

public class RequestProcessor extends Processor {

	private ServerSocket server;

	public RequestProcessor(int port, Server middleware) throws Exception {
		super(middleware);
		server = new ServerSocket(port);
		System.out.println("RequestProcessor started.");
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

	// public static void main(String[] args) throws Exception {
	// new RequestProcessor(4321);
	// }

}
