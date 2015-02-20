package p2pdistribute.swarmmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import p2pdistribute.p2pmeta.ParserException;
import p2pdistribute.message.MessageParser;
import p2pdistribute.swarmmanager.message.SwarmManagerMessage;

public class ClientHandler implements Runnable {

	private Socket client;
	private BufferedReader reader;
	private PrintWriter writer;
	
	SwarmIndex index;
	
	public ClientHandler(Socket client, SwarmIndex index) throws IOException {
		this.client = client;
		this.index = index;
		
		reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		writer = new PrintWriter(client.getOutputStream());
	}
	
	@Override
	public void run() {
		while(!client.isClosed()) {
			readMessage();
		}
	}

	public void readMessage() {
		try {
			String line = reader.readLine();
			
			if(line == null) {
				SwarmManagerMain.close(client);
				return;
			}
			
			handleMessage(line);
			
		} catch (IOException | ParserException e) {
			System.out.println("Error when reading from client: " + e.getMessage());
			SwarmManagerMain.close(client);
		}
	}

	private void handleMessage(String line) throws ParserException {
		
		SwarmManagerMessage msg = MessageParser.parseSwarmManageMessage(line);
		
		if(msg.cmd.equals("register")) {
			registerClient(msg);
		} else if(msg.cmd.equals("request_peers")) {
			sendPeerList(msg.metaHash);
		}
		
		// TODO OPTIONAL - "unregister" (p2pmeta hash): remove client from list associated with that hash
	}

	private void sendPeerList(String metaHash) {
		
		List<Peer> peers = index.get(metaHash);
		Peer[] peersArray;
		
		if(peers == null) {
			peersArray = new Peer[0];
		} else {
			peersArray = peers.toArray(new Peer[peers.size()]);
			
		}
		
		SwarmManagerMessage msg = new SwarmManagerMessage("peers", metaHash, peersArray);
		
		writer.print(MessageParser.serialiseMessage(msg));
		writer.flush();
		System.out.println("Sent peer list to client");
	}

	private void registerClient(SwarmManagerMessage msg) {
		index.registerClient(client.getInetAddress(), msg.getPort(), msg.metaHash);
		System.out.println("Registered client with SwarmIndex");
	}

}
