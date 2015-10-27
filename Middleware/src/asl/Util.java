package asl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import asl.client.Client;

public class Util {


	public static final Logger clientLogger = LogManager.getLogger("asl.log.client");
	public static final Logger serverLogger = LogManager.getLogger("asl.log.server");
	public static final Logger clientErrorLogger = LogManager.getLogger("asl.log.error.client");
	public static final Logger serverErrorLogger = LogManager.getLogger("asl.log.error.server");
	public static final Logger queuesLogger = LogManager.getLogger("asl.log.queues");
	
	public static final int NO_RECEIVER_CODE = 0; //needs a no receiver id in db!!


}
