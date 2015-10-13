package asl.middleware.request;
import java.io.IOException;
import java.net.ServerSocket;

import asl.middleware.Processor;
import asl.middleware.Server;

public class RequestProcessor extends Processor {

	private ServerSocket server;

	public RequestProcessor(int port, Server middleware) throws Exception {
		super(middleware);
		server = new ServerSocket(port);
		System.out.println("RequestProcessor started.");
	}

	public void run() {

		while (running) {
			RequestWorker cw;
			try {
				cw = new RequestWorker(server.accept(), middleware);
				executor.execute(cw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
