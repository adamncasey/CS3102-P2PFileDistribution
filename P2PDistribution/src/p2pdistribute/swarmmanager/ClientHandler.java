package p2pdistribute.swarmmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import p2pdistribute.common.Peer;
import p2pdistribute.common.message.MessageParserUtils;
import p2pdistribute.common.message.SwarmManagerMessageParser;
import p2pdistribute.common.message.SwarmManagerMessage;
import p2pdistribute.common.p2pmeta.ParserException;

/**
 * Main Client handling code of the Swarm Manager program.
 */
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
	
	/**
	 * Will attempt to read and process a message from the client socket 
	 * until the socket errors or closes.
	 */
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
		
		SwarmManagerMessage msg = SwarmManagerMessageParser.parseSwarmManageMessage(line);
		
		if(msg.cmd.equals("register")) {
			registerClient(msg);
		} else if(msg.cmd.equals("request_peers")) {
			sendPeerList(msg.metaHash);
		}
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
		
		writer.print(MessageParserUtils.serialiseMessageAsJSON(msg));
		writer.flush();
		System.out.println("Sent peer list to client");
	}

	private void registerClient(SwarmManagerMessage msg) {
		index.registerClient(client.getInetAddress(), msg.getPort(), msg.metaHash);
		System.out.println("Registered client with SwarmIndex");
	}
}
