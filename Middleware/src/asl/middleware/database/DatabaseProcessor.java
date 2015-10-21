package asl.middleware.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;

import asl.middleware.Processor;
import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class DatabaseProcessor extends Processor {

	public static final String DRIVER_NAME = "org.postgresql.Driver";

	private String databaseUrl;
	private String userName;
	private String password = "";

	public int numConnections;

	private Vector<Connection> connectionPool;

	public DatabaseProcessor(Server middleware, String database, String user,
			int numConnections, int numThreads) throws Exception {
		super(middleware, numThreads);
		this.databaseUrl = "jdbc:postgresql://" + database;
		this.userName = user;
		this.numConnections = numConnections;

		connectionPool = new Vector<Connection>();
		initializeConnectionPool();
//		emptyDb();
		System.out.println("DatabaseProcessor started.");
	}
	
	private void emptyDb() throws SQLException {
		Connection con = getConnectionFromPool();
		PreparedStatement stmt = con.prepareStatement("select emptydb()");
		stmt.executeQuery();
		returnConnectionToPool(con);
	}

	public void run() {
		while (running) {
			RequestWrapper cr = middleware.removeFromRequestQueue();
			if (cr != null) {
				DatabaseWorker dw = new DatabaseWorker(this, middleware, cr);
				executor.execute(dw);
			}
		}
	}

	private void initializeConnectionPool() {
		while (connectionPool.size() < numConnections)
			connectionPool.addElement(createNewConnectionForPool());

	}

	private Connection createNewConnectionForPool() {
		Connection connection = null;

		try {
			Class.forName(DRIVER_NAME);
			connection = DriverManager.getConnection(databaseUrl, userName,
					password);
		} catch (SQLException sqle) {
			System.err.println("SQLException: " + sqle);
			sqle.printStackTrace();
			return null;
		} catch (ClassNotFoundException cnfe) {
			System.err.println("ClassNotFoundException: " + cnfe);
			return null;
		}

		return connection;
	}

	public synchronized Connection getConnectionFromPool() {
		Connection connection = null;
		if (connectionPool.size() > 0) {
			connection = (Connection) connectionPool.firstElement();
			connectionPool.removeElementAt(0);
		}
		return connection;
	}

	public synchronized void returnConnectionToPool(Connection connection) {
		connectionPool.addElement(connection);
	}

}
