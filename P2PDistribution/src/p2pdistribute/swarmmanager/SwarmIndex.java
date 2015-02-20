package p2pdistribute.swarmmanager;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SwarmIndex extends HashMap<String, List<Peer>> {

	private static final long serialVersionUID = -7674745645581630801L;

	public synchronized void registerClient(InetAddress address, int port, String hash) {
		List<Peer> peers = this.get(hash);
		
		if(peers == null) {
			peers = new LinkedList<Peer>();
		}
		
		peers.add(new Peer(address, port));
		
		this.put(hash, peers);
	}
	
}
