package p2pdistribute.swarmmanager.message;

import java.util.List;

import p2pdistribute.message.Message;
import p2pdistribute.swarmmanager.Peer;

public class SwarmManagerMessage extends Message {
	
	private Integer port;
	private List<Peer> peers;
	
	public SwarmManagerMessage(String cmd, String metaHash) {
		super(cmd, metaHash);
	}
	
	public SwarmManagerMessage(String cmd, String metaHash, int port) {
		this(cmd, metaHash);
		
		this.port = port;
	}
	
	public SwarmManagerMessage(String cmd, String metaHash, List<Peer> peers) {
		this(cmd, metaHash);
		
		this.peers = peers;
	}
	
	public List<Peer> getPeers() {
		if(peers == null) {
			throw new RuntimeException("getPeers called on an invalid message");	
		}
		
		return peers;
	}
	public int getPort() {
		if(port == null) {
			throw new RuntimeException("getPort called on an invalid message");	
		}
		
		return port;
	}
}
