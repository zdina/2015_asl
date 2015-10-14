package asl.client;

import asl.Util;

public class ResponseHandler {
	
	private Client c;
	
	public ResponseHandler(Client c) {
		this.c = c;
	}
	
	public void processResponse(String response) {
		String[] responseParts = response.split(" ");
		int responseCode = Integer.parseInt(responseParts[0]);
		
		switch (responseCode) {
		case Util.REGISTER_RESPONSE_CODE:
			handleRegisterResponse(responseParts);
			break;
		case Util.SEND_RESPONSE_CODE:
			break;
		case Util.CREATE_QUEUE_RESPONSE_CODE:
			handleCreateQueueResponse(responseParts);
			break;
		case Util.REMOVE_QUEUE_RESPONSE_CODE:
			handleRemoveQueueResponse(responseParts);
			break;
		case Util.WRONG_QUEUE_ID_ERROR:
			handleRemoveQueueResponse(responseParts);
			break;
		case Util.WRONG_SENDER_ID_ERROR:
			handleWrongSenderIdError();
			break;
		case Util.WRONG_RECEIVER_ID_ERROR:
			handleWrongReceiverId(responseParts);
			break;
		case Util.SQL_ERROR:
			handleSQLError();
			break;
		default:
			System.out.println(responseCode);
			handleUnknownResponseCode();
			break;
		}
		
		if (responseCode != Util.WRONG_SENDER_ID_ERROR)
			c.nextRequest();
	}
	
	private void handleWrongReceiverId(String[] responseParts) {
		long id = Long.parseLong(responseParts[1]);
		// do something with wrong receiver id
	}

	private void handleSQLError() {
		// TODO Auto-generated method stub
		
	}

	private void handleUnknownResponseCode() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * Register response:
	 * code _ id
	 */
	private void handleRegisterResponse(String[] responseParts) {
		long id = Long.parseLong(responseParts[1]);
		c.setId(id);
	}
	
	private void handleCreateQueueResponse(String[] responseParts) {
		long id = Long.parseLong(responseParts[1]);
		c.addQueueId(id);
	}
	
	private void handleRemoveQueueResponse(String[] responseParts) {
		long id = Long.parseLong(responseParts[1]);
		c.removeQueueId(id);
	}
	
	private void handleWrongSenderIdError() {
		c.register();
	}

}
