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
		System.out
				.println("Primary timed out! Sending STARTVIEWCHANGE message to all replicas...");
		List<String> configuration = new Configuration().getReplicas();
		for (int i = 0; i < configuration.size(); i++)
			if (configuration.get(0).equals("127.0.0.1"))
				sr.send(svc, "127.0.0.1",
						new Configuration().getReplicasPort().get(i));
			else
				sr.send(svc, configuration.get(i),
						new Configuration().getReplicasPort().get(0));
	}

}
