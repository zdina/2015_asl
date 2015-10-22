package asl.middleware.database;

import java.sql.Connection;

import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class DatabaseWorker implements Runnable {

	private DatabaseProcessor dp;
	private Connection con;
	private RequestWrapper cr;
	private Server middleware;

	public DatabaseWorker(DatabaseProcessor dp, Server middleware,
			RequestWrapper cr) {
		this.dp = dp;
		this.cr = cr;
		this.middleware = middleware;
	}

	public void run() {
		con = dp.getConnectionFromPool();
		while (con == null)
			con = dp.getConnectionFromPool();

		RequestHandler rt = new RequestHandler(cr, con);
		String response = rt.getResponse();
		if (response != null) {
			response = response.replace("\n", " ");
			cr.setResponse(response);
			middleware.addToResponseQueue(cr);
		}

		dp.returnConnectionToPool(con);
	}

}
