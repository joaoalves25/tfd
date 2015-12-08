import java.util.Map;

public class NewState extends Message {

	private static final long serialVersionUID = -7873977142883743868L;

	private int viewNumber;
	private Map<Integer, Request> log;
	private int opNumber;
	private int commitNumber;

	public NewState(TypeMessage typemsg, int viewNumber, Map<Integer, Request> log, int opNumber, int commitNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.log = log;
		this.opNumber = opNumber;
		this.commitNumber = commitNumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public int getCommitNumber() {
		return commitNumber;
	}

	@Override
	public String toString() {
		return "NewState [viewNumber=" + viewNumber + ", log=" + log + ", opNumber=" + opNumber + ", commitNumber="
				+ commitNumber + "]";
	}

}