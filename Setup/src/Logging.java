import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Logging {

	private static final Logger logger = LogManager.getLogger(Logging.class);
	
	public static void main(String[] args) {
		logger.trace("Entering application");
		logger.error("some error");
		logger.trace("Exiting application");
		
	}
}
