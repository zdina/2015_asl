package asl.middleware.database;

import java.sql.Connection;

import asl.middleware.RequestWrapper;
import asl.middleware.Server;

public class DatabaseWorker implements Runnable {

	private DatabaseProcessor dp;
	private RequestWrapper cr;
	private Server middleware;

	public DatabaseWorker(DatabaseProcessor dp, Server middleware,
			RequestWrapper cr) {
		this.dp = dp;
		this.cr = cr;
		this.middleware = middleware;
	}

	public void run() {
		RequestHandler rh = dp.getRequestHandlerFromPool();
		while (rh == null)
			rh = dp.getRequestHandlerFromPool();

		rh.processRequest(cr);
		String response = rh.getResponse();
		if (response != null) {
			response = response.replace("\n", " ");
			cr.setResponse(response);
			middleware.addToResponseQueue(cr);
		}

		dp.returnRequestHandlerToPool(rh);
	}

}
