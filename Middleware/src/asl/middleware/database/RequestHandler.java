package asl.middleware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import asl.ErrorCodes;
import asl.RequestCodes;
import asl.ResponseCodes;
import asl.Util;
import asl.middleware.ClientProxy;

public class RequestHandler {

	private String[] requestParts;
	private Connection con;
	private ClientProxy cp;
	private String response;

	public RequestHandler(ClientProxy cp, Connection con) {
		this.cp = cp;
		this.con = con;
		requestParts = cp.getRequest().split(" ");
		int requestCode = Integer.parseInt(requestParts[0]);

		try {
			switch (requestCode) {
			case RequestCodes.REGISTER:
				register();
				break;
			case RequestCodes.SEND_MESSAGE:
				send();
				break;
			case RequestCodes.CREATE_QUEUE:
				createQueue();
				break;
			case RequestCodes.REMOVE_QUEUE:
				removeQueue();
				break;
			case RequestCodes.QUERY_FOR_QUEUES:
				queryForQueuesWithMessages();
				break;
			case RequestCodes.PEEK_QUEUE:
				queryByQueue(false);
				break;
			case RequestCodes.POP_QUEUE:
				queryByQueue(true);
				break;
			case RequestCodes.POP_BY_SENDER:
				queryBySender(true);
				break;
			default:
				// unclear message
				break;
			}
		} catch (SQLException e) {
			response = ErrorCodes.SQL_ERROR + " " + e.getMessage();
		}
	}

	public String getResponse() {
		return response;
	}

	/*
	 * Register Request: code _ port Checks first, whether client exists already
	 * (based on IP and port) If exists, returns the id found Else, inserts new
	 * entry and returns this entry's id
	 */
	private void register() throws SQLException {
		String ip = cp.getAddress().getHostName();
		int port = Integer.parseInt(requestParts[1]);

		long id;

		String query = "SELECT id FROM " + Util.CLIENT_TABLE + " WHERE ip = '"
				+ ip + "' AND port = " + port;
		PreparedStatement stmt = con.prepareStatement(query);
		ResultSet oldId = stmt.executeQuery();
		if (oldId.next()) {
			id = oldId.getLong(1);
		} else {

			String insert = "INSERT INTO " + Util.CLIENT_TABLE
					+ "(ip, port) VALUES('" + ip + "',?)";
			PreparedStatement stmt2 = con.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			stmt2.setInt(1, port);
			stmt2.executeUpdate();
			ResultSet generatedId = stmt2.getGeneratedKeys();
			generatedId.next();
			id = generatedId.getLong(1);
			System.out.println("Inserted id for '" + cp.getRequest() + "': "
					+ id);
		}

		response = ResponseCodes.REGISTER_RESPONSE_CODE + " " + id;
	}

	/*
	 * Create Queue Request: code _ port
	 */
	private void createQueue() throws SQLException {
		String query = "SELECT createQueue()";
		PreparedStatement stmt = con.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		response = ResponseCodes.CREATE_QUEUE_RESPONSE_CODE + " " + dbresponse;
	}

	/*
	 * Remove Queue Request: code _ port _ id. Can't delete queue, if there are
	 * still messages.
	 */
	private void removeQueue() throws SQLException {
		long queueId = Long.parseLong(requestParts[2]);
		String query = "SELECT removeQueue(?)";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, queueId);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_QUEUE_ID + " " + queueId;
		else if (dbresponse.equals("inuse"))
			response = ErrorCodes.QUEUE_CONTAINS_MESSAGES + " " + queueId;
		else
			response = ResponseCodes.REMOVE_QUEUE_RESPONSE_CODE + " " + queueId;
	}

	/*
	 * Message Send Request: code _ port _ senderId _ receiverId _ queueId _
	 * content
	 */
	private void send() throws SQLException {
		long senderId = Long.parseLong(requestParts[2]);
		long receiverId = Long.parseLong(requestParts[3]);
		long queueId = Long.parseLong(requestParts[4]);
		String content = requestParts[5];

		String insert = "SELECT sendMessage(?, ?, ?, ?)";
		PreparedStatement stmt = con.prepareStatement(insert);
		stmt.setLong(1, senderId);
		stmt.setLong(2, receiverId);
		stmt.setLong(3, queueId);
		stmt.setString(4, content);
		
		
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		
		if (dbresponse.equals("noreceiver"))
			response = ErrorCodes.WRONG_CLIENT_ID + " " + receiverId;
		else if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_QUEUE_ID + " " + queueId;
		else
			response = ResponseCodes.SEND_RESPONSE_CODE + "";
	}

	/*
	 * Peek (or pop if param true) Queue: code _ port _ receiverid _ queueid
	 */
	public void queryByQueue(boolean doDelete) throws SQLException {
		long receiverid = Long.parseLong(requestParts[2]);
		long queueid = Long.parseLong(requestParts[3]);
		String query = "SELECT queryByQueue(?,?,?)";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, receiverid);
		stmt.setLong(2, queueid);
		stmt.setBoolean(3, doDelete);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("noqueue"))
			response = ErrorCodes.WRONG_CLIENT_ID + " " + queueid;
		else {
			if (doDelete)
				response = ResponseCodes.POP_QUEUE_RESPONSE_CODE + " ";
			else
				response = ResponseCodes.PEEK_QUEUE_RESPONSE_CODE + " ";
			if (dbresponse.equals("empty"))
				response += queueid;
			else
				response += queueid + " " + dbresponse;
		}
	}

	/*
	 * Query for queues with messages: code _ port _ receiverid
	 */
	public void queryForQueuesWithMessages() throws SQLException {
		long receiverid = Long.parseLong(requestParts[2]);
		String query = "SELECT queryForQueues(?)";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, receiverid);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		response = ResponseCodes.QUERY_QUEUES_RESPONSE_CODE + " " + dbresponse;
	}

	/*
	 * Pops/peeks message by specific sender: code _ port _ receiverid _
	 * senderid
	 */
	public void queryBySender(boolean doDelete) throws SQLException {
		long receiverid = Long.parseLong(requestParts[2]);
		long senderid = Long.parseLong(requestParts[3]);
		String query = "SELECT queryBySender(?,?,?)";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, receiverid);
		stmt.setLong(2, senderid);
		stmt.setBoolean(3, doDelete);
		ResultSet rs = stmt.executeQuery();
		rs.next();
		String dbresponse = rs.getString(1);
		if (dbresponse.equals("nosender"))
			response = ErrorCodes.WRONG_CLIENT_ID + " " + senderid;
		else {
			if (doDelete)
				response = ResponseCodes.POP_SENDER_QUERY_RESPONSE_CODE + " ";
			else
				response = ResponseCodes.PEEK_SENDER_QUERY_RESPONSE_CODE + " ";
			if (dbresponse.equals("empty"))
				response += senderid; // sender vs receiver!!
			else
				response += senderid + " " + dbresponse;
		}
	}

}
