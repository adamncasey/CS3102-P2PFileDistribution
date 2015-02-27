package p2pdistribute.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.common.Peer;

/**
 * Manages P2P connections within Peer
 * 	- Decides which peers to connect to (Random selection algorithm)
 *  	- Will not choose to actively make the connection to any peer if we are complete
 *  	- Incomplete peers can connect to us. 
 *
 */
public class PeerManager {

	public final int MAX_PEERS = 10; // TODO Future Task: Settings file
	
	Peer[] peers;
	
	FileManager fileManager;
	SwarmManagerConnection smConn;
	ActiveConnectionManager connManager;
	
	Random random;
	
	
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
		
		random = new Random();
	}

	/**
	 * Registers with the Swarm Manager, updates the peer list
	 * and selects a peer to connect to if we haven't hit MAX_PEERS yet
	 * 
	 * @throws PeerManagerException thrown on connection problem with Swarm Manager.
	 */
	public void run() throws PeerManagerException {
		pruneConnections();
		
		// TODO Future Task: Move SM registering to a different thread.
		registerWithSwarmManager();
		
		updatePeerList();
		
		if((!fileManager.complete()) && (connManager.getNumPeers() < MAX_PEERS)) {
			Peer selectedPeer = selectNewPeer(peers);
			
			if(selectedPeer != null) {

				PeerConnection peerConn = connectToPeer(selectedPeer);
				if(peerConn != null) {
					connManager.addPeer(peerConn);
				} else {
					// Don't connect to this peer again for a while
					removePeer(selectedPeer);
				}
			}
		}
	}

	public void waitForPeers() {
		connManager.stop();
	}

	public boolean complete() {
		return connManager.complete();
	}

	private void updatePeerList() throws PeerManagerException {
		
		Peer[] peers;
		
		try {
			peers = smConn.getPeerList(fileManager.metadata.metaHash);
		} catch (IOException e) {
			throw new PeerManagerException("Communication Error with Swarm Manager: " + e.getMessage());
		}
		
		this.peers = peers;
	}

	private void removePeer(Peer selectedPeer) {
		
		Peer[] newPeers = new Peer[this.peers.length - 1];
		
		int i=0;
		
		for(Peer peer : this.peers) {
			if(peer.equals(selectedPeer)) {
				continue;
			}
			newPeers[i++] = peer;
		}
		
		this.peers = newPeers;
	}

	private void pruneConnections() {
		connManager.pruneConnections();
	}
	
	private void registerWithSwarmManager() throws PeerManagerException {
		try {
			smConn.register(fileManager.metadata.metaHash, connManager.getPort());
		} catch (IOException e) {
			throw new PeerManagerException("Communication Error with Swarm Manager: " + e.getMessage());
		}
	}
	
	private Peer selectNewPeer(Peer[] peers) {
		// Choose a peer we aren't already connected to.
		List<Peer> unconnectedPeers = new ArrayList<>(peers.length);
		for(Peer peer : peers) {
			if(!connManager.contains(peer)) {
				unconnectedPeers.add(peer);
			}
		}
		
		if(unconnectedPeers.size() == 0) {
			return null;
		}
		
		int index = random.nextInt(unconnectedPeers.size());
		
		return unconnectedPeers.get(index);
	}
	
	private PeerConnection connectToPeer(Peer peer) {
		try {
			PeerConnection conn = new PeerConnection(peer, fileManager);
			return conn;
		} catch(IOException e) {
			System.out.println("Could not connect to peer(" + peer.address.toString() + ":" + peer.port + "): " + e.getMessage());
			// Unable to connect to peer.
			return null;
		}
	}
}
