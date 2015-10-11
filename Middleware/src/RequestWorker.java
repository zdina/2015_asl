import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Queue;

class RequestWorker implements Runnable {

	private Socket client;
	private int number;
	private Server rp;

	RequestWorker(Socket client, int number, Server rp) {
		this.client = client;
		this.number = number;
		this.rp = rp;
	}

	public void run() {
		String line;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			line = in.readLine();
			System.out.println("Request worker " + number + " processing request: " + line);
			System.out.println(client.getInetAddress());
			ClientRequest cr = new ClientRequest(client.getInetAddress(), client.getLocalPort(), line);
			rp.addToRequestQueue(cr); 

			String[] splitted = line.split(" ");
			int size = splitted.length;
			
			out = new PrintWriter(new Socket(cr.getAddress(), Integer.parseInt(splitted[size-1])).getOutputStream(), true);
			out.println("request received");
		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}
	}
}