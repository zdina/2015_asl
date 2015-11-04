package asl.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import asl.Util;

import com.sun.corba.se.impl.ior.ByteBuffer;

public class ResponseAcceptor implements Runnable {

	private ResponseHandler rh;
	private Client c;
	private InputStream in;
	private boolean running;
	private long ownId;
	private long startTime;
	private long stopTime;

	public ResponseAcceptor(Client c, Socket socket, ResponseHandler rh)
			throws IOException {
		in = socket.getInputStream();
		this.rh = rh;
		this.c = c;
		running = true;
		startTime = System.currentTimeMillis();
	}

	public void setId(long ownId) {
		this.ownId = ownId;
	}

	public long getStopTime() {
		return stopTime;
	}

	@Override
	public void run() {
		while (running) {
			try {
				ByteBuffer bb = new ByteBuffer();
				byte b = 1;
				while (b != 0) {
					b = (byte) in.read();
					bb.append(b);
				}

				String response = new String(bb.toArray());
				Request r = c.getRequest();
				stopTime = System.nanoTime();
				int timePassed = (int) ((stopTime - r.getTimeSent())/10000);
				Util.clientLogger.info("{},{},{},{},{}",
						System.currentTimeMillis() - startTime, ownId,
						r.getRequestCode(),
						Integer.parseInt(response.split(" ")[0]), timePassed);
				rh.processResponse(response);
			} catch (Exception e) {
				Util.clientErrorLogger.error(c.getRequest().getRequest());
				Util.clientErrorLogger.catching(e);
				e.printStackTrace();
			}
		}
	}

	public void terminate() {
		running = false;
		System.out.println("Client " + ownId + " terminated.");
	}

}
