package p2pdistribute.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

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
	
	private int currentFileID;
	private int currentChunkID;
	
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
		currentFileID = currentChunkID = -1;
		
		peerStatus = new AcquisitionStatus(localFiles.numFiles());
		
		shouldStop = false;
		
		// Then create new thread for reading from socket.
		readThread = new Thread(this);
		readThread.start();
	}

	@Override
	public void run() {
		// Advertise chunks!
		try {
			advertiseChunks();
		} catch (IOException e1) {
			System.err.println("Error occured when advertising chunks");
			stop();
		}
		
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
			try {
				handleMessage(msg);
			} catch(IOException e) {
				System.out.println("Could not send reply to message");
				break;
			}
		}
		
		// writeThread.join();
		
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("IOException on peer socket close");
		}
		
		tidyIncompleteChunk();
	}
	
	private void tidyIncompleteChunk() {
		if(currentFileID != -1) {
			localFiles.status.setStatus(currentFileID, currentChunkID, Status.INCOMPLETE);
		}
	}

	private void advertiseChunks() throws IOException {
		// Get all the chunks we have from localFiles
		int[][] completeChunks = localFiles.status.getCompleteFileChunkIDs();
		
		// Make a AdvertiseJSONMessage using this data
		AdvertiseJSONMessage payload = new AdvertiseJSONMessage(completeChunks, localFiles.metadata.metaHash);
		
		byte[] messageBytes = P2PMessageParser.serialiseJSONMessage(payload);
		
		// Send
		System.out.println("Writing chunks to stream: " + new String(messageBytes));
		sock.getOutputStream().write(messageBytes);
	}

	public synchronized void stop() {
		this.shouldStop = true;
		
		try {
			this.sock.shutdownInput();
		} catch (IOException e) {
			// Means there is data in the buffer but the connection has terminated.
			// Nothing to be done here really.
		}
		
		// TODO tell writer thread to stop on completion.
	}
	
	private synchronized boolean stopRequested() {
		return shouldStop;
	}
	
	private void handleMessage(Message msg) throws IOException {
		System.out.println("handleMessage " + msg.type);
		
		if(msg.type == MessageType.CONTROL) {
			handleControlMessage((ControlMessage)msg);
			
		} else if(msg.type == MessageType.DATA) {
			handleDataMessage((DataMessage)msg);
		} else {
			System.err.println("Received unknown MessageType: " + msg.type);
		}
	}
	
	private void handleDataMessage(DataMessage msg) throws IOException {
		localFiles.setChunkData(msg.fileid, msg.chunkid, msg.data);
		currentFileID = currentChunkID = 0;
		
		// Ask for a new chunk once this is done.
		requestChunk();
		
	}

	private void handleControlMessage(ControlMessage msg) throws IOException {
		if(msg.payload.cmd.equals("advertise_chunks")) {
			AdvertiseJSONMessage message = (AdvertiseJSONMessage) msg.payload;

			updatePeerStatus(message.chunksComplete);
			
			requestChunk();
			
		} else if(msg.payload.cmd.equals("request_chunk")) {
			RequestChunkJSONMessage message = (RequestChunkJSONMessage) msg.payload;
			 // Send chunk... blocking? Why not? Well.. advertising new chunks asynchronously will not for one reason. 
			System.out.println("Chunk requested: " + message.fileid + "/" + message.chunkid);
			
			// TODO Verify fileid / chunkid?
			byte[] data = localFiles.getChunkData(message.fileid, message.chunkid);
			
			byte[] messageData = P2PMessageParser.serialiseData(data, localFiles.metadata.metaHash, message.fileid, message.chunkid);
			sock.getOutputStream().write(messageData);
		}
		
	}

	private void updatePeerStatus(List<List<Integer>> chunksComplete) {
		for(List<Integer> value : chunksComplete) {
			if(value.size() < 2) {
				System.out.println("Received badly formed message from peer");
				return;
			}
			peerStatus.setStatus(value.get(0), value.get(1), Status.COMPLETE);
		}
		
		// If we are complete and they are complete, no reason to stay connected.
		if(peerStatus.complete() && localFiles.complete()) {
			System.out.println("Local and peer download complete. Disconnecting");
			stop();
		}
	}

	private void requestChunk() throws IOException {
		// Choose an interesting chunk and reserve it so we will request a different chunk from a different peer
		int[] chunk = localFiles.status.pickUsefulChunk(peerStatus);
		
		if(chunk == null) {
			// Peer has no useful chunks for us
			System.out.println("Peer has no useful chunks for us");
			return;
		}
		
		RequestChunkJSONMessage payload = new RequestChunkJSONMessage(chunk[0], chunk[1], localFiles.metadata.metaHash);
		
		byte[] messageData = P2PMessageParser.serialiseJSONMessage(payload);
		
		sock.getOutputStream().write(messageData);
		System.out.println("Writing chunks to stream: " + new String(messageData));
	}

	public void advertiseChunk(int fileid, int chunkid) {
		// Should send advertise chunk message out to peer
	}

}
