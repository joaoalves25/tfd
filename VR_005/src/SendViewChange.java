import java.util.List;
import java.util.TimerTask;

public class SendViewChange extends TimerTask {

	private StartViewChange svc;
	private SendAndReceive sr;

	public SendViewChange(StartViewChange svc, SendAndReceive sr) {
		this.svc = svc;
		this.sr = sr;
	}

	@Override
	public void run() {
		System.out.println("Primary timed out! Sending SENDVIEWCHANGE message to all backups...");
		Configuration configuration = new Configuration();
		List<String> replicasList = configuration.getReplicas();
		List<Integer> replicasPort = configuration.getReplicasPort();
		for (int i = 0; i < replicasList.size(); i++)
			sr.send(svc, replicasList.get(i), replicasPort.get(i));
	}
}