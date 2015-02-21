package p2pdistribute.client;

import java.util.List;

import p2pdistribute.common.Peer;


public class SwarmManagerConnection {

	public SwarmManagerConnection(String swarmManagerHostname) {
		// TODO Auto-generated constructor stub
	}

	public void connect() {
		// TODO Write SM Connect
		
		// Open Socket to passed in hostname
	}
	
	public void register() {
		// TODO Write SM Register
		
		// Send register SwarmManager
	}
	
	public List<Peer> getPeerList() {
		// TODO Write SM request_peers
		
		// Send request_peers
		
		// receive peers
		
		// return list of peer
		
		return null;
	}

	public void disconnect() {
		// TODO write disconnect
		
		// close socket
	}
}
