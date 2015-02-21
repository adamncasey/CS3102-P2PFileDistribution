package p2pdistribute.client;

import java.util.LinkedList;
import java.util.List;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.common.Peer;


public class PeerManager {
	
	List<Peer> peers;
	List<PeerConnection> connections;
	
	FileManager fileManager;
	
	SwarmManagerConnection smConn;
	
	public final int PEER_CAP = 10; // TODO: Tweak / settings file?
	
	public PeerManager(String swarmManagerHostname, FileManager fileManager) {
		
		smConn = new SwarmManagerConnection(swarmManagerHostname);
		this.fileManager = fileManager;
		
		connections = new LinkedList<>();
	}

	public boolean run() {
		pruneConnections();
		
		updatePeerList();
		
		if(fileManager.numChunksNotStarted() > 0 && connections.size() < PEER_CAP) {
			Peer selectedPeer = selectNewPeer(peers);
			
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
		// TODO waitForPeers
		
		// Close ServerSocket
		
		// 
		
		// Wait for all clients to finish downloading
		
	}

}
