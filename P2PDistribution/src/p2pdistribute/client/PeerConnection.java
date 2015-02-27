package p2pdistribute.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import p2pdistribute.client.filemanager.AcquisitionStatus;
import p2pdistribute.client.filemanager.ChunkStatusChangeHandler;
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

/**
 * Handles all communication between peers.
 * 
 * 	- Sends and receives advertise_chunks messages
 * 		- Opposing peer's fileStatus is stored in {@link #peerStatus}
 *  - Sends and receives request_chunk messages
 *  	- Sending data if we receive the message, and passing the data to the {@link #localFiles} if we receive the data.
 *
 */
public class PeerConnection implements Runnable, ChunkStatusChangeHandler {
	
	// Low-ish timeout used to quickly disconnect when a problem occurs. This prevents blocking progress.
	public static final int PEER_SOCKET_TIMEOUT = 5000;
	
	public final Peer peer;
	private AcquisitionStatus peerStatus;
	
	private FileManager localFiles;
	
	Socket sock;
	Thread readThread;
	
	Thread writeThread;
	BlockingQueue<byte[]> queue;

	private boolean shouldStop;
	
	/**
	 * requestedChunk, currentFileID, currentChunkID used to ensure we only request 
	 * one chunk at a time from the remote peer.
	 * 
	 * requestedChunk should only be accessed using requestChunkLock and requestChunkUnlock.
	 * TODO Future Task: Encapsulating this functionality in a different class would reduce the scope for errors.
	 */
	private boolean requestedChunk;
	private int currentFileID;
	private int currentChunkID;

	public PeerConnection(Socket client, FileManager fileManager) throws IOException {
		sock = client;
		
		this.peer = new Peer(client.getInetAddress(), client.getLocalPort());
		
		initialise(fileManager);
	}

	public PeerConnection(Peer peer, FileManager fileManager) throws IOException {
		sock = new Socket();
		this.peer = peer;
		
		sock.connect(new InetSocketAddress(peer.address, peer.port), PEER_SOCKET_TIMEOUT);
		
		initialise(fileManager);
	}
	
	private void initialise(FileManager fileManager) throws IOException {
		
		localFiles = fileManager;

		requestedChunk = false;
		currentFileID = currentChunkID = -1;
		
		// Register a file status change handler so we can advertise new chunks to this remote peer.
		localFiles.status.registerHandler(this);
		
		peerStatus = new AcquisitionStatus(localFiles.status);
		
		shouldStop = false;
		
		sock.setSoTimeout(PEER_SOCKET_TIMEOUT);

		queue = new LinkedBlockingQueue<>();
		writeThread = new Thread(new PeerConnectionWriteTask(this.queue, sock.getOutputStream()));
		writeThread.start();
		
		// Then create new thread for reading from socket.
		readThread = new Thread(this);
		readThread.start();
	}

	/**
	 * The main loop for Peer communication
	 * 
	 * Behaviour:
	 * 	1. On connection, peers will exchange advertise_chunks messages.
	 *  
	 *  Then, until this thread is stopped or an error occures:
	 *  	2. processSocketMessage (read and dispatch 1 message from the socket).
	 */
	@Override
	public void run() {
		// Advertise chunks!
		advertiseChunks();
		
		while(!stopRequested()) {
			if(!processSocketMessage()) {
				// Error
				break;
			}
		}
		
		stop();
		
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("IOException on peer socket close");
		}
		
		// Make sure that if we had been assigned a chunk to download from this peer
		// It gets set back to INCOMPLETE (rather than INPROGRESS).
		tidyIncompleteChunk();
		
		try {
			writeThread.join();
		} catch (InterruptedException e) {
			// Unable to join writeThread...
		}
	}
	
	/**
	 * Use to stop this Peer thread
	 */
	public synchronized void stop() {
		this.shouldStop = true;
		
		try {
			this.sock.shutdownInput();
		} catch (IOException e) {
			// Means there is data in the buffer but the connection has terminated.
			// Nothing to be done here really.
		}
		
		writeThread.interrupt();
	}

	@Override
	public void onChunkComplete(int fileid, int chunkid) {
		// TODO Future Task: only send new fileid / chunk id, rather than re-send entire advertise chunks message (save bandwidth)
		advertiseChunks();
	}
	
	/**
	 * Check whether the remote peer has is at 100% download status.
	 * @return true if the remote peer has completed downloading
	 * @return false if the remote peer has not completed downloading
	 */
	public boolean peerComplete() {
		return peerStatus.complete();
	}
	
	private boolean processSocketMessage() {
		if(!writeThread.isAlive()) {
			// If write thread has died, there's no point continuing.
			return false;
		}
		
		if(transferComplete()) {
			return false;
		}
		
		Message msg;
		try {
			msg = P2PMessageParser.readMessage(sock.getInputStream());
		} catch (IOException e) {
			return false;
		} catch (ParserException e) {
			System.out.println("Peer sent malformed message. Disconnecting");
			return false;
		}
		
		try {
			handleMessage(msg);
		} catch(IOException e) {
			System.out.println("Error occured handling message: " + e.getMessage());
			return false;
		}
		
		return true;
	}

	private boolean transferComplete() {
		// If we are complete and they are complete, no reason to stay connected.
		if(peerStatus.complete() && localFiles.complete()) {
			return true;
		}
		
		return false;
	}

	private void tidyIncompleteChunk() {
		if(currentFileID != -1) {
			localFiles.status.setStatus(currentFileID, currentChunkID, Status.INCOMPLETE);
		}
	}

	private void advertiseChunks() {
		// Get all the chunks we have from localFiles
		int[][] completeChunks = localFiles.status.getCompleteFileChunkIDs();
		
		// Make a AdvertiseJSONMessage using this data
		AdvertiseJSONMessage payload = new AdvertiseJSONMessage(completeChunks, localFiles.metadata.metaHash);
		
		byte[] messageBytes = P2PMessageParser.serialiseJSONMessage(payload);
		
		// Send
		queue.add(messageBytes);
	}

	private synchronized boolean stopRequested() {
		return shouldStop;
	}

	private synchronized boolean requestChunkLock() {
		if(requestedChunk) {
			return false;
		}
		
		requestedChunk = true;
		return true;
	}

	private synchronized void requestChunkUnlock() {
		requestedChunk = false;
	}

	private void handleMessage(Message msg) throws IOException {
		if(msg.type == MessageType.CONTROL) {
			handleControlMessage((ControlMessage)msg);
			
		} else if(msg.type == MessageType.DATA) {
			handleDataMessage((DataMessage)msg);
		} else {
			System.err.println("Received unknown MessageType: " + msg.type);
		}
	}
	
	private void handleDataMessage(DataMessage msg) throws IOException {
		boolean result = localFiles.setChunkData(msg.fileid, msg.chunkid, msg.data);
		
		
		if(!result) {
			stop();
			// Received invalid chunk data.. Lets disconnect and try again.
			System.err.println("Chunk data did not match expected checksum: " + msg.fileid + "/" + msg.chunkid);
		} else {
			currentFileID = currentChunkID = -1;
			requestChunkUnlock();
			requestChunk();
		}
	}

	private void handleControlMessage(ControlMessage msg) throws IOException {
		if(msg.payload.cmd.equals("advertise_chunks")) {
			AdvertiseJSONMessage message = (AdvertiseJSONMessage) msg.payload;

			updatePeerStatus(message.chunksComplete);
			
			requestChunk();
			
		} else if(msg.payload.cmd.equals("request_chunk")) {
			RequestChunkJSONMessage message = (RequestChunkJSONMessage) msg.payload;
			
			byte[] data = localFiles.getChunkData(message.fileid, message.chunkid);
			
			byte[] messageData = P2PMessageParser.serialiseData(data, localFiles.metadata.metaHash, message.fileid, message.chunkid);
			queue.add(messageData);
		}
		
	}

	private void updatePeerStatus(List<List<Integer>> chunksComplete) {
		for(List<Integer> value : chunksComplete) {
			if(value.size() < 2) {
				System.err.println("Received badly formed message from peer");
				return;
			}
			peerStatus.setStatus(value.get(0), value.get(1), Status.COMPLETE);
		}
	}

	private void requestChunk() throws IOException {
		if(!requestChunkLock()) {
			// We are already in the process of receiving a chunk, don't ask for another one yet.
			return;
		}

		// Choose an interesting chunk and reserve it so we will request a different chunk from a different peer
		int[] chunk = localFiles.status.pickUsefulChunk(peerStatus);
		if(chunk == null) {
			// Peer has no useful chunks for us
			System.out.println("Peer has no useful chunks for us");
			return;
		}
		currentFileID = chunk[0];
		currentChunkID = chunk[1];
		
		RequestChunkJSONMessage payload = new RequestChunkJSONMessage(chunk[0], chunk[1], localFiles.metadata.metaHash);
		
		byte[] messageData = P2PMessageParser.serialiseJSONMessage(payload);
		
		queue.add(messageData);
	}
}
