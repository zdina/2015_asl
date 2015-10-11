import java.net.Socket;

public class ClientRequest {

	private String request;
	private Socket client;
	
	public ClientRequest(String request) {
		this.request = request;
	}

	public ClientRequest(Socket client, String request) {
		this.client = client;
		this.request = request;
	}

	public String getRequest() {
		return request;
	}

	public Socket getClient() {
		return client;
	}
}