package asl.middleware;

public class RequestWrapper {

	private String request;
	private String response;
	private long clientId;

	public RequestWrapper(long clientId, String request) {
		this.clientId = clientId;
		this.request = request;
		response = null;
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