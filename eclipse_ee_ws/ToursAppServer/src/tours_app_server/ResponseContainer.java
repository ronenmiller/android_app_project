package tours_app_server;

public class ResponseContainer {
	
	private String resType;
	private String jsonResponse;

	ResponseContainer(String responseType) {
		this.resType = responseType;
	}
	
	public String getType() {
		return resType;
	}
	
	public void setResponse(String jsonResponse) {
		this.jsonResponse = jsonResponse;
	}
	
	public String getResponse() {
		return jsonResponse;
	}
}
