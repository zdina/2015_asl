package asl.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

import asl.Util;

public class ResponseAcceptor implements Runnable {

	private ResponseHandler rh;
	private RequestSender rs;
	private InputStream in;
	private boolean running;
	private long ownId;
	private long startTime;

	public ResponseAcceptor(RequestSender rs, Socket socket, ResponseHandler rh)
			throws IOException {
		in = socket.getInputStream();
		this.rh = rh;
		this.rs = rs;
		running = true;
		startTime = System.currentTimeMillis();
	}

	public void setId(long ownId) {
		this.ownId = ownId;
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
						else {
							while (availableBytes == 0)
								availableBytes = in.available();
						}
						sb.append(new String(requestBytes));
					}
					String response = sb.toString();

					Request r = rs.getRequest();

					long timePassed = System.nanoTime() - r.getTimeSent();
					Util.clientLogger.info("{},{},{},{},{}",
							System.currentTimeMillis() - startTime, ownId,
							r.getRequestCode(), response.split(" ")[0],
							timePassed);
					System.out.println("Response received: " + response);
					rh.processResponse(response);
				}
			} catch (IOException e) {
				Util.clientErrorLogger.error(rs.getRequest());
				Util.clientErrorLogger.catching(e);
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		running = false;
	}

}
