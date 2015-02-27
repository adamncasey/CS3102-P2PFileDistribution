package p2pdistribute.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import p2pdistribute.client.filemanager.ChunkStatusChangeHandler;
import p2pdistribute.client.filemanager.FileManager;
import p2pdistribute.client.filemanager.FileManagerSetupException;
import p2pdistribute.common.p2pmeta.FileParser;
import p2pdistribute.common.p2pmeta.P2PMetadata;
import p2pdistribute.common.p2pmeta.ParserException;
import p2pdistribute.swarmmanager.SwarmManagerMain;


public class ClientMain implements ChunkStatusChangeHandler {
	public static final int SM_PORT = SwarmManagerMain.PORT;
	
	private static FileManager fileManager;
	private static boolean shouldSeed;
	private static String p2pMetaFile;
	private static String outputDir;
	
	public static void main(String[] args) throws InterruptedException {
		
		if(!checkArgs(args)) {
			return;
		}
		
		P2PMetadata metadata = readP2PMetaFile(p2pMetaFile);
		if(metadata == null) {
			return;
		}
		
		FileManager fileManager = setupFileManager(metadata, outputDir);
		if(fileManager == null) {
			return;
		}
		
		System.out.println("Download status: " + calculatePercentageCompletion() + "%");
		
		PeerManager peerManager;
		try {
			peerManager = new PeerManager(metadata.swarmManagerHostname, SM_PORT, fileManager);
		} catch (PeerManagerException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		while((!fileManager.complete()) || (!peerManager.complete()) || shouldSeed) {
			
			try {
				peerManager.run(); 
				
			} catch(PeerManagerException e) {
				System.err.println(e.getMessage());
				break;
			}
			
			// Rate limit registering / retrieving peer list.
			// TODO Future Task: Remove this sleep value when Swarm Manager registering etc is in a new thread.
			Thread.sleep(500);
		}
		
		peerManager.waitForPeers();
	}

	@Override
	public void onChunkComplete(int fileid, int chunkid) {
		System.out.println("Acquired chunk: " + fileid + "/" + chunkid + ". "  + calculatePercentageCompletion() + "% complete");
	}

	private static float calculatePercentageCompletion() {
		return ((float)fileManager.status.numChunksComplete() / fileManager.numChunks()) * 100;
	}
	
	private static FileManager setupFileManager(P2PMetadata metadata, String outputDir) {
		FileManager fileManager = new FileManager(metadata, outputDir);
		try {
			fileManager.setup();
		} catch(FileManagerSetupException e) {
			System.err.println("Error occured when preparing files: " + e.getMessage());
			return null;
		}
		
		// Just used to print message on chunk acquisition
		fileManager.status.registerHandler(new ClientMain());
		
		return fileManager;
	}

	private static boolean checkArgs(String[] args) {
		// If releasing this as a product a proper argument parsing library should be used.
		
		if(args.length < 2 || args.length > 3) {
			printHelp();
			return false;
		}
		p2pMetaFile = args[0];
		outputDir = args[1];
		
		if(args.length == 3) {
			if(args[0].equals("--seed")) {
				System.out.println("Seeding. Manual program exit required.");
				shouldSeed = true;
			}
			p2pMetaFile = args[1];
			outputDir = args[2];
		}
		
		return true;
	}

	private static void printHelp() {
		System.out.println("Usage: <P2PMeta File> <Destination Folder>");
		System.out.println("Optional Usage: --seed <P2PMeta File> <Destination Folder>");
	}

	private static P2PMetadata readP2PMetaFile(String filename) {
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
