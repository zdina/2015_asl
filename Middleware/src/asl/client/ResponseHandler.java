package asl.client;

import asl.Util;

public class ResponseHandler {
	
	private Client c;
	
	public ResponseHandler(Client c) {
		this.c = c;
	}
	
	public void processResponse(String response) {
		String[] responseParts = response.split(" ");
		int requestCode = Integer.parseInt(responseParts[0]);
		
		switch (requestCode) {
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
		default:
			// unclear message
			break;
		}
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

}
