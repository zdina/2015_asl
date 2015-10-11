import java.net.InetAddress;

public class ClientProxy {

	private String request;
	private String response;
	
	private InetAddress address;
	private int port;

	public ClientProxy(InetAddress adress, int port, String request) {
		this.address = adress;
		this.port = port;
		this.request = request;
		response = null;
	}

	public String getRequest() {
		return request;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public String getResponse() {
		return response;
	}
}