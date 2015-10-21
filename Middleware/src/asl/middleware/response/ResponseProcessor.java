package asl.middleware.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import asl.Util;
import asl.middleware.Processor;
import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class ResponseProcessor implements Runnable {

	private Server middleware;

	public ResponseProcessor(Server middleware) {
		this.middleware = middleware;
	}

	@Override
	public void run() {
		while (true) {
			RequestWrapper cp = middleware.removeFromResponseQueue();
			if (cp != null) {
				Socket client = middleware.getSocket(cp.getClientId());
				try {
					PrintWriter out = new PrintWriter(client.getOutputStream(),
							true);
					out.println(cp.getResponse());
					long responseSent = System.nanoTime();
					Util.serverLogger.info("{},{},{},{},{},{},{}", cp.getClientId(), cp
							.getRequest().split(" ")[0], cp.getResponse()
							.split(" ")[0],
							cp.getTimeDbStart() - cp.getTimeArrival(),
							cp.getTimeDbReceived() - cp.getTimeDbStart(),
							responseSent - cp.getTimeDbReceived(),
							responseSent - cp.getTimeArrival());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
