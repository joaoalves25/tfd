/**
 * 
 */

/**
 * @author group 005
 * StartViewChange 
 */
public class StartViewChange extends Message {

	
	private static final long serialVersionUID = 3545687018917454252L;
	private int viewNumber;
	private int replicaNumber;
	
	/**
	 * Method that creates a new StartViewChange message
	 * @requires TypeMessage typemsg == MesStartViewChange
	 * @param v - the new view number.
	 * @param i - replica numbers 
	 * @param typemsg - type of the message, in this case is a StartViewChange message.
	 */
	public StartViewChange(int v, int i,TypeMessage typemsg) {
		super(typemsg);
		this.viewNumber = v;
		this.replicaNumber = i;
	}

	/**
	 * Method that returns the view number
	 * @return viewNumber value (int)
	 */
	public int getViewNumber() {
		return viewNumber;
	}

	/**
	 * Method that returns the replicas number
	 * @return replicaNumber value (int)
	 */
	public int getReplicaNumber() {
		return replicaNumber;
	}

}
