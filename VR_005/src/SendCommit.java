import java.util.TimerTask;


public class SendCommit extends TimerTask {
	
	private Commit commit;
	private SendAndReceive sr;
	
	public SendCommit(Commit commit, SendAndReceive sr){
		this.commit = commit;
		this.sr = sr;
	}
	
	@Override
	public void run() {
		System.out.println("Client timed out! Sending COMMIT message to all backups...");
		Configuration config = new Configuration();
		for (int i = 1; i<config.getReplicas().size(); i++)
			sr.send(commit, config.getReplicas().get(i), config.getReplicasPort());
	}

}
