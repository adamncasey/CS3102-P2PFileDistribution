package p2pdistribute.client;

import java.util.List;

import p2pdistribute.common.Peer;


public class PeerManager {
	
	List<Peer> peers;
	List<PeerConnection> connections;
	
	FileManager fileManager;
	
	SwarmManagerConnection smConn;
	
	public final int PEER_CAP = 10; // TODO: Tweak / settings file?
	
	public PeerManager(String swarmManagerHostname, FileManager fileManager) {
		throw new RuntimeException("Not implemented");	// TODO Implement PeerManager constructor
	}

	public boolean run() {
		pruneConnections();
		
		updatePeerList();
		
		if(fileManager.numChunksNotStarted() > 0 && connections.size() < PEER_CAP) {
			// TODO Choose a peer to connect to
			Peer selectedPeer = selectNewPeer(peers);
			
			// TODO Connect to peer
			PeerConnection peerConn = connectToPeer(selectedPeer);
			if(peerConn != null) {
				connections.add(peerConn);
			}
		}
		
		return true;
	}
	
	private void pruneConnections() {
		// TODO Go through connections, see if any are complete or broken and remove them from list.
		
	}

	public void updatePeerList() {
		smConn.connect();
		
		peers = smConn.getPeerList();
				
		smConn.disconnect();
	}
	
	private Peer selectNewPeer(List<Peer> peers) {
		throw new RuntimeException("Not implemented");	// TODO Implement selectNewPeer
	}
	
	private PeerConnection connectToPeer(Peer peer) {

		throw new RuntimeException("Not implemented");	// TODO Implement connectToPeer
	}

	public void waitForPeers() {
		// TODO Auto-generated method stub
		
	}

}
