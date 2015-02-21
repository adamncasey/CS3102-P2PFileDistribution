package p2pdistribute.client;

import p2pdistribute.common.p2pmeta.P2PMetadata;


public class ClientMain {
	public static void main(String[] args) throws InterruptedException {
		
		if(!checkArgs(args)) {
			return;
		}
		
		P2PMetadata metadata = readP2PMetaFile(args[0]);
		if(metadata == null) {
			return;
		}
		
		FileManager fileManager = new FileManager(metadata, args[1]);
		if(!fileManager.prepareFiles()) {
			System.err.println("Error occured when allocating/preparing files in destination folder");
		}
		
		PeerManager peerManager = new PeerManager(metadata.swarmManagerHostname, fileManager);
		
		//TODO This loop does not handle starved swarm (Chunks left but no peers which have that chunk)
		while(fileManager.numChunksNotStarted() > 0) {
			if(!peerManager.run()) {
				System.out.println("Error occured..."); //TODO Get more detail for print message?
				break;
			}
			
			Thread.sleep(5000);
			
			System.out.println("Download Progress: \n\t" + fileManager.numChunksComplete() + " chunks complete. \n\t" + fileManager.numChunksInProgress() + " chunks in-progress. \n\t" +  fileManager.numChunksNotStarted() + " chunks not started.");
		}
		
		peerManager.waitForPeers();
		
		System.out.println("Exiting");
	}
	
	private static boolean checkArgs(String[] args) {
		if(args.length < 2) {
			printHelp();
			return false;
		}
		
		return false;
	}

	private static void printHelp() {
		System.out.println("Expected Arguments: <P2PMeta File Path> <Destination Folder>");
	}

	public static P2PMetadata readP2PMetaFile(String filename) {
		throw new RuntimeException("Not implemented");
	}
}
