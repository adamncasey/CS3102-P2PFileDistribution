package p2pdistribute.swarmmanager;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import p2pdistribute.common.Peer;

public class SwarmIndex extends HashMap<String, List<Peer>> {

	private static final long serialVersionUID = -7674745645581630801L;

	public synchronized void registerClient(InetAddress address, int port, String hash) {
		List<Peer> peers = this.get(hash);
		
		if(peers == null) {
			peers = new LinkedList<Peer>();
		}
		
		Peer newPeer = new Peer(address, port);
		
		if(peers.contains(newPeer)) {
			System.out.println("Pre-registered client registered");
			return;
		}
		
		peers.add(newPeer);
		
		this.put(hash, peers);
	}
	
}
