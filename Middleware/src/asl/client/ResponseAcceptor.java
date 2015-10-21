package asl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.logging.log4j.message.ObjectMessage;

import asl.Util;

public class ResponseAcceptor implements Runnable {

	private ResponseHandler rh;
	private RequestSender rs;
	
	private BufferedReader in;
	

	public ResponseAcceptor(RequestSender rs, Socket socket, ResponseHandler rh)
			throws IOException {
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.rh = rh;
		this.rs = rs;
	}

	@Override
	public void run() {
		while (true) {
			try {
				String response = in.readLine();
				long timePassed = System.nanoTime() - rs.getNanoTime();
				Util.clientLogger.info("{},{},{},{}", rs.getId(), rs.getRequestcode(), response.split(" ")[0], timePassed);
				System.out.println("Response received: " + response);
				rh.processResponse(response);
			} catch (Exception e) {
				Util.clientErrorLogger.catching(e);
			}
		}

	}

}
