package asl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ResponseAcceptor implements Runnable {

	private ServerSocket acceptor;
	private ResponseHandler rh;

	public ResponseAcceptor(int port, ResponseHandler rh) throws IOException {
		acceptor = new ServerSocket(port);
		this.rh = rh;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket middleware = acceptor.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						middleware.getInputStream()));
				String line = in.readLine();
				if (line != null) {
					System.out.println("Response received: " + line);
					rh.processResponse(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
