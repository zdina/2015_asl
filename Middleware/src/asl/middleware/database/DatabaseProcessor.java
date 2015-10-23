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

	private Vector<RequestHandler> connectionPool;

	public DatabaseProcessor(Server middleware, String database, String user,
			int numConnections, int numThreads) throws Exception {
		super(middleware, numThreads);
		this.databaseUrl = "jdbc:postgresql://" + database;
		this.userName = user;
		this.numConnections = numConnections;

		connectionPool = new Vector<RequestHandler>();
		initializeRequestHandlerPool();
		System.out.println("DatabaseProcessor started.");
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

	private void initializeRequestHandlerPool() {
		while (connectionPool.size() < numConnections)
			connectionPool.addElement(createNewRequestHandlerWithConnectionForPool());

	}

	private RequestHandler createNewRequestHandlerWithConnectionForPool() {
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

		return new RequestHandler(connection, middleware);
	}

	public synchronized RequestHandler getRequestHandlerFromPool() {
		RequestHandler rh = null;
		if (connectionPool.size() > 0) {
			rh = (RequestHandler) connectionPool.firstElement();
			connectionPool.removeElementAt(0);
		}
		return rh;
	}

	public synchronized void returnRequestHandlerToPool(RequestHandler rh) {
		connectionPool.addElement(rh);
	}

}
