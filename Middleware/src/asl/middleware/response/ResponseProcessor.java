package asl.middleware.response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import asl.Util;
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
					OutputStream os = client.getOutputStream();
					String response = cp.getResponse() + " ";
					byte[] responseInBytes = response.getBytes();
					int responseLength = responseInBytes.length;
					byte[] finishedByteResponse = new byte[responseLength + 1];
					System.arraycopy(responseInBytes, 0, finishedByteResponse,
							0, responseLength);
					finishedByteResponse[finishedByteResponse.length - 1] = 0;

					os.write(finishedByteResponse);

					long responseSent = System.nanoTime();
					Util.serverLogger.info("{},{},{},{},{},{},{},{}", System
							.nanoTime(), cp.getClientId(), cp.getRequest()
							.split(" ")[0], cp.getResponse().split(" ")[0],
							cp.getTimeDbStart() - cp.getTimeArrival(),
							cp.getTimeDbReceived() - cp.getTimeDbStart(),
							responseSent - cp.getTimeDbReceived(), responseSent
									- cp.getTimeArrival());
				} catch (IOException e) {
					Util.serverErrorLogger.catching(e);
					e.printStackTrace();
				}
			}
		}
	}

}
