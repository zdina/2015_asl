package ch.middleware.response;
import ch.middleware.ClientProxy;
import ch.middleware.Processor;
import ch.middleware.Server;

public class ResponseProcessor extends Processor {

	public ResponseProcessor(Server middleware) {
		super(middleware);
		System.out.println("Response processor started.");
	}

	@Override
	public void run() {
		int count = 1;
		while (running) {
			ClientProxy cp = middleware.removeFromResponseQueue();
			if (cp != null) {
				ResponseWorker rp = new ResponseWorker(cp);
				executor.execute(rp);
			}
			count++;
		}
	}

}
