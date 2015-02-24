package p2pdistribute.client;

import java.io.IOException;
import java.net.UnknownHostException;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.common.Peer;


public class PeerManager {
	
	Peer[] peers;
	
	FileManager fileManager;
	SwarmManagerConnection smConn;
	
	ActiveConnectionManager connManager;
	
	public final int MAX_PEERS = 10; // TODO: Tweak / settings file?
	
	public PeerManager(String swarmManagerHostname, int port, FileManager fileManager) throws PeerManagerException {
		
		try {
			smConn = new SwarmManagerConnection(swarmManagerHostname, port);
		} catch (UnknownHostException e) {
			throw new PeerManagerException("Unable to resolve Swarm Manager Hostname: " + e.getMessage());
		}
		this.fileManager = fileManager;
		
		
		try {
			connManager = new ActiveConnectionManager(fileManager);
		} catch (IOException e) {
			throw new PeerManagerException("Error occured initialising ConectionManager: " + e.getMessage());
		}
	}

	public boolean run() throws PeerManagerException {
		pruneConnections();
		
		registerWithSwarmManager();
		
		updatePeerList();
		
		if((!fileManager.complete()) && (connManager.getNumPeers() < MAX_PEERS)) {
			Peer selectedPeer = selectNewPeer(peers);
			
			if(selectedPeer != null) {

				PeerConnection peerConn = connectToPeer(selectedPeer);
				if(peerConn != null) {
					connManager.addPeer(peerConn);
				}
			}
			
		}
		
		return true;
	}

	private void pruneConnections() {
		// TODO Go through connections, see if any are complete or broken and remove them from list.
		
	}
	
	private void registerWithSwarmManager() throws PeerManagerException {
		try {
			smConn.register(fileManager.metadata.metaHash, connManager.getPort());
		} catch (IOException e) {
			throw new PeerManagerException("Communication Error with Swarm Manager: " + e.getMessage());
		}
	}

	public void updatePeerList() throws PeerManagerException {
		
		Peer[] peers;
		
		try {
			peers = smConn.getPeerList(fileManager.metadata.metaHash);
		} catch (IOException e) {
			throw new PeerManagerException("Communication Error with Swarm Manager: " + e.getMessage());
		}
		
		// Take us out of this list.
	}
	
	private Peer selectNewPeer(Peer[] peers) {
		// Choose a peer we aren't already connected to.
		
		for(Peer peer : peers) {
			if(!connManager.contains(peer)) {
				return peer;
			}
		}
		
		return null;
	}
	
	private PeerConnection connectToPeer(Peer peer) {
		try {
			PeerConnection conn = new PeerConnection(peer, fileManager);
			return conn;
		} catch(IOException e) {
			System.out.println("Could not connect to peer: " + e.getMessage());
			// Unable to connect to peer.
			return null;
		}
	}

	public void waitForPeers() {
		connManager.stop();
		
		// connManager.wait() ?
	}

	public boolean complete() {
		// Peers disconnect when both sides are complete.
		
		// Swarm likely to be complete if we have no peers
		return connManager.getNumPeers() == 0;
	}

}
