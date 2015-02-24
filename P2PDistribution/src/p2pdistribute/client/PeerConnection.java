package p2pdistribute.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map.Entry;

import p2pdistribute.client.filemanager.AcquisitionStatus;
import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.client.filemanager.Status;
import p2pdistribute.client.message.AdvertiseJSONMessage;
import p2pdistribute.client.message.ControlMessage;
import p2pdistribute.client.message.DataMessage;
import p2pdistribute.client.message.Message;
import p2pdistribute.client.message.MessageType;
import p2pdistribute.client.message.P2PMessageParser;
import p2pdistribute.client.message.RequestChunkJSONMessage;
import p2pdistribute.common.Peer;
import p2pdistribute.common.p2pmeta.ParserException;

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
		// Advertise chunks!
		
		
		while(!stopRequested()) {
			Message msg;
			try {
				msg = P2PMessageParser.readMessage(sock.getInputStream());
			} catch (IOException e) {
				System.out.println("Error getting input stream from peer socket");
				break;
			} catch (ParserException e) {
				System.out.println("Peer sent malformed message. Probably not recoverable...");
				break;
			}
			
			handleMessage(msg);
		}
		
		// writeThread.join();
		
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("IOException on peer socket close");
		}
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
		
		if(msg.type == MessageType.CONTROL) {
			handleControlMessage((ControlMessage)msg);
			
		} else if(msg.type == MessageType.DATA) {
			handleDataMessage((DataMessage)msg);
		}
	}
	
	private void handleDataMessage(DataMessage msg) {
		try {
			localFiles.setChunkData(msg.fileid, msg.chunkid, msg.data);
		} catch (IOException e) {
			System.out.println("Error writing fileid/chunkid: " + msg.fileid + "/" + msg.chunkid);
			System.out.println(e.getMessage());
			return;
		}
	}

	private void handleControlMessage(ControlMessage msg) {
		if(msg.payload.cmd.equals("advertise_chunk")) {
			AdvertiseJSONMessage message = (AdvertiseJSONMessage) msg.payload;

			for(Entry<Integer, Integer> value : message.chunksComplete.entrySet()) {
				peerStatus.setStatus(value.getKey(), value.getValue(), Status.COMPLETE);
			}
			
			// Pick a chunk we don't have but they have, then request it!
			
			
		} else if(msg.payload.cmd.equals("request_chunk")) {
			RequestChunkJSONMessage message = (RequestChunkJSONMessage) msg.payload;
			 // Send chunk... blocking? Why not? Well.. advertising new chunks asynchronously will not for one reason. 
			System.out.println("Chunk requested: " + message.fileid + "/" + message.chunkid + ". No chance yet.");
		}
		
	}

	public void advertiseChunk(int fileid, int chunkid) {
		// Should send advertise chunk message out to peer
	}

}
