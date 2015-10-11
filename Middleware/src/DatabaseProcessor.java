import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DatabaseProcessor implements Runnable {

	public static final int MAX_CONNECTION_POOL_SIZE = 30;
	public static final int MAX_THREAD_POOL_SIZE = 100;
	public static final String DRIVER_NAME = "org.postgresql.Driver";

	private static String databaseUrl = "jdbc:postgresql://localhost:5432/asldb";
	private static String userName = "dinazverinski";
	private static String password = "";

	private Vector<Connection> connectionPool;
	private ThreadPoolExecutor executor;
	private boolean running;

	private Server middleware;

	public DatabaseProcessor(Server middleware) throws Exception {
		connectionPool = new Vector<Connection>();
		executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
		this.middleware = middleware;
		running = true;
		initializeConnectionPool();
	}

	public void run() {
		int count = 1;
		while (running) {
			ClientRequest cr = middleware.removeFromRequestQueue();
			if (cr != null) {
				DatabaseWorker dw = new DatabaseWorker(this, cr);
				executor.execute(dw);
			}
			count++;
		}
	}

	public void shutdown() {
		running = false;
		executor.shutdown();
	}

	// public static void main(String[] args) throws Exception {
	// new DatabaseProcessor();
	// }

	private void initializeConnectionPool() {
		while (connectionPool.size() < MAX_CONNECTION_POOL_SIZE) {
			System.out
					.println("Connection Pool is NOT full. Proceeding with adding new connections");
			connectionPool.addElement(createNewConnectionForPool());
		}
		System.out.println("Connection Pool is full.");
	}

	private Connection createNewConnectionForPool() {
		Connection connection = null;

		try {
			Class.forName(DRIVER_NAME);
			connection = DriverManager.getConnection(databaseUrl, userName,
					password);
			System.out.println("Connection: " + connection);
		} catch (SQLException sqle) {
			System.err.println("SQLException: " + sqle);
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
