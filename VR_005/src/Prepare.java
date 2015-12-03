public class Prepare extends Message {

	private static final long serialVersionUID = -7922007062226612933L;

	private int viewNumber;
	private Request request;
	private int opNumber;
	private int commitNumber;

	public Prepare(TypeMessage typemsg, int viewNumber, Request request,
			int opNumber, int commitNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.request = request;
		this.opNumber = opNumber;
		this.commitNumber = commitNumber;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public Request getRequest() {
		return request;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public int getCommitNumber() {
		return commitNumber;
	}
}