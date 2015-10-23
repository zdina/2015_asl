package asl.client;

import asl.ErrorCodes;
import asl.ResponseCodes;
import asl.Util;

public class ResponseHandler {
	
	private Client c;
	
	public ResponseHandler(Client c) {
		this.c = c;
	}
	
	public void processResponse(String response) {
		try {
		String[] responseParts = response.split(" ");
		int responseCode = Integer.parseInt(responseParts[0]);
		
		switch (responseCode) {
		case ResponseCodes.REGISTER_RESPONSE_CODE:
			handleRegisterResponse(responseParts);
			break;
		case ResponseCodes.SEND_RESPONSE_CODE:
			break;
		case ResponseCodes.CREATE_QUEUE_RESPONSE_CODE:
			handleCreateQueueResponse(responseParts);
			break;
		case ResponseCodes.REMOVE_QUEUE_RESPONSE_CODE:
			handleRemoveQueueResponse(responseParts);
			break;
		case ResponseCodes.QUERY_QUEUES_RESPONSE_CODE:
			handleQueues(responseParts);
			break;
		case ResponseCodes.PEEK_QUEUE_RESPONSE_CODE:
			handlePeekQueue(responseParts);
			break;
		case ErrorCodes.WRONG_QUEUE_ID:
			handleRemoveQueueResponse(responseParts);
			break;
		case ErrorCodes.WRONG_CLIENT_ID:
			handleWrongSenderIdError();
			break;
//		case ErrorCodes.WRONG_RECEIVER_ID_ERROR:
//			handleWrongReceiverId(responseParts);
//			break;
		case ResponseCodes.POP_SENDER_QUERY_RESPONSE_CODE:
			handleQueryBySender(responseParts);
			break;
		case ErrorCodes.QUEUE_CONTAINS_MESSAGES:
			handleQueueInUse(responseParts);
			break;
		case ErrorCodes.SQL_ERROR:
			handleSQLError(response);
			break;
		default:
			System.out.println("Unknown response code: " + responseCode);
			handleUnknownResponseCode();
			break;
		}
		}
		catch (Exception e) {
			Util.clientErrorLogger.catching(e);
		}
		
//		if (responseCode != ErrorCodes.WRONG_CLIENT_ID)
			c.nextRequest();
	}
	
	private void handleQueryBySender(String[] responseParts) throws Exception {
		long senderId = Long.parseLong(responseParts[1]);
		if (responseParts.length == 3) {
			String message = responseParts[2];
			// do something with the message (log)
			System.out.println("Message received: " + message);
		}
		else {
			// no message contained in this queue for this receiver
		}
		
	}

	private void handleQueueInUse(String[] responseParts) throws Exception {
		// do something
	}

	/*
	 * Peek queue:
	 * code _ queueid _ content (if any message available)
	 */
	private void handlePeekQueue(String[] responseParts) throws Exception {
		long queueId = Long.parseLong(responseParts[1]);
		if (responseParts.length == 3) {
			String message = responseParts[2];
			// do something with the message (log)
			System.out.println("Message received: " + message);
		}
		else {
			// no message contained in this queue for this receiver
		}
	}

	/*
	 * List of queues that have messages for the client
	 * code _ num queues _ q1 .. qn
	 */
	private void handleQueues(String[] responseParts) throws Exception {
		int numQueues = Integer.parseInt(responseParts[1]);
		for (int i = 0; i < numQueues; i++)
			c.addQueueId(Long.parseLong(responseParts[i+2]));
	}

	private void handleWrongReceiverId(String[] responseParts) throws Exception {
		long id = Long.parseLong(responseParts[1]);
		// do something with wrong receiver id
	}

	private void handleSQLError(String response) throws Exception {
		Util.clientErrorLogger.error(response);
	}

	private void handleUnknownResponseCode() throws Exception  {
		// TODO Auto-generated method stub
		
	}

	/*
	 * Register response:
	 * code _ id
	 */
	private void handleRegisterResponse(String[] responseParts) throws Exception  {
		long id = Long.parseLong(responseParts[1]);
		c.setId(id);
	}
	
	private void handleCreateQueueResponse(String[] responseParts) throws Exception {
		long id = Long.parseLong(responseParts[1]);
		c.addQueueId(id);
	}
	
	private void handleRemoveQueueResponse(String[] responseParts) throws Exception {
		long id = Long.parseLong(responseParts[1]);
		c.removeQueueId(id);
	}
	
	private void handleWrongSenderIdError() throws Exception {
//		c.register();
	}

}
