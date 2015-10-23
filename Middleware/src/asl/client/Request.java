package asl.client;

public class Request {
	
	private String request;
	private long timeSent;
	private int requestCode;
	
	public Request(String request, long timeSent, int requestCode) {
		this.request = request;
		this.timeSent = timeSent;
		this.requestCode = requestCode;
	}

	public String getRequest() {
		return request;
	}

	public long getTimeSent() {
		return timeSent;
	}
	
	public int getRequestCode() {
		return requestCode;
	}
	
	

}
