package asl.middleware;

import java.util.TimerTask;

import asl.Util;

public class QueueLogger extends TimerTask {

	Server middleware;

	public QueueLogger(Server middleware) {
		this.middleware = middleware;
	}

	@Override
	public void run() {
		Util.queuesLogger.trace("{},{},{}", 0, System.currentTimeMillis(),
				middleware.getRequestQueueSize()); // 0 = request queue
		Util.queuesLogger.trace("{},{},{}", 1, System.currentTimeMillis(),
				middleware.getResponseQueueSize()); // 1 = response queue

	}

}
