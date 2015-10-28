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
			case ResponseCodes.REGISTER:
				handleRegisterResponse(responseParts);
				break;
			case ResponseCodes.SEND:
				break;
			case ResponseCodes.CREATE_QUEUE:
				handleCreateQueueResponse(responseParts);
				break;
			case ResponseCodes.REMOVE_QUEUE:
				handleRemoveQueueOrNoQueueResponse(responseParts);
				break;
			case ResponseCodes.QUERY_QUEUES:
				handleQueues(responseParts);
				break;
			case ResponseCodes.PEEK_QUEUE:
				handleMessage(responseParts);
				break;
			case ResponseCodes.POP_QUEUE:
				handleMessage(responseParts);
				break;
			case ResponseCodes.PEEK_SENDER:
				handleMessage(responseParts);
				break;
			case ResponseCodes.POP_SENDER:
				handleMessage(responseParts);
				break;
			case ErrorCodes.WRONG_QUEUE_ID:
				handleRemoveQueueOrNoQueueResponse(responseParts);
				break;
			case ErrorCodes.WRONG_CLIENT_ID:
				handleWrongClientIdError();
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
		} catch (Exception e) {
			Util.clientErrorLogger.catching(e);
		}

		c.nextRequest();
	}
	
	/*
	 * Peek/pop by queue/sender: code _ queueid/senderid _ content (if any message available)
	 */
	private void handleMessage(String[] responseParts) throws Exception {
		long id = Long.parseLong(responseParts[1]); // could be query or sender id
		if (responseParts.length == 3) {
			String message = responseParts[2];
			// do something with message
		}
		else {
			// no message for the particular query
		}
	}

	private void handleQueueInUse(String[] responseParts) throws Exception {
		// do something
	}


	/*
	 * List of queues that have messages for the client code _ num queues _ q1
	 * .. qn
	 */
	private void handleQueues(String[] responseParts) throws Exception {
		int numQueues = Integer.parseInt(responseParts[1]);
		for (int i = 0; i < numQueues; i++)
			c.addQueueId(Long.parseLong(responseParts[i + 2]));
	}

	private void handleSQLError(String response) throws Exception {
		Util.clientErrorLogger.error(c.getRequest().getRequest());
		Util.clientErrorLogger.error(response);
	}

	private void handleUnknownResponseCode() throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * Register response: code _ id
	 */
	private void handleRegisterResponse(String[] responseParts)
			throws Exception {
		long id = Long.parseLong(responseParts[1]);
		c.setId(id);
	}

	private void handleCreateQueueResponse(String[] responseParts)
			throws Exception {
		long id = Long.parseLong(responseParts[1]);
		c.addQueueId(id);
	}

	private void handleRemoveQueueOrNoQueueResponse(String[] responseParts)
			throws Exception {
		long id = Long.parseLong(responseParts[1]);
		c.removeQueueId(id);
	}

	private void handleWrongClientIdError() throws Exception {
		// c.register();
	}

}
