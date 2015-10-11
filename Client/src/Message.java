import java.util.Random;


public class Message {
	
	private int senderId;
	private int receiverId;
	
	private int messageLength;
	private String content;
	
	private String sendRequest;
	
	public Message(int senderId, int receiverId, int messageLength) {
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.messageLength = messageLength;
		generateContent();
		sendRequest = Util.SEND_REQUEST_CODE + " " + senderId + " " + receiverId + " " + content;
	}
	
	private void generateContent() {
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(messageLength);
		for( int i = 0; i < messageLength; i++ ) 
		      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		content = sb.toString();
	}
	
	public String getSendRequest() {
		return sendRequest;
	}

}
