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

public class PeerConnection implements Runnable, ChunkStatusChangeHandler {
	
	public static final int PEER_CONNECT_TIMEOUT = 5000;
	
	public final Peer peer;
	private AcquisitionStatus peerStatus;
	
	private FileManager localFiles;
	
	private int currentFileID;
	private int currentChunkID;
	
	Socket sock;
	Thread readThread;
	
	Thread writeThread;
	BlockingQueue<byte[]> queue;

	private boolean shouldStop;
	
	private boolean requestedChunk;

	public PeerConnection(Socket client, FileManager fileManager) throws IOException {
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
	
	private void initialise(FileManager fileManager) throws IOException {
		
		localFiles = fileManager;
		currentFileID = currentChunkID = -1;
		
		localFiles.status.registerHandler(this);
		
		peerStatus = new AcquisitionStatus(localFiles.numFiles());
		
		shouldStop = false;
		requestedChunk = false;
		
		// Then create new thread for reading from socket.
		readThread = new Thread(this);
		readThread.start();
		
		queue = new LinkedBlockingQueue<>();
		
		writeThread = new Thread(new PeerConnectionWriteTask(this.queue, sock.getOutputStream()));
		writeThread.start();
	}

	@Override
	public void run() {
		// Advertise chunks!
		advertiseChunks();
		
		while(!stopRequested()) {
			if(!writeThread.isAlive()) {
				// If write thread has died, there's no point continuing.
				break;
			}
			
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
				System.out.println("Error occured handling message: " + e.getMessage());
				break;
			}
		}
		
		stop();
		
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("IOException on peer socket close");
		}
		
		tidyIncompleteChunk();
		
		try {
			writeThread.join();
		} catch (InterruptedException e) {
			// Unable to join writeThread...
		}
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

	private synchronized boolean stopRequested() {
		return shouldStop;
	}

	private synchronized boolean requestChunkLock() {
		if(requestedChunk) {
			return false;
		}
		
		requestedChunk = true;
		//System.out.println("locked");
		return true;
	}

	private synchronized void requestChunkUnlock() {
		//System.out.println("unlocked");
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
		localFiles.setChunkData(msg.fileid, msg.chunkid, msg.data);
		currentFileID = currentChunkID = -1;
		
		requestChunkUnlock();

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
			
			byte[] data = localFiles.getChunkData(message.fileid, message.chunkid);
			
			byte[] messageData = P2PMessageParser.serialiseData(data, localFiles.metadata.metaHash, message.fileid, message.chunkid);
			queue.add(messageData);
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
		
		RequestChunkJSONMessage payload = new RequestChunkJSONMessage(chunk[0], chunk[1], localFiles.metadata.metaHash);
		
		byte[] messageData = P2PMessageParser.serialiseJSONMessage(payload);
		
		queue.add(messageData);
	}

	@Override
	public void onChunkComplete(int fileid, int chunkid) {
		// TODO: Future Task: only send new fileid / chunk id, rather than re-send entire advertise chunks message
		advertiseChunks();
	}
	
	public boolean peerComplete() {
		return peerStatus.complete();
	}

}
