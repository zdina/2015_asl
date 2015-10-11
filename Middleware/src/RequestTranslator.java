import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class RequestTranslator {

	private PreparedStatement stmt;
	private String[] requestParts;
	private Connection con;

	public RequestTranslator(String request, Connection con) throws SQLException {
		this.con = con;
		requestParts = request.split(" ");
		int requestCode = Integer.parseInt(requestParts[0]);
		
		switch (requestCode) {
		case Util.REGISTER_REQUEST_CODE:
			register();
			break;
		case Util.SEND_REQUEST_CODE:
			send();
			break;
		default:
			// unclear message
			break;
		}
	}

	public PreparedStatement getStatement() {
		return stmt;
	}
	
	/*
	 * Register Request:
	 * code _ name
	 */
	private void register() throws SQLException {
		String query = "INSERT INTO " + Util.CLIENT_TABLE + "(name) VALUES(?)";
		stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		stmt.setString(1, requestParts[1]);
	}
	
	/*
	 * Message Send Request:
	 * code _ senderId _ receiverId _ content
	 */
	private void send() throws SQLException {
		long senderId = Integer.parseInt(requestParts[1]);
		long receiverId = Integer.parseInt(requestParts[2]);
		String content = requestParts[3];// need to add queue!!!
		String query = "INSERT INTO TABLE " + Util.MESSAGE_TABLE + "VALUES(?, ?, ?)";
		stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		stmt.setLong(1, senderId);
		stmt.setLong(2, receiverId);
		stmt.setString(3, content);
	}
}
