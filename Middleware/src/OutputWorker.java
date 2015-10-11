import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class OutputWorker implements Runnable {
	
	private Socket client;
	
	public OutputWorker(Socket client) {
		this.client = client;
	}
	
	public void run() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
