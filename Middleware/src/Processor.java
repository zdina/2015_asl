import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public abstract class Processor implements Runnable {
	
	public static final int MAX_THREAD_POOL_SIZE = 100;

	protected Server middleware;
	protected boolean running;
	protected ThreadPoolExecutor executor;
	
	public Processor(Server middleware) {
		executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
		this.middleware = middleware;
		running = true;
	}
	
	public void shutdown() {
		running = false;
		executor.shutdown();
	}
	
	public abstract void run();
	
}
