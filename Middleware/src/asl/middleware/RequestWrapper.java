package asl.middleware;

public class RequestWrapper {

	private String request;
	private String response;
	private long clientId;
	
	private long timeArrival;
	private long timeDbStart;
	private long timeDbReceived;
	private long timeResponse;

	public RequestWrapper(long clientId, String request, long timeArrival) {
		this.clientId = clientId;
		this.request = request;
		this.timeArrival = timeArrival;
		response = null;
	}

	public long getTimeArrival() {
		return timeArrival;
	}

	public void setTimeArrival(long timeArrival) {
		this.timeArrival = timeArrival;
	}

	public long getTimeDbStart() {
		return timeDbStart;
	}

	public void setTimeDbStart(long timeDbStart) {
		this.timeDbStart = timeDbStart;
	}

	public long getTimeDbReceived() {
		return timeDbReceived;
	}

	public void setTimeDbReceived(long timeDbReceived) {
		this.timeDbReceived = timeDbReceived;
	}

	public long getTimeResponse() {
		return timeResponse;
	}

	public void setTimeResponse(long timeResponse) {
		this.timeResponse = timeResponse;
	}

	public String getRequest() {
		return request;
	}
	
	public long getClientId() {
		return clientId;
	}

	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public String getResponse() {
		return response;
	}
	
	
}