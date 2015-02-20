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
		assert peers != null;
		
		return peers;
	}
	public int getPort() {
		assert port != null;
		
		return port;
	}
}
