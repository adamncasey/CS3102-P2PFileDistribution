package p2pdistribute.swarmmanager;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import p2pdistribute.common.Peer;

public class SwarmIndex  {
	
	private static final int REGISTER_TIMEOUT = 10000;
	private Map<String, List<PeerEntry>> peers;
	
	public SwarmIndex() {
		peers = new HashMap<>();
	}

	public synchronized void registerClient(InetAddress address, int port, String hash) {
		List<PeerEntry> hashPeers = peers.get(hash);
		
		if(hashPeers == null) {
			hashPeers = new LinkedList<>();
		}
		
		Peer newPeer = new Peer(address, port);
		
		if(removePeerAndPrune(hashPeers, newPeer)) {
			// Client re-registered
			return;
		}
		
		hashPeers.add(new PeerEntry(new Date(), newPeer));
		
		peers.put(hash, hashPeers);
	}
	
	public synchronized List<Peer> get(String hash) {
		
		List<PeerEntry> hashPeers = peers.get(hash);
		
		if(hashPeers == null) {
			return null;
		}
		
		List<Peer> peerList = new LinkedList<>();
		
		prune(hashPeers);
		
		for(PeerEntry entry : hashPeers) {
			peerList.add(entry.peer);
		}
		
		return peerList;
	}
	
	void prune(List<PeerEntry> haystack) {
		removePeerAndPrune(haystack, null);
	}
	
	boolean removePeerAndPrune(List<PeerEntry> haystack, Peer needle) {
		boolean found = false;
		
		Date current = new Date();
		
		Iterator<PeerEntry> iter = haystack.iterator();
		while(iter.hasNext()) {
			PeerEntry entry = iter.next();
			if((current.getTime() - entry.date.getTime()) > REGISTER_TIMEOUT) {
				iter.remove();
				System.out.println("Pruned peer from registered.");
				continue;
			}
			
			if(entry.peer.equals(needle)) {
				iter.remove();
			}
		}
		
		return found;
	}
	
}
