package p2pdistribute.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

public class PeerConnectionWriteTask implements Runnable {

	private BlockingQueue<byte[]> queue;
	private OutputStream out;
	
	public PeerConnectionWriteTask(BlockingQueue<byte[]> queue, OutputStream out) {
		this.queue = queue;
		this.out = out;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
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
