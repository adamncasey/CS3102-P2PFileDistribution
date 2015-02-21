package p2pdistribute.client.filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import p2pdistribute.common.p2pmeta.P2PMetadata;

/**
 * Tracks and handles the progress of file data / chunk acquiring
 *
 */
public class FileManager {
	
	public final Path destinationFolder;
	
	
	public FileManager(P2PMetadata metadata, String destinationPath) {
		destinationFolder = Paths.get(destinationPath);
	}
	

	public void prepareFiles() throws FileManagerSetupException {
		
		// if !destinationPathExists: create folder
		if(Files.exists(destinationFolder)) {
			if(!Files.isDirectory(destinationFolder)) {
				throw new FileManagerSetupException("Supplied destination folder is not a directory");
			}
		} else {
			try {
				Files.createDirectory(destinationFolder);
			} catch (IOException e) {
				throw new FileManagerSetupException("Could not create destination directory: " + e.getMessage());
			}
		}
		
		// For each file in p2pmeta
		// If exists:
			// Find out what chunks we have, what chunks we do not have
		// Else:
			// Allocate space for file

		throw new RuntimeException("not implemented");
	}
	
	public void getChunkData(int fileid, int chunkid) {
		// TODO getChunkData
	}
	
	
	public void setChunkData(int fileid, int chunkid, int data) {
		// TODO setChunkData
		
		// If we don't already have it
		
		// verify contents
		
		// write to disk
		
		// Set chunk status to be COMPLETE
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
