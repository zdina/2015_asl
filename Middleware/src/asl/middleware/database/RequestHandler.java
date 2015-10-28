package asl.middleware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import asl.ErrorCodes;
import asl.RequestCodes;
import asl.ResponseCodes;
import asl.Util;
import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class RequestHandler {

	private Connection con;
	private Server middleware;
	private String response;

	private PreparedStatement registerStmt;
	private PreparedStatement createQueueStmt;
	private PreparedStatement removeQueueStmt;
	private PreparedStatement sendMessageStmt;
	private PreparedStatement queryByQueueStmt;
	private PreparedStatement queryForQueuesStmt;
	private PreparedStatement queryBySenderStmt;

	public RequestHandler(Connection con, Server middleware) {
		this.con = con;
		this.middleware = middleware;
		initStatements();
	}
	
	private void initStatements() {
		try {
			registerStmt = con.prepareStatement("SELECT registerClient()");
			createQueueStmt = con.prepareStatement("SELECT createQueue()");
			removeQueueStmt = con.prepareStatement("SELECT removeQueue(?)");
			sendMessageStmt = con
					.prepareStatement("SELECT sendMessage(?, ?, ?, ?)");
			queryByQueueStmt = con
					.prepareStatement("SELECT queryByQueue(?,?,?)");
			queryForQueuesStmt = con
					.prepareStatement("SELECT queryForQueues(?)");
			queryBySenderStmt = con
					.prepareStatement("SELECT queryBySender(?,?,?)");
		} catch (SQLException e) {
			Util.serverErrorLogger.catching(e);
		}
	}

	public void processRequest(RequestWrapper cp) {
		String[] requestParts = cp.getRequest().split(" ");
		int requestCode = Integer.parseInt(requestParts[0]);
		cp.setTimeDbStart(System.nanoTime());
		try {
			switch (requestCode) {
			case RequestCodes.REGISTER:
				register(requestParts, cp.getInternalClientId());
				break;
			case RequestCodes.SEND_MESSAGE:
				send(requestParts);
				break;
			case RequestCodes.CREATE_QUEUE:
				createQueue(requestParts);
				break;
			case RequestCodes.REMOVE_QUEUE:
				removeQueue(requestParts);
				break;
			case RequestCodes.QUERY_FOR_QUEUES:
				queryForQueuesWithMessages(requestParts);
				break;
			case RequestCodes.PEEK_QUEUE:
				queryByQueue(requestParts, false);
				break;
			case RequestCodes.POP_QUEUE:
				queryByQueue(requestParts, true);
				break;
			case RequestCodes.POP_BY_SENDER:
				queryBySender(requestParts, true);
				break;
			case RequestCodes.PEEK_BY_SENDER:
				queryBySender(requestParts, false);
				break;
			default:
				// unclear message
				break;
			}
		} catch (SQLException e) {
			response = ErrorCodes.SQL_ERROR + " " + e.getMessage();
			Util.serverErrorLogger.error(cp.getRequest());
			Util.serverErrorLogger.catching(e);
		}
		cp.setTimeDbReceived(System.nanoTime());
	}

	public String getResponse() {
		return response;
	}
	
	public void resetResponse() {
		response = "";
	}

	/*
	 * Register Request: code
	 */
	private void register(String[] requestParts, long internalId)
			throws SQLException {
		ResultSet rs = registerStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		response = ResponseCodes.REGISTER + " " + dbresponse;
		middleware.setClientDbId(internalId, Long.parseLong(dbresponse));
	}

	/*
	 * Create Queue Request: code
	 */
	private void createQueue(String[] requestParts) throws SQLException {
		ResultSet rs = createQueueStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		response = ResponseCodes.CREATE_QUEUE + " " + dbresponse;
	}

	/*
	 * Remove Queue Request: code _ id. Can't delete queue, if there are still
	 * messages.
	 */
	private void removeQueue(String[] requestParts) throws SQLException {
		long queueId = Long.parseLong(requestParts[1]);
		removeQueueStmt.setLong(1, queueId);
		ResultSet rs = removeQueueStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_QUEUE_ID + " " + queueId;
		else if (dbresponse.equals("inuse"))
			response = ErrorCodes.QUEUE_CONTAINS_MESSAGES + " " + queueId;
		else
			response = ResponseCodes.REMOVE_QUEUE + " " + queueId;
	}

	/*
	 * Message Send Request: code _ port _ senderId _ receiverId _ queueId _
	 * content
	 */
	private void send(String[] requestParts) throws SQLException {
		long senderId = Long.parseLong(requestParts[1]);
		long receiverId = Long.parseLong(requestParts[2]);
		long queueId = Long.parseLong(requestParts[3]);
		String content = requestParts[4];

		sendMessageStmt.setLong(1, senderId);
		sendMessageStmt.setLong(2, receiverId);
		sendMessageStmt.setLong(3, queueId);
		sendMessageStmt.setString(4, content);

		ResultSet rs = sendMessageStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);

		if (dbresponse.equals("noreceiver"))
			response = ErrorCodes.WRONG_CLIENT_ID + " " + receiverId;
		else if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_QUEUE_ID + " " + queueId;
		else
			response = ResponseCodes.SEND + "";
	}

	/*
	 * Peek (or pop if param true) Queue: code _ receiverid _ queueid
	 */
	public void queryByQueue(String[] requestParts, boolean doDelete)
			throws SQLException {
		long receiverid = Long.parseLong(requestParts[1]);
		long queueid = Long.parseLong(requestParts[2]);

		queryByQueueStmt.setLong(1, receiverid);
		queryByQueueStmt.setLong(2, queueid);
		queryByQueueStmt.setBoolean(3, doDelete);
		ResultSet rs = queryByQueueStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_QUEUE_ID + " " + queueid;
		else {
			if (doDelete)
				response = ResponseCodes.POP_QUEUE + " ";
			else
				response = ResponseCodes.PEEK_QUEUE + " ";
			if (dbresponse.equals("empty"))
				response += queueid;
			else
				response += queueid + " " + dbresponse;
		}
	}

	/*
	 * Query for queues with messages: code _ receiverid
	 */
	public void queryForQueuesWithMessages(String[] requestParts)
			throws SQLException {
		long receiverid = Long.parseLong(requestParts[1]);
		queryForQueuesStmt.setLong(1, receiverid);
		ResultSet rs = queryForQueuesStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		response = ResponseCodes.QUERY_QUEUES + " " + dbresponse;
	}

	/*
	 * Pops/peeks message by specific sender: code _ receiverid _ senderid
	 */
	public void queryBySender(String[] requestParts, boolean doDelete)
			throws SQLException {
		long receiverid = Long.parseLong(requestParts[1]);
		long senderid = Long.parseLong(requestParts[2]);

		queryBySenderStmt.setLong(1, receiverid);
		queryBySenderStmt.setLong(2, senderid);
		queryBySenderStmt.setBoolean(3, doDelete);
		ResultSet rs = queryBySenderStmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("nosender"))
			response = ErrorCodes.WRONG_CLIENT_ID + " " + senderid;
		else {
			if (doDelete)
				response = ResponseCodes.POP_SENDER + " ";
			else
				response = ResponseCodes.PEEK_SENDER + " ";
			if (dbresponse.equals("empty"))
				response += senderid; // sender vs receiver!!
			else
				response += senderid + " " + dbresponse;
		}
	}

}
