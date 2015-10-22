package asl.middleware.request;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import asl.Util;
import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class RequestProcessor implements Runnable {

	private Server middleware;
	private Map<Long, Socket> clients;

	public RequestProcessor(Server middleware) throws Exception {
		this.middleware = middleware;
		this.clients = middleware.getClients();
	}

	public void run() {
		while (true) {
			for (long clientId : clients.keySet()) {
				Socket client = clients.get(clientId);
				try {
					InputStream is = client.getInputStream();
					int availableBytes = is.available();
					if (availableBytes > 0) {
						boolean lastByteFound = false;
						StringBuffer sb = new StringBuffer();
						while (!lastByteFound) {
							byte[] requestBytes = new byte[availableBytes];
							is.read(requestBytes);
							if (requestBytes[availableBytes - 1] == 0)
								lastByteFound = true;
							else
								availableBytes = is.available();
							sb.append(new String(requestBytes));
						}
						String request = sb.toString();
						System.out.println("Request received: " + request);
						middleware.addToRequestQueue(new RequestWrapper(
								clientId, request, System.nanoTime()));
					}
				} catch (IOException e) {
					Util.serverErrorLogger.catching(e);
					e.printStackTrace();
				}
			}
		}
	}

}
