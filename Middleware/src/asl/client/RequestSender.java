package asl.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import asl.RequestCodes;
import asl.Util;

public class RequestSender {

	private InetAddress middlewareIp;
	private int middlewarePort;
	private int ownPort;
	private long ownId;

	public RequestSender(String middlewareIp, int middlewarePort, int ownPort)
			throws UnknownHostException {
		this.middlewareIp = InetAddress.getByName(middlewareIp);
		this.middlewarePort = middlewarePort;
		this.ownPort = ownPort;
	}
	
	public void setId(long id) {
		this.ownId = id;
	}

	
	private void executeRequest(String request) {
		// probably wait until response received
		try {
			Socket socket = new Socket(middlewareIp, middlewarePort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(request);
			System.out.println("Request sent: " + request);
			// maybe receive confirmation/acknowledgement!
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void register() {
		String request = RequestCodes.REGISTER + " " + ownPort;
		executeRequest(request);
	}
	
	public void createQueue() {
		String request = RequestCodes.CREATE_QUEUE + " " + ownPort;
		executeRequest(request);
	}

	public void removeQueue(long queueId) {
		String request = RequestCodes.REMOVE_QUEUE + " " + ownPort + " " + queueId;
		executeRequest(request);
	}
	
	/*
	 * Sends message to a queue indicating a receiver (no need for explicit receiver)
	 */
	public void sendMessage(long receiverId, String content, long queueId) {
		String request = RequestCodes.SEND_MESSAGE + " " + ownPort + " " + ownId + " " + receiverId + " " + queueId + " " + content + " ";
		executeRequest(request);
	}
	
	public void peekQueue(long queueId) {
		String request = RequestCodes.PEEK_QUEUE + " " + ownPort + " " + ownId + " " + queueId;
		executeRequest(request);
	}
	
	public void popQueue(long queueId) {
		String request = RequestCodes.POP_QUEUE + " " + ownPort + " " + ownId + " " + queueId;
		executeRequest(request);
	}
	
	public void queryFromSender(long senderId) {
		String request = RequestCodes.POP_BY_SENDER + " " + ownPort + " " + ownId + " " + senderId;
		executeRequest(request);
	}
	
	public void queryForQueuesWithMessages() {
		String request = RequestCodes.QUERY_FOR_QUEUES + " " + ownPort + " " + ownId;
		executeRequest(request);
	}

}
