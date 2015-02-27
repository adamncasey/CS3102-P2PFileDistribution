package p2pdistribute.client;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.common.Peer;

public class ActiveConnectionManager {
	private PeerConnectionListener listener;
	private Thread listenThread;

	private List<PeerConnection> connections;

	public ActiveConnectionManager(FileManager fileManager) throws IOException {
		listener = new PeerConnectionListener(this, fileManager);
		
		listenThread = new Thread(listener);
		listenThread.start();
		
		connections = new LinkedList<>();
	}
	
	public synchronized void addPeer(PeerConnection peer) {
		System.out.println("Added Peer: " + peer.sock.getInetAddress() + ":" + peer.sock.getLocalPort());
		connections.add(peer);
		
	}
	
	public synchronized boolean contains(Peer peer) {
		
		for(PeerConnection conn : connections) {
			if(conn.peer.equals(peer)) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getPort() {
		return listener.getPort();
	}
	
	public int getNumPeers() {
		return connections.size();
	}
	
	public synchronized void pruneConnections() {
		Iterator<PeerConnection> iter = connections.iterator();
		while(iter.hasNext()) {
			PeerConnection conn = iter.next();
			if(!conn.readThread.isAlive()) {
				conn.stop();
				
				iter.remove();
			}
		}
	}
	
	public synchronized void stop() {
		listener.stop();
		
		for(PeerConnection conn : connections) {
			conn.stop();
		}
	}
	
	public synchronized boolean complete() {

		for(PeerConnection conn : connections) {
			if(!conn.peerComplete()) {
				return false;
			}
		}
		
		
		return true;
	}
}
