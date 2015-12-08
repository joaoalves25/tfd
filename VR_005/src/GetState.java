public class GetState extends Message {

	private static final long serialVersionUID = 6874321241127241767L;
	private int viewNumber;
	private int opNumber;
	private int replicaNumber;

	public GetState(TypeMessage typemsg, int viewNumber, int opNumber, int replicaNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.opNumber = opNumber;
		this.replicaNumber = replicaNumber;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}

	@Override
	public String toString() {
		return "GetState [viewNumber=" + viewNumber + ", opNumber=" + opNumber + ", replicaNumber=" + replicaNumber
				+ "]";
	}

}