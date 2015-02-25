package p2pdistribute.swarmmanager;

import java.util.Date;

import p2pdistribute.common.Peer;

public class PeerEntry {
	Date date;
	Peer peer;
	
	public PeerEntry(Date date, Peer peer) {
		this.date = date;
		this.peer = peer;
	}
}
