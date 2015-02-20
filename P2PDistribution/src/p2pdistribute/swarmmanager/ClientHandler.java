package p2pdistribute.swarmmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import p2pdistribute.p2pmeta.ParserException;
import p2pdistribute.message.MessageParser;
import p2pdistribute.swarmmanager.message.SwarmManagerMessage;

public class ClientHandler implements Runnable {

	private Socket client;
	private BufferedReader reader;
	
	SwarmIndex index;
	
	public ClientHandler(Socket client, SwarmIndex index) throws IOException {
		this.client = client;
		this.index = index;
		
		reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}
	
	@Override
	public void run() {
		while(!client.isClosed()) {
			try {
				String line = reader.readLine();
				
				handleMessage(line);
				
			} catch (IOException | ParserException e) {
				System.out.println("Error when reading from client: " + e.getMessage());
				SwarmManager.close(client);
			}
		}
	}

	private void handleMessage(String line) throws ParserException {
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		if(msg.cmd.equals("register")) {
			registerClient(msg.metaHash);
		} else if(msg.cmd.equals("request_peers")) {
			sendPeerList(msg.metaHash);
		}
		
		// TODO OPTIONAL - "unregister" (p2pmeta hash): remove client from list associated with that hash
	}

	private void sendPeerList(String metaHash) {
		// TODO Auto-generated method stub

		// "request_peers" (p2pmeta hash): Send list of peers registered to that hash
			// Sends back a "peers"
	}

	private void registerClient(String hash) {
		// TODO Tidy up comment
		// "register" (p2pmeta hash): (re)add client to list associated with that hash with timestamp of register event
		index.registerClient(client.getInetAddress(), client.getPort(), hash);
	}

}
