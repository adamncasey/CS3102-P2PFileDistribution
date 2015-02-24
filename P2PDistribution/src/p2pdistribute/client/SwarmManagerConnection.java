package p2pdistribute.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Hex;

import p2pdistribute.common.Peer;
import p2pdistribute.common.message.SwarmManagerMessageParser;
import p2pdistribute.common.message.SwarmManagerMessage;
import p2pdistribute.common.p2pmeta.ParserException;


public class SwarmManagerConnection {

	public final InetAddress smAddress;
	public final int port;
	
	private Socket socket;
	private BufferedReader reader;
	private OutputStream out;
	
	public SwarmManagerConnection(String swarmManagerHostname, int port) throws UnknownHostException {
		this.port = port;
		
		this.smAddress = InetAddress.getByName(swarmManagerHostname);
		
		this.socket = null;
	}
	
	public void register(byte[] metaHash, int listenPort) throws IOException {
		
		connect();
		
		SwarmManagerMessage msg = new SwarmManagerMessage("register", Hex.encodeHexString(metaHash), listenPort); 
		sendMessage(msg);
		
		disconnect();
	}
	
	public Peer[] getPeerList(byte[] metaHash) throws IOException {
		connect();
		
		SwarmManagerMessage sentMsg = new SwarmManagerMessage("request_peers", Hex.encodeHexString(metaHash)); 
		sendMessage(sentMsg);
		
		SwarmManagerMessage msg;
		
		try {
			msg = receiveMessage();
		} catch (ParserException e) {
			throw new IOException("Received invalid message from SwarmManager: " + e.getMessage());
		}
		
		Peer[] peers = msg.getPeers();
		
		disconnect();
		
		return peers;
	}
	
	private void sendMessage(SwarmManagerMessage msg) throws IOException {
		String serialised = SwarmManagerMessageParser.serialiseMessageAsJSON(msg);
		
		try {
			out.write(serialised.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new IOException("Unable to send register command to SwarmManager: " + e.getMessage());
		}
	}
	
	private SwarmManagerMessage receiveMessage() throws IOException, ParserException {
		String line = reader.readLine();
		
		SwarmManagerMessage msg = SwarmManagerMessageParser.parseSwarmManageMessage(line);
		
		return msg;
	}

	private void connect() throws IOException {
		
		try {
			socket = new Socket(smAddress, port);
			
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch(IOException e) {
			throw new IOException("Unable to connect to SwarmManager: " + e.getMessage());
		}
	}

	private void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Cannot close socket. No idea");
		}
	}
}
