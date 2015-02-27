package p2pdistribute.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Handles writing messages (byte arrays) onto a P2P connection socket.
 * 
 * The BlockingQueue can be added to from multiple threads.
 *
 */
public class PeerConnectionWriteTask implements Runnable {

	private BlockingQueue<byte[]> queue;
	private OutputStream out;
	
	public PeerConnectionWriteTask(BlockingQueue<byte[]> queue, OutputStream out) {
		this.queue = queue;
		this.out = out;
	}
	
	/**
	 * Begins waiting for a queue item, and will write that message to the socket
	 * 
	 * run() will return when the socket is closed or the thread is interrupted.
	 */
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			byte[] message;
			try {
				message = queue.take();
			} catch (InterruptedException e) {
				return;
			}
			
			try {
				out.write(message);
			} catch (IOException e) {
				System.out.println("Unable to write to socket");
				break;
			}
		}
	}
}
