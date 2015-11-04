package asl.middleware;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import asl.Util;

public class ResponseProcessor implements Runnable {

	private Server middleware;
	private long startTime;
	private long startTimeNano;

	public ResponseProcessor(Server middleware) {
		this.middleware = middleware;
		startTime = System.currentTimeMillis();
		startTimeNano = System.nanoTime();
	}

	@Override
	public void run() {
		while (true) {
			RequestWrapper cp = middleware.removeFromResponseQueue();
			if (cp != null) {
				Socket client = middleware.getSocket(cp.getInternalClientId());
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

//					long dbClientId = middleware.getClientDbId(cp
//							.getInternalClientId());
					long responseSent = System.nanoTime();

					Util.serverLogger
							.info("{},{},{},{},{},{},{}",
									middleware.getClientDbId(cp.getInternalClientId()),
									cp.getRequest().split(" ")[0],
									cp.getResponse().split(" ")[0],
									(int) ((cp.getTimeArrival() - startTimeNano) / 10000),
									(int) ((cp.getTimeDbStart() - startTimeNano) / 10000),
									(int) ((cp.getTimeDbReceived() - startTimeNano) / 10000),
									(int) ((responseSent - startTimeNano) / 10000));
				} catch (IOException e) {
					Util.serverErrorLogger.error(cp.getRequest());
					Util.serverErrorLogger.error(cp.getResponse());
					Util.serverErrorLogger.catching(e);
					e.printStackTrace();
				}
			}
		}
	}
}
