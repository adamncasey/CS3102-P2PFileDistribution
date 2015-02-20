package p2pdistribute.swarmmanager;

import java.net.InetAddress;

public class Peer {
	public final InetAddress address;
	public final int port;
	
	public Peer(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
}
