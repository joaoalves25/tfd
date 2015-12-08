public class Request extends Message {

	private static final long serialVersionUID = -2182436451813567442L;
	private int requestNumber;
	private String operation;
	private String clientId;
	private boolean executed;

	public Request(String operation, String clientId, int requestNumber) {
		super(TypeMessage.REQUEST);
		this.requestNumber = requestNumber;
		this.operation = operation;
		this.clientId = clientId;
		this.executed = false;
	}

	public void setExecuted() {
		executed = true;
	}

	public int getRequestNumber() {
		return requestNumber;
	}

	public String getOperation() {
		return operation;
	}

	public String getClientId() {
		return clientId;
	}

	public boolean isExecuted() {
		return executed;
	}

	@Override
	public String toString() {
		return "Request [number=" + requestNumber + ", operation=" + operation + ", clientId=" + clientId
				+ ", executed=" + executed + "]";
	}

}