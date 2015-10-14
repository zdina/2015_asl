package asl.middleware.request;

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

import asl.middleware.ClientProxy;
import asl.middleware.Server;

class RequestWorker implements Runnable {

	private Socket client;
	private Server rp;

	public RequestWorker(Socket client, Server rp) {
		this.client = client;
		this.rp = rp;
	}

	public void run() {
		// PrintWriter out = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			String line = in.readLine();
			System.out.println("Request worker " + this.toString()
					+ " processing request: " + line);
			String[] splitted = line.split(" ");
			ClientProxy cr = new ClientProxy(client.getInetAddress(),
					Integer.parseInt(splitted[1]), line);
			client.close();
			rp.addToRequestQueue(cr);

			// out = new PrintWriter(new Socket(cr.getAddress(),
			// Integer.parseInt(splitted[size-1])).getOutputStream(), true);
			// out.println("request received");
		} catch (IOException e) {
			System.out.println("Request worker failed: " + this.toString());
			e.printStackTrace();
		}
	}
}