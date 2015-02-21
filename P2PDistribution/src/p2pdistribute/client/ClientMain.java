package p2pdistribute.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.client.filemanager.FileManagerSetupException;
import p2pdistribute.common.p2pmeta.FileParser;
import p2pdistribute.common.p2pmeta.P2PMetadata;
import p2pdistribute.common.p2pmeta.ParserException;


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
		try {
			fileManager.prepareFiles();
		} catch(FileManagerSetupException e) {
			System.err.println("Error occured when preparing files: " + e.getMessage());
			return;
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
		
		// TODO Seed?
		
		peerManager.waitForPeers();
		
		System.out.println("Exiting");
	}
	
	private static boolean checkArgs(String[] args) {
		if(args.length < 2) {
			printHelp();
			return false;
		}
		
		return true;
	}

	private static void printHelp() {
		System.out.println("Usage: <P2PMeta File> <Destination Folder>");
	}

	public static P2PMetadata readP2PMetaFile(String filename) {
		String fileContents;
		try {
			fileContents = FileUtils.readFileToString(new File(filename));
			
			return FileParser.parseP2PMetaFileContents(fileContents);
			
		} catch (IOException | ParserException e) {
			
			System.out.println("Could not read P2PMeta file: " + e.getMessage());
		}
		
		return null;
	}
}
