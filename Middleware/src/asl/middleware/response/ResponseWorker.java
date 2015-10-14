package asl.middleware.response;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import asl.middleware.ClientProxy;

public class ResponseWorker implements Runnable {

	private ClientProxy cp;

	public ResponseWorker(ClientProxy cp) {
		this.cp = cp;
	}

	public void run() {
		try {
			Socket client = new Socket(cp.getAddress(), cp.getPort());
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			out.println(cp.getResponse());
			client.close();
		} catch (IOException e) {
			System.out.println("Response worker failed: " + cp.getRequest());
			e.printStackTrace();
		}
	}

}
