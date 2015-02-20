package p2pdistribute.swarmmanager;

import java.net.InetAddress;

public class Peer {
	public final InetAddress address;
	public final int port;
	
	public Peer(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	/** 
	 * Eclipse generated hashCode and equals
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Peer other = (Peer) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	
}
