package asl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import asl.client.Client;

public class Util {
	
//	public static final int REGISTER_REQUEST_CODE = 0;
//	public static final int SEND_REQUEST_CODE = 1;
////	public static final int CONTACTS_REQUEST_CODE = 2;
//	public static final int CREATE_QUEUE_REQUEST_CODE = 3;
//	public static final int REMOVE_QUEUE_REQUEST_CODE = 4;
//	public static final int POP_QUEUE_REQUEST_CODE = 5;
//	public static final int PEEK_QUEUE_REQUEST_CODE = 6;
//	public static final int POP_SENDER_QUERY_REQUEST_CODE = 7;
//	public static final int PEEK_SENDER_QUERY_REQUEST_CODE = 8;
//	public static final int QUERY_QUEUES_REQUEST_CODE = 9;


	public static final Logger logger = LogManager.getLogger("asl.log.client");
	
	public static final int NO_RECEIVER_CODE = 0; //needs a no receiver id in db!!
	
//	public static final int REGISTER_RESPONSE_CODE = 100;
//	public static final int SEND_RESPONSE_CODE = 101;
////	public static final int CONTACTS_RESPONSE_CODE = 102;
//	public static final int CREATE_QUEUE_RESPONSE_CODE = 103;
//	public static final int REMOVE_QUEUE_RESPONSE_CODE = 104;
//	public static final int POP_QUEUE_RESPONSE_CODE = 105;
//	public static final int PEEK_QUEUE_RESPONSE_CODE = 106;
//	public static final int POP_SENDER_QUERY_RESPONSE_CODE = 107;
//	public static final int PEEK_SENDER_QUERY_RESPONSE_CODE = 108;
//	public static final int QUERY_QUEUES_RESPONSE_CODE = 109;
	
//	public static final int SQL_ERROR = 1000;
//	public static final int WRONG_SENDER_ID_ERROR = 1001;
//	public static final int WRONG_RECEIVER_ID_ERROR = 1002;
//	public static final int WRONG_QUEUE_ID_ERROR = 1004;
//	public static final int QUEUE_IN_USE = 1005;
	
	
	public static final String CLIENT_TABLE = "client";
	public static final String MESSAGE_TABLE = "message";
	public static final String QUEUE_TABLE = "queue";

}
