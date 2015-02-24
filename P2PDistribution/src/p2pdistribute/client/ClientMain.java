package p2pdistribute.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.client.filemanager.FileManagerSetupException;
import p2pdistribute.common.p2pmeta.FileParser;
import p2pdistribute.common.p2pmeta.P2PMetadata;
import p2pdistribute.common.p2pmeta.ParserException;
import p2pdistribute.swarmmanager.SwarmManagerMain;


public class ClientMain {
	public static final int SM_PORT = SwarmManagerMain.PORT;
	
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
			fileManager.setup();
		} catch(FileManagerSetupException e) {
			System.err.println("Error occured when preparing files: " + e.getMessage());
			return;
		}
		
		PeerManager peerManager;
		try {
			peerManager = new PeerManager(metadata.swarmManagerHostname, SM_PORT, fileManager);
		} catch (PeerManagerException e) {
			System.err.println(e.getMessage());
			return;
		}

		// TODO If we are not done or not everyone we are connected to is complete: keep going
		
		while(!fileManager.complete() || /*!peerManager.complete()*/ true) {
			//TODO Handle starved swarm?
			try {
				peerManager.run(); 
				
			} catch(PeerManagerException e) {
				System.err.println(e.getMessage()); //TODO Get more detail for print message?
				break;
			}
			
			Thread.sleep(5000);
			
			System.out.println("Download Progress: \n\tFinished?:" + fileManager.complete());
		}		
		
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
