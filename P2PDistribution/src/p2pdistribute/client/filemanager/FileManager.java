package p2pdistribute.client.filemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import p2pdistribute.common.p2pmeta.FileMetadata;
import p2pdistribute.common.p2pmeta.P2PMetadata;

/**
 * Tracks and handles the progress of file data / chunk acquiring
 *
 */
public class FileManager {
	
	public final Path destinationFolder;
	private P2PFile[] files;
	
	public final P2PMetadata metadata;
	
	public AcquisitionStatus status;
	
	public FileManager(P2PMetadata metadata, String destinationPath) {
		destinationFolder = Paths.get(destinationPath);
		
		this.metadata = metadata;
		
		files = new P2PFile[metadata.files.length];
		
		status = new AcquisitionStatus(this.numFiles(), this.numChunks());
	}
	

	public void setup() throws FileManagerSetupException {

		prepareDirectory();
		
		HashAlgorithm hashFunc = makeHashAlgorithm(metadata.hashType);
		prepareFiles(hashFunc);
	}

	private void prepareDirectory() throws FileManagerSetupException {
		// TODO Warning if directory not empty? Could overwrite files.
		
		if(Files.exists(destinationFolder)) {
			if(!Files.isDirectory(destinationFolder)) {
				throw new FileManagerSetupException("Supplied destination is not a directory");
			}
		}
		else {
			createDirectory(destinationFolder);
		}		
	}
	private void createDirectory(Path path) throws FileManagerSetupException {
		
		try {
			Files.createDirectory(path);
		} catch (IOException e) {
			throw new FileManagerSetupException("Could not create destination directory: " + e.getMessage());
		}
	}
	
	private HashAlgorithm makeHashAlgorithm(String hashType) throws FileManagerSetupException {
		HashAlgorithm hashFunc;
		try {
			hashFunc = new HashAlgorithm(metadata.hashType);
		} catch (NoSuchAlgorithmException e1) {
			throw new FileManagerSetupException("Unsupported hash type found in P2Pmeta file");
		}
		
		return hashFunc;
	}
	
	private void prepareFiles(HashAlgorithm hashFunc) throws FileManagerSetupException {
		int i=0;
		
		for(FileMetadata fileMeta : metadata.files) {
			P2PFile file = new P2PFile(destinationFolder, fileMeta, hashFunc);
			
			Status[] fileStatus;
			
			try {
				fileStatus = file.prepare();
			} catch (P2PFilePreparationException e) {
				throw new FileManagerSetupException("Error occured preparing file: " + e.getMessage()); 
			}
			
			status.setStatus(i, fileStatus);
			files[i] = file;
			i++;
		}
	}
	
	public byte[] getChunkData(int fileid, int chunkid) throws IOException {

		if(fileid >= files.length) {
			System.err.println("Err wtf");
			throw new IOException("Invalid FileID: " + fileid + ". Num files: " + files.length);
		}
		
		return files[fileid].readChunkData(chunkid);
	}
	
	
	public void setChunkData(int fileid, int chunkid, byte[] data) throws IOException {
		
		if(fileid >= files.length) {
			System.err.println("Err wtf");
			throw new IOException("Invalid FileID: " + fileid + ". Num files: " + files.length);
		}
		
		Status status = files[fileid].writeChunkData(chunkid, data);
		
		this.status.setStatus(fileid, chunkid, status);
	}
	
	// Store file/chunk progress (COMPLETE, INPROGRESS, NONE)
	public boolean complete() {
		return status.complete();
	}
	
	public int numFiles() {
		return metadata.files.length;
	}
	
	public int numChunks() {
		int total = 0;
		
		for(FileMetadata file : metadata.files) {
			
			total += file.chunks.length;
		}
		
		return total;
	}
}
