public class PrepareOK extends Message {
	private static final long serialVersionUID = 1758496722307924811L;
	private int viewNumber;
	private int opNumber;
	private int replicaNumber;

	public PrepareOK(TypeMessage typemsg, int viewNumber, int opNumber,
			int replicaNumber) {
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

	public int replicaNumber() {
		return replicaNumber;
	}
}