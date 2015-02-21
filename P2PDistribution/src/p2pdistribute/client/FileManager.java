package p2pdistribute.client;

import p2pdistribute.common.p2pmeta.P2PMetadata;

/**
 * Tracks and handles the progress of file data / chunk acquiring
 *
 */
public class FileManager {
	
	public FileManager(P2PMetadata metadata, String destinationPath) {

	}
	

	public boolean prepareFiles() {
		// if !destinationPathExists: create folder
		
		// TODO implement prepareFiles
		// For each file in p2pmeta
		// If exists:
			// Find out what chunks we have, what chunks we do not have
		// Else:
			// Allocate space for file
		
		return false;
	}
	
	// Store file/chunk progress (COMPLETE, INPROGRESS, NONE)
	
	public int numChunksNotStarted() {
		// TODO implement
		throw new RuntimeException("not implemented");
	}

	public int numChunksComplete() {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented");
	}

	public int numChunksInProgress() {
		// TODO Auto-generated method stub
		throw new RuntimeException("not implemented");
	}

}
