import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DatabaseProcessor extends Processor {

	public static final int MAX_CONNECTION_POOL_SIZE = 30;
	public static final String DRIVER_NAME = "org.postgresql.Driver";

	private static String databaseUrl = "jdbc:postgresql://localhost:5432/asldb";
	private static String userName = "dinazverinski";
	private static String password = "";

	private Vector<Connection> connectionPool;

	public DatabaseProcessor(Server middleware) throws Exception {
		super(middleware);
		connectionPool = new Vector<Connection>();
		initializeConnectionPool();
		System.out.println("DatabaseProcessor started.");
	}

	public void run() {
		int count = 1;
		while (running) {
			ClientProxy cr = middleware.removeFromRequestQueue();
			if (cr != null) {
				DatabaseWorker dw = new DatabaseWorker(this, middleware, cr);
				executor.execute(dw);
			}
			count++;
		}
	}

	// public static void main(String[] args) throws Exception {
	// new DatabaseProcessor();
	// }

	private void initializeConnectionPool() {
		while (connectionPool.size() < MAX_CONNECTION_POOL_SIZE)
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
