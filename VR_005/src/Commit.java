public class Commit extends Message {

	private static final long serialVersionUID = 3845053914004694841L;
	private int viewNumber;
	private int commitNumber;

	public Commit(TypeMessage typeMessage, int viewNumber, int commitNumber) {
		super(typeMessage);
		this.viewNumber = viewNumber;
		this.commitNumber = commitNumber;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public int getCommitNumber() {
		return commitNumber;
	}

	@Override
	public String toString() {
		return "Commit [viewNumber=" + viewNumber + ", commitNumber=" + commitNumber + "]";
	}
}