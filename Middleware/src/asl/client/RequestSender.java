package asl.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import asl.RequestCodes;
import asl.Util;

public class RequestSender {

	private OutputStream os;
	private long ownId;
	
	private Client c;

	public RequestSender(Client c, String middlewareIp, int middlewarePort, Socket socket)
			throws Exception {
		this.os = socket.getOutputStream();
		this.c = c;
	}

	public void setId(long id) {
		this.ownId = id;
	}
//
//	public long getId() {
//		return ownId;
//	}



	private void executeRequest(String request) {
		try {
			request = request + " ";
			byte[] requestInBytes = request.getBytes();
			int requestLength = requestInBytes.length;
			byte[] finishedByteRequest = new byte[requestLength + 1];
			System.arraycopy(requestInBytes, 0, finishedByteRequest, 0,
					requestLength);
			finishedByteRequest[finishedByteRequest.length - 1] = 0;

			os.write(finishedByteRequest);
			long nanoTime = System.nanoTime();
			int requestCode = Integer.parseInt(request.split(" ")[0]);
			Request r = new Request(request, nanoTime, requestCode);
			c.setRequest(r);
//			System.out.println("Request sent: " + request);

		} catch (IOException e) {
			Util.clientErrorLogger.catching(e);
		}
	}

	public void register() {
		String request = RequestCodes.REGISTER + "";
		if (ownId != 0)
			request += " " + ownId;
		executeRequest(request);
	}

	public void createQueue() {
		String request = RequestCodes.CREATE_QUEUE + "";
		executeRequest(request);
	}

	public void removeQueue(long queueId) {
		String request = RequestCodes.REMOVE_QUEUE + " " + queueId;
		executeRequest(request);
	}

	/*
	 * Sends message to a queue indicating a receiver (no need for explicit
	 * receiver)
	 */
	public void sendMessage(long receiverId, String content, long queueId) {
		String request = RequestCodes.SEND_MESSAGE + " " + ownId + " "
				+ receiverId + " " + queueId + " " + content;
		executeRequest(request);
	}

	public void broadcast(String content, long queueId) {
		String request = RequestCodes.SEND_MESSAGE + " " + ownId + " "
				+ Util.NO_RECEIVER_CODE + " " + queueId + " " + content;
		executeRequest(request);
	}

	public void peekQueue(long queueId) {
		String request = RequestCodes.PEEK_QUEUE + " " + ownId + " " + queueId;
		executeRequest(request);
	}

	public void popQueue(long queueId) {
		String request = RequestCodes.POP_QUEUE + " " + ownId + " " + queueId;
		executeRequest(request);
	}

	public void queryFromSenderPop(long senderId) {
		String request = RequestCodes.POP_BY_SENDER + " " + ownId + " "
				+ senderId;
		executeRequest(request);
	}
	
	public void queryFromSenderPeek(long senderId) {
		String request = RequestCodes.PEEK_BY_SENDER + " " + ownId + " "
				+ senderId;
		executeRequest(request);
	}

	public void queryForQueuesWithMessages() {
		String request = RequestCodes.QUERY_FOR_QUEUES + " " + ownId;
		executeRequest(request);
	}

}
