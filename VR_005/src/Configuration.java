import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
	private List<String> replicasList;
	private List<Integer> replicasPort;

	/*
	 * Inicializa as variaveis com os valores retornados do ficheiro Configuration.txt
	 */
	public Configuration() {
		replicasList = new ArrayList<>();
		replicasPort = new ArrayList<>();
	}

	/*
	 * Retorna todos os servidores do sistema
	 */
	public List<String> getReplicas() {
		try {
			BufferedReader file = new BufferedReader((new FileReader("Configuration.txt")));
			String line;
			while ((line = file.readLine()) != null && !line.equals("SERVERS_PORT")) {
				if (!line.equals("SERVERS"))
					replicasList.add(line);
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return replicasList;
	}

	/*
	 * Retorna o porto de cada servidor
	 */
	public List<Integer> getReplicasPort() {
		try {
			BufferedReader file = new BufferedReader((new FileReader("Configuration.txt")));
			String line;
			while ((line = file.readLine()) != null) {
				if (line.equals("SERVERS_PORT")) {
					while ((line = file.readLine()) != null) {
						replicasPort.add(Integer.parseInt(line));
					}
				}
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replicasPort;
	}
}