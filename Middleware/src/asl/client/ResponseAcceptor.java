package asl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ResponseAcceptor implements Runnable {

	private ResponseHandler rh;
	private BufferedReader in;

	public ResponseAcceptor(Socket socket, ResponseHandler rh)
			throws IOException {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.rh = rh;
	}

	@Override
	public void run() {
		while (true) {
			try {
				String response = in.readLine();
				System.out.println("Response received: " + response);
				rh.processResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
