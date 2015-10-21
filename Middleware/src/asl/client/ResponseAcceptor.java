package asl.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import asl.Util;

public class ResponseAcceptor implements Runnable {

	private ResponseHandler rh;
	private RequestSender rs;
	private InputStream in;
	private boolean running;

	public ResponseAcceptor(RequestSender rs, Socket socket, ResponseHandler rh)
			throws IOException {
		in = socket.getInputStream();
		this.rh = rh;
		this.rs = rs;
		running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				int availableBytes = in.available();
				if (availableBytes > 0) {
					boolean lastByteFound = false;
					StringBuffer sb = new StringBuffer();
					while (!lastByteFound) {
						byte[] requestBytes = new byte[availableBytes];
						in.read(requestBytes);
						if (requestBytes[availableBytes - 1] == 0)
							lastByteFound = true;
						else
							availableBytes = in.available();
						sb.append(new String(requestBytes));
					}
					String response = sb.toString();
					long timePassed = System.nanoTime() - rs.getNanoTime();
					Util.clientLogger.info("{},{},{},{},{}", System.nanoTime(),
							rs.getId(), rs.getRequestcode(),
							response.split(" ")[0], timePassed);
					System.out.println("Response received: " + response);
					rh.processResponse(response);
				}
			} catch (Exception e) {
				Util.clientErrorLogger.catching(e);
			}
		}
	}

	public void terminate() {
		running = false;
	}

}
