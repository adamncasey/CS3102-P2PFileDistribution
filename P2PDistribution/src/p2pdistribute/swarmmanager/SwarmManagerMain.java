package p2pdistribute.swarmmanager;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class SwarmManagerMain {

	public static final int PORT = 8889; // TODO Future Task: Settings file. Or store SM Port in .p2pmeta file
	
	public static void main(String[] args) {
		ServerSocket server;
		
		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("Unable to bind TCP listen socket: " + e.getMessage());
			return;
		}
		
		System.out.println("Started Swarm Manager");
		
		serverLoop(server);
		
		close(server);
		System.out.println("Closed TCP listen socket");
	}
	
	/**
	 * Main SM Server Loop:
	 * 		1. Accept client
	 * 		2. Start Client Handling Thread
	 * 		repeat until server socket closed.
	 * @param ServerSocket
	 */
	private static void serverLoop(ServerSocket server) {
		LinkedList<Thread> listenThreads = new LinkedList<>();
		// Keeps track of peers in swarms (One entry per active p2pmeta)
		SwarmIndex index = new SwarmIndex();
		
		while(!server.isClosed()) {
			
			pruneThreads(listenThreads);
			
			Socket client;
			try {
				client = server.accept();
				
				Thread thread = startClientThread(client, index);
				if(thread != null) {
					listenThreads.add(thread);
				}
				
			} catch (IOException e) {
				System.err.println("Unable to accept client on server socket");
				close(server);
				break;
			}
		}
		
		System.out.println("Stopping active client socket handling threads");
		stopThreads(listenThreads);
	}
	
	/**
	 * If a client handling thread has completed, remove it from the list of threads
	 */
	private static void pruneThreads(LinkedList<Thread> listenThreads) {
		Iterator<Thread> i = listenThreads.iterator();
		while (i.hasNext()) {
		   Thread thread = i.next();
		   
		   if(!thread.isAlive()) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					// Interrupted waiting for join to perform. Little reason to pass this up the chain
				}
				
				i.remove();
			}
		}
	}

	private static Thread startClientThread(Socket client, SwarmIndex index) {
		Thread clientThread;
		try {
			clientThread = new Thread(new ClientHandler(client, index));
		} catch (IOException e) {
			System.out.println("Error occured when accepting client. Continuing...");
			return null;
		}
		
		clientThread.start();
		
		return clientThread;
	}

	/**
	 * Interrupts and stops all threads.
	 * @param threads
	 */
	private static void stopThreads(LinkedList<Thread> threads) {
		for(Thread thread : threads) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Interrupted waiting for Thread to stop.");
				return;
			}
		}
	}

	public static void close(Closeable sock) {
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("IOException when closing socket");
		}
	}

}
