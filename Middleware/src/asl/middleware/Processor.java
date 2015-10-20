package asl.middleware;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public abstract class Processor implements Runnable {

	protected Server middleware;
	protected boolean running;
	protected ThreadPoolExecutor executor;
	
	public Processor(Server middleware, int numThreads) {
		executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(numThreads);
		this.middleware = middleware;
		running = true;
	}
	
	public void shutdown() {
		running = false;
		executor.shutdown();
	}
	
	public abstract void run();
	
}
