package p2pdistribute.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import p2pdistribute.client.filemanager.AcquisitionStatus;
import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.client.message.P2PMessageParser;
import p2pdistribute.common.Peer;
import p2pdistribute.common.message.Message;

public class PeerConnection implements Runnable {
	
	public static final int PEER_CONNECT_TIMEOUT = 5000;
	
	public final Peer peer;
	private AcquisitionStatus peerStatus;
	
	private FileManager localFiles; 
	
	Socket sock;
	Thread readThread;

	private boolean shouldStop;

	public PeerConnection(Socket client, FileManager fileManager) {
		sock = client;
		
		this.peer = new Peer(client.getInetAddress(), client.getLocalPort());
		
		// receive register message?
		
		initialise(fileManager);
	}

	public PeerConnection(Peer peer, FileManager fileManager) throws IOException {
		sock = new Socket();
		this.peer = peer;
		
		sock.connect(new InetSocketAddress(peer.address, peer.port), PEER_CONNECT_TIMEOUT);
		
		initialise(fileManager);
	}
	
	private void initialise(FileManager fileManager) {
		
		localFiles = fileManager;
		
		peerStatus = new AcquisitionStatus(localFiles.numFiles(), localFiles.numChunks());
		
		shouldStop = false;
		
		// Then create new thread for reading from socket.
		readThread = new Thread(this);
		readThread.start();
	}

	@Override
	public void run() {
		while(!stopRequested()) {
			Message msg;
			try {
				msg = P2PMessageParser.readMessage(sock.getInputStream());
			} catch (IOException e) {
				System.out.println("Error getting input streamn from peer socket");
				break;
			}
			
			handleMessage(msg);
		}
		
		// writeThread.join();
		
		// sock.close();
	}
	
	public synchronized void stop() {
		this.shouldStop = true;
		
		try {
			this.sock.shutdownInput();
		} catch (IOException e) {
			// TODO Handle exception?
		}
		
		// TODO tell writer thread to stop on completion.
	}
	
	private synchronized boolean stopRequested() {
		return shouldStop;
	}
	
	private void handleMessage(Message msg) {
		// if advertise_chunk:
			// update known file/chunk statuses for peer
		
		// if request_chunk:
			// send?
		
		// if chunk data:
			// pass it to FileManager
	}
	
	public void advertiseChunk(int fileid, int chunkid) {
		
	}

}
