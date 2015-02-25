package p2pdistribute.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import p2pdistribute.client.filemanager.FileManager;

public class PeerConnectionListener implements Runnable {

	private ServerSocket server;
	private ActiveConnectionManager acManager;
	private FileManager fileManager;
	
	public PeerConnectionListener(ActiveConnectionManager connManager, FileManager fileManager) throws IOException {
		server = new ServerSocket(0);
		acManager = connManager;
		this.fileManager = fileManager;
		System.out.println("Listening: " + server.getInetAddress().toString() + ":" + server.getLocalPort());
	}
	
	public void stop() {
		try {
			server.close();
		} catch(IOException e) {
			// Unhandled exception
		}
	}
	
	public int getPort() {
		return server.getLocalPort();
	}
	
	@Override
	public void run() {
		while(!server.isClosed()) {
			// Accept client
			
			Socket client;
			try {
				client = server.accept();
			} catch (IOException e) {
				return;
			}
		
			PeerConnection peer;
			try {
				peer = new PeerConnection(client, fileManager);
			} catch (IOException e) {
				// Error occurred while adding a new client, just move on to the next one
				System.out.println("Error occured constructing PeerConnection.");
				continue;
			}
			
			acManager.addPeer(peer);
		}
	}

}
