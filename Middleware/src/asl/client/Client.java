package asl.client;

import java.util.ArrayList;
import java.util.Random;

public class Client implements Runnable {

	private ArrayList<Long> queues;

	private RequestSender rs;

	// private BufferedReader in;

	private int messageLength;

	// "Dinas-MacBook-Air.local"
	// 4321
	public Client(String middlewareIp, int middlewarePort, int ownPort,
			int messageLength) throws Exception {
		queues = new ArrayList<Long>();
		rs = new RequestSender(middlewareIp, middlewarePort, ownPort);
		// in = new BufferedReader(new
		// InputStreamReader(socket.getInputStream()));
		this.messageLength = messageLength;

		ResponseAcceptor ra = new ResponseAcceptor(ownPort,
				new ResponseHandler(this));
		Thread t = new Thread(ra);
		t.start();
	}

	public void run() {
		try {
//			rs.register();
//			rs.createQueue();
//			rs.removeQueue(queues.get(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addQueueId(long id) {
		queues.add(id);
	}
	
	public void removeQueueId(long id) {
		queues.remove(id);
	}

	public void setId(long id) {
		rs.setId(id);
	}
	
	private String generateContent() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(messageLength);
		for( int i = 0; i < messageLength; i++ ) 
		      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		new Thread(new Client("10.0.1.70", 4321, 1234, 200)).start();
//		new Thread(new Client("10.0.1.70", 4321, 2345, 200)).start();
//		new Thread(new Client("10.0.1.70", 4321, 3456, 200)).start();
	}

	// public void sendMessage(int receiverId) {
	// Message message = new Message(id, receiverId, messageLength);
	// out.println(message.getSendRequest());
	// }

	// public String receiveMessage() {
	// String line = "";
	// try {
	// line = in.readLine();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return line;
	// }

}
