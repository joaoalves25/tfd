import java.util.Map;

public class DoViewChange extends Message {

	private static final long serialVersionUID = 7708642366712950582L;
	private int viewNumber;
	private Map<Integer, Request> log;
	private int viewNumberNormal;
	private int opNumber;
	private int commitNumber;
	private int replicaNumber;

	public DoViewChange(TypeMessage typemsg, int viewNumber, Map<Integer, Request> log, int viewNumberNormal, int opNumber,
			int commitNumber, int replicaNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.log = log;
		this.viewNumberNormal = viewNumberNormal;
		this.opNumber = opNumber;
		this.commitNumber = commitNumber;
		this.replicaNumber = replicaNumber;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public int getViewNumberNormal() {
		return viewNumberNormal;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public int getCommitNumber() {
		return commitNumber;
	}

	@Override
	public String toString() {
		return "DoViewChange [viewNumber=" + viewNumber + ", log=" + log + ", viewNumberNormal=" + viewNumberNormal
				+ ", opNumber=" + opNumber + ", commitNumber=" + commitNumber + ", replicaNumber=" + replicaNumber + "]";
	}
	
}