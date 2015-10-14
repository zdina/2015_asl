package asl.middleware.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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
			case Util.REGISTER_REQUEST_CODE:
				register();
				break;
			case Util.SEND_REQUEST_CODE:
				send();
				break;
			case Util.CREATE_QUEUE_REQUEST_CODE:
				createQueue();
				break;
			case Util.REMOVE_QUEUE_REQUEST_CODE:
				removeQueue();
				break;
			case Util.QUERY_QUEUES_REQUEST_CODE:
				queryForQueuesWithMessages();
				break;
			case Util.PEEK_QUEUE_REQUEST_CODE:
				peekQueue(false);
				break;
			case Util.POP_QUEUE_REQUEST_CODE:
				peekQueue(true);
				break;
			default:
				// unclear message
				break;
			}
		} catch (SQLException e) {
			response = Util.SQL_ERROR + " " + e.getMessage();
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

		response = Util.REGISTER_RESPONSE_CODE + " " + id;
	}

	/*
	 * Create Queue Request: code _ port
	 */
	private void createQueue() throws SQLException {
		String query = "INSERT INTO " + Util.QUEUE_TABLE + "(name) VALUES(?)";
		PreparedStatement stmt = con.prepareStatement(query,
				Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, "a");
		stmt.executeUpdate();

		ResultSet generatedId = stmt.getGeneratedKeys();
		generatedId.next();
		long id = generatedId.getLong(1);

		System.out.println("Inserted queue, id: " + id);

		response = Util.CREATE_QUEUE_RESPONSE_CODE + " " + id;
	}

	/*
	 * Remove Queue Request: code _ port _ id. Can't delete queue, if there are
	 * still messages.
	 */
	private void removeQueue() {
		long queueId = Long.parseLong(requestParts[2]);
		String query = "DELETE FROM " + Util.QUEUE_TABLE
				+ " AS q WHERE q.id = ? AND (SELECT COUNT(m.id) FROM "
				+ Util.MESSAGE_TABLE + " AS m WHERE m.queueid = ?) = 0";
		try {
			PreparedStatement stmt = con.prepareStatement(query);
			stmt.setLong(1, queueId);
			stmt.setLong(2, queueId);
			int rows = stmt.executeUpdate();
			if (rows == 1)
				response = Util.REMOVE_QUEUE_RESPONSE_CODE + " " + queueId;
			else
				response = Util.WRONG_QUEUE_ID_ERROR + " " + queueId;
			// handle if queue is used!!!!!
		} catch (SQLException e) {
			response = Util.QUEUE_IN_USE + " " + queueId;
		}
	}

	/*
	 * Message Send Request: code _ port _ senderId _ receiverId _ queueId _
	 * content
	 */
	private void send() {
		long senderId = Long.parseLong(requestParts[2]);
		long receiverId = Long.parseLong(requestParts[3]);
		long queueId = Long.parseLong(requestParts[4]);
		String content = requestParts[5];

		String insert = "INSERT INTO " + Util.MESSAGE_TABLE
				+ "(senderid, receiverid, content, queueid) VALUES(?, ?, ?, ?)";
		PreparedStatement stmt;
		try {
			stmt = con
					.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, senderId);
			stmt.setLong(2, receiverId);
			stmt.setString(3, content);
			stmt.setLong(4, queueId);
			stmt.executeUpdate();
			ResultSet generatedId = stmt.getGeneratedKeys();
			generatedId.next();
			long id = generatedId.getLong(1);
			response = Util.SEND_RESPONSE_CODE + " " + id;
		} catch (SQLException e) {
			String errorMessage = e.getMessage();
			if (errorMessage.contains("receiverid"))
				response = Util.WRONG_RECEIVER_ID_ERROR + " " + receiverId;
			else if (errorMessage.contains("queueid"))
				response = Util.WRONG_QUEUE_ID_ERROR + " " + queueId;
			else if (errorMessage.contains("senderid"))
				response = Util.WRONG_SENDER_ID_ERROR + " " + senderId;
			else
				response = Util.SQL_ERROR + " ";
		}

	}

	/*
	 * Peek (or pop if param true) Queue: code _ port _ receiverid _ queueid
	 */
	private void peekQueue(boolean pop) throws SQLException {
		long receiverId = Long.parseLong(requestParts[2]);
		long queueId = Long.parseLong(requestParts[3]);
		String query = "SELECT content, id from " + Util.MESSAGE_TABLE
				+ " WHERE times = (SELECT min(times) from "
				+ Util.MESSAGE_TABLE
				+ " WHERE (receiverid = ? OR receiverid = 0) AND queueid = ?)";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, receiverId);
		stmt.setLong(2, queueId);
		ResultSet rs = stmt.executeQuery();
		if (pop)
			response = Util.POP_QUEUE_RESPONSE_CODE + " " + queueId;
		else
			response = Util.PEEK_QUEUE_RESPONSE_CODE + " " + queueId;
		if (rs.next()) {
			response += " " + rs.getString(1);
			if (pop) {
				String delete = "DELETE FROM " + Util.MESSAGE_TABLE
						+ " WHERE id = ?";
				PreparedStatement delStmt = con.prepareStatement(delete);
				delStmt.setLong(1, rs.getLong(2));
				delStmt.executeUpdate();
			}
		} else {
			String check = "SELECT count(id) from " + Util.QUEUE_TABLE
					+ " WHERE id = ?";
			PreparedStatement checkStmt = con.prepareStatement(check);
			checkStmt.setLong(1, queueId);
			ResultSet checkRs = checkStmt.executeQuery();
			checkRs.next();
			if (checkRs.getInt(1) == 0)
				response = Util.WRONG_QUEUE_ID_ERROR + " " + queueId;
		}
	}

	/*
	 * Query for queues with messages: code _ port _ receiverid
	 */
	public void queryForQueuesWithMessages() throws SQLException {
		long receiverid = Long.parseLong(requestParts[2]);
		String query = "SELECT DISTINCT queueid FROM " + Util.MESSAGE_TABLE
				+ " WHERE receiverid = ?";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, receiverid);
		ResultSet rs = stmt.executeQuery();
		String result = "";
		int resultNum = 0;
		while (rs.next()) {
			result += " " + rs.getLong(1);
			resultNum++;
		}
		response = Util.QUERY_QUEUES_RESPONSE_CODE + " " + resultNum + result;
	}

}
