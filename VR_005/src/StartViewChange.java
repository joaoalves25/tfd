public class StartViewChange extends Message {

	private static final long serialVersionUID = 3545687018917454252L;
	private int viewNumber;
	private int replicaNumber;

	public StartViewChange(TypeMessage typemsg, int viewNumber, int replicaNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.replicaNumber = replicaNumber;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}
	
	@Override
	public String toString() {
		return "StartViewChange [viewNumber=" + viewNumber + ", replicaNumber=" + replicaNumber + "]";
	}
}