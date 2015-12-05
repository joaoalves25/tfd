import java.util.List;
import java.util.TimerTask;

public class SendCommit extends TimerTask {

	private Commit commit;
	private SendAndReceive sr;

	public SendCommit(Commit commit, SendAndReceive sr) {
		this.commit = commit;
		this.sr = sr;
	}

	@Override
	public void run() {
		System.out
				.println("Client timed out! Sending COMMIT message to all backups...");
		List<String> configuration = new Configuration().getReplicas();
		for (int i = 1; i < configuration.size(); i++)
			if (configuration.get(0).equals("127.0.0.1"))
				sr.send(commit, "127.0.0.1",
						new Configuration().getReplicasPort() + i);
			else
				sr.send(commit, configuration.get(i),
						new Configuration().getReplicasPort());
	}

}