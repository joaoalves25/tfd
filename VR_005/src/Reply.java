public class Reply extends Message {

	private static final long serialVersionUID = 2857020407562645166L;
	private int viewNumber;
	private int requestNumber;
	private String operation;

	public Reply(TypeMessage typemsg, int viewNumber, int requestNumber,
			String operation) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.requestNumber = requestNumber;
		this.operation = operation;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public int getRequestNumber() {
		return requestNumber;
	}

	public String getOperation() {
		return operation;
	}
}