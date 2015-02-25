package p2pdistribute.common.message;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import p2pdistribute.common.Peer;

public class SwarmManagerMessage extends JSONMessage {
	
	private final Integer port;
	private final Peer[] peers;
	
	public SwarmManagerMessage(String cmd, String metaHash) {
		super(cmd, metaHash);
		
		port = null;
		peers = null;
	}
	
	public SwarmManagerMessage(String cmd, String metaHash, int port) {
		super(cmd, metaHash);
		
		this.port = port;
		this.peers = null;
	}
	
	public SwarmManagerMessage(String cmd, String metaHash, Peer[] peers) {
		super(cmd, metaHash);
		
		this.peers = peers;
		this.port = null;
	}
	
	@Override
	public Map<String, Object> getJSON() {
		Map<String, Object> map = super.getJSON();

		if(this.port != null) {
			map.put("port", port);
		}
		
		if(this.peers != null) {
			List<List<Object>> jsonPeers = new LinkedList<>();
			
			for(Peer peer : peers) {
				List<Object> peerDetails = new LinkedList<>();
				peerDetails.add(peer.address.getHostAddress());
				peerDetails.add(peer.port);
				
				jsonPeers.add(peerDetails);
			}
			
			map.put("peers", jsonPeers);
		}
		
		return map;
	}
	
	public Peer[] getPeers() {
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

	
	/** 
	 * Eclipse generated hashCode and equals
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(peers);
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwarmManagerMessage other = (SwarmManagerMessage) obj;
		if (!Arrays.equals(peers, other.peers))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}
	
	
}
