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

	public RequestHandler(ClientProxy cp, Connection con) throws SQLException {
		this.cp = cp;
		this.con = con;
		requestParts = cp.getRequest().split(" ");
		int requestCode = Integer.parseInt(requestParts[0]);

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
		default:
			// unclear message
			break;
		}
	}

	public String getResponse() {
		return response;
	}

	/*
	 * Register Request: code _ port
	 * Checks first, whether client exists already (based on IP and port)
	 * If exists, returns the id found
	 * Else, inserts new entry and returns this entry's id
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
			System.out.println("Inserted id for '" + cp.getRequest() + "': " + id);
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
	 * still messages. delete from queue as q where q.id = 5 and (select
	 * count(m.id) from message as m where m.queueid = 5) = 0;
	 */
	private void removeQueue() throws SQLException {
		long queueId = Long.parseLong(requestParts[2]);
		String query = "DELETE FROM " + Util.QUEUE_TABLE
				+ " AS q WHERE q.id = ? AND (SELECT COUNT(m.id) FROM "
				+ Util.MESSAGE_TABLE + " AS m WHERE m.queueid = ?) = 0";
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setLong(1, queueId);
		stmt.setLong(2, queueId);
		int rows = stmt.executeUpdate();
		if (rows == 1)
			response = Util.REMOVE_QUEUE_RESPONSE_CODE + " " + queueId;
		// !!
	}

	/*
	 * Message Send Request: code _ port _ senderId _ receiverId _ queueId _
	 * content
	 */
	private void send() throws SQLException {
		long senderId = Long.parseLong(requestParts[2]);
		long receiverId = Long.parseLong(requestParts[3]);
		long queueId = Long.parseLong(requestParts[4]);
		String content = requestParts[4];

		String query = "INSERT INTO TABLE " + Util.MESSAGE_TABLE
				+ "VALUES(?, ?, ?)";
		PreparedStatement stmt = con.prepareStatement(query,
				Statement.RETURN_GENERATED_KEYS);
		stmt.setLong(1, senderId);
		stmt.setLong(2, receiverId);
		stmt.setLong(3, queueId);
		stmt.setString(3, content);
		// !!
	}

	/*
	 * Peek Queue: code _ port _
	 */
	private void peekQueue() throws SQLException {

	}

}
