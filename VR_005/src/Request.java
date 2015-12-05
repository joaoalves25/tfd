public class Request extends Message {

	private static final long serialVersionUID = -2182436451813567442L;
	private int number;
	private String operation;
	private String clientId;
	private boolean executed;

	public Request(String operation, String clientId, int number) {
		super(TypeMessage.REQUEST);
		this.number = number;
		this.operation = operation;
		this.clientId = clientId;
		this.executed = false;
	}

	public void setExecuted() {
		executed = true;
	}

	public int getNumber() {
		return number;
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
		return "Message Type = REQUEST\nOperation = " + operation
				+ "\nClient ID = " + clientId + "\nRequest Number = "
				+ Integer.toString(number)+"\n"+
				"Executed = "+executed;
	}
}
