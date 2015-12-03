import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
	private List<String> replicas;
	private int clientPort;
	private int replicaPort;

	/*
	 * Inicializa as variaveis com os valores retornados do ficheiro
	 * Configuration.txt
	 */
	public Configuration() {
		replicas = new ArrayList<>();
		try {
			BufferedReader file = new BufferedReader((new FileReader(
					"Configuration.txt")));
			String line;
			while ((line = file.readLine()) != null) {
				if (line.equals("SERVERS")) {
					while (!(line = file.readLine()).equals("SERVERS_PORT")) {
						if (!line.isEmpty()) {
							replicas.add(line);
						}
					}
				}
				if (line.equals("SERVERS_PORT")) {
					line = file.readLine();
					replicaPort = Integer.parseInt(line);
				}
//				if (line.equals("CLIENT_PORT")) {
//					line = file.readLine();
//					clientPort = Integer.parseInt(line);
//				}
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Retorna todas as replicas do sistema
	 */
	public List<String> getReplicas() {
		return replicas;
	}

	/*
	 * Retorna o porto dos clientes
	 */
	public int getClientsPort() {
		return clientPort;
	}

	/*
	 * Retorna o porto das replicas
	 */
	public int getReplicasPort() {
		return replicaPort;
	}
}