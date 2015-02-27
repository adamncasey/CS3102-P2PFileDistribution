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
 * Can be safely accessed across threads.
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
		
		status = new AcquisitionStatus(this.numFiles());
	}
	
	/**
	 * Attempts to check files expected from the Metadata for validity.
	 * If the files do not exist, they will be allocated when this function is called.
	 * @throws FileManagerSetupException thrown if there is an error creating/reading directories or files.
	 */
	public void setup() throws FileManagerSetupException {

		prepareDirectory();
		
		HashAlgorithm hashFunc = makeHashAlgorithm(metadata.hashType);
		prepareFiles(hashFunc);
	}
	
	/**
	 * Sets the data of a particular chunk in the specified file. 
	 * Checks the validity of this data and returns the new Status of this chunk.
	 * @param fileid
	 * @param chunkid
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean setChunkData(int fileid, int chunkid, byte[] data) throws IOException {
		
		if(fileid >= files.length) {
			System.err.println("Err wtf");
			throw new IOException("Invalid FileID: " + fileid + ". Num files: " + files.length);
		}
		
		if(status.getStatus(fileid, chunkid) == Status.COMPLETE) {
			// Chunk is already complete, this message is weird. Malcious?
			System.out.println("Attempt to overwrite complete chunk");
			return false;
		}
		
		Status status = files[fileid].writeChunkData(chunkid, data);
		
		if(status != Status.COMPLETE) {
			return false;
		}
		
		this.status.setStatus(fileid, chunkid, status);
		
		return true;
	}
	
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

	private void prepareDirectory() throws FileManagerSetupException {
		
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
}
