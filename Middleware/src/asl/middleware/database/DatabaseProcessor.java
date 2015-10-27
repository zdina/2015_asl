package asl.middleware.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class DatabaseProcessor implements Runnable {

	public static final String DRIVER_NAME = "org.postgresql.Driver";

	private Server middleware;
	private String databaseUrl;
	private String userName;
	private String password;

	protected ThreadPoolExecutor executor;

	public int numThreads;

	private Vector<RequestHandler> connectionPool;

	public DatabaseProcessor(Server middleware, String database, String user,
			String password, int numThreads) throws Exception {
		this.middleware = middleware;
		this.databaseUrl = "jdbc:postgresql://" + database;
		this.userName = user;
		this.password = password;
		this.numThreads = numThreads;

		executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(numThreads);

		connectionPool = new Vector<RequestHandler>();
		initializeRequestHandlerPool();
		// System.out.println("DatabaseProcessor started.");
	}

	public void run() {
		while (true) {
			RequestWrapper cr = middleware.removeFromRequestQueue();
			if (cr != null) {
				DatabaseWorker dw = new DatabaseWorker(this, middleware, cr);
				executor.execute(dw);
			}
		}
	}

	private void initializeRequestHandlerPool() {
		while (connectionPool.size() < numThreads)
			connectionPool
					.addElement(createNewRequestHandlerWithConnectionForPool());

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
